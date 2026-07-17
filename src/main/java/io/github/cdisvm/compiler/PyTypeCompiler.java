package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.function.Consumer;

import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.annotation.PyKwArgs;
import io.github.cdisvm.runtime.annotation.PyKwOnly;
import io.github.cdisvm.runtime.annotation.PyPosOnly;
import io.github.cdisvm.runtime.annotation.PyVarArgs;
import io.github.cdisvm.runtime.builtin.PyObjectType;
import io.github.cdisvm.runtime.builtin.PyTypeType;
import io.github.cdisvm.runtime.builtin.PyUserTypeCallBuilder;

public class PyTypeCompiler {
    private final CDisCompiler cDisCompiler;
    private final Map<Class<?>, PyType> classToCompiledType;
    private final Map<Class<?>, ClassDesc> classToMarkerInterfaceDesc;
    private final Map<String, AttributeDesc> attributeNameToAttributeDesc;
    private final Map<String, PyType> qualifiedNameToCompiledUserType;
    private final Map<String, ClassDesc> qualifiedNameToMarkerInterfaceDesc;

    public PyTypeCompiler(CDisCompiler cDisCompiler) {
        this.cDisCompiler = cDisCompiler;
        classToCompiledType = new LinkedHashMap<>();
        classToMarkerInterfaceDesc = new LinkedHashMap<>();
        attributeNameToAttributeDesc = new LinkedHashMap<>();
        qualifiedNameToCompiledUserType = new LinkedHashMap<>();
        qualifiedNameToMarkerInterfaceDesc = new LinkedHashMap<>();
    }

