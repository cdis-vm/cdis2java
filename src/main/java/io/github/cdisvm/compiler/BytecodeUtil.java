package io.github.cdisvm.compiler;

import java.lang.classfile.CodeBuilder;
import java.lang.classfile.Label;
import java.lang.classfile.instruction.SwitchCase;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.BiConsumer;

public class BytecodeUtil {
    public static void implementStringSwitchCase(CodeBuilder codeBuilder,
            Iterable<String> cases,
            boolean returning,
            BiConsumer<String, CodeBuilder> caseBuilder) {
        var switchCaseList = new ArrayList<SwitchCase>();
        var stringHashToMatchingStrings = new LinkedHashMap<Integer, List<String>>();
        var stringHashToLabel = new LinkedHashMap<Integer, Label>();
        for (var string : cases) {
            stringHashToMatchingStrings.computeIfAbsent(string.hashCode(), _ -> new ArrayList<>()).add(string);
        }
        for (var entry : stringHashToMatchingStrings.entrySet()) {
            var label = codeBuilder.newLabel();
            switchCaseList.add(SwitchCase.of(entry.getKey(), label));
            stringHashToLabel.put(entry.getKey(), label);
        }

        if (!switchCaseList.isEmpty()) {
            var defaultLabel = codeBuilder.newLabel();
            codeBuilder.dup();
            codeBuilder.invokevirtual(CD.OBJECT, "hashCode", MD.of(int.class));
            codeBuilder.lookupswitch(defaultLabel, switchCaseList);
            for (var entry : stringHashToMatchingStrings.entrySet()) {
                buildCase(codeBuilder, stringHashToLabel.get(entry.getKey()),
                        defaultLabel, entry.getValue(), returning, caseBuilder);
            }
            codeBuilder.labelBinding(defaultLabel);
            codeBuilder.pop();
        } else {
            codeBuilder.pop();
        }
    }

    private static void buildCase(CodeBuilder codeBuilder, Label label,
            Label defaultLabel, List<String> caseList,
            boolean returning,
            BiConsumer<String, CodeBuilder> caseBuilder) {
        codeBuilder.labelBinding(label);
        var nextCase = codeBuilder.newLabel();
        for (var string : caseList) {
            codeBuilder.dup();
            codeBuilder.loadConstant(string);
            codeBuilder.invokevirtual(CD.OBJECT, "equals", MD.of(boolean.class, Object.class));
            codeBuilder.ifeq(nextCase);
            caseBuilder.accept(string, codeBuilder);
            if (!returning) {
                codeBuilder.goto_(defaultLabel);
            }
            codeBuilder.labelBinding(nextCase);
        }
        codeBuilder.goto_(defaultLabel);
    }
}
