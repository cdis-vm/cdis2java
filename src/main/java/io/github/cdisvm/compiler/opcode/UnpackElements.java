package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;
import java.util.List;

import io.github.cdisvm.compiler.CD;
import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.MD;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.runtime.PyObject;

/**
 * Pops off the top of stack, and pushes its elements onto the stack in reversed order.
 * <p>
 * If {@code hasExtras} is false, {@code beforeCount} is the exact number of elements expected in
 * the iterable, and {@code afterCount} is 0.
 * <p>
 * If {@code hasExtras} is true, the first beforeCount elements of the iterable are added last to
 * the stack, then a list containing the items that are not first beforeCount elements of the
 * iterable or the last afterCount elements of the iterable is put between, and finally the last
 * afterCount elements of the iterable are put before the list.
 * <p>
 * Stack Effect: beforeCount + afterCount + (1 if hasExtras else 0) - 1
 * Prior: ..., iterable
 * After: ..., last_after_count, ..., last_after_1, extras_list, first_before_count, ..., first_1
 *
 * <pre>{@code
 * >>> a, b = 1, 2
 * NewList()
 * LoadConstant(constant=1)
 * ListAppend()
 * LoadConstant(constant=2)
 * ListAppend()
 * UnpackElements(before_count=2)
 * StoreLocal(name="a")  # 1
 * StoreLocal(name="b")  # 2
 *
 * >>> a, *b, c = 1, 2, 3, 4
 * NewList()
 * LoadConstant(constant=1)
 * ListAppend()
 * LoadConstant(constant=2)
 * ListAppend()
 * LoadConstant(constant=3)
 * ListAppend()
 * LoadConstant(constant=4)
 * ListAppend()
 * UnpackElements(before_count=1, has_extras=True, after_count=1)
 * StoreLocal(name="a")  # 1
 * StoreLocal(name="b")  # [2, 3]
 * StoreLocal(name="c")  # 4
 * }</pre>
 *
 * @param beforeCount the number of elements to unpack from the beginning
 * @param hasExtras whether there is a middle list for remaining elements
 * @param afterCount the number of elements to unpack from the end
 */
public record UnpackElements(int beforeCount,
                             boolean hasExtras,
                             int afterCount) implements Opcode {
    @Override
    public void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        if (!hasExtras) {
            implementWithoutExtras(codeBuilder, compilationRun, stackMetadata);
        } else {
            implementWithExtras(codeBuilder, compilationRun, stackMetadata);
        }
    }

    private void implementWithoutExtras(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // TODO: raise ValueError instead
        new GetIterator().implement(codeBuilder, compilationRun, stackMetadata);
        var tooFewElementsLabel = codeBuilder.newLabel();
        for (var i = 0; i < beforeCount; i++) {
            codeBuilder.dup();
            codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
            codeBuilder.dup();
            codeBuilder.aconst_null();
            codeBuilder.if_acmpeq(tooFewElementsLabel);
            codeBuilder.astore(compilationRun.getWorkSlot(i));
        }
        var exactElementsLabel = codeBuilder.newLabel();
        codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
        codeBuilder.aconst_null();
        codeBuilder.if_acmpeq(exactElementsLabel);

        // Too many elements
        codeBuilder.new_(CD.of(RuntimeException.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(RuntimeException.class), "<init>", MD.of(void.class));
        codeBuilder.athrow();

        // To few elements
        codeBuilder.labelBinding(tooFewElementsLabel);
        codeBuilder.pop();
        codeBuilder.new_(CD.of(RuntimeException.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(RuntimeException.class), "<init>", MD.of(void.class));
        codeBuilder.athrow();

        // Exact number of elements
        codeBuilder.labelBinding(exactElementsLabel);
        for (var i = beforeCount - 1; i >= 0; i--) {
            codeBuilder.aload(compilationRun.getWorkSlot(i));
            codeBuilder.aconst_null();
            codeBuilder.astore(compilationRun.getWorkSlot(i));
        }
    }

    private void implementWithExtras(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        // TODO: raise ValueError instead
        new GetIterator().implement(codeBuilder, compilationRun, stackMetadata);
        var tooFewElementsLabel = codeBuilder.newLabel();
        for (var i = 0; i < beforeCount; i++) {
            codeBuilder.dup();
            codeBuilder.invokeinterface(CD.PY_ITERATOR, "pyNext", MD.of(PyObject.class));
            codeBuilder.dup();
            codeBuilder.aconst_null();
            codeBuilder.if_acmpeq(tooFewElementsLabel);
            codeBuilder.astore(compilationRun.getWorkSlot(i));
        }
        new NewList().implement(codeBuilder, compilationRun, stackMetadata);
        codeBuilder.swap();
        new ListExtend().implement(codeBuilder, compilationRun, stackMetadata);

        var listSlot = beforeCount;
        var listSizeSlot = beforeCount + 1;
        codeBuilder.astore(listSlot);
        codeBuilder.aload(listSlot);
        codeBuilder.invokevirtual(CD.PY_LIST, "size", MD.of(int.class));
        codeBuilder.istore(listSizeSlot);

        var enoughElementsLabel = codeBuilder.newLabel();
        codeBuilder.iload(listSizeSlot);
        codeBuilder.loadConstant(afterCount);
        codeBuilder.if_icmpge(enoughElementsLabel);
        // Too few (not enough for after) elements
        codeBuilder.new_(CD.of(RuntimeException.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(RuntimeException.class), "<init>", MD.of(void.class));
        codeBuilder.athrow();

        // To few elements (not enough for before)
        codeBuilder.labelBinding(tooFewElementsLabel);
        codeBuilder.pop();
        codeBuilder.new_(CD.of(RuntimeException.class));
        codeBuilder.dup();
        codeBuilder.invokespecial(CD.of(RuntimeException.class), "<init>", MD.of(void.class));
        codeBuilder.athrow();

        codeBuilder.labelBinding(enoughElementsLabel);

        // Enough elements
        // First, the after elements in reverse order
        for (var i = afterCount - 1; i >= 0; i--) {
            codeBuilder.aload(listSlot);
            codeBuilder.invokeinterface(CD.of(List.class), "removeLast",
                    MD.of(Object.class));
            codeBuilder.checkcast(CD.PY_OBJECT);
        }

        // The list
        codeBuilder.aload(listSlot);
        codeBuilder.aconst_null();
        codeBuilder.astore(listSlot);

        // The before elements in reverse order
        for (var i = beforeCount - 1; i >= 0; i--) {
            codeBuilder.aload(compilationRun.getWorkSlot(i));
            codeBuilder.aconst_null();
            codeBuilder.astore(compilationRun.getWorkSlot(i));
        }
    }
}
