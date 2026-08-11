package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.Bytecode;
import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyConstant;

/**
 * Loads and binds an inner function.
 * <p>
 * The inner function's default values are expected to be on the stack in the order given by
 * {@code bytecode.signature().parameters()}.
 * <p>
 * Stack Effect: 1 - len(bytecode.signature().parameters())
 * Prior: ..., default1, default2, ..., defaultN
 * After: ..., bound_inner_function
 *
 * @param bytecode the inner function definition
 */
public record LoadAndBindInnerFunction(Bytecode bytecode) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var functionInstance = compilationRun.compiler().compile(bytecode);
        ((PyConstant) functionInstance).loadValueOntoStack(codeBuilder);
        for (var freeName : bytecode.freeNames()) {
            codeBuilder.dup();
            codeBuilder.aload(compilationRun.getVariableSlot(freeName));
            codeBuilder.checkcast(CD.PY_CELL);
            codeBuilder.invokevirtual(CD.of(functionInstance.getClass()),
                    "$" + freeName, MD.of(void.class, PyCell.class));
        }
        // TODO: bind functionInstance from stack
        var parametersWithDefaults = bytecode.signature().parameters()
                .stream()
                .filter(parameter -> parameter.defaultValue() != null)
                .toList();
    }
}
