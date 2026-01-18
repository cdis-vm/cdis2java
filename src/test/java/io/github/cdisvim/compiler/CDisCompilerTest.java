package io.github.cdisvim.compiler;

import static org.assertj.core.api.Assertions.assertThat;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.github.cdisvm.compiler.Bytecode;
import io.github.cdisvm.compiler.CDisCompiler;
import io.github.cdisvm.compiler.FunctionParameter;
import io.github.cdisvm.compiler.FunctionSignature;
import io.github.cdisvm.compiler.FunctionType;
import io.github.cdisvm.compiler.Instruction;
import io.github.cdisvm.compiler.MethodType;
import io.github.cdisvm.compiler.ParameterKind;
import io.github.cdisvm.compiler.StackMetadata;
import io.github.cdisvm.compiler.opcode.CallWithBuilder;
import io.github.cdisvm.compiler.opcode.CreateCallBuilder;
import io.github.cdisvm.compiler.opcode.LoadConstant;
import io.github.cdisvm.compiler.opcode.LoadLocal;
import io.github.cdisvm.compiler.opcode.ReturnValue;
import io.github.cdisvm.compiler.opcode.WithKeywordArg;
import io.github.cdisvm.runtime.PyCallable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.builtin.PyStr;
import io.github.cdisvm.runtime.PyType;

class CDisCompilerTest {
    private static final Path DUMP_LOCATION = Path.of("target", "cdis-generated-classes");
    private static final boolean DUMP_CLASSES = false;
    private CDisCompiler compiler;

    @BeforeEach
    void setUp() {
        compiler = new CDisCompiler();
    }

    @AfterEach
    void tearDown() {
        if (DUMP_CLASSES) {
            compiler.dumpClasses(DUMP_LOCATION);
        }
    }

    @Test
    void simple() {
        var bytecode = new Bytecode(
                "test",
                new FunctionSignature(List.of(new FunctionParameter(0, "argument", ParameterKind.POSITIONAL_OR_KEYWORD, PyType.of(
                        PyStr.class), null)), PyType.of(PyStr.class)),
                FunctionType.FUNCTION,
                MethodType.VIRTUAL,
                0,
                List.of(
                        new Instruction(new LoadLocal("argument"), 0, 0),
                        new Instruction(new ReturnValue(), 1, 1)
                ),
                List.of(
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false)),
                List.of(),
                null,
                Map.of(),
                Map.of(),
                Set.of()
        );
        var callable = compiler.compile(bytecode);
        var expected = "test data";
        assertThat(callable.pyCallBuilder().$appendArgument(new PyStr(expected)).pyCall())
                .isInstanceOf(PyStr.class)
                .extracting(pyObject -> ((PyStr) pyObject).value())
                .isEqualTo(expected);
    }

    @Test
    void constants() {
        var bytecode = new Bytecode(
                "test",
                FunctionSignature.builder()
                        .returning(PyStr.class)
                        .build(),
                FunctionType.FUNCTION,
                MethodType.VIRTUAL,
                0,
                List.of(
                        new Instruction(new LoadConstant(new PyStr("return")), 0, 0),
                        new Instruction(new ReturnValue(), 1, 1)
                ),
                List.of(
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false)),
                List.of(),
                null,
                Map.of(),
                Map.of(),
                Set.of()
        );
        var callable = compiler.compile(bytecode);
        var expected = "return";
        assertThat(callable.pyCallBuilder().pyCall())
                .isInstanceOf(PyStr.class)
                .extracting(pyObject -> ((PyStr) pyObject).value())
                .isEqualTo(expected);
    }

    @Test
    void functionCall() {
        var callableBytecode = new Bytecode(
                "function",
                FunctionSignature.builder()
                        .param("a", PyStr.class)
                        .returning(PyStr.class)
                        .build(),
                FunctionType.FUNCTION,
                MethodType.VIRTUAL,
                0,
                List.of(
                        new Instruction(new LoadLocal("a"), 0, 0),
                        new Instruction(new ReturnValue(), 1, 1)
                ),
                List.of(
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false)),
                List.of(),
                null,
                Map.of(),
                Map.of(),
                Set.of()
        );
        var callable = compiler.compile(callableBytecode);

        var functionBytecode = new Bytecode(
                "test",
                FunctionSignature.builder()
                        .param("callable", PyCallable.class)
                        .returning(PyStr.class)
                        .build(),
                FunctionType.FUNCTION,
                MethodType.VIRTUAL,
                0,
                List.of(
                        new Instruction(new LoadLocal("callable"), 0, 0),
                        new Instruction(new CreateCallBuilder(), 1, 0),
                        new Instruction(new LoadConstant(new PyStr("expected")), 2, 0),
                        new Instruction(new WithKeywordArg("a"), 3, 0),
                        new Instruction(new CallWithBuilder(), 4, 0),
                        new Instruction(new ReturnValue(), 5, 1)
                ),
                List.of(
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false),
                        new StackMetadata(null, null, null, false)),
                List.of(),
                null,
                Map.of(),
                Map.of(),
                Set.of()
        );
        var function = compiler.compile(functionBytecode);

        var expected = "expected";
        assertThat(function.pyCallBuilder().$appendArgument(callable).pyCall())
                .isInstanceOf(PyStr.class)
                .extracting(pyObject -> ((PyStr) pyObject).value())
                .isEqualTo(expected);
    }
}
