package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.UnaryOperator;

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
}
