package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;

/**
 * Top of stack is an exception type and the item below it is an exception.
 * <p>
 * If the exception is not an instance of the exception type, jump to target. If the exception
 * type is not a subclass of BaseException, raise {@code TypeError}.
 * <p>
 * Stack Effect: -2
 * Prior: ..., exception, exception_type
 * After: ...
 *
 * <pre>{@code
 * >>> try:
 * ...     pass
 * ... except ValueError:
 * ...     pass
 * StoreSynthetic(index=0)
 * LoadSynthetic(index=0)
 * LoadGlobal(name="ValueError")
 * JumpIfNotMatchExceptType(target=reraise)
 * Nop()
 * JumpTo(target=continue)
 *
 * label reraise
 * ReraiseLast()
 *
 * label continue
 * }</pre>
 *
 * @param targetBytecodeIndex where to jump if the exception type does not match
 */
public record JumpIfNotMatchExceptType(int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PyType.class), "instanceCheck", MD.of(boolean.class, PyObject.class));
        codeBuilder.loadConstant(1);
        codeBuilder.ifeq(compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex));
    }
}
