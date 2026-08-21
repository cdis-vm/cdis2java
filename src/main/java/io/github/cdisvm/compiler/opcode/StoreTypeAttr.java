package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyMapping;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySettable;
import io.github.cdisvm.runtime.builtin.PyStr;

/**
 * TOS is a mapping, and the item below it is a value.
 * <p>
 * Stores the value into the mapping.
 * <p>
 * Stack Effect: -2
 * Prior: ..., value, mapping
 * After: ...
 *
 * <pre>{@code
 * >>> class A:
 * ...     x = 0
 * LoadConstant(constant=0)
 * LoadSynthetic(index=0)
 * StoreTypeAttr(name="x")
 * }</pre>
 *
 * @param name the name of the type attribute
 */
public record StoreTypeAttr(String name) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.checkcast(CD.of(PySettable.class));
        codeBuilder.swap();
        new PyStr(name).loadValueOntoStack(codeBuilder);
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PySettable.class), "pySetItem", MD.of(void.class, PyObject.class, PyObject.class));
    }
}
