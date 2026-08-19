package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pops top of stack and sets the corresponding keyword argument.
 * <p>
 * Stack Effect: -1
 * Prior: ..., call_builder, arg
 * After: ..., call_builder
 *
 * <pre>{@code
 * >>> func(arg=1)
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * LoadConstant(constant=1)
 * WithKeywordArg(name="arg")
 * CallWithBuilder()
 * }</pre>
 *
 * @param argumentName the name of the keyword argument
 */
public record WithKeywordArg(String argumentName) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var interfaceCD = compilationRun.compiler().getFunctionParameterByNameClassDesc(argumentName);
        codeBuilder.swap();
        codeBuilder.dup();
        codeBuilder.instanceOf(interfaceCD);
        var doesNotHaveKeywordArgInterface = codeBuilder.newLabel();
        var done = codeBuilder.newLabel();
        codeBuilder.ifeq(doesNotHaveKeywordArgInterface);

        // Has interface, use it
        codeBuilder.checkcast(interfaceCD);
        codeBuilder.swap();
        codeBuilder.invokeinterface(interfaceCD,
                argumentName,
                MD.of(PyCallBuilder.class, PyObject.class));
        codeBuilder.goto_(done);

        codeBuilder.labelBinding(doesNotHaveKeywordArgInterface);
        // Does not have interface, need to use $putArgument
        codeBuilder.swap();
        codeBuilder.loadConstant(argumentName);
        codeBuilder.swap();
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                "$putArgument",
                MD.of(PyCallBuilder.class, String.class, PyObject.class));

        codeBuilder.labelBinding(done);
    }
}
