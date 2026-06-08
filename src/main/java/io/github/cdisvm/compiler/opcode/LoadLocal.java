package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Loads a local variable onto the stack.
 * <p>
 * The local variable is not a cell variable (a variable shared with another function) or a
 * synthetic variable (a variable introduced by the compiler).
 * <p>
 * Raises {@code UnboundLocalError} if the local variable is not defined yet.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., local
 *
 * <pre>{@code
 * >>> x
 * LoadLocal(name="x")
 * }</pre>
 *
 * @param localName the name of the local variable
 */
public record LoadLocal(String localName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return localName;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var slot = compilationRun.getVariableSlot(localName);
        codeBuilder.aload(slot);
    }
}
