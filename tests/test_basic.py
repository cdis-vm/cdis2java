from tests.conftest import create_function_match_asserter

def test_return_constant():
    def constant():
        return 42

    match = create_function_match_asserter(constant)
    match()


def test_return_tuple():
    def constant(x):
        return (x,)

    match = create_function_match_asserter(constant)
    match(1)
    match(2)
