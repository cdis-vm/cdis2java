import jpype
import jpype.imports
import inspect

from cdis.compiler._api import to_bytecode
import cdis.opcode as opcode

compiler = None

def start_jvm(jar_path):
    if not jpype.isJVMStarted():
        jpype.startJVM(classpath=[jar_path])


def _jclass(name):
    return jpype.JClass(name)

def _jpackage(name):
    return jpype.JPackage(name)

def _convert_py_constant(value):
    global compiler
    from types import CellType
    if isinstance(value, bool):
        PyBool = _jclass("io.github.cdisvm.runtime.builtin.PyBool")
        return PyBool.of(value)
    if isinstance(value, int):
        PyInt = _jclass("io.github.cdisvm.runtime.builtin.PyInt")
        return PyInt.of(value)
    if isinstance(value, str):
        PyStr = _jclass("io.github.cdisvm.runtime.builtin.PyStr")
        return PyStr(value)
    if value is None:
        PyNone = _jclass("io.github.cdisvm.runtime.builtin.PyNone")
        return PyNone.INSTANCE
    if isinstance(value, list):
        PyList = _jclass("io.github.cdisvm.runtime.builtin.PyList")
        out = PyList()
        for item in value:
            out.add(_convert_py_constant(item))
        return out
    if isinstance(value, tuple):
        AList = _jclass("java.util.ArrayList")
        PyTuple = _jclass("io.github.cdisvm.runtime.builtin.PyTuple")
        out = AList()
        for item in value:
            out.add(_convert_py_constant(item))
        return PyTuple(out)
    if isinstance(value, dict):
        PyDict = _jclass("io.github.cdisvm.runtime.builtin.PyDict")
        out = PyDict()
        for key, item in value.items():
            out.put(_convert_py_constant(key), _convert_py_constant(item))
        return out
    if isinstance(value, set):
        PySet = _jclass("io.github.cdisvm.runtime.builtin.PySet")
        out = PySet()
        for item in value:
            out.add(_convert_py_constant(item))
        return out
    if isinstance(value, BaseException):
        java_type = _jclass(f"io.github.cdisvm.runtime.exception.Py{type(value).__name__}")
        return java_type(*(_convert_py_constant(arg) for arg in value.args))
    if isinstance(value, CellType):
        return _convert_py_constant(value.cell_contents)
    if isinstance(value, type):
        return compiler.lookupType(value.__name__)

    raise ValueError(f"Unsupported constant type: {type(value)}")


def _convert_binary_operator(op):
    JBinaryOperator = _jclass("io.github.cdisvm.compiler.BinaryOperator")
    return JBinaryOperator.fromId(op.value.ast.__class__.__name__)


def _convert_unary_operator(op):
    JUnaryOperator = _jclass("io.github.cdisvm.compiler.UnaryOperator")
    return JUnaryOperator.fromId(op.name)


def _convert_inplace_binary_operator(op):
    JInplaceBinaryOperator = _jclass("io.github.cdisvm.compiler.InplaceBinaryOperator")
    return JInplaceBinaryOperator.fromId(op.name)


def _convert_format_conversion(conv):
    JFormatConversion = _jclass("io.github.cdisvm.compiler.FormatConversion")
    return getattr(JFormatConversion, conv.name)


