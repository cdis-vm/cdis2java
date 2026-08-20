package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.List;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyContainer;
import io.github.cdisvm.runtime.PyIterable;
import io.github.cdisvm.runtime.PyMapping;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySizable;
import io.github.cdisvm.runtime.builtin.PyBool;
import io.github.cdisvm.runtime.builtin.PyInt;
import io.github.cdisvm.runtime.builtin.PySequenceBase;

/**
 * Top of stack is the queried object.
 * <p>
 * Do not pop it off the stack, and check if it is a mapping with the given keys. If it is not
 * a mapping with the given keys, jump to target.
 * <p>
 * Stack Effect: 0
 * Prior: ..., query
 * After: ..., query
 *
 * <pre>{@code
 * >>> match query:
 * ...     case {'a': x, 'b': y}:
 * ...         pass
 * LoadLocal(name="query")
 * MatchMapping(keys=("a", "b"), target=no_match)
 * UnpackMapping(keys=("a", "b"), has_extras=False, target=no_match)
 * StoreLocal(name="x")
 * StoreLocal(name="y")
 * JumpTo(target=end_match)
 * label no_match
 * Pop()
 * label end_match
 * }</pre>
 *
 * @param keys the keys that must be present in the mapping
 * @param targetBytecodeIndex where to jump if the mapping does not contain the keys
 */
@NullMarked
public record MatchMapping(List<Object> keys,
                           int targetBytecodeIndex) implements Opcode, HasTarget {
    @Override
    public int getTargetBytecodeIndex() {
        return targetBytecodeIndex;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var notMatchLabel = compilationRun.bytecodeIndexToLabel().get(targetBytecodeIndex);
        codeBuilder.dup();
        codeBuilder.instanceOf(CD.of(PyMapping.class));
        codeBuilder.ifeq(notMatchLabel);
        for (var key : UnpackMapping.getPythonKeys(keys)) {
            codeBuilder.dup();
            if (key instanceof PyConstant pyConstant) {
                pyConstant.loadValueOntoStack(codeBuilder);
            } else {
                throw new IllegalArgumentException("key (%s) is not a constant".formatted(key));
            }
            codeBuilder.invokeinterface(CD.of(PyContainer.class), "pyHasItem", MD.of(PyBool.class, PyObject.class));
            codeBuilder.getstatic(CD.PY_BOOL, "TRUE", CD.PY_BOOL);
            codeBuilder.if_acmpne(notMatchLabel);
        }
    }
}
