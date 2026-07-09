package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;

/**
 * Loads a cell variable onto the stack.
 * <p>
 * A cell variable is a variable shared with another function. They are typically implemented by
 * creating a holder object called a cell, then reading/modifying an attribute of the cell to
 * read/set the variable.
 * <p>
 * Raises {@code NameError} if the cell variable is a free variable and undefined, and
 * {@code UnboundLocalError} if the cell variable is not defined and not a free variable.
 * <p>
 * Stack Effect: +1
 * Prior: ...
 * After: ..., cell_value
 *
 * <pre>{@code
 * >>> nonlocal x
 * ... x
 * LoadCell(name="x")
 * }</pre>
 *
 * @param cellName the name of the cell variable
 */
public record LoadCell(String cellName) implements Opcode, HasCell {
    @Override
    public String getVariableName() {
        return cellName;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var slot = compilationRun.getVariableSlot(cellName);
        codeBuilder.aload(slot);
        codeBuilder.invokevirtual(CD.PY_CELL, "getValue", MD.of(PyObject.class));
        // TODO: check if variable exists
    }
}
