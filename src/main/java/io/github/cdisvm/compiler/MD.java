package io.github.cdisvm.compiler;

import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.lang.reflect.Method;

public final class MD {
    private MD() {}

    public static MethodTypeDesc of(Method method) {
        var paramClassDesc = new ClassDesc[method.getParameterCount()];
        for (int i = 0; i < paramClassDesc.length; i++) {
            paramClassDesc[i] = CD.of(method.getParameterTypes()[i]);
        }
        return MethodTypeDesc.of(CD.of(method.getReturnType()), paramClassDesc);
    }

    public static MethodTypeDesc of(Class<?> returnType, Class<?>... paramTypes) {
        var paramClassDesc = new ClassDesc[paramTypes.length];
        for (int i = 0; i < paramTypes.length; i++) {
            paramClassDesc[i] = CD.of(paramTypes[i]);
        }
        return MethodTypeDesc.of(CD.of(returnType), paramClassDesc);
    }
}
