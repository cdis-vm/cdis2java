package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.BinaryOperator;

/**
 * Performs a binary operation on the two items on the top of the stack.
 * <p>
 * Despite seemingly simple, this is one of the most complex opcodes. First, get the types of the
 * left and right operands. If the right operand is a more specific type than the left operand
 * (i.e. is a subclass of the left operand's type), try the reflected operation first
 * (ex: right.__radd__(left)), otherwise try the normal operation first (ex: left.__add__(right)).
 * If the method corresponding to the operation is not present, the method returns
 * {@code NotImplemented}, or the operand is a builtin type and raises {@code TypeError},
 * then try the other operation.
 * <p>
 * Stack Effect: -1
 * Prior: ..., left, right
 * After: ..., result
 * <p>
 * Comparisons are also BinaryOp, and can return any type (for instance, (a &lt; b) can return an int).
 *
 * <pre>{@code
 * >>> x + y
 * LoadLocal(name="x")
 * LoadLocal(name="y")
 * BinaryOp(operator=BinaryOperator.Add)
 * }</pre>
 *
 * @param operator the binary or comparison operator to apply
 */
public record BinaryOp(BinaryOperator operator) implements Opcode {
}
