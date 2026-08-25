import jpype
import jpype.imports
import inspect
from types import ModuleType

from cdis.compiler._api import to_bytecode, Bytecode
import cdis.opcode as opcode
from cdis.opcode import ClassInfo

compiler = None

def start_jvm(jar_path):
    if not jpype.isJVMStarted():
        jpype.startJVM(classpath=[jar_path])


def _jclass(name):
    return jpype.JClass(name)

def _jpackage(name):
    return jpype.JPackage(name)


def _convert_module_as_class_info(module: ModuleType, visited: set):
    if id(module) in visited:
        return None

    visited.add(id(module))
    JClassInfo = _jclass("io.github.cdisvm.compiler.ClassInfo")
    JHashMap = _jclass("java.util.HashMap")
    class_attribute_to_type = JHashMap()
    instance_attribute_to_type = JHashMap()
    class_attribute_to_default = JHashMap()

    for module_field, value in module.__dict__.items():
        if module_field.startswith("_"):
            continue
        try:
            class_attribute_to_default.put(module_field, _convert_py_constant(value, visited))
            class_attribute_to_type.put(module_field, _convert_py_constant(object, visited))
        except Exception:
            continue

    return JClassInfo(
        module.__name__,
        module.__name__,
        class_attribute_to_type,
        instance_attribute_to_type,
        class_attribute_to_default
    )


def _convert_class_info(cls: type, visited):
    if id(cls) in visited:
        return None
    visited.add(id(cls))
    JClassInfo = _jclass("io.github.cdisvm.compiler.ClassInfo")
    JHashMap = _jclass("java.util.HashMap")
    class_attribute_to_type = JHashMap()
    instance_attribute_to_type = JHashMap()
    class_attribute_to_default = JHashMap()

    for field, field_type in getattr(cls, '__annotations__', {}).items():
        instance_attribute_to_type.put(field, _convert_py_constant(object, visited))

    for cls_field, value in cls.__dict__.items():
        if cls_field == '__module__':
            continue
        class_attribute_to_type.put(cls_field, _convert_py_constant(object, visited))
        class_attribute_to_default.put(cls_field, _convert_py_constant(value, visited))

    return JClassInfo(
        cls.__name__,
        cls.__qualname__,
        class_attribute_to_type,
        instance_attribute_to_type,
        class_attribute_to_default
    )

def _convert_type_value(value):
    if isinstance(value, type):
        out = compiler.lookupBuiltinType(value.__name__)
        if out is not None:
            return out
    return compiler.lookupBuiltinType("object")


def _convert_cdis_class_info(ci, outer_closure):
    JClassInfo = _jclass("io.github.cdisvm.compiler.ClassInfo")
    JHashMap = _jclass("java.util.HashMap")
    class_attribute_to_type = JHashMap()
    instance_attribute_to_type = JHashMap()
    class_attribute_to_default = JHashMap()

    for name, value in ci.class_attributes.items():
        class_attribute_to_type.put(name, _convert_type_value(value))
    for name, value in ci.instance_attributes.items():
        instance_attribute_to_type.put(name, _convert_type_value(value))
    for name, value in ci.class_attribute_defaults.items():
        if isinstance(value, Bytecode):
            class_attribute_to_default.put(name, compile_bytecode(value, outer_closure))
        else:
            class_attribute_to_default.put(name, _convert_py_constant(value))

    return JClassInfo(
        ci.name,
        ci.qualname,
        class_attribute_to_type,
        instance_attribute_to_type,
        class_attribute_to_default
    )


