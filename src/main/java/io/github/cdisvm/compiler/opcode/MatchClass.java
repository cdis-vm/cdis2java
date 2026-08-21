package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyByteArray;
import io.github.cdisvm.runtime.builtin.PyBytes;
import io.github.cdisvm.runtime.builtin.PyDict;
import io.github.cdisvm.runtime.builtin.PyFloat;
import io.github.cdisvm.runtime.builtin.PyInt;
import io.github.cdisvm.runtime.builtin.PyList;
import io.github.cdisvm.runtime.builtin.PySet;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.builtin.PyTuple;
import io.github.cdisvm.runtime.exception.PyTypeError;

/**
 * Top of stack is the checked type, and the item below it is the queried object.
 * <p>
 * Pop only the checked type off the stack. Jump to target if the object is not an instance of
 * the checked type, or does not have the specified attributes. If positionalCount is non-zero,
 * read {@code __match_args__} from the popped type, and raise {@code TypeError} if positionalCount
 * is greater than len(__match_args__), or if __match_args__ is missing from the type.
 * <p>
 * If the queried object is an instance of the type and has the specified attributes, push the
 * values of the specified attributes to the stack.
 * <p>
 * Stack Effect: len(attributes) + positionalCount - 1 if matched else -1
 * Prior: ..., query, type
 * After (matched): ..., query, positional_0, ..., positional_{positionalCount - 1}, attribute_0, ..., attribute_{len(attributes) - 1}
 * After (not matched): ..., query
 *
 * <pre>{@code
 * >>> match query:
 * ...     case MyType(positional_arg, my_attr=value):
 * ...         pass
 * LoadLocal(name="query")
 * MatchClass(target=no_match, positional_count=1, attributes=('my_attr',))
 * StoreSynthetic(index=0)  # my_attr
 * StoreSynthetic(index=1)  # positional_arg
 * LoadSynthetic(index=0)
 * StoreLocal(name='value')
 * LoadSynthetic(index=1)
 * StoreLocal(name='positional_arg')
 * JumpTo(target=end_match)
 * label no_match
 * Pop()
 * label end_match
 * }</pre>
 *
 * @param attributes the attribute names to extract if the type matches
 * @param positionalCount the number of positional match args to extract
 * @param targetBytecodeIndex where to jump if the type does not match
 */
