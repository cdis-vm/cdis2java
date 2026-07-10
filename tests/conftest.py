from cdis2java import compile_function, java_value, py_value

def create_function_match_asserter(function):
    try:
        adapted = compile_function(function)
    except Exception as e:
        e.printStackTrace()
        raise e
    def asserting_output_function(*args, **kwargs):
        nonlocal adapted, function
        call_builder = adapted.pyCallBuilder()
        converted_to_value = dict()
        for arg in args:
            if id(arg) in converted_to_value:
                getattr(call_builder, '$appendArgument')(converted_to_value[id(arg)])
            else:
                converted = java_value(arg)
                converted_to_value[id(arg)] = converted
                getattr(call_builder, '$appendArgument')(converted)
        for key, value in kwargs.items():
            if id(value) in converted_to_value:
                getattr(call_builder, '$putArgument')(key, converted_to_value[value])
            else:
                converted = java_value(value)
                converted_to_value[id(value)] = converted
                getattr(call_builder, '$putArgument')(key, converted)
        try:
            out = py_value(adapted.pyCall(call_builder))
        except Exception as java_exception:
            py_error = None
            try:
                function(*args, **kwargs)
            except Exception as py_exception:
                py_error = py_exception

            assert type(py_value(java_exception)) == type(py_error)
            return None
        assert out == function(*args, **kwargs)
        return out
    return asserting_output_function
