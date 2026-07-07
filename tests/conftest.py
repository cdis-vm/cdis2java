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
        for arg in args:
            getattr(call_builder, '$appendArgument')(java_value(arg))
        for key, value in kwargs.items():
            getattr(call_builder, '$putArgument')(key, java_value(value))
        try:
            out = py_value(adapted.pyCall(call_builder))
        except Exception as java_exception:
            py_error = None
            try:
                function(*args, **kwargs)
            except Exception as py_exception:
                py_error = py_exception
            assert py_value(java_exception) == py_error
            return None
        assert out == function(*args, **kwargs)
        return out
    return asserting_output_function
