package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyDict;

/**
 * Pops the top two items off the stack and put them in the dict prior to them.
 * <p>
 * The dict remains on the stack. The top of stack is the value, and the item before it is the key.
 * <p>
 * Stack Effect: -2
 * Prior: ..., dict, key, value
 * After: ..., dict
 *
 * <pre>{@code
 * >>> {"key": "value"}
 * NewDict()
 * LoadConstant(constant="key")
 * LoadConstant(constant="value")
 * DictPut()
 * }</pre>
 */
public record DictPut() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokevirtual(CD.PY_DICT,
                "pyPutAndReturnThis", MD.of(PyDict.class, PyObject.class, PyObject.class));
    }
}
