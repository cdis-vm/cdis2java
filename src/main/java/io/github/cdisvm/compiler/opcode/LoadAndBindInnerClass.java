package io.github.cdisvm.compiler.opcode;

import java.util.stream.Stream;

import io.github.cdisvm.compiler.Bytecode;

/**
 * Top of stack is keyword arguments, and the item below it are the tuple of base types.
 * <p>
 * Loads and binds an inner class.
 * <p>
 * Stack Effect: -1
 * Prior: ..., bases, keyword_args
 * After: ..., bound_inner_class
 *
 * @param className the name of the inner class
 * @param classBodyBytecode the bytecode for the class body
 */
public record LoadAndBindInnerClass(String className,
                                    Bytecode classBodyBytecode) implements Opcode, HasCellGroup {
    @Override
    public Stream<String> getCells() {
        return classBodyBytecode.instructions()
                .stream()
                .flatMap(instruction -> {
                    if (instruction.opcode() instanceof HasCell cell) {
                        return Stream.of(cell.getVariableName());
                    } else if (instruction.opcode() instanceof HasCellGroup cellGroup) {
                        return cellGroup.getCells();
                    } else {
                        return Stream.empty();
                    }
                });
    }
}
