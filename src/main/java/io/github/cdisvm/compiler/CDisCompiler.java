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
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.jspecify.annotations.Nullable;

import io.github.cdisvm.compiler.opcode.LoadConstant;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;

public class CDisCompiler {

    private final ClassFile classFile;
    private final CDisClassLoader classLoader;
    private final Map<Long, String> cellIdToCellClass;
    private long classIdGenerator;

    public CDisCompiler() {
        this.classLoader = new CDisClassLoader();
        this.classFile = ClassFile.of();
        this.cellIdToCellClass = new LinkedHashMap<>();
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

    private String nextClassId() {
        var next = "$" + classIdGenerator;
        classIdGenerator++;
        return next;
    }

    public String createCellClass(long cellId, PyObject value) {
        if (cellIdToCellClass.containsKey(cellId)) {
            return cellIdToCellClass.get(cellId);
        }
        if (value != null && !(value instanceof PyConstant)) {
            throw new IllegalArgumentException("Cannot convert initial value to a constant.");
        }

        var cellClass = PyCell.getClassName(cellId);
        var cellClassDesc = ClassDesc.of(cellClass);
        var bytecode = classFile.build(cellClassDesc, classBuilder -> {
            classBuilder.withField(PyCell.CELL_FIELD_NAME, CD.PY_CELL, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

            classBuilder.withMethodBody("<clinit>", MethodTypeDesc.of(CD.VOID), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                codeBuilder.new_(CD.PY_CELL);
                codeBuilder.dup();
                codeBuilder.loadConstant(cellId);
                if (value == null) {
                    codeBuilder.aconst_null();
                } else {
                    var constant = (PyConstant) value;
                    constant.loadValueOntoStack(codeBuilder);
                }
                codeBuilder.invokespecial(CD.PY_CELL, "<init>", MD.of(void.class, long.class, PyObject.class));
                codeBuilder.putstatic(cellClassDesc, PyCell.CELL_FIELD_NAME, CD.PY_CELL);
                codeBuilder.return_();
            });
        });
        classLoader.registerClass(cellClass, bytecode);
        return cellClass;
    }

    public static String arbitraryTextToJavaIdentifierName(String text) {
        if (text.isEmpty()) {
            return "$EMPTY$";
        }
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
        var callBuilderClassName = FunctionSignature.CALL_BUILDER_PACKAGE + nextClassId()  + "." + arbitraryTextToJavaIdentifierName(functionQualifiedName);
        var callBuilderClassDesc = ClassDesc.of(callBuilderClassName);
        var bytecode = classFile.build(callBuilderClassDesc, classBuilder -> {
            var constantPool = classBuilder.constantPool();
            var interfaces = new ArrayList<ClassEntry>();
            interfaces.add(constantPool.classEntry(CD.PY_CALL_BUILDER));
            for (var additionalInterface : additionalInterfaces) {
                interfaces.add(constantPool.classEntry(additionalInterface));
            }
            classBuilder.withInterfaces(interfaces);

            for (var parameter : signature.parameters()) {
                classBuilder.withField(parameter.parameterName(), CD.PY_OBJECT, Modifier.PUBLIC);
            }

            classBuilder.withField("$functionInstance", CD.PY_CALLABLE, Modifier.PRIVATE | Modifier.FINAL);
            classBuilder.withField("$argumentIndex", CD.INT, Modifier.PRIVATE);

            classBuilder.withMethodBody("<init>", MethodTypeDesc.of(CD.VOID, CD.PY_CALLABLE), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MethodTypeDesc.of(CD.VOID));

                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.putfield(callBuilderClassDesc, "$functionInstance", CD.PY_CALLABLE);

                codeBuilder.aload(0);
                codeBuilder.iconst_0();
                codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", CD.INT);

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
        classBuilder.withMethodBody("$" + parameter.parameterIndex(), MethodTypeDesc.of(CD.PY_CALL_BUILDER, CD.PY_OBJECT), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.aload(1);
            codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);

            codeBuilder.aload(0);
            codeBuilder.dup();
            codeBuilder.getfield(callBuilderClassDesc, "$argumentIndex", CD.INT);
            codeBuilder.iconst_1();
            codeBuilder.iadd();
            codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", CD.INT);

            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderKeywordArgument(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionParameter parameter) {
        classBuilder.withMethodBody(parameter.parameterName(), MethodTypeDesc.of(CD.PY_CALL_BUILDER, CD.PY_OBJECT), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.aload(1);
            codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);

            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderCall(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc) {
        classBuilder.withMethodBody("pyCall", MethodTypeDesc.of(CD.PY_OBJECT), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.getfield(callBuilderClassDesc, "$functionInstance", CD.PY_CALLABLE);
            codeBuilder.aload(0);
            codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCall", MethodTypeDesc.of(CD.PY_OBJECT, CD.PY_CALL_BUILDER));
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderAppend(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionSignature signature,
            @Nullable FunctionParameter vargsParameter) {
        classBuilder.withMethodBody("$appendArgument", MethodTypeDesc.of(CD.PY_CALL_BUILDER, CD.PY_OBJECT), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.getfield(callBuilderClassDesc, "$argumentIndex", CD.INT);

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
                codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);
                codeBuilder.goto_(successLabel);
            }

            codeBuilder.labelBinding(failureLabel);
            if (vargsParameter == null) {
                codeBuilder.new_(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()), "<init>", MethodTypeDesc.of(
                        CD.VOID));
                codeBuilder.athrow();
            } else {
                // TODO: Vargs handling
            }


            codeBuilder.labelBinding(successLabel);

            codeBuilder.aload(0);
            codeBuilder.dup();
            codeBuilder.getfield(callBuilderClassDesc, "$argumentIndex", CD.INT);
            codeBuilder.iconst_1();
            codeBuilder.iadd();
            codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", CD.INT);

            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    private void implementCallBuilderPut(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionSignature signature,
            @Nullable FunctionParameter kwargsParameter) {
        classBuilder.withMethodBody("$putArgument", MethodTypeDesc.of(CD.PY_CALL_BUILDER, ClassDesc.of(String.class.getCanonicalName()), CD.PY_OBJECT), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(1);
            codeBuilder.invokevirtual(CD.OBJECT, "hashCode", MethodTypeDesc.of(CD.INT));

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
                    codeBuilder.invokevirtual(ClassDesc.of(String.class.getCanonicalName()), "equals", MethodTypeDesc.of(
                            CD.BOOLEAN, CD.OBJECT));
                    codeBuilder.ifThen(blockBuilder -> {
                        blockBuilder.aload(0);
                        blockBuilder.aload(2);
                        blockBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);
                        blockBuilder.goto_(successLabel);
                    });
                }
                codeBuilder.goto_(failureLabel);
            }

            codeBuilder.labelBinding(failureLabel);
            if (kwargsParameter == null) {
                codeBuilder.new_(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()), "<init>", MethodTypeDesc.of(
                        CD.VOID));
                codeBuilder.athrow();
            } else {
                // TODO: Kwargs handling
            }


            codeBuilder.labelBinding(successLabel);
            codeBuilder.aload(0);
            codeBuilder.return_(TypeKind.REFERENCE);
        });
    }

    public ClassDesc getFunctionParameterByNameClassDesc(String parameterName) {
        var keywordInterface = FunctionSignature.getKeywordArgumentInterfaceName(parameterName);
        if (!classLoader.isClassDefined(keywordInterface)) {
            defineSignatureKeywordInterface(keywordInterface, parameterName);
        }
        return ClassDesc.of(keywordInterface);
    }

    public ClassDesc getFunctionParameterByIndexClassDesc(int index) {
        var positionalInterface = FunctionSignature.getPositionalArgumentInterfaceName(index);
        if (!classLoader.isClassDefined(positionalInterface)) {
            defineSignaturePositionalInterface(positionalInterface, index);
        }
        return ClassDesc.of(positionalInterface);
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
            classBuilder.withMethod("$" + argumentIndex, MethodTypeDesc.of(CD.PY_CALL_BUILDER, CD.PY_OBJECT),
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
            classBuilder.withMethod(argumentName, MethodTypeDesc.of(CD.PY_CALL_BUILDER, CD.PY_OBJECT),
                    Modifier.PUBLIC | Modifier.ABSTRACT, methodBuilder -> {
                        methodBuilder.with(MethodParametersAttribute.of(MethodParameterInfo.of(Optional.of("argument"))));
                    });
        });
        classLoader.registerClass(interfaceName, bytecode);
    }

