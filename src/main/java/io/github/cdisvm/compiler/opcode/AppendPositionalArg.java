package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pops top of stack and appends it to the positional argument list.
 * <p>
 * Stack Effect: -1
 * Prior: ..., call_builder, arg
 * After: ..., call_builder
 *
 * <pre>{@code
 * >>> func(*args, 1)
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * LoadLocal(name="args")
 * ExtendPositionalArgs()
 * LoadConstant(constant=1)
 * AppendPositionalArg()
 * CallWithBuilder()
 * }</pre>
 */
public record AppendPositionalArg() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                "$appendArgument",
                MD.of(PyCallBuilder.class, PyObject.class));
    }
}
