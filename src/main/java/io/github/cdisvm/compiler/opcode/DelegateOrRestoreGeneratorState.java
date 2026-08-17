package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyDict;
import io.github.cdisvm.runtime.builtin.PyInt;
import io.github.cdisvm.runtime.builtin.PyNone;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.exception.PyBaseException;

/**
 * Pops generator from TOS, restores the frame from the generator, then replaces TOS with the
 * sent value stored on the generator (or raises an exception if throw was called on the generator).
 * <p>
 * Stack Effect: 0
 * Prior: ..., generator
 * After: ..., sent_value_or_yield_from_return
 *
 * <pre>{@code
 * >>> yield 10
 * LoadConstant(constant=10)
 * LoadSynthetic(index=0)
 * SaveGeneratorState(stack=1, variables=(), closure=(), synthetic_variables=1)
 * YieldValue()
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(stack=1, variables=(), closure=(), synthetic_variables=1)
 * Pop()
 * }</pre>
 *
 * @param stateId the state identifier
 * @param savedStackMetadata the saved stack metadata for frame restoration
 */
public record DelegateOrRestoreGeneratorState(int stateId,
                                              StackMetadata savedStackMetadata) implements Opcode {
    private static final int OPERATION_THROW = 2;

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var generatorSlot = compilationRun.getWorkSlot(0);
        var stateListSlot = compilationRun.getWorkSlot(1);
        var variablesDictSlot = compilationRun.getWorkSlot(2);
        var operationSlot = compilationRun.getWorkSlot(3);
        var savedStackSizeSlot = compilationRun.getWorkSlot(4);
        var stackIndexSlot = compilationRun.getWorkSlot(5);
        var notThrowLabel = codeBuilder.newLabel();

        codeBuilder.astore(generatorSlot);

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_saved_state");
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName",
                MD.of(PyObject.class, String.class));
        codeBuilder.checkcast(CD.PY_LIST);
        codeBuilder.astore(stateListSlot);

        // Saved state layout: [stack items (N), variables dict (1), synthetic values (K)]
        codeBuilder.aload(stateListSlot);
        codeBuilder.invokevirtual(CD.PY_LIST, "size", MD.of(int.class));
        codeBuilder.loadConstant(1 + compilationRun.syntheticCount());
        codeBuilder.isub();
        codeBuilder.istore(savedStackSizeSlot);

        // Restore the saved stack
        codeBuilder.aload(stateListSlot);
        codeBuilder.loadConstant(0);
        codeBuilder.istore(stackIndexSlot);
        var stackRestoreLoopStart = codeBuilder.newLabel();
        var stackRestoreLoopEnd = codeBuilder.newLabel();
        codeBuilder.labelBinding(stackRestoreLoopStart);
        codeBuilder.iload(stackIndexSlot);
        codeBuilder.iload(savedStackSizeSlot);
        codeBuilder.if_icmpge(stackRestoreLoopEnd);
        codeBuilder.dup();
        codeBuilder.iload(stackIndexSlot);
        codeBuilder.invokevirtual(CD.PY_LIST, "get", MD.of(PyObject.class, int.class));
        codeBuilder.iinc(stackIndexSlot, 1);
        codeBuilder.goto_(stackRestoreLoopStart);
        codeBuilder.labelBinding(stackRestoreLoopEnd);
        codeBuilder.pop();

        // Restore the synthetic variables
        codeBuilder.aload(stateListSlot);
        for (var i = 0; i < compilationRun.syntheticCount(); i++) {
            codeBuilder.dup();
            codeBuilder.iload(savedStackSizeSlot);
            codeBuilder.loadConstant(1 + i);
            codeBuilder.iadd();
            codeBuilder.invokevirtual(CD.PY_LIST, "get", MD.of(PyObject.class, int.class));
            codeBuilder.astore(compilationRun.getSyntheticSlot(i));
        }
        codeBuilder.pop();

        // Restore the local variables
        codeBuilder.aload(stateListSlot);
        codeBuilder.dup();
        codeBuilder.iload(savedStackSizeSlot);
        codeBuilder.invokevirtual(CD.PY_LIST, "get", MD.of(PyObject.class, int.class));
        codeBuilder.checkcast(CD.of(PyDict.class));
        codeBuilder.astore(variablesDictSlot);
        codeBuilder.pop();

        codeBuilder.aload(variablesDictSlot);
        for (var variableEntry : compilationRun.variableNameToSlot().entrySet()) {
            codeBuilder.dup();
            codeBuilder.new_(CD.of(PyStr.class));
            codeBuilder.dup();
            codeBuilder.loadConstant(variableEntry.getKey());
            codeBuilder.invokespecial(CD.of(PyStr.class), "<init>", MD.of(void.class, String.class));
            codeBuilder.invokevirtual(CD.of(PyDict.class), "get", MD.of(PyObject.class, Object.class));
            codeBuilder.astore(variableEntry.getValue());
        }
        codeBuilder.pop();

        // Discard the saved yield value
        codeBuilder.pop();

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_operation");
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName",
                MD.of(PyObject.class, String.class));
        codeBuilder.checkcast(CD.of(PyInt.class));
        codeBuilder.invokevirtual(CD.of(PyInt.class), "intValue", MD.of(int.class));
        codeBuilder.istore(operationSlot);

        // If throw was called, re-raise the stored exception
        codeBuilder.iload(operationSlot);
        codeBuilder.loadConstant(OPERATION_THROW);
        codeBuilder.if_icmpne(notThrowLabel);
        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_thrown_value");
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName",
                MD.of(PyObject.class, String.class));
        codeBuilder.checkcast(CD.of(PyBaseException.class));
        codeBuilder.athrow();

        codeBuilder.labelBinding(notThrowLabel);

        // Next or send: replace the yield value with the sent value
        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_sent_value");
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName",
                MD.of(PyObject.class, String.class));

        // Reset the one-shot fields
        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_sent_value");
        PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName",
                MD.of(void.class, String.class, PyObject.class));

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_thrown_value");
        PyNone.INSTANCE.loadValueOntoStack(codeBuilder);
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName",
                MD.of(void.class, String.class, PyObject.class));

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_operation");
        PyInt.of(0).loadValueOntoStack(codeBuilder);
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName",
                MD.of(void.class, String.class, PyObject.class));
    }
}