@NullMarked
public record MatchClass(List<String> attributes,
                         int positionalCount,
                         int targetBytecodeIndex) implements Opcode, HasTarget {
    // Types that get special handling; see https://peps.python.org/pep-0634/#class-patterns
    private static final Class<?>[] LITERAL_TYPES = {
            PyBool.class, PyByteArray.class, PyBytes.class, PyDict.class, PyFloat.class,
            PyInt.class, PyList.class, PySet.class, PyStr.class, PyTuple.class
    };

    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var targetLabel = compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex);
        var querySlot = compilationRun.getWorkSlot(0);
        var typeSlot = compilationRun.getWorkSlot(1);
        var matchArgsSlot = compilationRun.getWorkSlot(2);
        var outputCount = positionalCount + attributes.size();
        var nameSlotStart = 3 + outputCount;

        // [..., query, type]  (type is on top of the stack)
        codeBuilder.astore(typeSlot);
        codeBuilder.astore(querySlot);
        codeBuilder.aload(typeSlot);
        codeBuilder.checkcast(CD.of(PyType.class));
        codeBuilder.aload(querySlot);
        codeBuilder.invokeinterface(CD.of(PyType.class), "instanceCheck", MD.of(boolean.class, PyObject.class));
        var matchLabel = codeBuilder.newLabel();
        codeBuilder.dup();
        codeBuilder.ifne(matchLabel);

        // Not an instance of the checked type; stack: ..., query
        codeBuilder.pop();
        codeBuilder.aload(querySlot);
        codeBuilder.goto_(targetLabel);

        // Match path; stack: [...]
        codeBuilder.labelBinding(matchLabel);
        codeBuilder.pop();
        if (positionalCount > 0) {
            var literalLabel = codeBuilder.newLabel();
            var literalDoneLabel = codeBuilder.newLabel();
            for (var literalType : LITERAL_TYPES) {
                codeBuilder.aload(typeSlot);
                codeBuilder.getstatic(CD.of(literalType), "type", CD.of(PyType.class));
                codeBuilder.if_acmpeq(literalLabel);
            }

            // Not a literal type, so read __match_args__ from the type
            codeBuilder.aload(typeSlot);
            codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
            codeBuilder.loadConstant("__match_args__");
            codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByNameOrNull",
                    MD.of(PyObject.class, String.class));
            var noMatchArgsLabel = codeBuilder.newLabel();
            codeBuilder.dup();
            codeBuilder.aconst_null();
            codeBuilder.if_acmpeq(noMatchArgsLabel);
            codeBuilder.astore(matchArgsSlot);
            var haveMatchArgsLabel = codeBuilder.newLabel();
            codeBuilder.goto_(haveMatchArgsLabel);

            codeBuilder.labelBinding(noMatchArgsLabel);
            codeBuilder.pop();
            raiseAcceptsPositionals(codeBuilder, typeSlot, 0);

            codeBuilder.labelBinding(haveMatchArgsLabel);
            codeBuilder.loadConstant(positionalCount);
            codeBuilder.aload(matchArgsSlot);
            codeBuilder.checkcast(CD.PY_TUPLE);
            codeBuilder.invokevirtual(CD.PY_TUPLE, "size", MD.of(int.class));
            var countOkLabel = codeBuilder.newLabel();
            codeBuilder.if_icmple(countOkLabel);
            raiseAcceptsMatchArgsSizePositionals(codeBuilder, typeSlot, matchArgsSlot);

            codeBuilder.labelBinding(countOkLabel);

            for (var i = 0; i < positionalCount; i++) {
                var nameSlot = compilationRun.getWorkSlot(nameSlotStart + i);
                codeBuilder.aload(matchArgsSlot);
                codeBuilder.checkcast(CD.PY_TUPLE);
                codeBuilder.loadConstant(i);
                codeBuilder.invokevirtual(CD.PY_TUPLE, "get", MD.of(PyObject.class, int.class));
                codeBuilder.checkcast(CD.of(PyStr.class));
                codeBuilder.astore(nameSlot);
                for (var j = 0; j < i; j++) {
                    var noDuplicateLabel = codeBuilder.newLabel();
                    codeBuilder.aload(nameSlot);
                    codeBuilder.aload(compilationRun.getWorkSlot(nameSlotStart + j));
                    codeBuilder.invokevirtual(CD.of(PyStr.class), "equals", MD.of(boolean.class, Object.class));
                    codeBuilder.ifeq(noDuplicateLabel);
                    raiseMultipleSubPatterns(codeBuilder, typeSlot, nameSlot, null);
                    codeBuilder.labelBinding(noDuplicateLabel);
                }
                loadAttributeByNameOrNull(codeBuilder, querySlot, targetLabel, nameSlot, null);
                codeBuilder.astore(compilationRun.getWorkSlot(3 + i));
            }

            codeBuilder.goto_(literalDoneLabel);

            codeBuilder.labelBinding(literalLabel);
            if (positionalCount == 1) {
                // Literal types match their own value as the single positional sub-pattern
                codeBuilder.aload(querySlot);
                codeBuilder.astore(compilationRun.getWorkSlot(3));
            } else {
                raiseAcceptsPositionals(codeBuilder, typeSlot, 1);
            }
            codeBuilder.labelBinding(literalDoneLabel);
        }

        for (var k = 0; k < attributes.size(); k++) {
            var name = attributes.get(k);
            for (var i = 0; i < positionalCount; i++) {
                var noDuplicateLabel = codeBuilder.newLabel();
                codeBuilder.aload(compilationRun.getWorkSlot(nameSlotStart + i));
                codeBuilder.invokevirtual(CD.of(PyStr.class), "value", MD.of(String.class));
                codeBuilder.loadConstant(name);
                codeBuilder.invokevirtual(CD.of(String.class), "equals", MD.of(boolean.class, Object.class));
                codeBuilder.ifeq(noDuplicateLabel);
                raiseMultipleSubPatterns(codeBuilder, typeSlot, -1, name);
                codeBuilder.labelBinding(noDuplicateLabel);
            }
            loadAttributeByNameOrNull(codeBuilder, querySlot, targetLabel, -1, name);
            codeBuilder.astore(compilationRun.getWorkSlot(3 + positionalCount + k));
        }

        // Matched; stack: ..., query, positional_0, ..., attribute_{outputCount - 1}
        codeBuilder.aload(querySlot);
        for (var i = 0; i < outputCount; i++) {
            codeBuilder.aload(compilationRun.getWorkSlot(3 + i));
        }
    }

    /**
     * Looks up an attribute by name on the queried object.
     * <p>
     * Prior: ...
     * After (attribute present): ..., query, value
     * After (attribute missing): jumps to targetLabel with ..., query
     *
     * @param nameSlot a slot holding the attribute name as a {@link PyStr}, or -1 if the name is
     *                 given as a compile time constant
     * @param nameConstant the attribute name, or null if the name is in nameSlot
     */
    private void loadAttributeByNameOrNull(CodeBuilder codeBuilder,
                                           int querySlot,
                                           Label targetLabel,
                                           int nameSlot,
                                           String nameConstant) {
        var notMatchLabel = codeBuilder.newLabel();
        codeBuilder.aload(querySlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.dup();
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(notMatchLabel);
        if (nameConstant == null) {
            codeBuilder.aload(nameSlot);
            codeBuilder.invokevirtual(CD.of(PyStr.class), "value", MD.of(String.class));
        } else {
            codeBuilder.loadConstant(nameConstant);
        }
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByNameOrNull",
                MD.of(PyObject.class, String.class));
        codeBuilder.dup();
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(notMatchLabel);
        var matchedLabel = codeBuilder.newLabel();
        codeBuilder.goto_(matchedLabel);
        codeBuilder.labelBinding(notMatchLabel);
        codeBuilder.pop();
        codeBuilder.aload(querySlot);
        codeBuilder.goto_(targetLabel);
        codeBuilder.labelBinding(matchedLabel);
    }

    /**
     * Raises {@code {type}() accepts {acceptedCount} positional sub-patterns
     * ({positionalCount} given)}.
     * <p>
     * Prior: ...
     * After: N/A (throws)
     */
    private void raiseAcceptsPositionals(CodeBuilder codeBuilder, int typeSlot, int acceptedCount) {
        newTypeError(codeBuilder);
        appendType(codeBuilder, typeSlot);
        appendConstant(codeBuilder, "() accepts " + acceptedCount + " positional sub-patterns (");
        codeBuilder.loadConstant(positionalCount);
        appendInt(codeBuilder);
        appendConstant(codeBuilder, " given)");
        throwTypeError(codeBuilder);
    }

    /**
     * Raises {@code {type}() accepts {len(matchArgs)} positional sub-patterns
     * ({positionalCount} given)}.
     * <p>
     * Prior: ..., type, matchArgs, size, positionalCount
     * After: N/A (throws)
     */
    private void raiseAcceptsMatchArgsSizePositionals(CodeBuilder codeBuilder, int typeSlot, int matchArgsSlot) {
        newTypeError(codeBuilder);
        appendType(codeBuilder, typeSlot);
        appendConstant(codeBuilder, "() accepts ");
        codeBuilder.aload(matchArgsSlot);
        codeBuilder.checkcast(CD.PY_TUPLE);
        codeBuilder.invokevirtual(CD.PY_TUPLE, "size", MD.of(int.class));
        appendInt(codeBuilder);
        appendConstant(codeBuilder, " positional sub-patterns (");
        codeBuilder.loadConstant(positionalCount);
        appendInt(codeBuilder);
        appendConstant(codeBuilder, " given)");
        throwTypeError(codeBuilder);
    }

    /**
     * Raises {@code {type}() got multiple sub-patterns for attribute '{attribute}'}.
     * <p>
     * Prior: ..., boolean
     * After: N/A (throws)
     *
     * @param nameSlot a slot holding the attribute name as a {@link PyStr}, or -1 if the name is
     *                 given as a compile time constant
     * @param nameConstant the attribute name, or null if the name is in nameSlot
     */
    private void raiseMultipleSubPatterns(CodeBuilder codeBuilder, int typeSlot,
                                          int nameSlot, String nameConstant) {
        newTypeError(codeBuilder);
        appendType(codeBuilder, typeSlot);
        appendConstant(codeBuilder, "() got multiple sub-patterns for attribute '");
        if (nameConstant == null) {
            codeBuilder.aload(nameSlot);
            codeBuilder.invokevirtual(CD.of(PyStr.class), "value", MD.of(String.class));
        } else {
            codeBuilder.loadConstant(nameConstant);
        }
        appendString(codeBuilder);
        appendConstant(codeBuilder, "'");
        throwTypeError(codeBuilder);
    }

    private void newTypeError(CodeBuilder codeBuilder) {
        codeBuilder.new_(CD.of(PyTypeError.class));
        codeBuilder.dup();
    }

    private void appendType(CodeBuilder codeBuilder, int typeSlot) {
        newStringBuilder(codeBuilder);
        codeBuilder.aload(typeSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyString", MD.of(PyStr.class));
        codeBuilder.invokevirtual(CD.of(PyStr.class), "value", MD.of(String.class));
        appendString(codeBuilder);
    }

    private void newStringBuilder(CodeBuilder codeBuilder) {
        codeBuilder.new_(CD.of(StringBuilder.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(StringBuilder.class), "<init>", MD.of(void.class));
    }

    private void appendConstant(CodeBuilder codeBuilder, String value) {
        codeBuilder.loadConstant(value);
        appendString(codeBuilder);
    }

    private void appendString(CodeBuilder codeBuilder) {
        codeBuilder.invokevirtual(CD.of(StringBuilder.class), "append",
                MD.of(StringBuilder.class, String.class));
    }

    private void appendInt(CodeBuilder codeBuilder) {
        codeBuilder.invokevirtual(CD.of(StringBuilder.class), "append",
                MD.of(StringBuilder.class, int.class));
    }

    private void throwTypeError(CodeBuilder codeBuilder) {
        codeBuilder.invokevirtual(CD.of(StringBuilder.class), "toString", MD.of(String.class));
        codeBuilder.invokespecial(CD.of(PyTypeError.class), "<init>", MD.of(void.class, String.class));
        codeBuilder.athrow();
    }
}
