package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Stores the value at the top of stack into a local variable.
 * <p>
 * The local variable is not a cell variable (a variable shared with another function) or a
 * synthetic variable (a variable introduced by the compiler).
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> x = 0
 * LoadConstant(constant=0)
 * StoreLocal(name="x")
 * }</pre>
 *
 * @param localName the name of the local variable
 */
public record StoreLocal(String localName) implements Opcode, HasVariable {
    @Override
    public String getVariableName() {
        return localName;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var slot = compilationRun.getVariableSlot(localName);
        codeBuilder.astore(slot);
    }
}