    private ClassDesc getMarkerInterfaceDesc(Class<?> sourceClass) {
        if (classToMarkerInterfaceDesc.containsKey(sourceClass)) {
            return classToMarkerInterfaceDesc.get(sourceClass);
        }
        var interfaceName = cDisCompiler.createClass("%sMarker".formatted(sourceClass.getSimpleName()), (classDesc, classBuilder) -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            var superInterfaces = new ArrayList<ClassDesc>();
            if (PyObject.class.isAssignableFrom(sourceClass.getSuperclass())) {
                superInterfaces.add(getMarkerInterfaceDesc(sourceClass.getSuperclass()));
            }
            classBuilder.withInterfaceSymbols(superInterfaces);
        });
        var interfaceDesc = ClassDesc.of(interfaceName);
        classToMarkerInterfaceDesc.put(sourceClass, interfaceDesc);
        return interfaceDesc;
    }

    private ClassDesc getMarkerInterfaceDesc(String qualifiedName) {
        var sanitizedName = CDisCompiler.arbitraryTextToJavaIdentifierName(qualifiedName);
        if (qualifiedNameToMarkerInterfaceDesc.containsKey(sanitizedName)) {
            return qualifiedNameToMarkerInterfaceDesc.get(sanitizedName);
        }
        var interfaceName = cDisCompiler.createClass("%sMarker".formatted(sanitizedName), (classDesc, classBuilder) -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            // TODO: Bases
        });
        var interfaceDesc = ClassDesc.of(interfaceName);
        qualifiedNameToMarkerInterfaceDesc.put(qualifiedName, interfaceDesc);
        return interfaceDesc;
    }

    public AttributeDesc getAttributeDesc(String attributeName) {
        if (attributeNameToAttributeDesc.containsKey(attributeName)) {
            return attributeNameToAttributeDesc.get(attributeName);
        }
        var interfaceName = cDisCompiler.createClass("Has$%s".formatted(attributeName), (classDesc, classBuilder) -> {
            var attrDesc = new AttributeDesc(classDesc, attributeName);
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            classBuilder.withMethod(attrDesc.getter(), MD.of(PyObject.class),
                    Modifier.PUBLIC | Modifier.ABSTRACT, _ -> {});
            classBuilder.withMethod(attrDesc.setter(), MD.of(void.class, PyObject.class),
                    Modifier.PUBLIC | Modifier.ABSTRACT, _ -> {});
            classBuilder.withMethod(attrDesc.delete(), MD.of(void.class),
                    Modifier.PUBLIC | Modifier.ABSTRACT, _ -> {});
        });
        var attributeDesc = new AttributeDesc(ClassDesc.of(interfaceName), attributeName);
        attributeNameToAttributeDesc.put(attributeName, attributeDesc);
        return attributeDesc;
    }

    public PyType compileBuiltinType(List<Consumer<CodeBuilder>> initializerList, Class<?> builtinClass) {
        if (classToCompiledType.containsKey(builtinClass)) {
            return classToCompiledType.get(builtinClass);
        }

        PyType parentType = PyObjectType.INSTANCE;
        if (PyObject.class.isAssignableFrom(builtinClass.getSuperclass()) &&
                !Modifier.isAbstract(builtinClass.getSuperclass().getModifiers())) {
            parentType = compileBuiltinType(initializerList, builtinClass.getSuperclass());
        }
        var finalParentType = parentType;

        var constructorMethod = Arrays.stream(builtinClass.getMethods())
                .filter(method -> method.getAnnotation(PyConstructor.class) != null)
                .findFirst()
                .orElseThrow(() -> new NoSuchElementException("Class (%s) does not have a constructor".formatted(builtinClass.getSimpleName())));

        var functionSignatureBuilder = FunctionSignature.builder();
        var parameterIndex = 0;
        for (var parameter : constructorMethod.getParameters()) {
            var defaultValue = parameter.getAnnotation(PyDefault.class);
            var parameterKind = ParameterKind.POSITIONAL_OR_KEYWORD;
            if (parameter.getAnnotation(PyPosOnly.class) != null) {
                parameterKind = ParameterKind.POSITIONAL_ONLY;
            } else if (parameter.getAnnotation(PyKwOnly.class) != null) {
                parameterKind = ParameterKind.KEYWORD_ONLY;
            } else if (parameter.getAnnotation(PyVarArgs.class) != null) {
                parameterKind = ParameterKind.VARGS;
            } else if (parameter.getAnnotation(PyKwArgs.class) != null) {
                parameterKind = ParameterKind.KWARGS;
            }
            functionSignatureBuilder.param(new FunctionParameter(
                    parameterIndex,
                    parameter.getName(),
                    parameterKind,
                    PyObjectType.INSTANCE,
                    (defaultValue != null)? defaultValue.type().getValue(defaultValue.value()) : null
            ));
            parameterIndex++;
        }
        var functionSignature = functionSignatureBuilder.build();
        var constructorCallable = cDisCompiler.compile(Bytecode.ofJavaCode(functionSignature, (codeBuilder, compilationRun) -> {
            for (var parameter : constructorMethod.getParameters()) {
                codeBuilder.aload(compilationRun.getVariableSlot(parameter.getName()));
                codeBuilder.checkcast(CD.of(parameter.getType()));
            }
            codeBuilder.invokestatic(CD.of(builtinClass), constructorMethod.getName(), MD.of(constructorMethod));
            codeBuilder.areturn();
        }));
        var constructorCD = CD.of(constructorCallable.getClass());
        var createdClass = cDisCompiler.createClass(builtinClass.getSimpleName() + "Type", (classDesc, classBuilder) -> {
            var markerInterfaceCD = getMarkerInterfaceDesc(builtinClass);
            classBuilder.withInterfaceSymbols(CD.of(PyType.class), CD.of(PyObject.class), CD.of(PyCallable.class),
                    markerInterfaceCD);
            classBuilder.withField("INSTANCE", classDesc, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            classBuilder.withField("MRO", CD.of(List.class), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

            classBuilder.withMethodBody("<clinit>", MD.of(void.class), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                codeBuilder.new_(classDesc);
                codeBuilder.dup();
                codeBuilder.invokespecial(classDesc, "<init>", MD.of(void.class));
                codeBuilder.putstatic(classDesc, "INSTANCE", classDesc);

                codeBuilder.new_(CD.of(ArrayList.class));
                codeBuilder.dup();
                codeBuilder.invokespecial(CD.of(ArrayList.class), "<init>", MD.of(void.class));
                codeBuilder.dup();
                codeBuilder.getstatic(classDesc, "INSTANCE", classDesc);
                codeBuilder.invokevirtual(CD.of(ArrayList.class), "add", MD.of(boolean.class, Object.class));
                codeBuilder.pop();
                for (var mroEntry : finalParentType.mro()) {
                    var mroEntryCD = CD.of(mroEntry.getClass());
                    codeBuilder.dup();
                    codeBuilder.getstatic(mroEntryCD, "INSTANCE", mroEntryCD);
                    codeBuilder.invokevirtual(CD.of(ArrayList.class), "add", MD.of(boolean.class, Object.class));
                    codeBuilder.pop();
                }
                codeBuilder.putstatic(classDesc, "MRO", CD.of(List.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("<init>", MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MD.of(void.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("pyCallBuilder", MD.of(PyCallBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(constructorCD);
                codeBuilder.dup();
                codeBuilder.invokespecial(constructorCD, "<init>", MD.of(void.class));
                codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("pyCall", MD.of(PyObject.class, PyCallBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.invokeinterface(CD.of(PyCallBuilder.class), "pyCall", MD.of(PyObject.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("mro", MD.of(List.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.getstatic(classDesc, "MRO", CD.of(List.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("pyType", MD.of(PyType.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.getstatic(CD.of(PyTypeType.class), "INSTANCE", CD.of(PyTypeType.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("instanceCheck", MD.of(boolean.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyType", MD.of(PyType.class));
                codeBuilder.instanceOf(markerInterfaceCD);
                codeBuilder.ireturn();
            });

            classBuilder.withMethodBody("subclassCheck", MD.of(boolean.class, PyType.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.instanceOf(markerInterfaceCD);
                codeBuilder.ireturn();
            });
        });
        var createdClassCD = ClassDesc.of(createdClass);
        initializerList.add(codeBuilder -> {
            codeBuilder.getstatic(createdClassCD, "INSTANCE", createdClassCD);
            codeBuilder.putstatic(CD.PY_BUILTINS, builtinClass.getAnnotation(PyBuiltin.class).value(), CD.PY_OBJECT);
            for (var alias : builtinClass.getAnnotation(PyBuiltin.class).aliases()) {
                codeBuilder.getstatic(createdClassCD, "INSTANCE", createdClassCD);
                codeBuilder.putstatic(CD.PY_BUILTINS, alias, CD.PY_OBJECT);
            }
        });
        var loadedClass = cDisCompiler.loadClass(createdClass);
        try {
            var compiledType = (PyType) loadedClass.getField("INSTANCE").get(null);
            classToCompiledType.put(builtinClass, compiledType);
            builtinClass.getField("type").set(null, compiledType);
            return compiledType;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public PyType compileUserType(ClassInfo classInfo) {
        if (qualifiedNameToCompiledUserType.containsKey(classInfo.qualifiedName())) {
            return qualifiedNameToCompiledUserType.get(classInfo.qualifiedName());
        }
        var createdClass = cDisCompiler.createClass(classInfo.simpleName() + "Type", (classDesc, classBuilder) -> {
            var markerInterfaceCD = getMarkerInterfaceDesc(classInfo.qualifiedName());
            classBuilder.withInterfaceSymbols(CD.of(PyType.class), CD.of(PyObject.class), CD.of(PyCallable.class),
                    markerInterfaceCD);
            classBuilder.withField("INSTANCE", classDesc, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            classBuilder.withField("MRO", CD.of(List.class), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

            classBuilder.withMethodBody("<clinit>", MD.of(void.class), Modifier.PUBLIC | Modifier.STATIC, codeBuilder -> {
                codeBuilder.new_(classDesc);
                codeBuilder.dup();
                codeBuilder.invokespecial(classDesc, "<init>", MD.of(void.class));
                codeBuilder.putstatic(classDesc, "INSTANCE", classDesc);

                codeBuilder.new_(CD.of(ArrayList.class));
                codeBuilder.dup();
                codeBuilder.invokespecial(CD.of(ArrayList.class), "<init>", MD.of(void.class));
                codeBuilder.dup();
                codeBuilder.getstatic(classDesc, "INSTANCE", classDesc);
                codeBuilder.invokevirtual(CD.of(ArrayList.class), "add", MD.of(boolean.class, Object.class));
                codeBuilder.pop();

                codeBuilder.dup();
                codeBuilder.getstatic(CD.of(PyObjectType.class), "INSTANCE", CD.of(PyObjectType.class));
                codeBuilder.invokevirtual(CD.of(ArrayList.class), "add", MD.of(boolean.class, Object.class));
                codeBuilder.pop();
                // TODO: Bases
                codeBuilder.putstatic(classDesc, "MRO", CD.of(List.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("<init>", MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MD.of(void.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("pyCallBuilder", MD.of(PyCallBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(CD.of(PyUserTypeCallBuilder.class));
                codeBuilder.dup();
                codeBuilder.new_(classDesc);
                codeBuilder.dup();
                codeBuilder.invokespecial(classDesc, "<init>", MD.of(void.class));
                codeBuilder.invokespecial(CD.of(PyUserTypeCallBuilder.class), "<init>", MD.of(void.class, PyObject.class));
                // TODO: Call __init__

                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("pyCall", MD.of(PyObject.class, PyCallBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.invokeinterface(CD.of(PyCallBuilder.class), "pyCall", MD.of(PyObject.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("mro", MD.of(List.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.getstatic(classDesc, "MRO", CD.of(List.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("pyType", MD.of(PyType.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.getstatic(CD.of(PyTypeType.class), "INSTANCE", CD.of(PyTypeType.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("instanceCheck", MD.of(boolean.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyType", MD.of(PyType.class));
                codeBuilder.instanceOf(markerInterfaceCD);
                codeBuilder.ireturn();
            });

            classBuilder.withMethodBody("subclassCheck", MD.of(boolean.class, PyType.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.instanceOf(markerInterfaceCD);
                codeBuilder.ireturn();
            });
        });
        var loadedClass = cDisCompiler.loadClass(createdClass);
        try {
            var compiledType = (PyType) loadedClass.getField("INSTANCE").get(null);
            qualifiedNameToCompiledUserType.put(classInfo.qualifiedName(), compiledType);
            return compiledType;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
