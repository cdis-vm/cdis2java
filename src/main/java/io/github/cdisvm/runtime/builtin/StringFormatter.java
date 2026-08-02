package io.github.cdisvm.runtime.builtin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.github.cdisvm.runtime.PyAsciiable;
import io.github.cdisvm.runtime.PyFormattable;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.exception.PyKeyError;
import io.github.cdisvm.runtime.exception.PyTypeError;
import io.github.cdisvm.runtime.exception.PyValueError;
import io.github.cdisvm.runtime.util.DefaultFormatSpec;

public class StringFormatter {
    final static String IDENTIFIER = "(?:(?:\\p{javaUnicodeIdentifierStart}|_)\\p{javaUnicodeIdentifierPart}*)";
    final static String ARG_NAME = "(?<argName>" + IDENTIFIER + "|\\d+)?";
    final static String ATTRIBUTE_NAME = IDENTIFIER;
    final static String ELEMENT_INDEX = "[^]]+";
    final static String ITEM_NAME = "(?:(?:\\." + ATTRIBUTE_NAME + ")|(?:\\[" + ELEMENT_INDEX + "\\]))";
    final static String FIELD_NAME = "(?<fieldName>" + ARG_NAME + "(" + ITEM_NAME + ")*)?";
    final static String CONVERSION = "(?:!(?<conversion>[rsa]))?";
    final static String FORMAT_SPEC = "(?::(?<formatSpec>[^{}]*))?";

    final static Pattern REPLACEMENT_FIELD_PATTERN = Pattern.compile("\\{" +
            FIELD_NAME +
            CONVERSION +
            FORMAT_SPEC +
            "}|(?<literal>\\{\\{|}})");

    final static Pattern INDEX_CHAIN_PART_PATTERN = Pattern.compile(ITEM_NAME);

    /**
     * Pattern that matches conversion specifiers for the "%" operator. See
     * <a href="https://docs.python.org/3/library/stdtypes.html#printf-style-string-formatting">
     * Python printf-style String Formatting documentation</a> for details.
     */
    private final static Pattern PRINTF_FORMAT_REGEX = Pattern.compile("%(?:(?<key>\\([^()]+\\))?" +
            "(?<flags>[#0\\-+ ]*)?" +
            "(?<minWidth>\\*|\\d+)?" +
            "(?<precision>\\.(?:\\*|\\d+))?" +
            "[hlL]?" + // ignored length modifier
            "(?<type>[diouxXeEfFgGcrsa%])|.*)");

    private enum PrintfConversionType {
        SIGNED_INTEGER_DECIMAL("d", "i", "u"),
        SIGNED_INTEGER_OCTAL("o"),
        SIGNED_HEXADECIMAL_LOWERCASE("x"),
        SIGNED_HEXADECIMAL_UPPERCASE("X"),
        FLOATING_POINT_EXPONENTIAL_LOWERCASE("e"),
        FLOATING_POINT_EXPONENTIAL_UPPERCASE("E"),
        FLOATING_POINT_DECIMAL("f", "F"),
        FLOATING_POINT_DECIMAL_OR_EXPONENTIAL_LOWERCASE("g"),
        FLOATING_POINT_DECIMAL_OR_EXPONENTIAL_UPPERCASE("G"),
        SINGLE_CHARACTER("c"),
        REPR_STRING("r"),
        STR_STRING("s"),
        ASCII_STRING("a"),
        LITERAL_PERCENT("%");

        final String[] matchedCharacters;

        PrintfConversionType(String... matchedCharacters) {
            this.matchedCharacters = matchedCharacters;
        }

        public static PrintfConversionType getConversionType(Matcher matcher) {
            String conversion = matcher.group("type");

            if (conversion == null) {
                throw new PyValueError("Invalid specifier at position %d in string ".formatted(matcher.start()));
            }

            for (PrintfConversionType conversionType : PrintfConversionType.values()) {
                for (String matchedCharacter : conversionType.matchedCharacters) {
                    if (matchedCharacter.equals(conversion)) {
                        return conversionType;
                    }
                }
            }
            throw new IllegalStateException("Conversion (%s) does not match any defined conversions".formatted(conversion));
        }
    }

    public enum PrintfStringType {
        STRING,
        BYTES
    }

