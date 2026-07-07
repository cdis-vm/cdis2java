from cdis2java import compile_function, java_value, py_value

def adapt_function(function):
    adapted = compile_function(function)
    def adapted_function(*args, **kwargs):
        nonlocal adapted, function
        call_builder = adapted.pyCallBuilder()
        for arg in args:
            getattr(call_builder, '$appendArgument')(java_value(arg))
        for key, value in kwargs.items():
            getattr(call_builder, '$putArgument')(key, java_value(value))
        out = py_value(adapted.pyCall(call_builder))
        assert out == function(*args, **kwargs)
        return out
    return adapted_function
