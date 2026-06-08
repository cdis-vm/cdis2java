package io.github.cdisvm.compiler.opcode;

import io.github.cdisvm.compiler.FormatConversion;

/**
 * Formats the value on the top of stack, performing a conversion if necessary.
 * <p>
 * Raises {@code TypeError} if {@code __format__} does not return a {@code str}.
 * <p>
 * Stack Effect: 0
 * Prior: ..., value
 * After: ..., formatted_value
 *
 * <pre>{@code
 * >>> f'{x}'
 * LoadLocal(name="x")
 * FormatValue(conversion=FormatConversion.NONE, format_spec='')
 *
 * >>> f'{x!s}'
 * LoadLocal(name="x")
 * FormatValue(conversion=FormatConversion.TO_STRING, format_spec='')
 *
 * >>> f'{x:spec}'
 * LoadLocal(name="x")
 * FormatValue(conversion=FormatConversion.NONE, format_spec='spec')
 *
 * >>> f'{x!s:spec}'
 * LoadLocal(name="x")
 * FormatValue(conversion=FormatConversion.TO_STRING, format_spec='spec')
 * }</pre>
 *
 * @param conversion how the value should be converted before formatting
 * @param formatSpec the format specification to apply
 */
public record FormatValue(FormatConversion conversion, String formatSpec) implements Opcode {
}
