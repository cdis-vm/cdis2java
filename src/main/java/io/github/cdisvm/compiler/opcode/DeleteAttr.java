package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;

/**
 * Deletes an attribute of the object on top of stack.
 * <p>
 * This calls {@code __delattr__} on the type of the object.
 * <p>
 * Stack Effect: -1
 * Prior: ..., object
 * After: ...
 *
 * <pre>{@code
 * >>> del obj.attribute
 * LoadLocal(name="obj")
 * DeleteAttr(name="attribute")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record DeleteAttr(String attributeName) implements Opcode {

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
        codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.delete(), MD.of(void.class));
        codeBuilder.goto_(doneLabel);

        codeBuilder.labelBinding(isAddedAttributeLabel);
        codeBuilder.loadConstant(attrDesc.attributeName());
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "deleteAttributeByName", MD.of(void.class, String.class));

        codeBuilder.labelBinding(doneLabel);
    }
}
