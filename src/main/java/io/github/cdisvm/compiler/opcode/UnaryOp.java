package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.compiler.UnaryOperator;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyHasPos;
import io.github.cdisvm.runtime.PyInvertible;
import io.github.cdisvm.runtime.PyNegatable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyBool;

/**
 * Performs a unary operation on the operand on the top of the stack.
 * <p>
 * Stack Effect: 0
 * Prior: ..., operand
 * After: ..., result
 *
 * <pre>{@code
 * >>> -x
 * LoadLocal(name="x")
 * UnaryOp(operator=UnaryOperator.USub)
 * }</pre>
 *
 * @param operator the unary operator to apply
 */
public record UnaryOp(UnaryOperator operator) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        switch (operator) {
            case INVERT -> {
                codeBuilder.invokestatic(CD.of(PyInvertible.class), "wrapping", MD.of(PyInvertible.class, PyObject.class), true);
                codeBuilder.invokeinterface(CD.of(PyInvertible.class), "pyInvert", MD.of(PyObject.class));
            }
            case POSITIVE -> {
                codeBuilder.invokestatic(CD.of(PyHasPos.class), "wrapping", MD.of(PyHasPos.class, PyObject.class), true);
                codeBuilder.invokeinterface(CD.of(PyHasPos.class), "pyPositive", MD.of(PyObject.class));
            }
            case NEGATE -> {
                codeBuilder.invokestatic(CD.of(PyNegatable.class), "wrapping", MD.of(PyNegatable.class, PyObject.class), true);
                codeBuilder.invokeinterface(CD.of(PyNegatable.class), "pyNegate", MD.of(PyObject.class));
            }
        }
    }
}