def _convert_opcode(op):
    O = _jpackage("io.github.cdisvm.compiler.opcode")

    if isinstance(op, opcode.LoadConstant):
        return O.LoadConstant(_convert_py_constant(op.constant))
    if isinstance(op, opcode.Nop):
        return O.Nop()
    if isinstance(op, opcode.ImportModule):
        return O.ImportModule(op.name, op.level, _py_tuple_to_java(op.from_list))
    if isinstance(op, opcode.LoadGlobal):
        return O.LoadGlobal(op.name)
    if isinstance(op, opcode.LoadLocal):
        return O.LoadLocal(op.name)
    if isinstance(op, opcode.LoadCell):
        return O.LoadCell(op.name)
    if isinstance(op, opcode.LoadSynthetic):
        return O.LoadSynthetic(op.index)
    if isinstance(op, opcode.StoreGlobal):
        return O.StoreGlobal(op.name)
    if isinstance(op, opcode.StoreLocal):
        return O.StoreLocal(op.name)
    if isinstance(op, opcode.StoreCell):
        return O.StoreCell(op.name)
    if isinstance(op, opcode.StoreSynthetic):
        return O.StoreSynthetic(op.index)
    if isinstance(op, opcode.DeleteGlobal):
        return O.DeleteGlobal(op.name)
    if isinstance(op, opcode.DeleteLocal):
        return O.DeleteLocal(op.name)
    if isinstance(op, opcode.DeleteCell):
        return O.DeleteCell(op.name)
    if isinstance(op, opcode.ReturnValue):
        return O.ReturnValue()
    if isinstance(op, opcode.IfTrue):
        return O.IfTrue(op.target.index)
    if isinstance(op, opcode.IfFalse):
        return O.IfFalse(op.target.index)
    if isinstance(op, opcode.JumpTo):
        return O.JumpTo(op.target.index)
    if isinstance(op, opcode.JumpIfNotMatchExceptType):
        return O.JumpIfNotMatchExceptType(op.target.index)
    if isinstance(op, opcode.MatchClass):
        return O.MatchClass(
            _py_tuple_to_java(op.attributes),
            op.positional_count,
            op.target.index,
        )
    if isinstance(op, opcode.MatchSequence):
        return O.MatchSequence(op.length, op.is_exact, op.target.index)
    if isinstance(op, opcode.MatchMapping):
        return O.MatchMapping(_py_tuple_to_java(op.keys), op.target.index)
    if isinstance(op, opcode.ReraiseLast):
        return O.ReraiseLast()
    if isinstance(op, opcode.Raise):
        return O.Raise()
    if isinstance(op, opcode.RaiseWithCause):
        return O.RaiseWithCause()
    if isinstance(op, opcode.Dup):
        return O.Dup()
    if isinstance(op, opcode.DupX1):
        return O.DupX1()
    if isinstance(op, opcode.Pop):
        return O.Pop()
    if isinstance(op, opcode.Swap):
        return O.Swap()
    if isinstance(op, opcode.UnaryOp):
        return O.UnaryOp(_convert_unary_operator(op.operator))
    if isinstance(op, opcode.BinaryOp):
        return O.BinaryOp(_convert_binary_operator(op.operator))
    if isinstance(op, opcode.InplaceBinaryOp):
        return O.InplaceBinaryOp(_convert_inplace_binary_operator(op.operator))
    if isinstance(op, opcode.IsSameAs):
        return O.IsSameAs(op.negate)
    if isinstance(op, opcode.IsContainedIn):
        return O.IsContainedIn(op.negate)
    if isinstance(op, opcode.FormatValue):
        return O.FormatValue(
            _convert_format_conversion(op.conversion),
            op.format_spec,
        )
    if isinstance(op, opcode.JoinStringValues):
        return O.JoinStringValues(op.count)
    if isinstance(op, opcode.AsBool):
        return O.AsBool()
    if isinstance(op, opcode.GetType):
        return O.GetType()
    if isinstance(op, opcode.LoadAttr):
        return O.LoadAttr(op.name)
    if isinstance(op, opcode.LoadObjectTypeAttr):
        return O.LoadObjectTypeAttr(op.name)
    if isinstance(op, opcode.StoreAttr):
        return O.StoreAttr(op.name)
    if isinstance(op, opcode.DeleteAttr):
        return O.DeleteAttr(op.name)
    if isinstance(op, opcode.NewList):
        return O.NewList()
    if isinstance(op, opcode.NewSet):
        return O.NewSet()
    if isinstance(op, opcode.NewDict):
        return O.NewDict()
    if isinstance(op, opcode.ListAppend):
        return O.ListAppend()
    if isinstance(op, opcode.ListExtend):
        return O.ListExtend()
    if isinstance(op, opcode.SetAdd):
        return O.SetAdd()
    if isinstance(op, opcode.SetUpdate):
        return O.SetUpdate()
    if isinstance(op, opcode.DictPut):
        return O.DictPut()
    if isinstance(op, opcode.DictUpdate):
        return O.DictUpdate()
    if isinstance(op, opcode.ListToTuple):
        return O.ListToTuple()
    if isinstance(op, opcode.BuildSlice):
        return O.BuildSlice()
    if isinstance(op, opcode.GetItem):
        return O.GetItem()
    if isinstance(op, opcode.SetItem):
        return O.SetItem()
    if isinstance(op, opcode.DeleteItem):
        return O.DeleteItem()
    if isinstance(op, opcode.GetIterator):
        return O.GetIterator()
    if isinstance(op, opcode.GetAwaitableIterator):
        return O.GetAwaitableIterator()
    if isinstance(op, opcode.GetAsyncIterator):
        return O.GetAsyncIterator()
    if isinstance(op, opcode.GetAsyncNext):
        return O.GetAsyncNext()
    if isinstance(op, opcode.GetNextElseJumpTo):
        return O.GetNextElseJumpTo(op.target.index)
    if isinstance(op, opcode.UnpackElements):
        return O.UnpackElements(op.before_count, op.has_extras, op.after_count)
    if isinstance(op, opcode.UnpackMapping):
        return O.UnpackMapping(_py_tuple_to_java(op.keys), op.has_extras)
    if isinstance(op, opcode.CreateCallBuilder):
        return O.CreateCallBuilder()
    if isinstance(op, opcode.WithPositionalArg):
        return O.WithPositionalArg(op.index)
    if isinstance(op, opcode.AppendPositionalArg):
        return O.AppendPositionalArg()
    if isinstance(op, opcode.WithKeywordArg):
        return O.WithKeywordArg(op.name)
    if isinstance(op, opcode.ExtendPositionalArgs):
        return O.ExtendPositionalArgs()
    if isinstance(op, opcode.ExtendKeywordArgs):
        return O.ExtendKeywordArgs()
    if isinstance(op, opcode.CallWithBuilder):
        return O.CallWithBuilder()
    if isinstance(op, opcode.LoadAndBindInnerFunction):
        inner_bc = _convert_bytecode(op.inner_function.bytecode)
        return O.LoadAndBindInnerFunction(inner_bc)
    if isinstance(op, opcode.LoadAndBindInnerClass):
        class_body_bc = _convert_bytecode(op.class_body)
        return O.LoadAndBindInnerClass(op.class_name, class_body_bc)
    if isinstance(op, opcode.LoadAndBindInnerGenerator):
        return O.LoadAndBindInnerGenerator()
    if isinstance(op, opcode.SaveGeneratorState):
        return O.SaveGeneratorState(op.state_id)
    if isinstance(op, opcode.SetGeneratorDelegate):
        return O.SetGeneratorDelegate()
    if isinstance(op, opcode.DelegateOrRestoreGeneratorState):
        return O.DelegateOrRestoreGeneratorState(op.state_id)
    if isinstance(op, opcode.YieldValue):
        return O.YieldValue()
    if isinstance(op, opcode.LoadTypeAttrOrGlobal):
        return O.LoadTypeAttrOrGlobal(op.name)
    if isinstance(op, opcode.StoreTypeAttr):
        return O.StoreTypeAttr(op.name)
    if isinstance(op, opcode.DeleteTypeAttr):
        return O.DeleteTypeAttr(op.name)

    raise ValueError(f"Unknown opcode type: {type(op)}")


