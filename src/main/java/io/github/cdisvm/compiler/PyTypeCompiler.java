package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyIterator;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;
import io.github.cdisvm.runtime.annotation.PyKwArgs;
import io.github.cdisvm.runtime.annotation.PyKwOnly;
import io.github.cdisvm.runtime.annotation.PyPosOnly;
import io.github.cdisvm.runtime.annotation.PyVarArgs;
import io.github.cdisvm.runtime.builtin.PyDefaultInstanceAttributes;
import io.github.cdisvm.runtime.builtin.PyDefaultTypeAttributes;
import io.github.cdisvm.runtime.builtin.PyObjectType;
import io.github.cdisvm.runtime.builtin.PyTypeType;
import io.github.cdisvm.runtime.builtin.PyUserTypeCallBuilder;
import io.github.cdisvm.runtime.descriptor.PyDeleteDescriptor;
import io.github.cdisvm.runtime.descriptor.PyGetDescriptor;
import io.github.cdisvm.runtime.descriptor.PySetDescriptor;
import io.github.cdisvm.runtime.exception.PyAttributeError;
import io.github.cdisvm.runtime.exception.PyStopIteration;

public class PyTypeCompiler {
    private final CDisCompiler cDisCompiler;
    private final Map<Class<?>, PyType> classToCompiledType;
    private final Map<Class<?>, ClassDesc> classToMarkerInterfaceDesc;
    private final Map<String, AttributeDesc> attributeNameToAttributeDesc;
    private final Map<ClassInfo, PyType> classInfoToCompiledUserType;
    private final Map<String, ClassDesc> qualifiedNameToMarkerInterfaceDesc;

    public PyTypeCompiler(CDisCompiler cDisCompiler) {
        this.cDisCompiler = cDisCompiler;
        classToCompiledType = new ConcurrentHashMap<>();
        classToMarkerInterfaceDesc = new ConcurrentHashMap<>();
        attributeNameToAttributeDesc = new ConcurrentHashMap<>();
        classInfoToCompiledUserType = new ConcurrentHashMap<>();
        qualifiedNameToMarkerInterfaceDesc = new ConcurrentHashMap<>();
    }

