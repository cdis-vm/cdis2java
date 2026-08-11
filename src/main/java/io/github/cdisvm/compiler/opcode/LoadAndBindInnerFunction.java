package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.TypeKind;
import java.util.List;

import io.github.cdisvm.compiler.Bytecode;
import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyCell;
import io.github.cdisvm.runtime.PyConstant;
import io.github.cdisvm.runtime.PyObject;

/**
 * Loads and binds an inner function.
 * <p>
 * The inner function's default values are expected to be on the stack in the order given by
 * {@code bytecode.signature().parameters()}.
 * <p>
 * Stack Effect: 1 - len(bytecode.signature().parameters())
 * Prior: ..., default1, default2, ..., defaultN
 * After: ..., bound_inner_function
 *
 * @param bytecode the inner function definition
 */
public record LoadAndBindInnerFunction(Bytecode bytecode,
                                       List<String> parametersWithDefaults) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        var functionInstance = compilationRun.compiler().compile(bytecode);
        ((PyConstant) functionInstance).loadValueOntoStack(codeBuilder);
        for (var freeName : bytecode.freeNames()) {
            codeBuilder.dup();
            codeBuilder.aload(compilationRun.getVariableSlot(freeName));
            codeBuilder.checkcast(CD.PY_CELL);
            codeBuilder.invokevirtual(CD.of(functionInstance.getClass()),
                    "$" + freeName, MD.of(void.class, PyCell.class));
        }
        if (!parametersWithDefaults.isEmpty()) {
            codeBuilder.astore(compilationRun.getWorkSlot(0));

            var nextWorkSlot = 1;
            for (var _ : parametersWithDefaults) {
                codeBuilder.astore(compilationRun.getWorkSlot(nextWorkSlot));
                nextWorkSlot++;
            }

            codeBuilder.aload(compilationRun.getWorkSlot(0));
            codeBuilder.loadConstant(parametersWithDefaults.size());
            codeBuilder.anewarray(CD.of(PyObject.class));

            var defaultIndex = 0;
            for (var workSlot = nextWorkSlot - 1; workSlot >= 1; workSlot--) {
                codeBuilder.dup();
                codeBuilder.loadConstant(defaultIndex);
                codeBuilder.aload(compilationRun.getWorkSlot(workSlot));
                codeBuilder.aastore();
            }
            codeBuilder.loadConstant(parametersWithDefaults.size());
            codeBuilder.newarray(TypeKind.INT);

            defaultIndex = 0;
            for (var parametersWithDefault : parametersWithDefaults) {
                codeBuilder.dup();
                codeBuilder.loadConstant(defaultIndex);
                for (var parameter : bytecode.signature().parameters()) {
                    if (parameter.parameterName().equals(parametersWithDefault)) {
                        codeBuilder.loadConstant(parameter.parameterIndex());
                        break;
                    }
                }
                codeBuilder.iastore();
                defaultIndex++;
            }
            codeBuilder.invokevirtual(CD.of(functionInstance.getClass()),
                    "set$Default", MD.of(void.class, PyObject[].class, int[].class));
            codeBuilder.aload(compilationRun.getWorkSlot(0));
        }
    }
}
