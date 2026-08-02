package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.builtin.PyStr;

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
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.new_(CD.of(StringBuilder.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(StringBuilder.class), "<init>", MD.of(void.class));
        for (var i = 0; i < count; i++) {
            codeBuilder.swap();
            codeBuilder.invokevirtual(CD.of(PyStr.class), "value", MD.of(String.class));
            codeBuilder.loadConstant(0);
            codeBuilder.swap();
            codeBuilder.invokevirtual(CD.of(StringBuilder.class), "insert", MD.of(StringBuilder.class, int.class, String.class));
        }
        codeBuilder.invokevirtual(CD.of(StringBuilder.class), "toString", MD.of(String.class));
        codeBuilder.new_(CD.of(PyStr.class));
        codeBuilder.dup_x1();
        codeBuilder.swap();
        codeBuilder.invokespecial(CD.of(PyStr.class), "<init>", MD.of(void.class, String.class));
    }
}
