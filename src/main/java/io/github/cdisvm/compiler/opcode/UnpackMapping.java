package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.SequencedMap;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyGettable;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyIterator;
import io.github.cdisvm.runtime.PyMapping;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyDict;
import io.github.cdisvm.runtime.builtin.PyFloat;
import io.github.cdisvm.runtime.builtin.PyInt;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.builtin.PyTuple;

/**
 * Pops off the top of stack (which is a mapping), and pushes the values of the given keys onto
 * the stack in reversed order.
 * <p>
 * If {@code hasExtras} is true, push all items in the mapping not specified by the given keys
 * into a new dict at the top of the stack.
 * <p>
 * Stack Effect: len(keys) + (1 if hasExtras else 0) - 1
 * Prior: ..., mapping
 * After: ..., value_(len(keys) - 1), ..., value_1, value_0, (extras_dict if hasExtras)
 *
 * <pre>{@code
 * >>> match mapping:
 * ...     case {'a': x, 'b': y}:
 * LoadLocal(name="mapping")
 * MatchMapping(keys=("a", "b"))
 * UnpackMapping(keys=("a", "b"), has_extras=False)
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * }</pre>
 *
 * @param keys the keys to extract from the mapping
 * @param hasExtras whether to include remaining keys in an extras dict
 */
@NullMarked
public record UnpackMapping(List<Object> keys,
                            boolean hasExtras) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        if (hasExtras) {
            implementWithoutExtras(codeBuilder, compilationRun);
            implementWithExtras(codeBuilder, compilationRun);
        } else {
            implementWithoutExtras(codeBuilder, compilationRun);
        }
    }

    static List<PyObject> getPythonKeys(List<Object> keys) {
        return keys.stream()
                .map(key -> {
                    if (key instanceof PyObject pyObject) {
                        return pyObject;
                    }
                    if (key instanceof String str) {
                        return new PyStr(str);
                    }
                    if (key instanceof Integer integer) {
                        return PyInt.of(integer);
                    }
                    if (key instanceof Double real) {
                        return PyFloat.of(real);
                    }
                    throw new UnsupportedOperationException("Unsupported key type: " + key.getClass());
                })
                .toList();
    }

    private void implementWithExtras(CodeBuilder codeBuilder, CompilationRun compilationRun) {
        var MAPPING_SLOT = compilationRun.getWorkSlot(0);
        var ITERATOR_SLOT = compilationRun.getWorkSlot(1);
        var EXTRAS_DICT_SLOT = compilationRun.getWorkSlot(2);
        var KEYS_SLOT = compilationRun.getWorkSlot(3);
        var CURRENT_KEY_SLOT = compilationRun.getWorkSlot(4);

        codeBuilder.aload(MAPPING_SLOT);
        codeBuilder.invokeinterface(CD.of(PyIterable.class), "pyIterator", MD.of(PyIterator.class));
        codeBuilder.astore(ITERATOR_SLOT);
        codeBuilder.new_(CD.PY_DICT);
        codeBuilder.dup();
        codeBuilder.new_(CD.of(LinkedHashMap.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(LinkedHashMap.class), "<init>", MD.of(void.class));
        codeBuilder.invokespecial(CD.of(PyDict.class), "<init>", MD.of(void.class, SequencedMap.class));
        codeBuilder.astore(EXTRAS_DICT_SLOT);
        new PyTuple<>(getPythonKeys(keys)).loadValueOntoStack(codeBuilder);
        codeBuilder.astore(KEYS_SLOT);

        var done = codeBuilder.newLabel();
        var loopStart = codeBuilder.newBoundLabel();
        var skipItem = codeBuilder.newLabel();
        codeBuilder.aload(compilationRun.getWorkSlot(0));
        codeBuilder.invokeinterface(CD.of(PyIterator.class), "pyNext", MD.of(PyObject.class));
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(done);

        codeBuilder.dup();
        codeBuilder.aload(KEYS_SLOT);
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.of(PyTuple.class), "contains", MD.of(boolean.class, Object.class));
        codeBuilder.ifne(skipItem);

        codeBuilder.astore(CURRENT_KEY_SLOT);
        codeBuilder.aload(MAPPING_SLOT);
        codeBuilder.aload(CURRENT_KEY_SLOT);
        codeBuilder.invokeinterface(CD.of(PyGettable.class), "pyGetItem", MD.of(PyObject.class, PyObject.class));
        codeBuilder.aload(EXTRAS_DICT_SLOT);
        codeBuilder.swap();
        codeBuilder.aload(CURRENT_KEY_SLOT);
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.of(PyDict.class), "pySetItem", MD.of(void.class, PyObject.class, PyObject.class));
        codeBuilder.goto_(loopStart);

        codeBuilder.labelBinding(skipItem);
        codeBuilder.pop();
        codeBuilder.goto_(loopStart);
        codeBuilder.labelBinding(done);
    }

    private void implementWithoutExtras(CodeBuilder codeBuilder, CompilationRun compilationRun) {
        codeBuilder.checkcast(CD.of(PyMapping.class));
        codeBuilder.astore(compilationRun.getWorkSlot(0));
        for (var key : getPythonKeys(keys).reversed()) {
            codeBuilder.aload(compilationRun.getWorkSlot(0));
            if (key instanceof PyConstant pyConstant) {
                pyConstant.loadValueOntoStack(codeBuilder);
            } else {
                throw new IllegalArgumentException("key (%s) is not a constant".formatted(key));
            }
            codeBuilder.invokeinterface(CD.of(PyGettable.class), "pyGetItem", MD.of(PyObject.class, PyObject.class));
        }
    }
}
