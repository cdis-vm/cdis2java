package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.util.LinkedHashMap;
import java.util.Map;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.opcode.HasTarget;
import io.github.cdisvm.compiler.opcode.HasVariable;

@NullMarked
public record CompilationRun(ClassDesc callableClassDesc,
                             CodeBuilder codeBuilder,
                             Bytecode bytecode,
                             Map<Integer, Label> bytecodeIndexToLabel,
                             Map<String, Integer> variableNameToSlot,
                             int syntheticStart) {
    public static CompilationRun init(
            ClassDesc callableClassDesc,
            CodeBuilder codeBuilder,
            Bytecode bytecode,
            int reservedSlots) {
        var variableNameToSlot  = new LinkedHashMap<String, Integer>();
        var bytecodeIndexToLabel = new LinkedHashMap<Integer, Label>();
        var lastLine = -1;

        for (var parameter : bytecode.signature().parameters()) {
            variableNameToSlot.put(parameter.parameterName(), reservedSlots + variableNameToSlot.size());
        }

        for (var instruction : bytecode.instructions()) {
            if (instruction.sourceLineNumber() != lastLine) {
                lastLine = instruction.sourceLineNumber();
                bytecodeIndexToLabel.computeIfAbsent(instruction.bytecodeIndex(), _ -> codeBuilder.newLabel());
            }
            if (instruction.opcode() instanceof HasTarget hasTarget) {
                bytecodeIndexToLabel.computeIfAbsent(hasTarget.getTargetBytecodeIndex(), _ -> codeBuilder.newLabel());
            }
            if (instruction.opcode() instanceof HasVariable hasVariable) {
                variableNameToSlot.computeIfAbsent(hasVariable.getVariableName(), _ -> reservedSlots + variableNameToSlot.size());
            }
        }

        for (var exceptionHandler : bytecode.exceptionHandlers()) {
            bytecodeIndexToLabel.computeIfAbsent(exceptionHandler.fromBytecodeIndex(), _ -> codeBuilder.newLabel());
            bytecodeIndexToLabel.computeIfAbsent(exceptionHandler.toBytecodeIndex(), _ -> codeBuilder.newLabel());
            bytecodeIndexToLabel.computeIfAbsent(exceptionHandler.handlerBytecodeIndex(), _ -> codeBuilder.newLabel());
        }

        return new CompilationRun(callableClassDesc, codeBuilder, bytecode, bytecodeIndexToLabel, variableNameToSlot, reservedSlots + variableNameToSlot.size());
    }

    public int getVariableSlot(String variableName) {
        return variableNameToSlot.get(variableName);
    }

    public int getSyntheticSlot(int syntheticIndex) {
        return syntheticStart + syntheticIndex;
    }
}