def _convert_instruction(instr):
    JInstruction = _jclass("io.github.cdisvm.compiler.Instruction")
    return JInstruction(_convert_opcode(instr.opcode), instr.bytecode_index, instr.lineno)


def _convert_value_source(vs):
    JValueSource = _jclass("io.github.cdisvm.compiler.ValueSource")
    sources = _py_list_to_java([_convert_instruction(i) for i in vs.sources])
    PyType = _jclass("io.github.cdisvm.runtime.PyType")
    PyObject = _jclass("io.github.cdisvm.runtime.PyObject")
    return JValueSource(sources, PyType.of(PyObject))


def _convert_stack_metadata(sm):
    JStackMetadata = _jclass("io.github.cdisvm.compiler.StackMetadata")
    stack = _py_list_to_java([_convert_value_source(v) for v in sm.stack])
    local_vars = _jclass("java.util.HashMap")()
    for k, v in sm.variables.items():
        local_vars.put(k, _convert_value_source(v))
    synthetics = _py_list_to_java([_convert_value_source(v) for v in sm.synthetic_variables])
    return JStackMetadata(stack, local_vars, synthetics, sm.dead)


def _convert_exception_handler(eh):
    JExceptionHandler = _jclass("io.github.cdisvm.compiler.ExceptionHandler")
    PyType = _jclass("io.github.cdisvm.runtime.PyType")
    PyObject = _jclass("io.github.cdisvm.runtime.PyObject")
    return JExceptionHandler(
        PyType.of(PyObject),
        eh.from_label.index,
        eh.to_label.index,
        eh.handler_label.index,
    )


def _convert_parameter_kind(kind):
    JParameterKind = _jclass("io.github.cdisvm.compiler.ParameterKind")
    mapping = {
        inspect.Parameter.POSITIONAL_ONLY: JParameterKind.POSITIONAL_ONLY,
        inspect.Parameter.POSITIONAL_OR_KEYWORD: JParameterKind.POSITIONAL_OR_KEYWORD,
        inspect.Parameter.VAR_POSITIONAL: JParameterKind.VARGS,
        inspect.Parameter.KEYWORD_ONLY: JParameterKind.KEYWORD_ONLY,
        inspect.Parameter.VAR_KEYWORD: JParameterKind.KWARGS,
    }
    return mapping[kind]


