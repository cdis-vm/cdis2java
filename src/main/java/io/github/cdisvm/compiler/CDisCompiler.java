package io.github.cdisvm.compiler;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.MethodParameterInfo;
import java.lang.classfile.attribute.MethodParametersAttribute;
import java.lang.classfile.constantpool.ClassEntry;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import io.github.cdisvm.compiler.opcode.LoadConstant;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;

public class CDisCompiler {
    public static final ClassDesc VOID_CD = ClassDesc.ofDescriptor("V");
    public static final ClassDesc INT_CD = ClassDesc.ofDescriptor("I");
    public static final ClassDesc BOOLEAN_CD = ClassDesc.ofDescriptor("Z");

    public static final ClassDesc OBJECT_CD = ClassDesc.of(Object.class.getName());
    public static final ClassDesc PY_OBJECT_CD = ClassDesc.of(PyObject.class.getCanonicalName());
    public static final ClassDesc PY_CALLABLE_CD = ClassDesc.of(PyCallable.class.getCanonicalName());
    public static final ClassDesc PY_CALL_BUILDER_CD = ClassDesc.of(PyCallBuilder.class.getCanonicalName());

    private final ClassFile classFile;
    private final CDisClassLoader classLoader;
    private long classIdGenerator = 0;

    public CDisCompiler() {
        this.classLoader = new CDisClassLoader();
        this.classFile = ClassFile.of();
        this.classIdGenerator = 0;
    }

    public void dumpClasses(Path dumpLocation) {
        classLoader.dumpClasses(dumpLocation);
    }

    public PyCallable compile(Bytecode bytecode) {
        var signature = bytecode.signature();
        var callBuilderInterfaces = new LinkedHashSet<ClassDesc>();
        for (var parameter : signature.parameters()) {
            callBuilderInterfaces.addAll(createSignatureParameterInterface(parameter));
        }
        var callBuilder = createCallBuilder(bytecode.functionName(), bytecode.signature(), callBuilderInterfaces);
        return createCallable(bytecode, callBuilder);
    }

    private String nextId() {
        var next = "$" + classIdGenerator;
        classIdGenerator++;
        return next;
    }

    public static String arbitraryTextToJavaIdentifierName(String text) {
        var out = new StringBuilder();
        if (Character.isJavaIdentifierStart(text.charAt(0))) {
            var ch = text.charAt(0);
            if (ch == '$') {
                out.append("$$");
            } else {
                out.append(ch);
            }
        } else {
            var ch = text.charAt(0);
            out.append('$').append((int) ch).append('$');
        }
        for (var i = 1; i < text.length(); i++) {
            var ch = text.charAt(i);
            if (ch == '$') {
                out.append("$$");
            }
            if (Character.isJavaIdentifierPart(ch)) {
                out.append(ch);
            } else {
                out.append('$').append((int) ch).append('$');
            }
        }
        return out.toString();
    }

