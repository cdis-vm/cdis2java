package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Deletes a global variable.
 * <p>
 * If a global variable has the same name as a builtin, it does not delete the builtin.
 * <p>
 * Raises a {@code NameError} if the global variable is not defined.
 * <p>
 * Stack Effect: 0
 * Prior: ...
 * After: ...
 *
 * <pre>{@code
 * >>> global x
 * ... del x
 * DeleteGlobal(name="x")
 * }</pre>
 *
 * @param globalName the name of the global variable
 */
public record DeleteGlobal(String globalName) implements Opcode, HasGlobal {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        compilationRun.globalMap().get(globalName).delete(codeBuilder);
    }
}
