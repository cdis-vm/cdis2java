package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyType;

/**
 * Replaces top of stack with the result of an attribute lookup from its type.
 * <p>
 * Stack Effect: 0
 * Prior: ..., object
 * After: ..., type_attribute
 *
 * <pre>{@code
 * >>> with ctx:
 * LoadLocal(name="ctx")
 * LoadObjectTypeAttr(name="__enter__")
 * }</pre>
 *
 * @param attributeName the name of the attribute
 */
public record LoadObjectTypeAttr(String attributeName) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokeinterface(CD.PY_OBJECT, "pyType", MD.of(PyType.class));
        new LoadAttr(attributeName).implement(codeBuilder, compilationRun, stackMetadata);
    }
}
