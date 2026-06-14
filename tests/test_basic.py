from cdis2java import compile_function

def test_return_constant():
    def constant():
        return 42

    compile_function(constant)
