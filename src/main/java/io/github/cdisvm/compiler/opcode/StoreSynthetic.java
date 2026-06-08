package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Stores the value at the top of stack into a synthetic variable.
 * <p>
 * A synthetic variable is a variable introduced by the compiler and is not included in
 * {@code locals()}.
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> for item in collection:
 * ...     pass
 * LoadLocal(name="collection")
 * GetIterator()
 * StoreSynthetic(index=0)
 *
 * label loop_start
 *
 * LoadSynthetic(index=0)
 * GetNextElseJumpTo(target=loop_end)
 * StoreLocal(name="item")
 * JumpTo(target=loop_start)
 *
 * label loop_end
 * }</pre>
 *
 * @param syntheticIndex the index of the synthetic variable
 */
public record StoreSynthetic(int syntheticIndex) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var slot = compilationRun.getSyntheticSlot(syntheticIndex);
        codeBuilder.astore(slot);
    }
}