    public static String printfInterpolate(CharSequence value, List<PyObject> tuple, PrintfStringType stringType) {
        var matcher = PRINTF_FORMAT_REGEX.matcher(value);

        var out = new StringBuilder();
        var start = 0;
        var currentElement = 0;

        while (matcher.find()) {
            out.append(value, start, matcher.start());
            start = matcher.end();

            var key = matcher.group("key");
            if (key != null) {
                throw new PyTypeError("format requires a mapping");
            }

            var flags = matcher.group("flags");
            var minWidth = matcher.group("minWidth");
            var precisionString = matcher.group("precision");

            var conversionType = PrintfConversionType.getConversionType(matcher);

            if (conversionType != PrintfConversionType.LITERAL_PERCENT) {
                if (tuple.size() <= currentElement) {
                    throw new PyTypeError("not enough arguments for format string");
                }

                var toConvert = tuple.get(currentElement);

                currentElement++;

                if ("*".equals(minWidth)) {
                    if (tuple.size() <= currentElement) {
                        throw new PyTypeError("not enough arguments for format string");
                    }
                    minWidth = tuple.get(currentElement).pyString().value();
                    currentElement++;
                }

                if ("*".equals(precisionString)) {
                    if (tuple.size() <= currentElement) {
                        throw new PyTypeError("not enough arguments for format string");
                    }
                    precisionString = tuple.get(currentElement).pyString().value();;
                    currentElement++;
                }

                Optional<Integer> maybePrecision, maybeWidth;
                if (precisionString != null) {
                    maybePrecision = Optional.of(Integer.parseInt(precisionString.substring(1)));
                } else {
                    maybePrecision = Optional.empty();
                }

                if (minWidth != null) {
                    maybeWidth = Optional.of(Integer.parseInt(minWidth));
                } else {
                    maybeWidth = Optional.empty();
                }
                out.append(performInterpolateConversion(flags, maybeWidth, maybePrecision, conversionType, toConvert,
                        stringType));
            } else {
                out.append("%");
            }
        }

        out.append(value.subSequence(start, value.length()));

        return out.toString();
    }

    public static String printfInterpolate(CharSequence value, PyDict<?,?> dict, PrintfStringType stringType) {
        Matcher matcher = PRINTF_FORMAT_REGEX.matcher(value);

        StringBuilder out = new StringBuilder();
        int start = 0;
        while (matcher.find()) {
            out.append(value, start, matcher.start());
            start = matcher.end();

            PrintfConversionType conversionType = PrintfConversionType.getConversionType(matcher);

            if (conversionType != PrintfConversionType.LITERAL_PERCENT) {
                var key = matcher.group("key");
                if (key == null) {
                    throw new PyValueError(
                            "When a dict is used for the interpolation operator, all conversions must have parenthesised keys");
                }
                key = key.substring(1, key.length() - 1);

                var flags = matcher.group("flags");
                var minWidth = matcher.group("minWidth");
                var precisionString = matcher.group("precision");

                if ("*".equals(minWidth)) {
                    throw new PyValueError(
                            "* cannot be used for minimum field width when a dict is used for the interpolation operator");
                }

                if ("*".equals(precisionString)) {
                    throw new PyValueError("* cannot be used for precision when a dict is used for the interpolation operator");
                }

                PyObject toConvert;
                if (stringType == PrintfStringType.STRING) {
                    toConvert = dict.pyGetItem(new PyStr(key));
                } else {
                    toConvert = dict.pyGetItem(new PyStr(key).asAsciiBytes());
                }

                Optional<Integer> maybePrecision, maybeWidth;
                if (precisionString != null) {
                    maybePrecision = Optional.of(Integer.parseInt(precisionString.substring(1)));
                } else {
                    maybePrecision = Optional.empty();
                }

                if (minWidth != null) {
                    maybeWidth = Optional.of(Integer.parseInt(minWidth));
                } else {
                    maybeWidth = Optional.empty();
                }

                out.append(performInterpolateConversion(flags, maybeWidth, maybePrecision, conversionType, toConvert,
                        stringType));
            } else {
                out.append("%");
            }
        }

        out.append(value.subSequence(start, value.length()));
        return out.toString();
    }

    private static BigDecimal getBigDecimalWithPrecision(BigDecimal number, Optional<Integer> precision) {
        var currentScale = number.scale();
        var currentPrecision = number.precision();
        var precisionDelta = precision.orElse(6) - currentPrecision;
        return number.setScale(currentScale + precisionDelta, RoundingMode.HALF_EVEN);
    }