compiled_type_dict = {}
def _convert_py_constant(value, visited=None):
    global compiler
    global compiled_type_dict
    from types import CellType, FunctionType

    if isinstance(value, type) and id(value) in compiled_type_dict:
        return compiled_type_dict[id(value)]

    if visited is None:
        visited = set()

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
            out.add(_convert_py_constant(item, visited))
        return out
    if isinstance(value, tuple):
        AList = _jclass("java.util.ArrayList")
        PyTuple = _jclass("io.github.cdisvm.runtime.builtin.PyTuple")
        out = AList()
        for item in value:
            out.add(_convert_py_constant(item, visited))
        return PyTuple(out)
    if isinstance(value, dict):
        PyDict = _jclass("io.github.cdisvm.runtime.builtin.PyDict")
        out = PyDict()
        for key, item in value.items():
            out.put(_convert_py_constant(key, visited), _convert_py_constant(item, visited))
        return out
    if isinstance(value, set):
        PySet = _jclass("io.github.cdisvm.runtime.builtin.PySet")
        out = PySet()
        for item in value:
            out.add(_convert_py_constant(item, visited))
        return out
    if isinstance(value, BaseException):
        java_type = _jclass(f"io.github.cdisvm.runtime.exception.Py{type(value).__name__}")
        return java_type(*(_convert_py_constant(arg, visited) for arg in value.args))
    if isinstance(value, CellType):
        try:
            cell_contents = value.cell_contents
        except ValueError:
            # Cell is empty, so it has no initializer
            return None
        return _convert_py_constant(cell_contents, visited)
    if isinstance(value, type):
        out = compiler.lookupBuiltinType(value.__name__)
        if out is not None:
            compiled_type_dict[id(value)] = out
            return out
        cls_info = _convert_class_info(value, visited)
        out = compiler.lookupUserType(cls_info)
        compiled_type_dict[id(value)] = out
        return out
    if isinstance(value, ModuleType):
        cls_info = _convert_module_as_class_info(value, visited)
        out = compiler.lookupUserType(cls_info)
        return out
    if isinstance(value, FunctionType):
        if id(value) in visited:
            return None
        visited.add(id(value))
        return compile_function(value, visited)

    if is_defined_in_c(value):
        return None

    if not is_defined_in_c(type(value)):
        java_type = _convert_py_constant(type(value), visited)
        instance = java_type.newInstance()
        for key, value in value.__dict__.items():
            instance.pyAttributes().setAttributeByName(key, _convert_py_constant(value, visited))
        return instance
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


def _convert_opcode(bytecode, op, outer_closure,
                    visited):
    O = _jpackage("io.github.cdisvm.compiler.opcode")

    if isinstance(op, opcode.LoadConstant):
        return O.LoadConstant(_convert_py_constant(op.constant))
    if isinstance(op, opcode.Nop):
        return O.Nop()
    if isinstance(op, opcode.ImportModule):
        module = __import__(op.name, bytecode.globals, bytecode.globals, op.from_list, op.level)
        class_info = _convert_module_as_class_info(module, set())
        return O.ImportModule(class_info)
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
        inner_bc = _convert_bytecode(op.inner_function.bytecode, visited)
        parameters_with_defaults = _py_list_to_java(op.inner_function.parameters_with_defaults)
        return O.LoadAndBindInnerFunction(inner_bc, parameters_with_defaults)
    if isinstance(op, opcode.LoadAndBindInnerClass):
        class_body_bc = _convert_bytecode(op.class_body, visited)
        return O.LoadAndBindInnerClass(op.class_name, class_body_bc)
    if isinstance(op, opcode.LoadAndBindInnerGenerator):
        return O.LoadAndBindInnerGenerator(_convert_cdis_class_info(op.inner_generator, outer_closure))
    if isinstance(op, opcode.SaveGeneratorState):
        return O.SaveGeneratorState(op.state_id, _convert_stack_metadata(bytecode, op.stack_metadata, outer_closure, visited))
    if isinstance(op, opcode.SetGeneratorDelegate):
        return O.SetGeneratorDelegate()
    if isinstance(op, opcode.DelegateOrRestoreGeneratorState):
        return O.DelegateOrRestoreGeneratorState(op.state_id, _convert_stack_metadata(bytecode, op.stack_metadata, outer_closure, visited))
    if isinstance(op, opcode.YieldValue):
        return O.YieldValue()
    if isinstance(op, opcode.LoadTypeAttrOrGlobal):
        return O.LoadTypeAttrOrGlobal(op.name)
    if isinstance(op, opcode.StoreTypeAttr):
        return O.StoreTypeAttr(op.name)
    if isinstance(op, opcode.DeleteTypeAttr):
        return O.DeleteTypeAttr(op.name)

    raise ValueError(f"Unknown opcode type: {type(op)}")


def _convert_instruction(bytecode,
                         instr,
                         outer_closure,
                         visited):
    JInstruction = _jclass("io.github.cdisvm.compiler.Instruction")
    return JInstruction(_convert_opcode(bytecode, instr.opcode, outer_closure, visited), instr.bytecode_index, instr.lineno)


