package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;

/**
 * TOS is generator, and the item below it is the delegate.
 * <p>
 * Stack Effect: -2
 * Prior: ..., iterable, generator
 * After: ...
 *
 * <pre>{@code
 * >>> yield from [1, 2, 3]
 * NewList()
 * LoadConstant(constant=1)
 * ListAppend()
 * LoadConstant(constant=2)
 * ListAppend()
 * LoadConstant(constant=3)
 * ListAppend()
 * GetIter()
 * LoadSynthetic(index=0)
 * SetGeneratorDelegate()
 * LoadSynthetic(index=0)
 * SaveGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * Pop()
 * }</pre>
 */
public record SetGeneratorDelegate() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var generatorSlot = compilationRun.getWorkSlot(0);
        var delegateSlot = compilationRun.getWorkSlot(1);

        codeBuilder.astore(generatorSlot);
        codeBuilder.astore(delegateSlot);

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_sub_generator");
        codeBuilder.aload(delegateSlot);
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName",
                MD.of(void.class, String.class, PyObject.class));
    }
}
