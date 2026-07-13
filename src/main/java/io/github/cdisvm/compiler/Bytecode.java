package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import io.github.cdisvm.compiler.opcode.JavaCode;
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
    public static Bytecode ofJavaCode(FunctionSignature signature, BiConsumer<CodeBuilder, CompilationRun> codeBuilderConsumer) {
        return new Bytecode("$builtin", signature,
                FunctionType.FUNCTION, MethodType.STATIC, 0,
                List.of(new Instruction(new JavaCode(codeBuilderConsumer), 0, -1)),
                List.of(StackMetadata.empty()),
                Collections.emptyList(),
                null,
                Collections.emptyMap(),
                Collections.emptyMap(),
                0L,
                Collections.emptySet());
    }
}
