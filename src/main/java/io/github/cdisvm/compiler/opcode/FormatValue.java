package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.FormatConversion;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAsciiable;
import io.github.cdisvm.runtime.PyFormattable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyNone;
import io.github.cdisvm.runtime.builtin.PyStr;

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
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        switch (conversion) {
            case NONE -> {
            }
            case TO_STRING -> {
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyString", MD.of(PyStr.class));
            }
            case TO_REPR -> {
                codeBuilder.invokeinterface(CD.PY_OBJECT, "pyRepr", MD.of(PyStr.class));
            }
            case TO_ASCII -> {
                codeBuilder.invokestatic(CD.of(PyAsciiable.class), "wrapping", MD.of(PyAsciiable.class, PyObject.class), true);
                codeBuilder.invokeinterface(CD.of(PyAsciiable.class), "pyAscii", MD.of(PyStr.class));
            }
        }
        codeBuilder.invokestatic(CD.of(PyFormattable.class), "wrapping", MD.of(PyFormattable.class, PyObject.class), true);
        if (formatSpec == null || formatSpec.isEmpty()) {
            PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
        } else {
            new PyStr(formatSpec).loadValueOntoStack(codeBuilder);
        }
        codeBuilder.invokeinterface(CD.of(PyFormattable.class), "pyFormat", MD.of(PyStr.class, PyObject.class));
    }
}
