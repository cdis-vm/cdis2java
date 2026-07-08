package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;

/**
 * Stores the value at the top of stack into a cell variable.
 * <p>
 * A cell variable is a variable shared with another function. They are typically implemented by
 * creating a holder object called a cell, then reading/modifying an attribute of the cell to
 * read/set the variable.
 * <p>
 * Stack Effect: -1
 * Prior: ..., value
 * After: ...
 *
 * <pre>{@code
 * >>> nonlocal x
 * ... x = 0
 * LoadConstant(constant=0)
 * StoreCell(name="x")
 * }</pre>
 *
 * @param cellName the name of the cell variable
 */
public record StoreCell(String cellName) implements Opcode, HasCell {
    @Override
    public String getVariableName() {
        return cellName;
    }

    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var slot = compilationRun.getVariableSlot(cellName);
        codeBuilder.aload(slot);
        codeBuilder.swap();
        codeBuilder.invokevirtual(CD.PY_CELL, "setValue", MD.of(void.class, PyObject.class));
    }
}