def _convert_value_source(bc, vs, outer_closure,
                          visited):
    JValueSource = _jclass("io.github.cdisvm.compiler.ValueSource")
    sources = _py_list_to_java([_convert_instruction(bc, i, outer_closure, visited) for i in vs.sources])
    PyType = _jclass("io.github.cdisvm.runtime.PyType")
    PyObject = _jclass("io.github.cdisvm.runtime.PyObject")
    return JValueSource(sources, PyType.of(PyObject))


def _convert_stack_metadata(bc, sm, outer_closure, visited):
    JStackMetadata = _jclass("io.github.cdisvm.compiler.StackMetadata")
    stack = _py_list_to_java([_convert_value_source(bc, v, outer_closure, visited) for v in sm.stack])
    local_vars = _jclass("java.util.HashMap")()
    for k, v in sm.variables.items():
        local_vars.put(k, _convert_value_source(bc, v, outer_closure, visited))
    synthetics = _py_list_to_java([_convert_value_source(bc, v, outer_closure, visited) for v in sm.synthetic_variables])
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


def _convert_bytecode(bc,
                      visited,
                      outer_closure=None):
    JBytecode = _jclass("io.github.cdisvm.compiler.Bytecode")
    JFunctionType = _jclass("io.github.cdisvm.compiler.FunctionType")
    JMethodType = _jclass("io.github.cdisvm.compiler.MethodType")

    closure = _jclass("java.util.HashMap")()
    JCell = _jclass("io.github.cdisvm.runtime.PyCell")

    if outer_closure is None:
        for key, value in bc.closure.items():
            # value is a cell
            try:
                bc.closure[key].cell_contents
            except ValueError:
                continue
            closure.put(key, JCell(id(value), _convert_py_constant(value, visited)))
    else:
        closure = outer_closure

    instructions = _py_list_to_java([_convert_instruction(bc, i, closure, visited) for i in bc.instructions])
    stack_metadata = _py_list_to_java([_convert_stack_metadata(bc, s, closure, visited) for s in bc.stack_metadata])
    exception_handlers = _py_list_to_java([_convert_exception_handler(e) for e in bc.exception_handlers])

    annotate_function = None
    if bc.annotate_function is not None:
        annotate_function = _convert_bytecode(bc.annotate_function, visited)

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


def compile_bytecode(bc,
                     visited,
                     outer_closure=None):
    global compiler
    return compiler.compile(_convert_bytecode(bc, visited, outer_closure))


def compile_function(func, visited=None, jar_path='target/cdis2java-999-SNAPSHOT.jar'):
    global compiler
    start_jvm(jar_path)
    JCDisCompiler = _jclass("io.github.cdisvm.compiler.CDisCompiler")
    if compiler is None:
        compiler = JCDisCompiler()
    if visited is None:
        visited = set()
    py_bytecode = to_bytecode(func)
    java_bytecode = _convert_bytecode(py_bytecode, visited)
    return compiler.compile(java_bytecode)


def lookup_class(cls):
    global compiler
    return compiler.lookupUserClass(cls.__qualname__)


def customize_class(cls, customizer, jar_path='target/cdis2java-999-SNAPSHOT.jar'):
    global compiler
    start_jvm(jar_path)
    JCDisCompiler = _jclass("io.github.cdisvm.compiler.CDisCompiler")
    if compiler is None:
        compiler = JCDisCompiler()
    class_info = _convert_class_info(cls, set())
    compiler.customizeUserType(class_info, customizer)
    return compiler.lookupUserClass(cls.__qualname__)


def as_interface(func, interface_name):
    global compiler
    compiled_callable = compile_function(func)
    return compiler.toJavaInterface(compiled_callable, _jclass(interface_name))


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
    PyType = _jclass("io.github.cdisvm.runtime.PyType")

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
    if isinstance(value, PyType):
        class_name = str(value)
        for builtin_type in (tuple, list, frozenset, set, dict, type(None), type(...),
                             int, float, str, bool, type, object, range):
            if str(builtin_type) == class_name:
                return builtin_type

    raise ValueError(f"Unsupported constant type: {type(value)}")


import inspect
def is_defined_in_c(obj):
    # Get the actual class of the object
    obj_type = type(obj)

    # 1. Check the module source
    if obj_type.__module__ == 'builtins' and not isinstance(obj, type):
        return True

    # 2. Check if the code source file is available
    try:
        inspect.getsource(obj)
        return False
    except Exception:
        # C extensions and built-ins do not have a Python source file
        try:
            inspect.getsource(obj_type)
            return False
        except Exception:
            return True