    private static String getUppercaseEngineeringString(BigDecimal number, Optional<Integer> precision) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(out);
        printStream.printf("%%1.%dE".formatted(precision.orElse(6) - 1), number);
        return out.toString();
    }

    private static String performInterpolateConversion(String flags, Optional<Integer> maybeWidth,
            Optional<Integer> maybePrecision,
            PrintfConversionType conversionType,
            PyObject toConvert,
            PrintfStringType stringType) {
        var useAlternateForm = flags.contains("#");
        var isZeroPadded = flags.contains("0");
        var isLeftAdjusted = flags.contains("-");
        if (isLeftAdjusted) {
            isZeroPadded = false;
        }

        var putSpaceBeforePositiveNumber = flags.contains(" ");
        var putSignBeforeConversion = flags.contains("+");
        if (putSignBeforeConversion) {
            putSpaceBeforePositiveNumber = false;
        }

        String result;
        switch (conversionType) {
            case SIGNED_INTEGER_DECIMAL: {
                if (toConvert instanceof PyFloat) {
                    toConvert = ((PyFloat) toConvert).asInt();
                }
                if (!(toConvert instanceof PyInt)) {
                    throw new PyTypeError(
                            "%%d format: a real number is required, not %s".formatted(toConvert.pyType().pyString()));
                }
                result = ((PyInt) toConvert).bigIntegerValue().toString(10);
                break;
            }
            case SIGNED_INTEGER_OCTAL: {
                if (toConvert instanceof PyFloat) {
                    toConvert = ((PyFloat) toConvert).asInt();
                }
                if (!(toConvert instanceof PyInt)) {
                    throw new PyTypeError(
                            "%%o format: a real number is required, not %s".formatted(toConvert.pyType().pyString()));
                }
                result = ((PyInt) toConvert).bigIntegerValue().toString(8);
                if (useAlternateForm) {
                    result = (result.startsWith("-")) ? "-0o" + result.substring(1) : "0o" + result;
                }
                break;
            }
            case SIGNED_HEXADECIMAL_LOWERCASE: {
                if (toConvert instanceof PyFloat) {
                    toConvert = ((PyFloat) toConvert).asInt();
                }
                if (!(toConvert instanceof PyInt)) {
                    throw new PyTypeError(
                            "%%x format: a real number is required, not %s".formatted(toConvert.pyType().pyString()));
                }
                result = ((PyInt) toConvert).hexString();
                if (useAlternateForm) {
                    result = (result.startsWith("-")) ? "-0x" + result.substring(1) : "0x" + result;
                }
                break;
            }
            case SIGNED_HEXADECIMAL_UPPERCASE: {
                if (toConvert instanceof PyFloat) {
                    toConvert = ((PyFloat) toConvert).asInt();
                }
                if (!(toConvert instanceof PyInt)) {
                    throw new PyTypeError("%X format: a real number is required, not " + toConvert.pyType().pyString());
                }
                result = ((PyInt) toConvert).hexString().toUpperCase();
                if (useAlternateForm) {
                    result = (result.startsWith("-")) ? "-0X" + result.substring(1) : "0X" + result;
                }
                break;
            }
            case FLOATING_POINT_EXPONENTIAL_LOWERCASE: {
                if (toConvert instanceof PyInt) {
                    toConvert = ((PyInt) toConvert).asFloat();
                }
                if (!(toConvert instanceof PyFloat)) {
                    throw new PyTypeError("%e format: a real number is required, not " + toConvert.pyType().pyString());
                }
                var value = BigDecimal.valueOf(((PyFloat) toConvert).value());
                result = getUppercaseEngineeringString(value, maybePrecision.map(precision -> precision + 1)
                        .or(() -> Optional.of(7))).toLowerCase();
                if (useAlternateForm && !result.contains(".")) {
                    result = result + ".0";
                }
                break;
            }
            case FLOATING_POINT_EXPONENTIAL_UPPERCASE: {
                if (toConvert instanceof PyInt) {
                    toConvert = ((PyInt) toConvert).asFloat();
                }
                if (!(toConvert instanceof PyFloat)) {
                    throw new PyTypeError("%E format: a real number is required, not " + toConvert.pyType().pyString());
                }
                var value = BigDecimal.valueOf(((PyFloat) toConvert).value());
                result = getUppercaseEngineeringString(value, maybePrecision.map(precision -> precision + 1)
                        .or(() -> Optional.of(7)));
                if (useAlternateForm && !result.contains(".")) {
                    result = result + ".0";
                }
                break;
            }
            case FLOATING_POINT_DECIMAL: {
                if (toConvert instanceof PyInt) {
                    toConvert = ((PyInt) toConvert).asFloat();
                }
                if (!(toConvert instanceof PyFloat)) {
                    throw new PyTypeError("%f format: a real number is required, not " + toConvert.pyType().pyString());
                }
                BigDecimal value = BigDecimal.valueOf(((PyFloat) toConvert).value());
                BigDecimal valueWithPrecision = value.setScale(maybePrecision.orElse(6), RoundingMode.HALF_EVEN);
                result = valueWithPrecision.toPlainString();
                if (useAlternateForm && !result.contains(".")) {
                    result = result + ".0";
                }
                break;
            }
            case FLOATING_POINT_DECIMAL_OR_EXPONENTIAL_LOWERCASE: {
                if (toConvert instanceof PyInt) {
                    toConvert = ((PyInt) toConvert).asFloat();
                }
                if (!(toConvert instanceof PyFloat)) {
                    throw new PyTypeError("%g format: a real number is required, not " + toConvert.pyType().pyString());
                }
                BigDecimal value = BigDecimal.valueOf(((PyFloat) toConvert).value());
                BigDecimal valueWithPrecision;

                if (value.scale() > 4 || value.precision() >= maybePrecision.orElse(6)) {
                    valueWithPrecision = getBigDecimalWithPrecision(value, maybePrecision);
                    result = getUppercaseEngineeringString(valueWithPrecision, maybePrecision).toLowerCase();
                } else {
                    valueWithPrecision = value.setScale(maybePrecision.orElse(6), RoundingMode.HALF_EVEN);
                    result = valueWithPrecision.toPlainString();
                }

                if (result.length() >= 3 && result.charAt(result.length() - 3) == 'e') {
                    result = result.substring(0, result.length() - 1) + "0" + result.charAt(result.length() - 1);
                }
                break;
            }
            case FLOATING_POINT_DECIMAL_OR_EXPONENTIAL_UPPERCASE: {
                if (toConvert instanceof PyInt) {
                    toConvert = ((PyInt) toConvert).asFloat();
                }
                if (!(toConvert instanceof PyFloat)) {
                    throw new PyTypeError("%G format: a real number is required, not " + toConvert.pyType().pyString());
                }
                BigDecimal value = BigDecimal.valueOf(((PyFloat) toConvert).value());
                BigDecimal valueWithPrecision;

                if (value.scale() > 4 || value.precision() >= maybePrecision.orElse(6)) {
                    valueWithPrecision = getBigDecimalWithPrecision(value, maybePrecision);
                    result = getUppercaseEngineeringString(valueWithPrecision, maybePrecision);
                } else {
                    valueWithPrecision = value.setScale(maybePrecision.orElse(6), RoundingMode.HALF_EVEN);
                    result = valueWithPrecision.toPlainString();
                }
                break;
            }
            case SINGLE_CHARACTER: {
                if (stringType == PrintfStringType.STRING) {
                    if (toConvert instanceof PyStr) {
                        PyStr convertedCharacter = (PyStr) toConvert;
                        if (convertedCharacter.value().length() != 1) {
                            throw new PyValueError("c specifier can only take an integer or single character string");
                        }
                        result = convertedCharacter.value();
                    } else {
                        result = Character.toString(((PyInt) toConvert).intValue());
                    }
                } else {
                    if (toConvert instanceof PyBytes) {
                        var convertedCharacter = (PyBytes) toConvert;
                        if (convertedCharacter.value().length != 1) {
                            throw new PyValueError("c specifier can only take an integer or single character string");
                        }
                        result = convertedCharacter.asCharSequence().toString();
                    } else if (toConvert instanceof PyByteArray) {
                        var convertedCharacter = (PyByteArray) toConvert;
                        if (convertedCharacter.valueBuffer().limit() != 1) {
                            throw new PyValueError("c specifier can only take an integer or single character string");
                        }
                        result = convertedCharacter.asCharSequence().toString();
                    } else {
                        result = Character.toString(((PyInt) toConvert).intValue());
                    }
                }
                break;
            }
            case REPR_STRING: {
                result = toConvert.pyRepr().value();
                break;
            }
            case STR_STRING: {
                if (stringType == PrintfStringType.STRING) {
                    result = toConvert.pyString().value();
                } else {
                    if (toConvert instanceof PyBytes) {
                        result = ((PyBytes) toConvert).asCharSequence().toString();
                    } else if (toConvert instanceof PyByteArray) {
                        result = ((PyByteArray) toConvert).asCharSequence().toString();
                    } else {
                        result = toConvert.pyString().value();
                    }
                }
                break;
            }
            case ASCII_STRING: {
                result = PyAsciiable.wrapping(toConvert).pyAscii().value();
                break;
            }
            case LITERAL_PERCENT: {
                result = "%";
                break;
            }
            default:
                throw new IllegalStateException("Unhandled case: " + conversionType);
        }

        if (putSignBeforeConversion && !(result.startsWith("+") || result.startsWith("-"))) {
            result = "+" + result;
        }

        if (putSpaceBeforePositiveNumber && !(result.startsWith("-"))) {
            result = " " + result;
        }

        if (maybeWidth.isPresent() && maybeWidth.get() > result.length()) {
            var padding = maybeWidth.get() - result.length();
            if (isZeroPadded) {
                if (result.startsWith("+") || result.startsWith("-")) {
                    result = result.charAt(0) + "0".repeat(padding) + result.substring(1);
                } else {
                    result = "0".repeat(padding) + result;
                }
            } else if (isLeftAdjusted) {
                result = result + " ".repeat(padding);
            }
        }

        return result;
    }

    public static String format(String text, List<PyObject> positionalArguments,
            PyDict<? extends PyObject, PyObject> namedArguments) {
        var matcher = REPLACEMENT_FIELD_PATTERN.matcher(text);
        var out = new StringBuilder();
        var start = 0;
        var implicitField = 0;

        while (matcher.find()) {
            out.append(text, start, matcher.start());
            start = matcher.end();

            var literal = matcher.group("literal");
            if (literal != null) {
                switch (literal) {
                    case "{{":
                        out.append("{");
                        continue;
                    case "}}":
                        out.append("}");
                        continue;
                    default:
                        throw new IllegalStateException("Unhandled literal: " + literal);
                }
            }

            var argName = matcher.group("argName");

            PyObject toConvert;

            if (positionalArguments != null) {
                if (argName == null) {
                    if (implicitField >= positionalArguments.size()) {
                        throw new PyValueError(
                                "(%d) is larger than sequence length (%d)".formatted(implicitField, positionalArguments.size()));
                    }
                    toConvert = positionalArguments.get(implicitField);
                    implicitField++;
                } else {
                    try {
                        int argumentIndex = Integer.parseInt(argName);
                        if (argumentIndex >= positionalArguments.size()) {
                            throw new PyValueError("(%d) is larger than sequence length (%d)".formatted(implicitField,
                                    positionalArguments.size()));
                        }
                        toConvert = positionalArguments.get(argumentIndex);
                    } catch (NumberFormatException e) {
                        if (namedArguments == null) {
                            throw new PyValueError("(%s) cannot be used to index a sequence".formatted(argName));
                        } else {
                            toConvert = namedArguments.get(new PyStr(argName));
                        }
                    }
                }
            } else {
                toConvert = namedArguments.get(new PyStr(argName));
            }

            if (toConvert == null) {
                throw new PyKeyError(argName);
            }

            toConvert = getFinalObjectInChain(toConvert, matcher.group("fieldName"));

            var conversion = matcher.group("conversion");
            if (conversion != null) {
                switch (conversion) {
                    case "s":
                        toConvert = toConvert.pyString();
                        break;
                    case "r":
                        toConvert = toConvert.pyRepr();
                        break;
                    case "a":
                        toConvert = PyAsciiable.wrapping(toConvert).pyAscii();
                        break;
                }
            }

            var formatSpec = Objects.requireNonNullElse(matcher.group("formatSpec"), "");
            out.append(PyFormattable.wrapping(toConvert).pyFormat(new PyStr(formatSpec)));
        }
        out.append(text.substring(start));
        return out.toString();
    }

    private static PyObject getFinalObjectInChain(PyObject chainStart, String chain) {
        if (chain == null) {
            return chainStart;
        }

        var current = chainStart;
        Matcher matcher = INDEX_CHAIN_PART_PATTERN.matcher(chain);

        while (matcher.find()) {
            var result = matcher.group();
            var gettable = PyGettable.wrapping(current);
            if (result.startsWith(".")) {
                var attributeName = result.substring(1);
                current = gettable.pyGetItem(new PyStr(attributeName));
            } else {
                var index = result.substring(1, result.length() - 1);
                try {
                    var intIndex = Integer.parseInt(index);
                    current = gettable.pyGetItem(PyInt.of(intIndex));
                } catch (NumberFormatException e) {
                    current = gettable.pyGetItem(new PyStr(index));
                }
            }
        }
        return current;
    }

    public static void addGroupings(StringBuilder out, DefaultFormatSpec formatSpec, int groupSize) {
        if (formatSpec.groupingOption.isEmpty()) {
            return;
        }

        if (groupSize <= 0) {
            throw new PyValueError(
                    "Invalid format spec: grouping option now allowed for conversion type " + formatSpec.conversionType);
        }

        var decimalSeperator = out.indexOf(".");
        char seperator;
        switch (formatSpec.groupingOption.get()) {
            case COMMA:
                seperator = ',';
                break;
            case UNDERSCORE:
                seperator = '_';
                break;
            default:
                throw new IllegalStateException("Unhandled case: " + formatSpec.groupingOption.get());
        }

        int index;
        if (decimalSeperator != -1) {
            index = decimalSeperator - 1;
        } else {
            index = out.length() - 1;
        }

        var groupIndex = 0;
        while (index >= 0 && out.charAt(index) != '-') {
            groupIndex++;
            if (groupIndex == groupSize) {
                out.insert(index, seperator);
                groupIndex = 0;
            }
            index--;
        }
    }

    public static void align(StringBuilder out, DefaultFormatSpec formatSpec,
            DefaultFormatSpec.AlignmentOption defaultAlignment) {
        if (formatSpec.width.isPresent()) {
            switch (formatSpec.alignment.orElse(defaultAlignment)) {
                case LEFT_ALIGN:
                    leftAlign(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
                case RIGHT_ALIGN:
                    rightAlign(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
                case RESPECT_SIGN_RIGHT_ALIGN:
                    respectSignRightAlign(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
                case CENTER_ALIGN:
                    center(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
            }
        }
    }

    public static void alignWithPrefixRespectingSign(StringBuilder out, String prefix, DefaultFormatSpec formatSpec,
            DefaultFormatSpec.AlignmentOption defaultAlignment) {
        var insertPosition = (out.charAt(0) == '+' || out.charAt(0) == '-' || out.charAt(0) == ' ') ? 1 : 0;
        if (formatSpec.width.isPresent()) {
            switch (formatSpec.alignment.orElse(defaultAlignment)) {
                case LEFT_ALIGN:
                    out.insert(insertPosition, prefix);
                    leftAlign(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
                case RIGHT_ALIGN:
                    out.insert(insertPosition, prefix);
                    rightAlign(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
                case RESPECT_SIGN_RIGHT_ALIGN:
                    respectSignRightAlign(out, formatSpec.fillCharacter, formatSpec.width.get());
                    out.insert(insertPosition, prefix);
                    break;
                case CENTER_ALIGN:
                    out.insert(insertPosition, prefix);
                    center(out, formatSpec.fillCharacter, formatSpec.width.get());
                    break;
            }
        } else {
            out.insert(insertPosition, prefix);
        }
    }

    public static void leftAlign(StringBuilder builder, String fillCharAsString, int width) {
        if (width <= builder.length()) {
            return;
        }

        var rightPadding = width - builder.length();
        builder.append(fillCharAsString.repeat(rightPadding));
    }

    public static void rightAlign(StringBuilder builder, String fillCharAsString, int width) {
        if (width <= builder.length()) {
            return;
        }

        var leftPadding = width - builder.length();
        builder.insert(0, fillCharAsString.repeat(leftPadding));
    }

    public static void respectSignRightAlign(StringBuilder builder, String fillCharAsString, int width) {
        if (width <= builder.length()) {
            return;
        }

        var leftPadding = width - builder.length();
        if (builder.length() >= 1 && (builder.charAt(0) == '+' || builder.charAt(0) == '-' || builder.charAt(0) == ' ')) {
            builder.insert(1, fillCharAsString.repeat(leftPadding));
        } else {
            builder.insert(0, fillCharAsString.repeat(leftPadding));
        }
    }

    public static void center(StringBuilder builder, String fillCharAsString, int width) {
        if (width <= builder.length()) {
            return;
        }
        var extraWidth = width - builder.length();
        var rightPadding = extraWidth / 2;
        // left padding get extra character if extraWidth is odd
        var leftPadding = rightPadding + (extraWidth & 1); // x & 1 == x % 2

        builder.insert(0, fillCharAsString.repeat(leftPadding))
                .append(fillCharAsString.repeat(rightPadding));
    }
}
