package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.exception.PyNameError;

/**
 * Loads a global variable or builtin onto the stack.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., global
 *
 * <pre>{@code
 * >>> global x
 * ... x
 * LoadGlobal(name="x")
 *
 * >>> int
 * LoadGlobal(name="int")
 * }</pre>
 *
 * @param globalName the name of the global variable or builtin
 */
public record LoadGlobal(String globalName) implements Opcode, HasGlobal {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        compilationRun.globalMap().get(globalName).read(codeBuilder);
        codeBuilder.dup();
        codeBuilder.aconst_null();
        var globalExistsLabel = codeBuilder.newLabel();
        codeBuilder.if_acmpne(globalExistsLabel);
        codeBuilder.pop();
        if (compilationRun.builtins().contains(globalName)) {
            codeBuilder.getstatic(CD.PY_BUILTINS, globalName, CD.PY_OBJECT);
        } else {
            // TODO: include global name
            codeBuilder.new_(CD.of(PyNameError.class));
            codeBuilder.dup();
            codeBuilder.invokespecial(CD.of(PyNameError.class), "<init>", MD.of(void.class));
            codeBuilder.athrow();
        }
        codeBuilder.labelBinding(globalExistsLabel);
    }
}
