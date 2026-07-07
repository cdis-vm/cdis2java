from tests.conftest import create_function_match_asserter

def test_return_constant():
    def constant():
        return 42

    match = create_function_match_asserter(constant)
    match()
