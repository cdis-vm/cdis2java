package io.github.cdisvm.compiler;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
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
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import io.github.cdisvm.compiler.opcode.HasGlobal;
import io.github.cdisvm.compiler.opcode.LoadConstant;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyGlobal;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.exception.PyBaseException;

public class CDisCompiler {

    private final ClassFile classFile;
    private final CDisClassLoader classLoader;
    private final PyTypeCompiler builtinCompiler;
    private final Map<Long, String> cellIdToCellClass;
    private final Map<Long, Map<String, String>> globalDictIdToGlobalToClass;
    private final Set<String> builtinSet;
    private long classIdGenerator;

    public CDisCompiler() {
        this.classLoader = new CDisClassLoader();
        this.classFile = ClassFile.of();
        this.cellIdToCellClass = new LinkedHashMap<>();
        this.globalDictIdToGlobalToClass = new LinkedHashMap<>();
        this.classIdGenerator = 0;
        this.builtinSet = new LinkedHashSet<>();
        this.builtinCompiler = new PyTypeCompiler(this);
        createBuiltins();
    }

    public PyType lookupType(String typeName) {
        if (!builtinSet.contains(typeName)) {
            throw new IllegalArgumentException();
        }
        try {
            var builtinClass = classLoader.loadClass(CD.PY_BUILTINS_NAME);
            return (PyType) builtinClass.getField(typeName).get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public void dumpClasses() {
        classLoader.dumpClasses(Path.of("target", "cdis-generated-classes"));
    }

    public void createBuiltins() {
        var runtimeClassList = ClasspathScanner.getRuntimeClasses();
        var bytecode = classFile.build(CD.PY_BUILTINS, classBuilder -> {
            List<Consumer<CodeBuilder>> classInitializers = new ArrayList<>();
            for (var runtimeClass : runtimeClassList) {
                if (runtimeClass.getAnnotation(PyBuiltin.class) != null) {
                    var builtinName = runtimeClass.getAnnotation(PyBuiltin.class).value();
                    classBuilder.withField(builtinName, CD.PY_OBJECT,
                            Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC);
                    for (var alias : runtimeClass.getAnnotation(PyBuiltin.class).aliases()) {
                        classBuilder.withField(alias, CD.PY_OBJECT,
                                Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC);
                        builtinSet.add(alias);
                    }
                    builtinCompiler.compileBuiltinType(classInitializers, runtimeClass);
                    builtinSet.add(builtinName);
                }
                for (var field : runtimeClass.getDeclaredFields()) {
                    if (field.getAnnotation(PyBuiltin.class) != null) {
                        var annotation = field.getAnnotation(PyBuiltin.class);
                        builtinSet.add(annotation.value());
                        classBuilder.withField(annotation.value(), CD.PY_OBJECT,
                                Modifier.PUBLIC | Modifier.FINAL | Modifier.STATIC);
                        try {
                            var constant = (PyConstant) field.get(null);
                            classInitializers.add(codeBuilder -> {
                                constant.loadValueOntoStack(codeBuilder);
                                codeBuilder.putstatic(CD.PY_BUILTINS, annotation.value(), CD.PY_OBJECT);
                            });
                        } catch (IllegalAccessException e) {
                            throw new RuntimeException(e);
                        }
                    }
                }
            }
            classBuilder.withMethodBody("<clinit>", MD.of(void.class), Modifier.PUBLIC | Modifier.STATIC,
                    codeBuilder -> {
                        for (var classInitializer : classInitializers) {
                            classInitializer.accept(codeBuilder);
                        }
                        codeBuilder.return_();
                    });
        });
        classLoader.registerClass(CD.PY_BUILTINS_NAME, bytecode);
    }

    public void dumpClasses(Path dumpLocation) {
        classLoader.dumpClasses(dumpLocation);
    }

    public Class<?> loadClass(String className) {
        try {
            return classLoader.loadClass(className);
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public String createClass(String classNameHint, BiConsumer<ClassDesc, ClassBuilder> classBuilderConsumer) {
        var className = "io.github.cdisvm.codegen.builtins.builtin%s.%s".formatted(nextClassId(), classNameHint);
        var classDesc = ClassDesc.of(className);
        var bytecode = classFile.build(classDesc, cb -> classBuilderConsumer.accept(classDesc, cb));
        classLoader.registerClass(className, bytecode);
        return className;
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

    public void createGlobalClass(long globalDictId, String globalName, @Nullable PyObject value) {
        var globalToClass = globalDictIdToGlobalToClass.computeIfAbsent(globalDictId, _ -> new LinkedHashMap<>());
        if (globalToClass.containsKey(globalName)) {
            return;
        }

        var globalClass = PyGlobal.getClassName(globalDictId, globalName);
        var globalClassDesc = ClassDesc.of(globalClass);
        var bytecode = classFile.build(globalClassDesc, classBuilder -> {
            classBuilder.withField(PyGlobal.GLOBAL_FIELD_NAME, CD.PY_GLOBAL, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

            classBuilder.withMethodBody("<clinit>", MethodTypeDesc.of(CD.VOID), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                codeBuilder.new_(CD.PY_GLOBAL);
                codeBuilder.dup();
                codeBuilder.loadConstant(globalName);
                codeBuilder.loadConstant(globalDictId);
                if (value == null) {
                    codeBuilder.aconst_null();
                } else {
                    var constant = (PyConstant) value;
                    constant.loadValueOntoStack(codeBuilder);
                }
                codeBuilder.invokespecial(CD.PY_GLOBAL, "<init>", MD.of(void.class, String.class, long.class, PyObject.class));
                codeBuilder.putstatic(globalClassDesc, PyGlobal.GLOBAL_FIELD_NAME, CD.PY_GLOBAL);
                codeBuilder.return_();
            });
        });
        classLoader.registerClass(globalClass, bytecode);
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
                if (parameter.defaultValue() != null) {
                    classBuilder.withField(parameter.parameterName() + "$Default", CD.PY_OBJECT, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
                }
            }

            classBuilder.withField("$functionInstance", CD.PY_CALLABLE, Modifier.PRIVATE | Modifier.FINAL);
            classBuilder.withField("$argumentIndex", CD.INT, Modifier.PRIVATE);

            classBuilder.withMethodBody("<clinit>", MD.of(void.class), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                for (var parameter : signature.parameters()) {
                    if (parameter.defaultValue() != null) {
                        if (parameter.defaultValue() instanceof PyConstant constant) {
                            constant.loadValueOntoStack(codeBuilder);
                            codeBuilder.putstatic(callBuilderClassDesc, parameter.parameterName() + "$Default", CD.PY_OBJECT);
                        } else {
                            throw new UnsupportedOperationException();
                        }
                    }
                }
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("<init>", MD.of(void.class, PyCallable.class), Modifier.PUBLIC, codeBuilder -> {
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
                        codeBuilder.aload(0);
                        codeBuilder.getstatic(callBuilderClassDesc, parameter.parameterName() + "$Default", CD.PY_OBJECT);
                        codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);
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
            Map<String, PyGlobal> globalMap = new LinkedHashMap<>();
            for (var instruction : bytecode.instructions()) {
                if (instruction.opcode() instanceof LoadConstant(var constant)) {
                    constantMap.put(constant.getJavaIdentifierName(), constant);
                }
                if (instruction.opcode() instanceof HasGlobal hasGlobal) {
                    createGlobalClass(bytecode.globalsId(), hasGlobal.globalName(), bytecode.globals().get(hasGlobal.globalName()));
                    globalMap.put(hasGlobal.globalName(), new PyGlobal(hasGlobal.globalName(), bytecode.globalsId()));
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
                var compileRun = CompilationRun.init(this, callableClassDescriptor, codeBuilder, bytecode,
                        globalMap, builtinSet, 3);

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
                    if (!(parameter.defaultValue() instanceof PyDefault.NullConstant)) {
                        codeBuilder.dup();
                        codeBuilder.aconst_null();
                        codeBuilder.if_acmpeq(invalidArgumentsLabel);
                    }
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
                implementInstructions(codeBuilder, compileRun, bytecode, -1, -1,0,
                        bytecode.instructions().size());
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

    private void implementInstructions(CodeBuilder codeBuilder, CompilationRun compileRun, Bytecode bytecode,
            int lastSourceLine, int lastTryStart, int from, int to) {
        for (var i = from; i < to; i++) {
            var matchedExceptionHandler = false;
            do {
                matchedExceptionHandler = false;
                for (var exceptionHandler : bytecode.exceptionHandlers()) {
                    if (i != lastTryStart && i == exceptionHandler.fromBytecodeIndex()) {
                        var currentSourceLine = lastSourceLine;
                        var currentTryStart = i;
                        codeBuilder.trying(
                                tryBlockCodeBuilder -> implementInstructions(tryBlockCodeBuilder,
                                        compileRun, bytecode, currentSourceLine,
                                        currentTryStart, exceptionHandler.fromBytecodeIndex(), exceptionHandler.toBytecodeIndex()),
                                catchBuilder -> {
                                    catchBuilder.catching(CD.of(PyBaseException.class), catchBlockCodeBuilder -> {
                                        catchBlockCodeBuilder.astore(compileRun.getLastRaisedExceptionSlot());
                                        catchBlockCodeBuilder.aload(compileRun.getLastRaisedExceptionSlot());
                                        catchBlockCodeBuilder.goto_(
                                                compileRun.bytecodeIndexToLabel().get(exceptionHandler.handlerBytecodeIndex()));
                                    });
                                });
                        i = exceptionHandler.toBytecodeIndex();
                        matchedExceptionHandler = true;
                        break;
                    }
                }
            } while (matchedExceptionHandler);
            var instruction = bytecode.instructions().get(i);
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
    }
}
