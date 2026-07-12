package io.github.cdisvm.compiler;

import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyObject;

@NullMarked
public record Bytecode(
        String functionName,
        FunctionSignature signature,
        FunctionType functionType,
        MethodType methodType,
        int syntheticCount,
        List<Instruction> instructions,
        List<StackMetadata> stackMetadataForInstruction,
        List<ExceptionHandler> exceptionHandlers,
        @Nullable Bytecode annotateFunction,
        Map<String, PyCell> closure,
        Map<String, PyObject> globals,
        long globalsId,
        Set<String> freeNames
) {
}
