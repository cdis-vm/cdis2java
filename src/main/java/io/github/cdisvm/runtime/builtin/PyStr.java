package io.github.cdisvm.runtime.builtin;

import java.lang.classfile.CodeBuilder;
import java.lang.constant.ClassDesc;
import java.lang.constant.MethodTypeDesc;
import java.util.stream.IntStream;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CDisCompiler;
import io.github.cdisvm.runtime.PyAsciiable;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyFormattable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.binary.PyAddable;
import io.github.cdisvm.runtime.exception.PyTypeError;
import io.github.cdisvm.runtime.exception.PyValueError;
import io.github.cdisvm.runtime.util.DefaultFormatSpec;

@PyBuiltin("str")
public record PyStr(String value) implements PyConstant, PyContainer, PyAddable, PyFormattable,
        PyAsciiable {
    public static PyType type;

    @PyConstructor
    public static PyStr create() {
        // TODO
        return new PyStr("");
    }

    @Override
    public void loadValueOntoStack(CodeBuilder codeBuilder) {
        codeBuilder.new_(ClassDesc.of(PyStr.class.getCanonicalName()));
        codeBuilder.dup();
        codeBuilder.loadConstant(value);
        codeBuilder.invokespecial(ClassDesc.of(PyStr.class.getCanonicalName()), "<init>",
                MethodTypeDesc.of(CD.VOID, ClassDesc.of(String.class.getCanonicalName())));
    }

    public PyBytes asAsciiBytes() {
        char[] charData = value.toCharArray();
        int length = 0;
        for (char charDatum : charData) {
            if (charDatum < 0xFF) {
                length++;
            } else {
                length += 2;
            }
        }
        byte[] out = new byte[length];

        int outIndex = 0;
        for (char charDatum : charData) {
            if (charDatum < 0xFF) {
                out[outIndex] = (byte) charDatum;
                outIndex++;
            } else {
                out[outIndex] = (byte) ((charDatum & 0xFF00) >> 8);
                outIndex++;
                out[outIndex] = (byte) (charDatum & 0x00FF);
                outIndex++;
            }
        }
        return new PyBytes(out);
    }

    @Override
    public String getJavaIdentifierName() {
        return "PyStr_" + CDisCompiler.arbitraryTextToJavaIdentifierName(value);
    }

    @Override
    public PyAttributes pyAttributes() {
        throw new UnsupportedOperationException();
    }

    @Override
    public PyType pyType() {
        return type;
    }

    @Override
    public PyBool pyTruth() {
        return PyBool.of(value.isEmpty());
    }

    @Override
    public PyBool pyHasItem(PyObject item) {
        if (!(item instanceof PyStr(String query))) {
            throw new PyTypeError();
        }
        return PyBool.of(value.contains(query));
    }

    private static boolean isCharacterPrintable(int character) {
        if (character == ' ') {
            return true;
        }
        switch (Character.getType(character)) {
            // Others
            case Character.PRIVATE_USE:
            case Character.FORMAT:
            case Character.CONTROL:
            case Character.UNASSIGNED:

                // Separators
            case Character.SPACE_SEPARATOR:
            case Character.LINE_SEPARATOR:
            case Character.PARAGRAPH_SEPARATOR:
                return false;

            default:
                return true;
        }
    }

    @Override
    public String toString() {
        return value;
    }

    @Override
    public PyStr pyRepr() {
        var seperator = (value.contains("'"))? '"' : '\'';
        return new PyStr(seperator + value.codePoints()
                .flatMap(character -> {
                    if (character == '\\') {
                        return IntStream.of('\\', '\\');
                    }
                    if (character == seperator) {
                        return IntStream.of('\\', seperator);
                    }
                    if (isCharacterPrintable(character)) {
                        return IntStream.of(character);
                    } else {
                        switch (character) {
                            case '\r':
                                return IntStream.of('\\', 'r');
                            case '\n':
                                return IntStream.of('\\', 'n');
                            case '\t':
                                return IntStream.of('\\', 't');
                            default: {
                                if (character < 0xFFFF) {
                                    return String.format("u%04x", character).codePoints();
                                } else {
                                    return String.format("U%08x", character).codePoints();
                                }

                            }
                        }
                    }
                })
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append)
                .toString() + seperator);
    }

    @Override
    public PyObject pyAdd(PyObject other) {
        if (other instanceof PyStr) {
            return new PyStr(value.concat(((PyStr) other).value));
        }
        return PyNotImplemented.INSTANCE;
    }

    @Override
    public PyStr pyFormat(PyObject formatSpec) {
        PyStr spec;
        if (formatSpec == PyNone.INSTANCE) {
            return formatSelf();
        } else if (formatSpec instanceof PyStr) {
            spec = (PyStr) formatSpec;
        } else {
            throw new PyTypeError("__format__ argument 0 has incorrect type (expecting str or None)");
        }
        return formatSelf(spec);
    }

    public PyStr formatSelf() {
        return this;
    }

    public PyStr formatSelf(PyStr spec) {
        if (spec.value.isEmpty()) {
            return this;
        }

        DefaultFormatSpec formatSpec = DefaultFormatSpec.fromStringSpec(spec);

        var out = new StringBuilder();

        if (formatSpec.conversionType.orElse(
                DefaultFormatSpec.ConversionType.STRING) == DefaultFormatSpec.ConversionType.STRING) {
            out.append(value);
        } else {
            throw new PyValueError("Invalid conversion type for str: " + formatSpec.conversionType);
        }

        StringFormatter.align(out, formatSpec, DefaultFormatSpec.AlignmentOption.LEFT_ALIGN);
        return new PyStr(out.toString());
    }

    @Override
    public PyStr pyAscii() {
        return new PyStr(value.codePoints()
                .flatMap(character -> {
                    if (character < 128) {
                        return IntStream.of(character);
                    }

                    var hex = new StringBuilder(Integer.toHexString(character));
                    if (character < 0xFF) {
                        StringFormatter.rightAlign(hex, "0", 2);
                        return IntStream.of('\\', 'x', hex.charAt(0), hex.charAt(1));
                    }
                    if (character < 0xFFFF) {
                        StringFormatter.rightAlign(hex, "0", 4);
                        return IntStream.of('\\', 'u', hex.charAt(0), hex.charAt(1), hex.charAt(2), hex.charAt(3));
                    }
                    StringFormatter.rightAlign(hex, "0", 8);
                    return IntStream.of('\\', 'U',
                            hex.charAt(0), hex.charAt(1), hex.charAt(2), hex.charAt(3),
                            hex.charAt(4), hex.charAt(5), hex.charAt(6), hex.charAt(7));
                })
                .collect(StringBuilder::new,
                        StringBuilder::appendCodePoint, StringBuilder::append)
                .toString());
    }
}
