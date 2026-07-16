package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.constant.ClassDesc;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jspecify.annotations.NullMarked;

import io.github.cdisvm.compiler.opcode.HasCell;
import io.github.cdisvm.compiler.opcode.HasTarget;
import io.github.cdisvm.compiler.opcode.HasVariable;
import io.github.cdisvm.runtime.PyGlobal;

@NullMarked
public record CompilationRun(CDisCompiler compiler,
                             ClassDesc callableClassDesc,
                             CodeBuilder codeBuilder,
                             Bytecode bytecode,
                             Map<Integer, Label> bytecodeIndexToLabel,
                             Map<String, Integer> variableNameToSlot,
                             Set<String> cellVariableNameSet,
                             Map<String, PyGlobal> globalMap,
                             Set<String> builtins,
                             int syntheticStart,
                             int syntheticCount) {
    public static CompilationRun init(
            CDisCompiler compiler,
            ClassDesc callableClassDesc,
            CodeBuilder codeBuilder,
            Bytecode bytecode,
            Map<String, PyGlobal> globalMap,
            Set<String> builtins,
            int reservedSlots) {
        var variableNameToSlot  = new LinkedHashMap<String, Integer>();
        var bytecodeIndexToLabel = new LinkedHashMap<Integer, Label>();
        var cellVariableNameSet = new LinkedHashSet<String>();
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
            if (instruction.opcode() instanceof HasCell hasCell) {
                cellVariableNameSet.add(hasCell.getVariableName());
            }
        }

        for (var exceptionHandler : bytecode.exceptionHandlers()) {
            bytecodeIndexToLabel.computeIfAbsent(exceptionHandler.fromBytecodeIndex(), _ -> codeBuilder.newLabel());
            bytecodeIndexToLabel.computeIfAbsent(exceptionHandler.toBytecodeIndex(), _ -> codeBuilder.newLabel());
            bytecodeIndexToLabel.computeIfAbsent(exceptionHandler.handlerBytecodeIndex(), _ -> codeBuilder.newLabel());
        }

        return new CompilationRun(compiler, callableClassDesc, codeBuilder, bytecode, bytecodeIndexToLabel, variableNameToSlot,
                cellVariableNameSet, globalMap, builtins,
                reservedSlots + variableNameToSlot.size(),
                bytecode.syntheticCount());
    }

    public int[] getCellSlots() {
        var out = new int[cellVariableNameSet.size()];
        var index = 0;
        for (var cellVariableName : cellVariableNameSet) {
            out[index] = variableNameToSlot.get(cellVariableName);
            index++;
        }
        return out;
    }

    public AttributeDesc getAttributeDesc(String attributeName) {
        return compiler.getAttributeDesc(attributeName);
    }

    public boolean isCell(String variableName) {
        return cellVariableNameSet.contains(variableName);
    }

    public int getVariableSlot(String variableName) {
        return variableNameToSlot.get(variableName);
    }

    public int getSyntheticSlot(int syntheticIndex) {
        return syntheticStart + syntheticIndex;
    }

    public int getLastRaisedExceptionSlot() {
        return syntheticStart + syntheticCount;
    }

    public int getWorkSlot(int workSlot) {
        return getLastRaisedExceptionSlot() + 1 + workSlot;
    }
}
