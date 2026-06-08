package io.github.cdisvm.compiler.opcode;

/**
 * Joins the top count items on the stack into a single string.
 * <p>
 * The items on the stack are guaranteed to be instances of {@code str}.
 * <p>
 * Stack Effect: -count + 1
 * Prior: ..., str_1, str_2, ..., str_count
 * After: ..., combined_str
 *
 * <pre>{@code
 * >>> f'{greetings} {noun}!'
 * LoadLocal(name="greetings")
 * FormatValue(conversion=FormatConversion.NONE, format_spec='')
 * LoadConstant(constant=' ')
 * LoadLocal(name="noun")
 * FormatValue(conversion=FormatConversion.NONE, format_spec='')
 * LoadConstant(constant='!')
 * JoinStringValues(count=3)
 * }</pre>
 *
 * @param count the number of string items to join
 */
public record JoinStringValues(int count) implements Opcode {
}
