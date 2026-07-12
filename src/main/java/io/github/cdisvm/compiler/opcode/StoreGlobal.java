package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.exception.PyNameError;

/**
 * Stores the value at the top of the stack into a global variable.
 * <p>
 * If a global variable has the same name as a builtin, it does not overwrite the builtin.
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> global x
 * ... x = 10
 * LoadConstant(constant=10)
 * StoreGlobal(name="x")
 * }</pre>
 *
 * @param globalName the name of the global variable
 */
public record StoreGlobal(String globalName) implements Opcode, HasGlobal {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        compilationRun.globalMap().get(globalName).write(codeBuilder);
    }
}
