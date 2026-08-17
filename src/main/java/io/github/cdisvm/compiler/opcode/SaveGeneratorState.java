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
import io.github.cdisvm.runtime.builtin.PyStr;

/**
 * Saves the frame to the generator at TOS, then pops the generator.
 * <p>
 * Stack Effect: -1
 * Prior: ..., generator
 * After: ...
 *
 * <pre>{@code
 * >>> yield 10
 * LoadConstant(constant=10)
 * LoadSynthetic(index=0)
 * SaveGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * YieldValue()
 * LoadSynthetic(index=0)
 * DelegateOrRestoreGeneratorState(StackMetadata(stack=1, variables=(), closure=(), synthetic_variables=1))
 * Pop()
 * }</pre>
 *
 * @param stateId the state identifier
 * @param savedStackMetadata the state of the frame when this opcode is executed
 */
public record SaveGeneratorState(int stateId, StackMetadata savedStackMetadata) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // The generator on top of stack is not saved; everything below it is
        var savedStackSize = savedStackMetadata().stack().size();
        var generatorSlot = compilationRun.getWorkSlot(0);
        var stateListSlot = compilationRun.getWorkSlot(1);
        var firstStackItemSlot = compilationRun.getWorkSlot(2);

        codeBuilder.astore(generatorSlot);
        for (var i = savedStackSize - 1; i >= 0; i--) {
            codeBuilder.astore(firstStackItemSlot + i);
        }

        codeBuilder.new_(CD.PY_LIST);
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.PY_LIST, "<init>", MD.of(void.class));
        for (var i = 0; i < savedStackSize; i++) {
            codeBuilder.dup();
            codeBuilder.aload(firstStackItemSlot + i);
            codeBuilder.invokevirtual(CD.PY_LIST, "add", MD.of(boolean.class, PyObject.class));
            codeBuilder.pop();
        }

        var variablesDictSlot = compilationRun.getWorkSlot(3);
        codeBuilder.new_(CD.of(PyDict.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(PyDict.class), "<init>", MD.of(void.class));
        codeBuilder.astore(variablesDictSlot);

        codeBuilder.aload(variablesDictSlot);
        for (var variableName : savedStackMetadata().localVariables().keySet()) {
            var slot = compilationRun.variableNameToSlot().get(variableName);
            codeBuilder.dup();
            codeBuilder.new_(CD.of(PyStr.class));
            codeBuilder.dup();
            codeBuilder.loadConstant(variableName);
            codeBuilder.invokespecial(CD.of(PyStr.class), "<init>", MD.of(void.class, String.class));
            codeBuilder.aload(slot);
            codeBuilder.invokevirtual(CD.of(PyDict.class), "put", MD.of(Object.class, Object.class, Object.class));
            codeBuilder.pop();
        }
        codeBuilder.pop();

        // Add the variables dict to the state list
        codeBuilder.dup();
        codeBuilder.aload(variablesDictSlot);
        codeBuilder.invokevirtual(CD.PY_LIST, "add", MD.of(boolean.class, PyObject.class));
        codeBuilder.pop();

        // Add the synthetic values to the state list
        for (var i = 0; i < compilationRun.syntheticCount(); i++) {
            codeBuilder.dup();
            codeBuilder.aload(compilationRun.getSyntheticSlot(i));
            codeBuilder.invokevirtual(CD.PY_LIST, "add", MD.of(boolean.class, PyObject.class));
            codeBuilder.pop();
        }

        codeBuilder.astore(stateListSlot);

        // Re-push the saved stack items (yield value on top) so they can be returned
        for (var i = 0; i < savedStackSize; i++) {
            codeBuilder.aload(firstStackItemSlot + i);
        }

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_state_id");
        PyInt.of(stateId).loadValueOntoStack(codeBuilder);
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName", MD.of(void.class, String.class,
                PyObject.class));

        codeBuilder.aload(generatorSlot);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.loadConstant("_saved_state");
        codeBuilder.aload(stateListSlot);
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName", MD.of(void.class, String.class,
                PyObject.class));
    }
}