    ClassDesc createCallBuilder(String functionQualifiedName, FunctionSignature signature, Set<ClassDesc> additionalInterfaces) {
        var callBuilderClassName = FunctionSignature.CALL_BUILDER_PACKAGE + nextId()  + "." + arbitraryTextToJavaIdentifierName(functionQualifiedName);
        var callBuilderClassDesc = ClassDesc.of(callBuilderClassName);
        var bytecode = classFile.build(callBuilderClassDesc, classBuilder -> {
            var constantPool = classBuilder.constantPool();
            var interfaces = new ArrayList<ClassEntry>();
            interfaces.add(constantPool.classEntry(PY_CALL_BUILDER_CD));
            for (var additionalInterface : additionalInterfaces) {
                interfaces.add(constantPool.classEntry(additionalInterface));
            }
            classBuilder.withInterfaces(interfaces);

            for (var parameter : signature.parameters()) {
                classBuilder.withField(parameter.parameterName(), PY_OBJECT_CD, Modifier.PUBLIC);
            }

            classBuilder.withField("$functionInstance", PY_CALLABLE_CD, Modifier.PRIVATE | Modifier.FINAL);
            classBuilder.withField("$argumentIndex", INT_CD, Modifier.PRIVATE);

            classBuilder.withMethodBody("<init>", MethodTypeDesc.of(VOID_CD, PY_CALLABLE_CD), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(OBJECT_CD, "<init>", MethodTypeDesc.of(VOID_CD));

                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.putfield(callBuilderClassDesc, "$functionInstance", PY_CALLABLE_CD);

                codeBuilder.aload(0);
                codeBuilder.iconst_0();
                codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", INT_CD);

                for (var parameter : signature.parameters()) {
                    if (parameter.defaultValue() != null) {
                        // TODO: Set the field to the default value
                        //       which is stored in a static field after
                        //       the class is created.
                    }
                }
                codeBuilder.return_();
            });

            FunctionParameter vargsParameter = null;
            FunctionParameter kwargsParameter = null;

            for (var parameter : signature.parameters()) {
                switch (parameter.parameterKind()) {
                    case POSITIONAL_OR_KEYWORD -> {
                        implementCallBuilderPositionalArgument(classBuilder, callBuilderClassDesc, parameter);
                        implementCallBuilderKeywordArgument(classBuilder, callBuilderClassDesc, parameter);
                    }
                    case POSITIONAL_ONLY -> {
                        implementCallBuilderPositionalArgument(classBuilder, callBuilderClassDesc, parameter);
                    }
                    case KEYWORD_ONLY -> {
                        implementCallBuilderKeywordArgument(classBuilder, callBuilderClassDesc, parameter);
                    }
                    case VARGS -> {
                        vargsParameter = parameter;
                    }
                    case KWARGS -> {
                        kwargsParameter = parameter;
                    }
                }
            }
            implementCallBuilderAppend(classBuilder, callBuilderClassDesc, signature, vargsParameter);
            implementCallBuilderPut(classBuilder, callBuilderClassDesc, signature, kwargsParameter);
            implementCallBuilderCall(classBuilder, callBuilderClassDesc);
        });
        classLoader.registerClass(callBuilderClassName, bytecode);
        return callBuilderClassDesc;
    }

    private void implementCallBuilderPositionalArgument(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionParameter parameter) {
        classBuilder.withMethodBody("$" + parameter.parameterIndex(), MethodTypeDesc.of(PY_CALL_BUILDER_CD, PY_OBJECT_CD), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.aload(1);
            codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), PY_OBJECT_CD);

            codeBuilder.aload(0);
            codeBuilder.dup();
            codeBuilder.getfield(callBuilderClassDesc, "$argumentIndex", INT_CD);
            codeBuilder.iconst_1();
            codeBuilder.iadd();
            codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", INT_CD);

            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderKeywordArgument(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionParameter parameter) {
        classBuilder.withMethodBody(parameter.parameterName(), MethodTypeDesc.of(PY_CALL_BUILDER_CD, PY_OBJECT_CD), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.aload(1);
            codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), PY_OBJECT_CD);

            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderCall(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc) {
        classBuilder.withMethodBody("call", MethodTypeDesc.of(PY_OBJECT_CD), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.getfield(callBuilderClassDesc, "$functionInstance", PY_CALLABLE_CD);
            codeBuilder.aload(0);
            codeBuilder.invokeinterface(PY_CALLABLE_CD, "call", MethodTypeDesc.of(PY_OBJECT_CD, PY_CALL_BUILDER_CD));
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderAppend(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionSignature signature,
            @Nullable FunctionParameter vargsParameter) {
        classBuilder.withMethodBody("$appendArgument", MethodTypeDesc.of(PY_CALL_BUILDER_CD, PY_OBJECT_CD), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.getfield(callBuilderClassDesc, "$argumentIndex", INT_CD);

            var switchCases = new ArrayList<SwitchCase>();
            var positionalParameters = new ArrayList<FunctionParameter>();
            var successLabel = codeBuilder.newLabel();
            var failureLabel = codeBuilder.newLabel();

            for (var parameter : signature.parameters()) {
                switch (parameter.parameterKind()) {
                    case POSITIONAL_OR_KEYWORD, POSITIONAL_ONLY -> {
                        switchCases.add(SwitchCase.of(parameter.parameterIndex(), codeBuilder.newLabel()));
                        positionalParameters.add(parameter);
                    }
                    default -> {}
                }
            }
            codeBuilder.lookupswitch(failureLabel, switchCases);

            for (var index = 0; index < switchCases.size(); index++) {
                var switchCase = switchCases.get(index);
                var parameter = positionalParameters.get(index);

                codeBuilder.labelBinding(switchCase.target());
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), PY_OBJECT_CD);
                codeBuilder.goto_(successLabel);
            }

            codeBuilder.labelBinding(failureLabel);
            if (vargsParameter == null) {
                codeBuilder.new_(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()), "<init>", MethodTypeDesc.of(VOID_CD));
                codeBuilder.athrow();
            } else {
                // TODO: Vargs handling
            }


            codeBuilder.labelBinding(successLabel);

            codeBuilder.aload(0);
            codeBuilder.dup();
            codeBuilder.getfield(callBuilderClassDesc, "$argumentIndex", INT_CD);
            codeBuilder.iconst_1();
            codeBuilder.iadd();
            codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", INT_CD);

            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderPut(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionSignature signature,
            @Nullable FunctionParameter kwargsParameter) {
        classBuilder.withMethodBody("$putArgument", MethodTypeDesc.of(PY_CALL_BUILDER_CD, ClassDesc.of(String.class.getCanonicalName()), PY_OBJECT_CD), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(1);
            codeBuilder.invokevirtual(OBJECT_CD, "hashCode", MethodTypeDesc.of(INT_CD));

            var switchCaseByHashCode = new LinkedHashMap<Integer, SwitchCase>();
            var parametersByHashCode = new LinkedHashMap<Integer, List<FunctionParameter>>();
            var successLabel = codeBuilder.newLabel();
            var failureLabel = codeBuilder.newLabel();

            for (var parameter : signature.parameters()) {
                switch (parameter.parameterKind()) {
                    case POSITIONAL_OR_KEYWORD, KEYWORD_ONLY -> {
                        parametersByHashCode.computeIfAbsent(parameter.parameterName().hashCode(), _ -> new ArrayList<>())
                                .add(parameter);
                        switchCaseByHashCode.computeIfAbsent(parameter.parameterName().hashCode(), hashCode -> SwitchCase.of(hashCode,
                                codeBuilder.newLabel()));
                    }
                    default -> {}
                }
            }
            codeBuilder.lookupswitch(failureLabel, new ArrayList<>(switchCaseByHashCode.values()));

            for (var hashCode : parametersByHashCode.keySet()) {
                var switchCase = switchCaseByHashCode.get(hashCode);
                var parameters = parametersByHashCode.get(hashCode);

                codeBuilder.labelBinding(switchCase.target());

                for (var parameter : parameters) {
                    codeBuilder.loadConstant(parameter.parameterName());
                    codeBuilder.aload(1);
                    codeBuilder.invokevirtual(ClassDesc.of(String.class.getCanonicalName()), "equals", MethodTypeDesc.of(BOOLEAN_CD, OBJECT_CD));
                    codeBuilder.ifThen(blockBuilder -> {
                        blockBuilder.aload(0);
                        blockBuilder.aload(2);
                        blockBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), PY_OBJECT_CD);
                        blockBuilder.goto_(successLabel);
                    });
                }
                codeBuilder.goto_(failureLabel);
            }

            codeBuilder.labelBinding(failureLabel);
            if (kwargsParameter == null) {
                codeBuilder.new_(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()), "<init>", MethodTypeDesc.of(VOID_CD));
                codeBuilder.athrow();
            } else {
                // TODO: Kwargs handling
            }


            codeBuilder.labelBinding(successLabel);
            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private List<ClassDesc> createSignatureParameterInterface(FunctionParameter parameter) {
        var positionalInterface = FunctionSignature.getPositionalArgumentInterfaceName(parameter.parameterIndex());
        var keywordInterface = FunctionSignature.getKeywordArgumentInterfaceName(parameter.parameterName());

        return switch (parameter.parameterKind()) {
            case POSITIONAL_OR_KEYWORD -> {
                if (!classLoader.isClassDefined(positionalInterface)) {
                    defineSignaturePositionalInterface(positionalInterface, parameter.parameterIndex());
                }
                if (!classLoader.isClassDefined(keywordInterface)) {
                    defineSignatureKeywordInterface(keywordInterface, parameter.parameterName());
                }
                yield List.of(ClassDesc.of(positionalInterface),
                        ClassDesc.of(keywordInterface));
            }
            case POSITIONAL_ONLY -> {
                if (!classLoader.isClassDefined(positionalInterface)) {
                    defineSignaturePositionalInterface(positionalInterface, parameter.parameterIndex());
                }
                yield List.of(ClassDesc.of(positionalInterface));
            }
            case KEYWORD_ONLY -> {
                if (!classLoader.isClassDefined(keywordInterface)) {
                    defineSignatureKeywordInterface(keywordInterface, parameter.parameterName());
                }
                yield List.of(ClassDesc.of(keywordInterface));
            }
            case VARGS, KWARGS ->
                // VARGS/KWARGS don't contribute additional interfaces;
                // they use the $ methods of PyCallBuilder
                    Collections.emptyList();
        };
    }

    private void defineSignaturePositionalInterface(String interfaceName, int argumentIndex) {
        var classDesc = ClassDesc.of(interfaceName);
        var bytecode = classFile.build(classDesc, classBuilder -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            classBuilder.withMethod("$" + argumentIndex, MethodTypeDesc.of(PY_CALL_BUILDER_CD, PY_OBJECT_CD),
                    Modifier.PUBLIC | Modifier.ABSTRACT, methodBuilder -> {
                        methodBuilder.with(MethodParametersAttribute.of(MethodParameterInfo.of(Optional.of("argument"))));
                    });
        });
        classLoader.registerClass(interfaceName, bytecode);
    }

    private void defineSignatureKeywordInterface(String interfaceName, String argumentName) {
        var classDesc = ClassDesc.of(interfaceName);
        var bytecode = classFile.build(classDesc, classBuilder -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            classBuilder.withMethod(argumentName, MethodTypeDesc.of(PY_CALL_BUILDER_CD, PY_OBJECT_CD),
                    Modifier.PUBLIC | Modifier.ABSTRACT, methodBuilder -> {
                        methodBuilder.with(MethodParametersAttribute.of(MethodParameterInfo.of(Optional.of("argument"))));
                    });
        });
        classLoader.registerClass(interfaceName, bytecode);
    }

    private PyCallable createCallable(Bytecode bytecode, ClassDesc callBuilderClassDescriptor) {
        var callableClass = FunctionSignature.CALLABLE_PACKAGE + nextId() + "." + bytecode.functionName();
        var callableClassDescriptor = ClassDesc.of(callableClass);
        var classBytecode = classFile.build(callableClassDescriptor, classBuilder -> {
            var constantPool = classBuilder.constantPool();
            classBuilder.withInterfaces(constantPool.classEntry(PY_CALLABLE_CD));

            Map<String, PyConstant> constantMap = new LinkedHashMap<>();
            for (var instruction : bytecode.instructions()) {
                if (instruction.opcode() instanceof LoadConstant(var constant)) {
                    constantMap.put(constant.getJavaIdentifierName(), constant);
                }
            }

            if (!constantMap.isEmpty()) {
                for (var constantEntry : constantMap.entrySet()) {
                    classBuilder.withField(constantEntry.getKey(), ClassDesc.of(constantEntry.getValue().getClass().getCanonicalName()), Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
                }

                classBuilder.withMethodBody("<clinit>", MethodTypeDesc.of(VOID_CD), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                    for (var constantEntry : constantMap.entrySet()) {
                        constantEntry.getValue().loadValueOntoStack(codeBuilder);
                        codeBuilder.putstatic(callableClassDescriptor, constantEntry.getKey(), ClassDesc.of(constantEntry.getValue().getClass().getCanonicalName()));
                    }
                    codeBuilder.return_();
                });
            }

            classBuilder.withMethodBody("<init>", MethodTypeDesc.of(VOID_CD), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(OBJECT_CD, "<init>", MethodTypeDesc.of(VOID_CD));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("getCallBuilder", MethodTypeDesc.of(PY_CALL_BUILDER_CD), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(callBuilderClassDescriptor);
                codeBuilder.dup();
                codeBuilder.aload(0);
                codeBuilder.invokespecial(callBuilderClassDescriptor, "<init>", MethodTypeDesc.of(VOID_CD, PY_CALLABLE_CD));
                codeBuilder.return_(TypeKind.REFERENCE);
            });

            classBuilder.withMethodBody("call", MethodTypeDesc.of(PY_OBJECT_CD, PY_CALL_BUILDER_CD), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.checkcast(callBuilderClassDescriptor);
                codeBuilder.astore(2);
                var compileRun = CompilationRun.init(callableClassDescriptor, codeBuilder, bytecode, 3);

                var invalidArgumentsLabel = codeBuilder.newLabel();
                for (var parameter : bytecode.signature().parameters()) {
                    codeBuilder.aload(2);
                    codeBuilder.getfield(callBuilderClassDescriptor, parameter.parameterName(), PY_OBJECT_CD);
                    codeBuilder.dup();
                    codeBuilder.aconst_null();
                    codeBuilder.if_acmpeq(invalidArgumentsLabel);
                    codeBuilder.astore(compileRun.getVariableSlot(parameter.parameterName()));
                }
                var codeStartLabel = codeBuilder.newLabel();
                codeBuilder.goto_(codeStartLabel);
                codeBuilder.labelBinding(invalidArgumentsLabel);

                codeBuilder.new_(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()), "<init>", MethodTypeDesc.of(VOID_CD));
                codeBuilder.athrow();

                codeBuilder.labelBinding(codeStartLabel);
                var lastSourceLine = -1;
                for (var instruction : bytecode.instructions()) {
                    var label = compileRun.bytecodeIndexToLabel().get(instruction.bytecodeIndex());
                    if (label != null) {
                        codeBuilder.labelBinding(label);
                    }
                    if (lastSourceLine != instruction.sourceLineNumber()) {
                        lastSourceLine = instruction.sourceLineNumber();
                        codeBuilder.lineNumber(lastSourceLine);
                    }
                    instruction.opcode().implement(codeBuilder, compileRun, bytecode.stackMetadataForInstruction().get(instruction.bytecodeIndex()));
                }
            });
        });
        classLoader.registerClass(callableClass, classBytecode);
        try {
            var loadedCallableClass = classLoader.loadClass(callableClass);
            return (PyCallable) loadedCallableClass.getConstructor().newInstance();
        } catch (ClassNotFoundException | InvocationTargetException | InstantiationException | NoSuchMethodException |
                IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
