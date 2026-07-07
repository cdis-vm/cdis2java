from tests.conftest import adapt_function

def test_return_constant():
    def constant():
        return 42

    adapted = adapt_function(constant)
    adapted()
