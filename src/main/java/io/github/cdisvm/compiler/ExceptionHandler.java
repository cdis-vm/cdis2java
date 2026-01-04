package io.github.cdisvm.compiler;

import io.github.cdisvm.runtime.PyType;

public record ExceptionHandler(PyType exceptionType,
                               int fromBytecodeIndex,
                               int toBytecodeIndex,
                               int handlerBytecodeIndex) {
}
