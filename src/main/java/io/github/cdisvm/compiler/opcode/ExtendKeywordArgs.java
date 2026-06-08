package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.Map;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCallBuilder;

/**
 * Pops top of stack and unpacks it into the keyword argument dict.
 * <p>
 * Stack Effect: -1
 * Prior: ..., call_builder, mapping
 * After: ..., call_builder
 *
 * <pre>{@code
 * >>> func(**args)
 * LoadLocal(name="func")
 * CreateCallBuilder()
 * LoadLocal(name="args")
 * ExtendKeywordArgs()
 * CallWithBuilder()
 * }</pre>
 */
public record ExtendKeywordArgs() implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        codeBuilder.checkcast(CD.of(Map.class));
        codeBuilder.invokeinterface(CD.PY_CALL_BUILDER,
                "$mergeArguments",
                MD.of(PyCallBuilder.class, Map.class));
    }
}
