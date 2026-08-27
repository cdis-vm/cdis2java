package io.github.cdisvm.compiler;

import java.lang.classfile.Annotation;
import java.lang.classfile.AnnotationElement;
import java.lang.classfile.AnnotationValue;
import java.lang.classfile.ClassBuilder;
import java.lang.classfile.MethodSignature;
import java.lang.classfile.Signature;
import java.lang.classfile.attribute.RuntimeVisibleAnnotationsAttribute;
import java.lang.classfile.attribute.SignatureAttribute;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyNone;

public record UserTypeCustomizer(
        CDisCompiler compiler,
        ClassBuilder classBuilder,
        ClassInfo classInfo,
        ClassDesc userTypeInterface) {
    @SafeVarargs
    private static RuntimeVisibleAnnotationsAttribute getAnnotationAttribute(Map<String, Object>... annotations) {
        var annotationList = new ArrayList<Annotation>(annotations.length);
        for (var annotation : annotations) {
            if (!annotation.containsKey("annotationType")) {
                throw new IllegalArgumentException("Annotation (%s) is missing required \"annotationType\" key.".formatted(annotation));
            }
            var annotationType = ClassDesc.of((String) annotation.get("annotationType"));
            var annotationElements = new ArrayList<AnnotationElement>(annotation.size() - 1);
            for (var annotationEntry : annotation.entrySet()) {
                var propertyName = annotationEntry.getKey();
                if (propertyName.equals("annotationType")) {
                    continue;
                }
                var propertyValue = annotationEntry.getValue();
                annotationElements.add(getAnnotationElement(propertyName, propertyValue));
            }
            annotationList.add(Annotation.of(annotationType, annotationElements));
        }
        return RuntimeVisibleAnnotationsAttribute.of(annotationList);
    }

    private static AnnotationElement getAnnotationElement(String name, Object v) {
        AnnotationValue annotationValue = switch (v) {
            case Boolean value -> AnnotationValue.ofBoolean(value);
            case Integer value -> AnnotationValue.ofInt(value);
            case Character value -> AnnotationValue.ofChar(value);
            case Byte value -> AnnotationValue.ofByte(value);
            case Short value -> AnnotationValue.ofShort(value);
            case Long value -> AnnotationValue.ofLong(value);
            case Float value -> AnnotationValue.ofFloat(value);
            case Double value -> AnnotationValue.ofDouble(value);
            case String value -> AnnotationValue.ofString(value);
            default -> throw new IllegalArgumentException();
        };
        return AnnotationElement.of(name, annotationValue);
    }

    // This class is primarily used via Python, so use Python naming standards for public methods
    @SafeVarargs
    public final void add_type_annotations(Map<String, Object>... annotations) {
        classBuilder.accept(getAnnotationAttribute(annotations));
    }

    @SafeVarargs
    public final void add_getter_setter(String propertyName,
            ClassDesc typeDesc,
            Map<String, Object>... annotations) {
        var baseName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        var getterName = "get" + baseName;
        var setterName = "set" + baseName;

        classBuilder.withMethod(getterName, MethodTypeDesc.of(typeDesc), Modifier.PUBLIC, methodBuilder -> {
            methodBuilder.accept(getAnnotationAttribute(annotations));
            methodBuilder.withCode(codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.checkcast(CD.PY_OBJECT);
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
                var attrDesc = compiler.getAttributeDesc(propertyName);
                codeBuilder.checkcast(attrDesc.interfaceDesc());
                codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.getter(), MD.of(PyObject.class));
                codeBuilder.dup();
                PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
                var isNull = codeBuilder.newLabel();
                codeBuilder.if_acmpeq(isNull);
                codeBuilder.checkcast(typeDesc);
                codeBuilder.areturn();
                codeBuilder.labelBinding(isNull);
                codeBuilder.pop();
                codeBuilder.aconst_null();
                codeBuilder.areturn();
            });
        });

        classBuilder.withMethodBody(setterName, MethodTypeDesc.of(CD.VOID, typeDesc), Modifier.PUBLIC, codeBuilder -> {
            codeBuilder.aload(0);
            codeBuilder.checkcast(CD.PY_OBJECT);
            codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
            var attrDesc = compiler.getAttributeDesc(propertyName);
            codeBuilder.checkcast(attrDesc.interfaceDesc());
            codeBuilder.aload(1);
            codeBuilder.aconst_null();
            var isNull = codeBuilder.newLabel();
            codeBuilder.if_acmpeq(isNull);
            codeBuilder.aload(1);
            codeBuilder.checkcast(CD.PY_OBJECT);
            codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.setter(), MD.of(void.class, PyObject.class));
            codeBuilder.return_();
            codeBuilder.labelBinding(isNull);
            PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
            codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.setter(), MD.of(void.class, PyObject.class));
            codeBuilder.return_();
        });
    }

    @SafeVarargs
    public final void add_list_getter_setter(String propertyName,
            ClassDesc typeDesc,
            Map<String, Object>... annotations) {
        var baseName = Character.toUpperCase(propertyName.charAt(0)) + propertyName.substring(1);
        var getterName = "get" + baseName;
        var setterName = "set" + baseName;

        classBuilder.withMethod(getterName, MD.of(List.class), Modifier.PUBLIC, methodBuilder -> {
            methodBuilder.accept(getAnnotationAttribute(annotations));
            methodBuilder.accept(SignatureAttribute.of(
                    MethodSignature.parseFrom("()Ljava/util/List<%s>;".formatted(typeDesc.descriptorString()))));
            methodBuilder.withCode(codeBuilder -> {
                codeBuilder.aload(0);
                codeBuilder.checkcast(CD.PY_OBJECT);
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
                var attrDesc = compiler.getAttributeDesc(propertyName);
                codeBuilder.checkcast(attrDesc.interfaceDesc());
                codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.getter(), MD.of(PyObject.class));
                codeBuilder.dup();
                PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
                var isNull = codeBuilder.newLabel();
                codeBuilder.if_acmpeq(isNull);
                codeBuilder.checkcast(CD.of(List.class));
                codeBuilder.areturn();
                codeBuilder.labelBinding(isNull);
                codeBuilder.pop();
                codeBuilder.aconst_null();
                codeBuilder.areturn();
            });
        });

        classBuilder.withMethod(setterName, MethodTypeDesc.of(CD.VOID, typeDesc), Modifier.PUBLIC, methodBuilder -> {
            methodBuilder.accept(SignatureAttribute.of(
                    MethodSignature.parseFrom("(Ljava/util/List<%s>;)V".formatted(typeDesc.descriptorString()))));
            methodBuilder.withCode(codeBuilder -> {
                codeBuilder.aload(1);
                codeBuilder.aconst_null();
                var isNull = codeBuilder.newLabel();
                codeBuilder.if_acmpeq(isNull);

                codeBuilder.aload(0);
                codeBuilder.checkcast(CD.PY_OBJECT);
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
                var attrDesc = compiler.getAttributeDesc(propertyName);
                codeBuilder.checkcast(attrDesc.interfaceDesc());
                codeBuilder.new_(CD.PY_LIST);
                codeBuilder.dup();
                codeBuilder.aload(1);
                codeBuilder.invokespecial(CD.PY_LIST, "<init>", MD.of(void.class, List.class));
                codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.setter(), MD.of(void.class, PyObject.class));
                codeBuilder.return_();

                codeBuilder.labelBinding(isNull);
                codeBuilder.aload(0);
                codeBuilder.checkcast(CD.PY_OBJECT);
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
                codeBuilder.checkcast(attrDesc.interfaceDesc());
                PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
                codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.setter(), MD.of(void.class, PyObject.class));
                codeBuilder.return_();
            });
        });
    }
}
