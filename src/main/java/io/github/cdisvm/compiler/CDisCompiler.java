package io.github.cdisvm.compiler;

import java.lang.classfile.ClassBuilder;
import java.lang.classfile.ClassFile;
import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.lang.classfile.attribute.MethodParameterInfo;
import java.lang.classfile.attribute.MethodParametersAttribute;
import java.lang.classfile.instruction.SwitchCase;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.SequencedMap;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jspecify.annotations.Nullable;

import io.github.cdisvm.compiler.opcode.HasGlobal;
import io.github.cdisvm.compiler.opcode.LoadConstant;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyGlobal;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.descriptor.PyGetDescriptor;
import io.github.cdisvm.runtime.exception.PyBaseException;

public class CDisCompiler {

    private final ClassFile classFile;
    private final CDisClassLoader classLoader;
    private final PyTypeCompiler typeCompiler;
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
        this.typeCompiler = new PyTypeCompiler(this);
        createBuiltins();
    }

    public PyType lookupBuiltinType(String typeName) {
        if (!builtinSet.contains(typeName)) {
            return null;
        }
        try {
            var builtinClass = classLoader.loadClass(CD.PY_BUILTINS_NAME);
            return (PyType) builtinClass.getField(typeName).get(null);
        } catch (ClassNotFoundException | NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public PyType lookupUserType(ClassInfo classInfo) {
        return typeCompiler.compileUserType(classInfo);
    }

    public AttributeDesc getAttributeDesc(String attributeName) {
        return typeCompiler.getAttributeDesc(attributeName);
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
                    typeCompiler.compileBuiltinType(classInitializers, runtimeClass);
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
            throw new IllegalArgumentException("Cannot convert initial value (%s) to a constant.".formatted(value));
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
            var interfaces = new ArrayList<ClassDesc>();
            interfaces.add(CD.PY_CALL_BUILDER);
            interfaces.addAll(additionalInterfaces);
            classBuilder.withInterfaceSymbols(interfaces);

            for (var parameter : signature.parameters()) {
                switch (parameter.parameterKind()) {
                    case VARGS -> classBuilder.withField(parameter.parameterName(), CD.LIST, Modifier.PUBLIC);
                    case KWARGS -> classBuilder.withField(parameter.parameterName(), CD.of(SequencedMap.class), Modifier.PUBLIC);
                    default -> classBuilder.withField(parameter.parameterName(), CD.PY_OBJECT, Modifier.PUBLIC);
                }
                if (parameter.defaultValue() != null) {
                    classBuilder.withField(parameter.parameterName() + "$Default", CD.PY_OBJECT, Modifier.PRIVATE | Modifier.STATIC | Modifier.FINAL);
                }
            }

            classBuilder.withField("$functionInstance", CD.PY_CALLABLE, Modifier.PRIVATE | Modifier.FINAL);
            classBuilder.withField("$argumentIndex", CD.INT, Modifier.PRIVATE);
            classBuilder.withField("$binding", CD.PY_OBJECT, Modifier.PRIVATE);
            classBuilder.withField("$returnValue", CD.PY_OBJECT, Modifier.PRIVATE);

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

                codeBuilder.aload(0);
                codeBuilder.aconst_null();
                codeBuilder.putfield(callBuilderClassDesc, "$binding", CD.PY_OBJECT);

                codeBuilder.aload(0);
                codeBuilder.aconst_null();
                codeBuilder.putfield(callBuilderClassDesc, "$returnValue", CD.PY_OBJECT);

                for (var parameter : signature.parameters()) {
                    if (parameter.defaultValue() != null) {
                        codeBuilder.aload(0);
                        codeBuilder.getstatic(callBuilderClassDesc, parameter.parameterName() + "$Default", CD.PY_OBJECT);
                        codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);
                    }
                    if (parameter.parameterKind() == ParameterKind.VARGS) {
                        codeBuilder.aload(0);
                        codeBuilder.new_(CD.of(ArrayList.class));
                        codeBuilder.dup();
                        codeBuilder.invokespecial(CD.of(ArrayList.class), "<init>", MD.of(void.class));
                        codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.LIST);
                    } else if (parameter.parameterKind() == ParameterKind.KWARGS) {
                        codeBuilder.aload(0);
                        codeBuilder.new_(CD.of(LinkedHashMap.class));
                        codeBuilder.dup();
                        codeBuilder.invokespecial(CD.of(LinkedHashMap.class), "<init>", MD.of(void.class));
                        codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.of(SequencedMap.class));
                    }
                }
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("<init>", MD.of(void.class, PyCallable.class, PyObject[].class, int[].class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MethodTypeDesc.of(CD.VOID));

                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.putfield(callBuilderClassDesc, "$functionInstance", CD.PY_CALLABLE);

                codeBuilder.aload(0);
                codeBuilder.iconst_0();
                codeBuilder.putfield(callBuilderClassDesc, "$argumentIndex", CD.INT);

                codeBuilder.aload(0);
                codeBuilder.aconst_null();
                codeBuilder.putfield(callBuilderClassDesc, "$binding", CD.PY_OBJECT);

                codeBuilder.aload(0);
                codeBuilder.aconst_null();
                codeBuilder.putfield(callBuilderClassDesc, "$returnValue", CD.PY_OBJECT);

                var arrayLengthSlot = 4;
                var iterationIndexSlot = 5;

                codeBuilder.aload(2);
                codeBuilder.arraylength();
                codeBuilder.istore(arrayLengthSlot);
                codeBuilder.loadConstant(0);
                codeBuilder.istore(iterationIndexSlot);

                var done = codeBuilder.newLabel();
                codeBuilder.iload(arrayLengthSlot);
                codeBuilder.iload(iterationIndexSlot);
                codeBuilder.if_icmpeq(done);
                for (var parameter : signature.parameters()) {
                    var next = codeBuilder.newLabel();
                    codeBuilder.aload(3);
                    codeBuilder.iload(iterationIndexSlot);
                    codeBuilder.iaload();
                    codeBuilder.loadConstant(parameter.parameterIndex());

                    codeBuilder.if_icmpne(next);

                    codeBuilder.aload(0);
                    codeBuilder.aload(2);
                    codeBuilder.iload(iterationIndexSlot);
                    codeBuilder.aaload();
                    codeBuilder.putfield(callBuilderClassDesc, parameter.parameterName(), CD.PY_OBJECT);
                    codeBuilder.iinc(iterationIndexSlot, 1);
                    codeBuilder.iload(arrayLengthSlot);
                    codeBuilder.iload(iterationIndexSlot);
                    codeBuilder.if_icmpeq(done);

                    codeBuilder.labelBinding(next);
                }
                codeBuilder.labelBinding(done);
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
            implementCallBuilderBindTo(classBuilder, callBuilderClassDesc);
            implementCallBuilderReturning(classBuilder, callBuilderClassDesc);
            implementCallBuilderAppend(classBuilder, callBuilderClassDesc, signature, vargsParameter);
            implementCallBuilderPut(classBuilder, callBuilderClassDesc, signature, kwargsParameter);
            implementCallBuilderCall(classBuilder, callBuilderClassDesc);
        });
        classLoader.registerClass(callBuilderClassName, bytecode);
        return callBuilderClassDesc;
    }

    private void implementCallBuilderBindTo(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc) {
        classBuilder.withMethodBody("$bindTo", MD.of(PyCallBuilder.class, PyObject.class), Modifier.PUBLIC,
                codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.aload(1);
                    codeBuilder.putfield(callBuilderClassDesc, "$binding", CD.PY_OBJECT);

                    codeBuilder.aload(0);
                    codeBuilder.aload(1);
                    codeBuilder.invokevirtual(callBuilderClassDesc, "$appendArgument",
                            MD.of(PyCallBuilder.class, PyObject.class));

                    codeBuilder.aload(0);
                    codeBuilder.areturn();
                });
    }

    private void implementCallBuilderReturning(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc) {
        classBuilder.withMethodBody("$returning", MD.of(PyCallBuilder.class, PyObject.class), Modifier.PUBLIC,
                codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.aload(1);
                    codeBuilder.putfield(callBuilderClassDesc, "$returnValue", CD.PY_OBJECT);

                    codeBuilder.aload(0);
                    codeBuilder.areturn();
                });
    }

    private void implementCallBuilderPositionalArgument(ClassBuilder classBuilder, ClassDesc callBuilderClassDesc, FunctionParameter parameter) {
        classBuilder.withMethodBody("$" + parameter.parameterIndex(), MethodTypeDesc.of(CD.PY_CALL_BUILDER, CD.PY_OBJECT), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.getfield(callBuilderClassDesc, "$binding", CD.PY_OBJECT);
            var hasNoBinding = codeBuilder.newLabel();
            codeBuilder.aconst_null();
            codeBuilder.if_acmpeq(hasNoBinding);

            codeBuilder.aload(0);
            codeBuilder.aload(1);
            codeBuilder.invokevirtual(callBuilderClassDesc, "$appendArgument",
                    MD.of(PyCallBuilder.class, PyObject.class));
            codeBuilder.areturn();

            codeBuilder.labelBinding(hasNoBinding);
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

            codeBuilder.aload(0);
            codeBuilder.getfield(callBuilderClassDesc, "$returnValue", CD.PY_OBJECT);;
            codeBuilder.dup();
            codeBuilder.aconst_null();
            var hasNoReturnOverride = codeBuilder.newLabel();

            codeBuilder.if_acmpeq(hasNoReturnOverride);
            codeBuilder.swap();
            codeBuilder.pop();
            codeBuilder.areturn();

            codeBuilder.labelBinding(hasNoReturnOverride);
            codeBuilder.pop();
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
                codeBuilder.aload(0);
                codeBuilder.getfield(callBuilderClassDesc, vargsParameter.parameterName(), CD.LIST);
                codeBuilder.aload(1);
                codeBuilder.invokeinterface(CD.LIST, "add", MD.of(boolean.class, Object.class));
                codeBuilder.pop();
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
                codeBuilder.aload(0);
                codeBuilder.getfield(callBuilderClassDesc, kwargsParameter.parameterName(), CD.of(SequencedMap.class));
                codeBuilder.new_(CD.of(PyStr.class));
                codeBuilder.dup();
                codeBuilder.aload(1);
                codeBuilder.invokespecial(CD.of(PyStr.class), "<init>", MD.of(void.class, String.class));
                codeBuilder.aload(2);
                codeBuilder.invokeinterface(CD.of(SequencedMap.class), "put", MD.of(Object.class, Object.class, Object.class));
                codeBuilder.pop();
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
        var keywordInterface = FunctionSignature.getKeywordArgumentInterfaceName(parameter.parameterName());

        return switch (parameter.parameterKind()) {
            case POSITIONAL_OR_KEYWORD, KEYWORD_ONLY -> {
                if (!classLoader.isClassDefined(keywordInterface)) {
                    defineSignatureKeywordInterface(keywordInterface, parameter.parameterName());
                }
                yield List.of(ClassDesc.of(keywordInterface));
            }
            case VARGS, KWARGS, POSITIONAL_ONLY ->
                // POSITIONAL_ONLY/VARGS/KWARGS don't contribute additional interfaces;
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
            if (bytecode.methodType() == MethodType.STATIC) {
                classBuilder.withInterfaceSymbols(CD.PY_CALLABLE, CD.of(PyConstant.class));
            } else {
                classBuilder.withInterfaceSymbols(CD.PY_CALLABLE, CD.of(PyConstant.class), CD.of(PyGetDescriptor.class));
            }

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

                classBuilder.withMethodBody("<clinit>", MD.of(void.class), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                    for (var constantEntry : constantMap.entrySet()) {
                        constantEntry.getValue().loadValueOntoStack(codeBuilder);
                        codeBuilder.putstatic(callableClassDescriptor, constantEntry.getKey(), ClassDesc.of(constantEntry.getValue().getClass().getCanonicalName()));
                    }
                    codeBuilder.return_();
                });
            }

            for (var freeName : bytecode.freeNames()) {
                classBuilder.withField(freeName, CD.PY_CELL, Modifier.PRIVATE);
            }

            classBuilder.withField("$default", CD.PY_OBJECT.arrayType(), Modifier.PRIVATE);
            classBuilder.withField("$defaultIndices", CD.INT.arrayType(), Modifier.PRIVATE);
            if (bytecode.methodType() != MethodType.STATIC) {
                classBuilder.withField("$bound", CD.PY_OBJECT, Modifier.PRIVATE | Modifier.FINAL);
            }

            classBuilder.withMethodBody("<init>", MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MD.of(void.class));
                for (var freeName : bytecode.freeNames()) {
                    codeBuilder.aload(0);
                    codeBuilder.new_(CD.PY_CELL);
                    codeBuilder.dup();
                    codeBuilder.invokespecial(CD.PY_CELL, "<init>", MD.of(void.class));
                    codeBuilder.putfield(callableClassDescriptor, freeName, CD.PY_CELL);
                }
                codeBuilder.aload(0);
                codeBuilder.aconst_null();
                codeBuilder.putfield(callableClassDescriptor, "$default", CD.PY_OBJECT.arrayType());

                codeBuilder.aload(0);
                codeBuilder.aconst_null();
                codeBuilder.putfield(callableClassDescriptor, "$defaultIndices", CD.INT.arrayType());

                if (bytecode.methodType() != MethodType.STATIC) {
                    codeBuilder.aload(0);
                    codeBuilder.aconst_null();
                    codeBuilder.putfield(callableClassDescriptor, "$bound", CD.PY_OBJECT);
                }

                codeBuilder.return_();
            });

            if (bytecode.methodType() != MethodType.STATIC) {
                classBuilder.withMethodBody("<init>", MD.of(void.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.invokespecial(CD.OBJECT, "<init>", MD.of(void.class));
                    for (var freeName : bytecode.freeNames()) {
                        codeBuilder.aload(0);
                        codeBuilder.new_(CD.PY_CELL);
                        codeBuilder.dup();
                        codeBuilder.invokespecial(CD.PY_CELL, "<init>", MD.of(void.class));
                        codeBuilder.putfield(callableClassDescriptor, freeName, CD.PY_CELL);
                    }
                    codeBuilder.aload(0);
                    codeBuilder.aconst_null();
                    codeBuilder.putfield(callableClassDescriptor, "$default", CD.PY_OBJECT.arrayType());

                    codeBuilder.aload(0);
                    codeBuilder.aconst_null();
                    codeBuilder.putfield(callableClassDescriptor, "$defaultIndices", CD.INT.arrayType());

                    codeBuilder.aload(0);
                    codeBuilder.aload(1);
                    codeBuilder.putfield(callableClassDescriptor, "$bound", CD.PY_OBJECT);

                    codeBuilder.return_();
                });
            }

            if (bytecode.methodType() != MethodType.STATIC) {
                classBuilder.withMethodBody("pyGet", MD.of(PyObject.class, PyObject.class, PyType.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.new_(callableClassDescriptor);
                    codeBuilder.dup();
                    switch (bytecode.methodType()) {
                        case VIRTUAL -> {
                            codeBuilder.aload(1);
                        }
                        case CLASS -> {
                            codeBuilder.aload(2);
                        }
                    }
                    codeBuilder.invokespecial(callableClassDescriptor, "<init>", MD.of(void.class, PyObject.class));
                    codeBuilder.dup();

                    codeBuilder.aload(0);
                    codeBuilder.getfield(callableClassDescriptor, "$default", CD.PY_OBJECT.arrayType());
                    codeBuilder.aload(0);
                    codeBuilder.getfield(callableClassDescriptor, "$defaultIndices", CD.INT.arrayType());
                    codeBuilder.invokevirtual(callableClassDescriptor, "set$Default", MD.of(void.class, PyObject[].class, int[].class));

                    codeBuilder.areturn();
                });
            }

            classBuilder.withMethodBody("loadValueOntoStack", MD.of(void.class, CodeBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);

                codeBuilder.loadConstant(callableClass);
                codeBuilder.invokestatic(CD.of(ClassDesc.class), "of", MD.of(ClassDesc.class, String.class),
                        true);
                codeBuilder.astore(2);

                codeBuilder.aload(2);
                codeBuilder.invokeinterface(CD.of(CodeBuilder.class), "new_", MD.of(CodeBuilder.class, ClassDesc.class));
                codeBuilder.invokeinterface(CD.of(CodeBuilder.class), "dup", MD.of(CodeBuilder.class));

                codeBuilder.aload(2);
                codeBuilder.loadConstant("<init>");

                codeBuilder.loadConstant("V");
                codeBuilder.invokestatic(CD.of(ClassDesc.class), "ofDescriptor", MD.of(ClassDesc.class, String.class),
                        true);
                codeBuilder.invokestatic(CD.of(MethodTypeDesc.class), "of", MD.of(MethodTypeDesc.class, ClassDesc.class),
                        true);

                codeBuilder.invokeinterface(CD.of(CodeBuilder.class), "invokespecial",
                        MD.of(CodeBuilder.class, ClassDesc.class, String.class, MethodTypeDesc.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("getJavaIdentifierName", MD.of(String.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.loadConstant("PyCallable_" + arbitraryTextToJavaIdentifierName(callableClass));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("pyCallBuilder", MethodTypeDesc.of(CD.PY_CALL_BUILDER), Modifier.PUBLIC, codeBuilder -> {
                var overrideDefaults = codeBuilder.newLabel();
                codeBuilder.aload(0);
                codeBuilder.getfield(callableClassDescriptor, "$default", CD.PY_OBJECT.arrayType());
                codeBuilder.aconst_null();
                codeBuilder.if_acmpne(overrideDefaults);

                codeBuilder.new_(callBuilderClassDescriptor);
                codeBuilder.dup();
                codeBuilder.aload(0);
                codeBuilder.invokespecial(callBuilderClassDescriptor, "<init>", MD.of(void.class, PyCallable.class));
                if (bytecode.methodType() != MethodType.STATIC) {
                    codeBuilder.aload(0);
                    codeBuilder.getfield(callableClassDescriptor, "$bound", CD.PY_OBJECT);
                    codeBuilder.dup();
                    codeBuilder.aconst_null();

                    var noBinding = codeBuilder.newLabel();
                    codeBuilder.if_acmpeq(noBinding);

                    codeBuilder.invokeinterface(CD.of(PyCallBuilder.class), "$bindTo", MD.of(PyCallBuilder.class, PyObject.class));
                    codeBuilder.areturn();

                    codeBuilder.labelBinding(noBinding);
                    codeBuilder.pop();
                }
                codeBuilder.areturn();

                codeBuilder.labelBinding(overrideDefaults);
                codeBuilder.new_(callBuilderClassDescriptor);
                codeBuilder.dup();
                codeBuilder.aload(0);

                codeBuilder.aload(0);
                codeBuilder.getfield(callableClassDescriptor, "$default", CD.PY_OBJECT.arrayType());

                codeBuilder.aload(0);
                codeBuilder.getfield(callableClassDescriptor, "$defaultIndices", CD.INT.arrayType());

                codeBuilder.invokespecial(callBuilderClassDescriptor, "<init>", MD.of(void.class, PyCallable.class, PyObject[].class, int[].class));
                if (bytecode.methodType() != MethodType.STATIC) {
                    codeBuilder.aload(0);
                    codeBuilder.getfield(callableClassDescriptor, "$bound", CD.PY_OBJECT);
                    codeBuilder.dup();
                    codeBuilder.aconst_null();

                    var noBinding = codeBuilder.newLabel();
                    codeBuilder.if_acmpeq(noBinding);

                    codeBuilder.invokeinterface(CD.of(PyCallBuilder.class), "$bindTo", MD.of(PyCallBuilder.class, PyObject.class));
                    codeBuilder.areturn();

                    codeBuilder.labelBinding(noBinding);
                    codeBuilder.pop();
                }
                codeBuilder.areturn();
            });

            for (var freeName : bytecode.freeNames()) {
                classBuilder.withMethodBody("$" + freeName, MD.of(void.class, PyCell.class),
                        Modifier.PUBLIC,  codeBuilder -> {
                            codeBuilder.aload(0);
                            codeBuilder.aload(1);
                            codeBuilder.putfield(callableClassDescriptor, freeName, CD.PY_CELL);
                            codeBuilder.return_();
                        });
            }

            classBuilder.withMethodBody("set$Default", MD.of(void.class, PyObject[].class, int[].class),
                    Modifier.PUBLIC,  codeBuilder -> {
                        codeBuilder.aload(0);
                        codeBuilder.aload(1);
                        codeBuilder.putfield(callableClassDescriptor, "$default", CD.PY_OBJECT.arrayType());

                        codeBuilder.aload(0);
                        codeBuilder.aload(2);
                        codeBuilder.putfield(callableClassDescriptor, "$defaultIndices", CD.INT.arrayType());

                        codeBuilder.return_();
                    });

            classBuilder.withMethodBody("pyCall", MethodTypeDesc.of(CD.PY_OBJECT, CD.PY_CALL_BUILDER), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.checkcast(callBuilderClassDescriptor);
                codeBuilder.astore(2);
                var compileRun = CompilationRun.init(this, callableClassDescriptor, codeBuilder, bytecode,
                        globalMap, builtinSet, 3);

                var invalidArgumentsLabel = codeBuilder.newLabel();

                for (var variableEntry : compileRun.variableNameToSlot().entrySet()) {
                    codeBuilder.localVariable(variableEntry.getValue(), variableEntry.getKey(),
                            CD.PY_OBJECT, codeBuilder.startLabel(), codeBuilder.endLabel());
                    if (compileRun.isCell(variableEntry.getKey())) {
                        var slot = compileRun.getVariableSlot(variableEntry.getKey());
                        if (!bytecode.closure().containsKey(variableEntry.getKey())) {
                            if (bytecode.freeNames().contains(variableEntry.getKey())) {
                                codeBuilder.aload(0);
                                codeBuilder.getfield(callableClassDescriptor, variableEntry.getKey(), CD.PY_CELL);
                                codeBuilder.astore(slot);
                            } else {
                                codeBuilder.new_(CD.PY_CELL);
                                codeBuilder.dup();
                                codeBuilder.invokespecial(CD.PY_CELL, "<init>", MethodTypeDesc.of(CD.VOID));
                                codeBuilder.astore(slot);
                            }
                        } else {
                            var cell = bytecode.closure().get(variableEntry.getKey());
                            var cellClass = createCellClass(cell.getCellId(), cell.getValue());
                            codeBuilder.getstatic(ClassDesc.of(cellClass), PyCell.CELL_FIELD_NAME, CD.PY_CELL);
                            codeBuilder.astore(slot);
                        }
                    }
                }

                for (var parameter : bytecode.signature().parameters()) {
                    if (parameter.parameterKind() == ParameterKind.VARGS) {
                        codeBuilder.new_(CD.PY_TUPLE);
                        codeBuilder.dup();
                        codeBuilder.aload(2);
                        codeBuilder.getfield(callBuilderClassDescriptor, parameter.parameterName(), CD.LIST);
                        codeBuilder.invokespecial(CD.PY_TUPLE, "<init>", MethodTypeDesc.of(CD.VOID, CD.LIST));
                    } else if (parameter.parameterKind() == ParameterKind.KWARGS) {
                        codeBuilder.new_(CD.PY_DICT);
                        codeBuilder.dup();
                        codeBuilder.aload(2);
                        codeBuilder.getfield(callBuilderClassDescriptor, parameter.parameterName(), CD.of(SequencedMap.class));
                        codeBuilder.invokespecial(CD.PY_DICT, "<init>", MethodTypeDesc.of(CD.VOID, CD.of(SequencedMap.class)));
                    } else {
                        codeBuilder.aload(2);
                        codeBuilder.getfield(callBuilderClassDescriptor, parameter.parameterName(), CD.PY_OBJECT);
                    }

                    switch (parameter.parameterKind()) {
                        case VARGS, KWARGS -> { /* cannot be null */ }
                        default -> {
                            if (parameter.defaultValue() == null) {
                                codeBuilder.dup();
                                codeBuilder.aconst_null();
                                codeBuilder.if_acmpeq(invalidArgumentsLabel);
                            }
                        }
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
                // Need to assign variables to null in case the Python code tries to access
                // them before they are definitely assigned.
                for (var variableEntry : compileRun.variableNameToSlot().entrySet()) {
                    if (compileRun.isCell(variableEntry.getKey()) || bytecode.signature().parameters()
                            .stream().anyMatch(parameter -> parameter.parameterName().equals(variableEntry.getKey()))) {
                        continue;
                    }
                    codeBuilder.aconst_null();
                    codeBuilder.astore(compileRun.getVariableSlot(variableEntry.getKey()));
                }
                for (var i = 0; i < compileRun.syntheticCount(); i++) {
                    codeBuilder.aconst_null();
                    codeBuilder.astore(compileRun.getSyntheticSlot(i));
                }
                implementInstructions(codeBuilder, compileRun, bytecode, -1, -1, 0,
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
                var originalIndex = i;
                for (var exceptionHandler : bytecode.exceptionHandlers()) {
                    if (originalIndex != lastTryStart && originalIndex == exceptionHandler.fromBytecodeIndex()) {
                        var currentSourceLine = lastSourceLine;
                        var currentInstructionStart = i;
                        codeBuilder.trying(
                                tryBlockCodeBuilder -> implementInstructions(tryBlockCodeBuilder,
                                        compileRun, bytecode, currentSourceLine,
                                        originalIndex, currentInstructionStart, exceptionHandler.toBytecodeIndex()),
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