    private PyCallable createCallable(Bytecode bytecode, ClassDesc callBuilderClassDescriptor) {
        var callableClass = FunctionSignature.CALLABLE_PACKAGE + nextClassId() + "." + bytecode.functionName();
        var callableClassDescriptor = ClassDesc.of(callableClass);
        var classBytecode = classFile.build(callableClassDescriptor, classBuilder -> {
            var constantPool = classBuilder.constantPool();
            classBuilder.withInterfaces(constantPool.classEntry(CD.PY_CALLABLE));

            Map<String, PyConstant> constantMap = new LinkedHashMap<>();
            for (var instruction : bytecode.instructions()) {
                if (instruction.opcode() instanceof LoadConstant(var constant)) {
                    constantMap.put(constant.getJavaIdentifierName(), constant);
                }
            }

            if (!constantMap.isEmpty()) {
                for (var constantEntry : constantMap.entrySet()) {
                    classBuilder.withField(constantEntry.getKey(), CD.of(constantEntry.getValue().getClass()), Modifier.PRIVATE | Modifier.FINAL | Modifier.STATIC);
                }

                classBuilder.withMethodBody("<clinit>", MethodTypeDesc.of(CD.VOID), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                    for (var constantEntry : constantMap.entrySet()) {
                        constantEntry.getValue().loadValueOntoStack(codeBuilder);
                        codeBuilder.putstatic(callableClassDescriptor, constantEntry.getKey(), ClassDesc.of(constantEntry.getValue().getClass().getCanonicalName()));
                    }
                    codeBuilder.return_();
                });
            }

            classBuilder.withMethodBody("<init>", MethodTypeDesc.of(CD.VOID), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MethodTypeDesc.of(CD.VOID));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("pyCallBuilder", MethodTypeDesc.of(CD.PY_CALL_BUILDER), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(callBuilderClassDescriptor);
                codeBuilder.dup();
                codeBuilder.aload(0);
                codeBuilder.invokespecial(callBuilderClassDescriptor, "<init>", MethodTypeDesc.of(CD.VOID, CD.PY_CALLABLE));
                codeBuilder.return_(TypeKind.REFERENCE);
            });

            classBuilder.withMethodBody("pyCall", MethodTypeDesc.of(CD.PY_OBJECT, CD.PY_CALL_BUILDER), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.checkcast(callBuilderClassDescriptor);
                codeBuilder.astore(2);
                var compileRun = CompilationRun.init(this, callableClassDescriptor, codeBuilder, bytecode, 3);

                var invalidArgumentsLabel = codeBuilder.newLabel();

                for (var variableEntry : compileRun.variableNameToSlot().entrySet()) {
                    if (compileRun.isCell(variableEntry.getKey())) {
                        var slot = compileRun.getVariableSlot(variableEntry.getKey());
                        if (!bytecode.closure().containsKey(variableEntry.getKey())) {
                            codeBuilder.new_(CD.PY_CELL);
                            codeBuilder.dup();
                            codeBuilder.invokespecial(CD.PY_CELL, "<init>", MethodTypeDesc.of(CD.VOID));
                            codeBuilder.astore(slot);
                        } else {
                            var cell = bytecode.closure().get(variableEntry.getKey());
                            var cellClass = createCellClass(cell.getCellId(), cell.getValue());
                            codeBuilder.getstatic(ClassDesc.of(cellClass), PyCell.CELL_FIELD_NAME, CD.PY_CELL);
                            codeBuilder.astore(slot);
                        }
                    }
                }

                for (var parameter : bytecode.signature().parameters()) {
                    codeBuilder.aload(2);
                    codeBuilder.getfield(callBuilderClassDescriptor, parameter.parameterName(), CD.PY_OBJECT);
                    codeBuilder.dup();
                    codeBuilder.aconst_null();
                    codeBuilder.if_acmpeq(invalidArgumentsLabel);
                    var slot = compileRun.getVariableSlot(parameter.parameterName());
                    if (compileRun.isCell(parameter.parameterName())) {
                        codeBuilder.aload(slot);
                        codeBuilder.swap();
                        codeBuilder.invokevirtual(CD.PY_CELL, "setValue", MD.of(void.class, PyObject.class));
                    } else {
                        codeBuilder.astore(slot);
                    }
                }
                var codeStartLabel = codeBuilder.newLabel();
                codeBuilder.goto_(codeStartLabel);
                codeBuilder.labelBinding(invalidArgumentsLabel);

                codeBuilder.new_(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(IllegalArgumentException.class.getCanonicalName()), "<init>", MethodTypeDesc.of(
                        CD.VOID));
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
