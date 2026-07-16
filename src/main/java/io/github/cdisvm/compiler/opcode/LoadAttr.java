package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;

/**
 * Replaces top of stack with the result of an attribute lookup.
 * <p>
 * Attribute lookup calls {@code __getattribute__} on the type of the object on top of stack.
 * {@code __getattribute__} is relatively complex, handling descriptors, method resolution order
 * and class variables.
 * <p>
 * If {@code __getattribute__} raises {@code AttributeError}, it calls {@code __getattr__} if the
 * type has it defined, otherwise it raises the {@code AttributeError}.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., attribute
 *
 * <pre>{@code
 * >>> obj.attribute
 * LoadLocal(name="obj")
 * LoadAttr(name="attribute")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record LoadAttr(String attributeName) implements Opcode {
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
        codeBuilder.invokeinterface(attrDesc.interfaceDesc(), attrDesc.getter(), MD.of(PyObject.class));
        codeBuilder.goto_(doneLabel);

        codeBuilder.labelBinding(isAddedAttributeLabel);
        codeBuilder.loadConstant(attrDesc.attributeName());
        codeBuilder.invokeinterface(CD.of(PyAttributes.class), "getAttributeByName", MD.of(PyObject.class, String.class));

        codeBuilder.labelBinding(doneLabel);
    }
}