    private ClassDesc getBuiltinMarkerInterfaceDesc(Class<?> sourceClass) {
        if (classToMarkerInterfaceDesc.containsKey(sourceClass)) {
            return classToMarkerInterfaceDesc.get(sourceClass);
        }
        var interfaceName = cDisCompiler.createClass("%sMarker".formatted(sourceClass.getSimpleName()), (classDesc, classBuilder) -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            var superInterfaces = new ArrayList<ClassDesc>();
            if (PyObject.class.isAssignableFrom(sourceClass.getSuperclass())) {
                superInterfaces.add(getBuiltinMarkerInterfaceDesc(sourceClass.getSuperclass()));
            }
            classBuilder.withInterfaceSymbols(superInterfaces);
        });
        var interfaceDesc = ClassDesc.of(interfaceName);
        classToMarkerInterfaceDesc.put(sourceClass, interfaceDesc);
        return interfaceDesc;
    }

    private ClassDesc getUserMarkerInterfaceDesc(ClassInfo classInfo, Consumer<UserTypeCustomizer> customizerConsumer) {
        if (qualifiedNameToMarkerInterfaceDesc.containsKey(classInfo.qualifiedName())) {
            return qualifiedNameToMarkerInterfaceDesc.get(classInfo.qualifiedName());
        }
        var interfaceName = cDisCompiler.createUserClass("%sMarker".formatted(classInfo.qualifiedName()), (classDesc, classBuilder) -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            // TODO: Bases
            var customizer = new UserTypeCustomizer(cDisCompiler, classBuilder, classInfo, classDesc);
            customizerConsumer.accept(customizer);
        });
        var interfaceDesc = ClassDesc.of(interfaceName);
        qualifiedNameToMarkerInterfaceDesc.put(classInfo.qualifiedName(), interfaceDesc);
        return interfaceDesc;
    }

    private ClassDesc getTypeMarkerInterfaceDesc(String qualifiedName) {
        // Can reuse same map; Python classes cannot use "$"
        if (qualifiedNameToMarkerInterfaceDesc.containsKey(qualifiedName + "$TypeMarker")) {
            return qualifiedNameToMarkerInterfaceDesc.get(qualifiedName + "$TypeMarker");
        }
        var interfaceName = cDisCompiler.createUserClass("%s$TypeMarker".formatted(qualifiedName), (classDesc, classBuilder) -> {
            classBuilder.withFlags(Modifier.PUBLIC | Modifier.ABSTRACT | Modifier.INTERFACE);
            // TODO: Bases
        });
        var interfaceDesc = ClassDesc.of(interfaceName);
        qualifiedNameToMarkerInterfaceDesc.put(qualifiedName + "$TypeMarker", interfaceDesc);
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
            var markerInterfaceCD = getBuiltinMarkerInterfaceDesc(builtinClass);
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

            classBuilder.withMethodBody("newInstance", MD.of(PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.new_(constructorCD);
                codeBuilder.dup();
                codeBuilder.invokespecial(constructorCD, "<init>", MD.of(void.class));
                codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
                codeBuilder.invokevirtual(classDesc, "pyCall", MD.of(PyObject.class, PyCallBuilder.class));
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

    public PyType compileUserType(ClassInfo classInfo, Consumer<UserTypeCustomizer> customizerConsumer) {
        if (classInfoToCompiledUserType.containsKey(classInfo)) {
            return classInfoToCompiledUserType.get(classInfo);
        }
        var createdClass = cDisCompiler.createClass(classInfo.simpleName() + "Type", (classDesc, classBuilder) -> {
            var typeMarkerInterfaceCD = getTypeMarkerInterfaceDesc(classInfo.qualifiedName());
            classBuilder.withInterfaceSymbols(CD.of(PyType.class), CD.of(PyObject.class), CD.of(PyCallable.class),
                    typeMarkerInterfaceCD);

            var attributeClass = createTypeAttributes(classInfo);
            var instanceClassDesc = compileUserTypeInstance(classInfo,
                    classDesc,
                    ClassDesc.of(attributeClass),
                    customizerConsumer);
            classBuilder.withField("INSTANCE", classDesc, Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            classBuilder.withField("MRO", CD.of(List.class), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);
            classBuilder.withField("ATTRIBUTES", ClassDesc.of(attributeClass), Modifier.PUBLIC | Modifier.STATIC | Modifier.FINAL);

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

                codeBuilder.new_(ClassDesc.of(attributeClass));
                codeBuilder.dup();
                codeBuilder.invokespecial(ClassDesc.of(attributeClass), "<init>", MD.of(void.class));
                codeBuilder.putstatic(classDesc, "ATTRIBUTES", ClassDesc.of(attributeClass));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("<init>", MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MD.of(void.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("pyCallBuilder", MD.of(PyCallBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                if (classInfo.classAttributeToDefaultValue().containsKey("__init__")) {
                    var initInterface = getAttributeDesc("__init__");
                    codeBuilder.getstatic(classDesc, "ATTRIBUTES", ClassDesc.of(attributeClass));
                    codeBuilder.invokeinterface(initInterface.interfaceDesc(), initInterface.getter(), MD.of(PyObject.class));
                    codeBuilder.checkcast(CD.of(PyCallable.class));
                    codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));

                    codeBuilder.new_(instanceClassDesc);
                    codeBuilder.dup();
                    codeBuilder.invokespecial(instanceClassDesc, "<init>", MD.of(void.class));
                    codeBuilder.dup_x1();
                    codeBuilder.invokeinterface(CD.PY_CALL_BUILDER, "$bindTo", MD.of(PyCallBuilder.class, PyObject.class));
                    codeBuilder.swap();
                    codeBuilder.invokeinterface(CD.PY_CALL_BUILDER, "$returning", MD.of(PyCallBuilder.class, PyObject.class));
                    codeBuilder.areturn();
                } else {
                    codeBuilder.new_(CD.of(PyUserTypeCallBuilder.class));
                    codeBuilder.dup();
                    codeBuilder.new_(instanceClassDesc);
                    codeBuilder.dup();
                    codeBuilder.invokespecial(instanceClassDesc, "<init>", MD.of(void.class));
                    codeBuilder.invokespecial(CD.of(PyUserTypeCallBuilder.class), "<init>", MD.of(void.class, PyObject.class));
                    codeBuilder.areturn();
                }
            });

            classBuilder.withMethodBody("pyCall", MD.of(PyObject.class, PyCallBuilder.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.invokeinterface(CD.of(PyCallBuilder.class), "pyCall", MD.of(PyObject.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("newInstance", MD.of(PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(instanceClassDesc);
                codeBuilder.dup();
                codeBuilder.invokespecial(instanceClassDesc, "<init>", MD.of(void.class));
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
                codeBuilder.instanceOf(typeMarkerInterfaceCD);
                codeBuilder.ireturn();
            });

            classBuilder.withMethodBody("subclassCheck", MD.of(boolean.class, PyType.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.instanceOf(typeMarkerInterfaceCD);
                codeBuilder.ireturn();
            });

            classBuilder.withMethodBody("pyAttributes", MD.of(PyAttributes.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.getstatic(classDesc, "ATTRIBUTES", ClassDesc.of(attributeClass));
                codeBuilder.areturn();
            });
        });
        var loadedClass = cDisCompiler.loadClass(createdClass);
        try {
            var compiledType = (PyType) loadedClass.getField("INSTANCE").get(null);
            classInfoToCompiledUserType.put(classInfo, compiledType);
            return compiledType;
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    public ClassDesc compileUserTypeInstance(ClassInfo classInfo,
            ClassDesc typeClassDesc,
            ClassDesc typeAttributeClassDesc,
            Consumer<UserTypeCustomizer> customizerConsumer) {
        var hasIteratorProtocol = classInfo.classAttributeToDefaultValue().containsKey("__iter__")
                && classInfo.classAttributeToDefaultValue().containsKey("__next__");
        var createdClass = cDisCompiler.createUserClass(classInfo.qualifiedName(), (classDesc, classBuilder) -> {
            var markerInterfaceCD = getUserMarkerInterfaceDesc(classInfo, customizerConsumer);
            var interfaces = new ArrayList<ClassDesc>();
            interfaces.add(CD.of(PyObject.class));
            interfaces.add(markerInterfaceCD);
            if (hasIteratorProtocol) {
                interfaces.add(CD.of(PyIterable.class));
                interfaces.add(CD.of(PyIterator.class));
            }
            classBuilder.withInterfaceSymbols(interfaces);

            var attributeClass = createTypeInstanceAttributes(classInfo, typeClassDesc, typeAttributeClassDesc);
            classBuilder.withField("$attributes", ClassDesc.of(attributeClass), Modifier.PRIVATE | Modifier.FINAL);

            classBuilder.withMethodBody("<init>", MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.OBJECT, "<init>", MD.of(void.class));
                codeBuilder.aload(0);
                codeBuilder.new_(ClassDesc.of(attributeClass));
                codeBuilder.dup();
                codeBuilder.aload(0);
                codeBuilder.invokespecial(ClassDesc.of(attributeClass), "<init>", MD.of(void.class, PyObject.class));
                codeBuilder.putfield(classDesc, "$attributes", ClassDesc.of(attributeClass));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("pyType", MD.of(PyType.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.getstatic(typeClassDesc, "INSTANCE", typeClassDesc);
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("pyAttributes", MD.of(PyAttributes.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.getfield(classDesc, "$attributes", ClassDesc.of(attributeClass));
                codeBuilder.areturn();
            });

            if (hasIteratorProtocol) {
                classBuilder.withMethodBody("pyIterator", MD.of(PyIterator.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
                    codeBuilder.loadConstant("__iter__");
                    codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName", MD.of(PyObject.class, String.class));
                    codeBuilder.checkcast(CD.PY_CALLABLE);
                    codeBuilder.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
                    codeBuilder.invokeinterface(CD.PY_CALL_BUILDER, "pyCall", MD.of(PyObject.class));
                    codeBuilder.checkcast(CD.PY_ITERATOR);
                    codeBuilder.areturn();
                });
                classBuilder.withMethodBody("pyNext", MD.of(PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.trying(
                            tryBlock -> {
                                tryBlock.aload(0);
                                tryBlock.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
                                tryBlock.loadConstant("__next__");
                                tryBlock.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName", MD.of(PyObject.class, String.class));
                                tryBlock.checkcast(CD.PY_CALLABLE);
                                tryBlock.invokeinterface(CD.PY_CALLABLE, "pyCallBuilder", MD.of(PyCallBuilder.class));
                                tryBlock.invokeinterface(CD.PY_CALL_BUILDER, "pyCall", MD.of(PyObject.class));
                                tryBlock.areturn();
                            },
                            catchBlock -> catchBlock.catching(CD.of(PyStopIteration.class), catchCode -> {
                                catchCode.pop();
                                catchCode.aconst_null();
                                catchCode.areturn();
                            }));
                });
            }
        });
        return ClassDesc.of(createdClass);
    }

    private String createTypeAttributes(ClassInfo classInfo) {
        var createdClass = cDisCompiler.createUserClass(classInfo.qualifiedName() + "$TypeAttributes", (classDesc, classBuilder) -> {
            var attributeInterfaces = new ArrayList<ClassDesc>();
            attributeInterfaces.add(CD.of(PyAttributes.class));
            for (var attr : classInfo.classAttributeToType().keySet()) {
                var attributeDesc = getAttributeDesc(attr);
                attributeInterfaces.add(attributeDesc.interfaceDesc());
            }
            classBuilder.withInterfaceSymbols(attributeInterfaces);
            classBuilder.withSuperclass(CD.of(PyDefaultTypeAttributes.class));
            for (var attr : classInfo.classAttributeToType().entrySet()) {
                classBuilder.withField(attr.getKey(), CD.PY_OBJECT, Modifier.PRIVATE);
            }
            classBuilder.withMethodBody("<init>", MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.of(PyDefaultTypeAttributes.class), "<init>", MD.of(void.class));
                for (var attr : classInfo.classAttributeToDefaultValue().entrySet()) {
                    if (attr.getValue() == null) {
                        // Object was defined in C
                        continue;
                    }
                    codeBuilder.aload(0);
                    var value = attr.getValue();
                    if (value instanceof PyConstant constant) {
                        constant.loadValueOntoStack(codeBuilder);
                    } else {
                        // Generic/C, initialize to null
                        codeBuilder.aconst_null();
                    }
                    codeBuilder.putfield(classDesc, attr.getKey(), CD.PY_OBJECT);
                }
                codeBuilder.return_();
            });
            for (var attr : classInfo.classAttributeToType().keySet()) {
                var attributeDesc = getAttributeDesc(attr);
                classBuilder.withMethodBody(attributeDesc.getter(), MD.of(PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.getfield(classDesc, attr, CD.PY_OBJECT);
                    codeBuilder.dup();
                    codeBuilder.aconst_null();
                    var returnLabel = codeBuilder.newLabel();
                    codeBuilder.if_acmpne(returnLabel);

                    codeBuilder.pop();
                    codeBuilder.new_(CD.of(PyAttributeError.class));
                    codeBuilder.dup();
                    codeBuilder.loadConstant("<class '%s'> object has no attribute '%s'."
                            .formatted(classInfo.simpleName(), attr));
                    codeBuilder.invokespecial(CD.of(PyAttributeError.class), "<init>",
                            MD.of(void.class, String.class));
                    codeBuilder.athrow();

                    codeBuilder.labelBinding(returnLabel);
                    codeBuilder.areturn();
                });
                classBuilder.withMethodBody(attributeDesc.setter(), MD.of(void.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.aload(1);
                    codeBuilder.putfield(classDesc, attr, CD.PY_OBJECT);
                    codeBuilder.return_();
                });
                classBuilder.withMethodBody(attributeDesc.delete(), MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.aconst_null();
                    codeBuilder.putfield(classDesc, attr, CD.PY_OBJECT);
                    codeBuilder.return_();
                });
            }

            classBuilder.withMethodBody("attributeNames", MD.of(Collection.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(CD.of(ArrayList.class));
                codeBuilder.dup();
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.of(PyDefaultTypeAttributes.class), "attributeNames", MD.of(Collection.class));
                codeBuilder.invokespecial(CD.of(ArrayList.class), "<init>", MD.of(void.class, Collection.class));
                for (var attr : classInfo.classAttributeToType().keySet()) {
                    codeBuilder.dup();
                    codeBuilder.loadConstant(attr);
                    codeBuilder.invokevirtual(CD.of(ArrayList.class), "add", MD.of(boolean.class, Object.class));
                    codeBuilder.pop();
                }
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("getAttributeByNameOrNull", MD.of(PyObject.class, String.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                BytecodeUtil.implementStringSwitchCase(codeBuilder,
                        classInfo.classAttributeToType().keySet(),
                        true,
                        (attributeName, caseBuilder) -> {
                            caseBuilder.aload(0);
                            caseBuilder.getfield(classDesc, attributeName, CD.PY_OBJECT);
                            caseBuilder.areturn();
                        });
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.invokespecial(CD.of(PyDefaultTypeAttributes.class), "getAttributeByNameOrNull", MD.of(PyObject.class, String.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("setAttributeByName", MD.of(void.class, String.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                BytecodeUtil.implementStringSwitchCase(codeBuilder,
                        classInfo.classAttributeToType().keySet(),
                        true,
                        (attributeName, caseBuilder) -> {
                            caseBuilder.aload(0);
                            caseBuilder.aload(2);
                            caseBuilder.putfield(classDesc, attributeName, CD.PY_OBJECT);
                            caseBuilder.return_();
                        });
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.aload(2);
                codeBuilder.invokespecial(CD.of(PyDefaultTypeAttributes.class), "setAttributeByName", MD.of(void.class, String.class, PyObject.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("deleteAttributeByName", MD.of(void.class, String.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                BytecodeUtil.implementStringSwitchCase(codeBuilder,
                        classInfo.classAttributeToType().keySet(),
                        true,
                        (attributeName, caseBuilder) -> {
                            caseBuilder.aload(0);
                            caseBuilder.aconst_null();
                            caseBuilder.putfield(classDesc, attributeName, CD.PY_OBJECT);
                            caseBuilder.return_();
                        });
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.invokespecial(CD.of(PyDefaultTypeAttributes.class), "deleteAttributeByName", MD.of(void.class, String.class));
                codeBuilder.return_();
            });
        });
        return createdClass;
    }

    private String createTypeInstanceAttributes(ClassInfo classInfo, ClassDesc typeClassDesc, ClassDesc typeAttributeClassDesc) {
        var createdClass = cDisCompiler.createUserClass(classInfo.qualifiedName() + "$Attributes", (classDesc, classBuilder) -> {
            var attributeInterfaces = new ArrayList<ClassDesc>();
            attributeInterfaces.add(CD.of(PyAttributes.class));

            for (var attr : classInfo.classAttributeToType().keySet()) {
                var attributeDesc = getAttributeDesc(attr);
                attributeInterfaces.add(attributeDesc.interfaceDesc());
            }

            for (var attr : classInfo.instanceAttributeToType().keySet()) {
                var attributeDesc = getAttributeDesc(attr);
                attributeInterfaces.add(attributeDesc.interfaceDesc());
            }

            classBuilder.withInterfaceSymbols(attributeInterfaces);
            classBuilder.withSuperclass(CD.of(PyDefaultInstanceAttributes.class));

            for (var attr : classInfo.instanceAttributeToType().entrySet()) {
                classBuilder.withField(attr.getKey(), CD.PY_OBJECT, Modifier.PRIVATE);
            }

            classBuilder.withMethodBody("<init>", MD.of(void.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.getstatic(typeClassDesc, "ATTRIBUTES", typeAttributeClassDesc);
                codeBuilder.invokespecial(CD.of(PyDefaultInstanceAttributes.class), "<init>", MD.of(void.class, PyObject.class, PyAttributes.class));
                codeBuilder.return_();
            });

            for (var attr : classInfo.classAttributeToType().keySet()) {
                var attributeDesc = getAttributeDesc(attr);
                classBuilder.withMethodBody(attributeDesc.getter(), MD.of(PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "typeAttributes", MD.of(PyAttributes.class));
                    codeBuilder.checkcast(typeAttributeClassDesc);
                    codeBuilder.invokevirtual(typeAttributeClassDesc, attributeDesc.getter(), MD.of(PyObject.class));
                    codeBuilder.dup();
                    codeBuilder.instanceOf(CD.of(PyGetDescriptor.class));
                    var nonDescriptor = codeBuilder.newLabel();
                    codeBuilder.ifeq(nonDescriptor);

                    codeBuilder.checkcast(CD.of(PyGetDescriptor.class));
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "instance", MD.of(PyObject.class));
                    codeBuilder.getstatic(typeClassDesc, "INSTANCE", typeClassDesc);
                    codeBuilder.invokeinterface(CD.of(PyGetDescriptor.class), "pyGet", MD.of(PyObject.class, PyObject.class, PyType.class));

                    codeBuilder.labelBinding(nonDescriptor);
                    codeBuilder.areturn();
                });
                classBuilder.withMethodBody(attributeDesc.setter(), MD.of(void.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "typeAttributes", MD.of(PyAttributes.class));
                    codeBuilder.checkcast(typeAttributeClassDesc);
                    codeBuilder.invokevirtual(typeAttributeClassDesc, attributeDesc.getter(), MD.of(PyObject.class));
                    codeBuilder.dup();
                    codeBuilder.instanceOf(CD.of(PySetDescriptor.class));
                    var nonDescriptor = codeBuilder.newLabel();
                    codeBuilder.ifeq(nonDescriptor);

                    codeBuilder.checkcast(CD.of(PySetDescriptor.class));
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "instance", MD.of(PyObject.class));
                    codeBuilder.aload(1);
                    codeBuilder.invokeinterface(CD.of(PySetDescriptor.class), "pySet", MD.of(void.class, PyObject.class, PyObject.class));
                    codeBuilder.return_();

                    codeBuilder.labelBinding(nonDescriptor);
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "typeAttributes", MD.of(PyAttributes.class));
                    codeBuilder.checkcast(typeAttributeClassDesc);
                    codeBuilder.aload(1);
                    codeBuilder.invokevirtual(typeAttributeClassDesc, attributeDesc.setter(), MD.of(void.class, PyObject.class));
                    codeBuilder.return_();
                });
                classBuilder.withMethodBody(attributeDesc.delete(), MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "typeAttributes", MD.of(PyAttributes.class));
                    codeBuilder.checkcast(typeAttributeClassDesc);
                    codeBuilder.invokevirtual(typeAttributeClassDesc, attributeDesc.getter(), MD.of(PyObject.class));
                    codeBuilder.dup();
                    codeBuilder.instanceOf(CD.of(PyDeleteDescriptor.class));
                    var nonDescriptor = codeBuilder.newLabel();
                    codeBuilder.ifeq(nonDescriptor);

                    codeBuilder.checkcast(CD.of(PyDeleteDescriptor.class));
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "instance", MD.of(PyObject.class));
                    codeBuilder.invokeinterface(CD.of(PyDeleteDescriptor.class), "pyDelete", MD.of(void.class, PyObject.class));
                    codeBuilder.return_();

                    codeBuilder.labelBinding(nonDescriptor);
                    codeBuilder.aload(0);
                    codeBuilder.invokevirtual(CD.of(PyDefaultInstanceAttributes.class), "typeAttributes", MD.of(PyAttributes.class));
                    codeBuilder.checkcast(typeAttributeClassDesc);
                    codeBuilder.invokevirtual(typeAttributeClassDesc, attributeDesc.delete(), MD.of(void.class));
                    codeBuilder.return_();
                });
            }

            for (var attr : classInfo.instanceAttributeToType().keySet()) {
                var attributeDesc = getAttributeDesc(attr);
                classBuilder.withMethodBody(attributeDesc.getter(), MD.of(PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.getfield(classDesc, attr, CD.PY_OBJECT);
                    codeBuilder.dup();
                    codeBuilder.aconst_null();
                    var returnLabel = codeBuilder.newLabel();
                    codeBuilder.if_acmpne(returnLabel);

                    codeBuilder.pop();
                    codeBuilder.new_(CD.of(PyAttributeError.class));
                    codeBuilder.dup();
                    codeBuilder.loadConstant("<class '%s'> object has no attribute '%s'."
                            .formatted(classInfo.simpleName(), attr));
                    codeBuilder.invokespecial(CD.of(PyAttributeError.class), "<init>",
                            MD.of(void.class, String.class));
                    codeBuilder.athrow();

                    codeBuilder.labelBinding(returnLabel);
                    codeBuilder.areturn();
                });
                classBuilder.withMethodBody(attributeDesc.setter(), MD.of(void.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.aload(1);
                    codeBuilder.putfield(classDesc, attr, CD.PY_OBJECT);
                    codeBuilder.return_();
                });
                classBuilder.withMethodBody(attributeDesc.delete(), MD.of(void.class), Modifier.PUBLIC, codeBuilder -> {
                    codeBuilder.aload(0);
                    codeBuilder.aconst_null();
                    codeBuilder.putfield(classDesc, attr, CD.PY_OBJECT);
                    codeBuilder.return_();
                });
            }

            classBuilder.withMethodBody("attributeNames", MD.of(Collection.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.new_(CD.of(ArrayList.class));
                codeBuilder.dup();
                codeBuilder.aload(0);
                codeBuilder.invokespecial(CD.of(PyDefaultInstanceAttributes.class), "attributeNames", MD.of(Collection.class));
                codeBuilder.invokespecial(CD.of(ArrayList.class), "<init>", MD.of(void.class, Collection.class));
                for (var attr : classInfo.instanceAttributeToType().keySet()) {
                    codeBuilder.dup();
                    codeBuilder.loadConstant(attr);
                    codeBuilder.invokevirtual(CD.of(ArrayList.class), "add", MD.of(boolean.class, Object.class));
                    codeBuilder.pop();
                }
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("getAttributeByNameOrNull", MD.of(PyObject.class, String.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                BytecodeUtil.implementStringSwitchCase(codeBuilder,
                        classInfo.instanceAttributeToType().keySet(),
                        true,
                        (attributeName, caseBuilder) -> {
                            caseBuilder.aload(0);
                            caseBuilder.invokevirtual(classDesc, getAttributeDesc(attributeName).getter(), MD.of(PyObject.class));
                            caseBuilder.areturn();
                        });
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.invokespecial(CD.of(PyDefaultInstanceAttributes.class), "getAttributeByNameOrNull", MD.of(PyObject.class, String.class));
                codeBuilder.areturn();
            });

            classBuilder.withMethodBody("setAttributeByName", MD.of(void.class, String.class, PyObject.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                BytecodeUtil.implementStringSwitchCase(codeBuilder,
                        classInfo.instanceAttributeToType().keySet(),
                        true,
                        (attributeName, caseBuilder) -> {
                            caseBuilder.aload(0);
                            caseBuilder.aload(2);
                            caseBuilder.invokevirtual(classDesc, getAttributeDesc(attributeName).setter(), MD.of(void.class, PyObject.class));
                            caseBuilder.return_();
                        });
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.aload(2);
                codeBuilder.invokespecial(CD.of(PyDefaultInstanceAttributes.class), "setAttributeByName", MD.of(void.class, String.class, PyObject.class));
                codeBuilder.return_();
            });

            classBuilder.withMethodBody("deleteAttributeByName", MD.of(void.class, String.class), Modifier.PUBLIC, codeBuilder -> {
                codeBuilder.aload(1);
                BytecodeUtil.implementStringSwitchCase(codeBuilder,
                        classInfo.instanceAttributeToType().keySet(),
                        true,
                        (attributeName, caseBuilder) -> {
                            caseBuilder.aload(0);
                            caseBuilder.invokevirtual(classDesc, getAttributeDesc(attributeName).delete(), MD.of(void.class));
                            caseBuilder.return_();
                        });
                codeBuilder.aload(0);
                codeBuilder.aload(1);
                codeBuilder.invokespecial(CD.of(PyDefaultInstanceAttributes.class), "deleteAttributeByName", MD.of(void.class, String.class));
                codeBuilder.return_();
            });
        });
        return createdClass;
    }
}