def _convert_signature(sig):
    import inspect
    JFunctionSignature = _jclass("io.github.cdisvm.compiler.FunctionSignature")
    builder = JFunctionSignature.builder()
    PyType = _jclass("io.github.cdisvm.runtime.PyType")
    PyObject = _jclass("io.github.cdisvm.runtime.PyObject")

    for i, (name, param) in enumerate(sig.parameters.items()):
        kind = _convert_parameter_kind(param.kind)
        JFunctionParameter = _jclass("io.github.cdisvm.compiler.FunctionParameter")
        default = None
        if param.default is not inspect.Parameter.empty:
            default = _convert_py_constant(param.default)
        fp = JFunctionParameter(
            i,
            name,
            kind,
            PyType.of(PyObject),  # TODO: convert param.annotation to type
            default,
        )
        builder.param(fp)

    if sig.return_annotation is not inspect.Signature.empty:
        # TODO: convert sig.return_annotation to type
        builder.returningType(PyType.of(PyObject))
    else:
        builder.returningType(PyType.of(PyObject))

    return builder.build()


def _py_list_to_java(py_list):
    ArrayList = jpype.JClass("java.util.ArrayList")
    result = ArrayList()
    for item in py_list:
        result.add(item)
    return result


def _py_tuple_to_java(py_tuple):
    return _py_list_to_java(py_tuple)


def _py_set_to_java(py_set):
    HashSet = jpype.JClass("java.util.HashSet")
    result = HashSet()
    for item in py_set:
        result.add(item)
    return result


def _convert_bytecode(bc):
    JBytecode = _jclass("io.github.cdisvm.compiler.Bytecode")
    JFunctionType = _jclass("io.github.cdisvm.compiler.FunctionType")
    JMethodType = _jclass("io.github.cdisvm.compiler.MethodType")

    instructions = _py_list_to_java([_convert_instruction(i) for i in bc.instructions])
    stack_metadata = _py_list_to_java([_convert_stack_metadata(s) for s in bc.stack_metadata])
    exception_handlers = _py_list_to_java([_convert_exception_handler(e) for e in bc.exception_handlers])

    annotate_function = None
    if bc.annotate_function is not None:
        annotate_function = _convert_bytecode(bc.annotate_function)

    closure = _jclass("java.util.HashMap")()
    JCell = _jclass("io.github.cdisvm.runtime.PyCell")

    for key, value in bc.closure.items():
        # value is a cell
        try:
            closure.put(key, JCell(id(value), _convert_py_constant(value)))
        except ValueError:
            closure.put(key, JCell(id(value)))

    globals_map = _jclass("java.util.HashMap")()
    free_names = _py_set_to_java(bc.free_names)

    return JBytecode(
        bc.function_name,
        _convert_signature(bc.signature),
        JFunctionType.fromId(bc.function_type.value),
        JMethodType.fromId(bc.method_type.value),
        bc.synthetic_count,
        instructions,
        stack_metadata,
        exception_handlers,
        annotate_function,
        closure,
        globals_map,
        id(globals_map),
        free_names,
    )


def compile_function(func, jar_path='target/cdis2java-999-SNAPSHOT.jar'):
    global compiler
    start_jvm(jar_path)
    JCDisCompiler = _jclass("io.github.cdisvm.compiler.CDisCompiler")
    if compiler is None:
        compiler = JCDisCompiler()
    py_bytecode = to_bytecode(func)
    java_bytecode = _convert_bytecode(py_bytecode)
    return compiler.compile(java_bytecode)


def java_value(value):
    return _convert_py_constant(value)


def py_value(value):
    PyBool = _jclass("io.github.cdisvm.runtime.builtin.PyBool")
    PyInt = _jclass("io.github.cdisvm.runtime.builtin.PyInt")
    PyStr = _jclass("io.github.cdisvm.runtime.builtin.PyStr")
    PyNone = _jclass("io.github.cdisvm.runtime.builtin.PyNone")
    PyList = _jclass("io.github.cdisvm.runtime.builtin.PyList")
    PyTuple = _jclass("io.github.cdisvm.runtime.builtin.PyTuple")
    PyDict = _jclass("io.github.cdisvm.runtime.builtin.PyDict")
    PySet = _jclass("io.github.cdisvm.runtime.builtin.PySet")
    PyBaseException = _jclass("io.github.cdisvm.runtime.exception.PyBaseException")

    if isinstance(value, PyBool):
        return value.value()
    if isinstance(value, PyInt):
        return int(str(value.hexString()), 16)
    if isinstance(value, PyStr):
        return value.value()
    if isinstance(value, PyList):
        return [py_value(v) for v in value]
    if isinstance(value, PyTuple):
        return tuple(py_value(v) for v in value)
    if isinstance(value, PyDict):
        return {py_value(entry.getKey()): py_value(entry.getValue()) for entry in value.entrySet()}
    if isinstance(value, PySet):
        return {py_value(v) for v in value}
    if isinstance(value, PyNone):
        return None
    if isinstance(value, PyBaseException):
        import builtins
        python_error_class_name = str(value.getClass().getSimpleName())[2:]
        python_error_class = getattr(builtins, python_error_class_name)
        return python_error_class(*py_value(value.args))

    raise ValueError(f"Unsupported constant type: {type(value)}")