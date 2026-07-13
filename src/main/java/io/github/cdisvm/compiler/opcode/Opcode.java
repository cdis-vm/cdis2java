package io.github.cdisvm.compiler.opcode;

import java.lang.classfile.CodeBuilder;

import io.github.cdisvm.compiler.CompilationRun;
import io.github.cdisvm.compiler.StackMetadata;

/**
 * Represents a bytecode operation.
 * <p>
 * Each Opcode documents the state of the stack prior to and after the opcode:
 * <pre>
 * | Opcode
 * | Stack Effect: +1
 * | Prior: ..., a, b
 * | After: ..., c, d, e
 * </pre>
 * <p>
 * When the same identifier is used in both prior and after, it represents the same, identical
 * value. For instance, the {@link Dup} opcode stack effect:
 * <pre>
 * | Dup
 * | Stack Effect: +1
 * | Prior: ..., value
 * | After: ..., value, value
 * </pre>
 * <p>
 * {@code value} is repeated in after, meaning it is a duplicate of the value from prior.
 */
public sealed interface Opcode
        permits AppendPositionalArg, AsBool, BinaryOp, BuildSlice, CallWithBuilder, CreateCallBuilder,
        DelegateOrRestoreGeneratorState, DeleteAttr, DeleteCell, DeleteGlobal, DeleteItem, DeleteLocal, DictPut, DictUpdate,
        Dup, DupX1, ExtendKeywordArgs, ExtendPositionalArgs, FormatValue, GetAsyncIterator, GetAsyncNext, GetAwaitableIterator,
        GetItem, GetIterator, GetNextElseJumpTo, GetType, IfFalse, IfTrue, ImportModule, InplaceBinaryOp, IsContainedIn,
        IsSameAs, JavaCode, JoinStringValues, JumpIfNotMatchExceptType, JumpTo, ListAppend, ListExtend, ListToTuple,
        LoadAndBindInnerClass, LoadAndBindInnerFunction, LoadAndBindInnerGenerator, LoadAttr, LoadCell, LoadConstant,
        LoadGlobal, LoadLocal, LoadObjectTypeAttr, LoadSynthetic, LoadTypeAttrOrGlobal, MatchClass, MatchMapping, MatchSequence,
        NewDict, NewList, NewSet, Nop, Pop, Raise, RaiseWithCause, ReraiseLast, ReturnValue, SaveGeneratorState, SetAdd,
        SetGeneratorDelegate, SetItem, SetUpdate, StoreAttr, StoreCell, StoreGlobal, StoreLocal, StoreSynthetic, StoreTypeAttr,
        Swap, UnaryOp, UnpackElements, UnpackMapping, WithKeywordArg, WithPositionalArg, YieldValue {
    default void implement(CodeBuilder codeBuilder, CompilationRun compilationRun, StackMetadata stackMetadata) {
        throw new UnsupportedOperationException(getClass().getSimpleName());
    }
}
