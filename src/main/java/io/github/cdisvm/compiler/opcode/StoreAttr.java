package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;

/**
 * Sets an attribute of an object.
 * <p>
 * Pop two items from the stack. The first (top of stack) is the object, and the second is the
 * value. This calls {@code __setattr__} on the type of the object with value.
 * <p>
 * Stack Effect: -2
 * Prior: ..., value, object
 * After: ...
 *
 * <pre>{@code
 * >>> obj.attribute = 10
 * LoadConstant(constant=10)
 * LoadLocal(name="obj")
 * StoreAttr(name="attribute")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record StoreAttr(String attributeName) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var attrDesc = compilationRun.getAttributeDesc(attributeName);
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyAttributes", MD.of(PyAttributes.class));
        codeBuilder.dup();
        codeBuilder.instanceOf(attrDesc.interfaceDesc());

        var isAddedAttributeLabel = codeBuilder.newLabel();
        var doneLabel = codeBuilder.newLabel();
        codeBuilder.ifeq(isAddedAttributeLabel);

        codeBuilder.checkcast(attrDesc.interfaceDesc());
        codeBuilder.swap();
        codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.setter(), MD.of(void.class, PyObject.class));
        codeBuilder.goto_(doneLabel);

        codeBuilder.labelBinding(isAddedAttributeLabel);
        codeBuilder.swap();
        codeBuilder.loadConstant(attrDesc.attributeName());
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "setAttributeByName", MD.of(void.class, String.class, PyObject.class));

        codeBuilder.labelBinding(doneLabel);
    }
}
