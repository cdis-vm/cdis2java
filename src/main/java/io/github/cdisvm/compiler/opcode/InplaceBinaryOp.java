package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.InplaceBinaryOperator;

/**
 * Performs an inplace binary operation on the two items on the top of the stack.
 * <p>
 * The left operand is modified in place (if possible) and the result is pushed onto the stack.
 * <p>
 * Stack Effect: -1
 * Prior: ..., left, right
 * After: ..., result
 *
 * <pre>{@code
 * >>> x += y
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * InplaceBinaryOp(operator=InplaceBinaryOperator.IAdd)
 * }</pre>
 *
 * @param operator the inplace binary operator to apply
 */
public record InplaceBinaryOp(InplaceBinaryOperator operator) implements Opcode {
}
