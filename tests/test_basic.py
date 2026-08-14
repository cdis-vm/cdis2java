import pytest
import sys
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

def test_return_closure():
    a = 10

    def closure():
        nonlocal a
        old = a
        a = 20
        return old

    match = create_function_match_asserter(closure)
    match()
    assert a == 20
    match()

def test_return_arg():
    def identity(a):
        return a

    match = create_function_match_asserter(identity)
    match(0)
    match(10)
    match("a")

def test_no_return():
    def no_return():
        pass

    match = create_function_match_asserter(no_return)
    match()

def test_single_assignment():
    def single_assignment(value):
        a = value
        return a

    match = create_function_match_asserter(single_assignment)
    match(10)
    match(20)
    match("a")

def test_variable_deletion():
    def delete_variable():
        a = 1
        del a
        return a  # noqa

    match = create_function_match_asserter(delete_variable)
    match()

def test_free_variable_deletion():
    a = 0  # noqa
    del a

    def delete_variable():
        nonlocal a
        return a  # noqa

    # Python error messages might change across versions
    match = create_function_match_asserter(delete_variable)
    match()


def test_multi_assignment():
    def multi_assignment(value):
        a = b = value
        return a + b

    match = create_function_match_asserter(multi_assignment)
    match(10)
    match(20)
    match("a")


def test_multi_target_assignment():
    def multi_target_assignment(value):
        a, b = value
        return a + b

    # Python error messages might change across versions
    match = create_function_match_asserter(multi_target_assignment)
    match([1])
    match([1, 2])
    match([1, 2, 3])


def test_nested_multi_target_assignment():
    def nested_multi_target_assignment(value):
        a, (b, c) = value
        return a, b, c

    # Python error messages might change across versions
    match = create_function_match_asserter(nested_multi_target_assignment)
    match([1])
    match([1, (2,)])
    match([1, (2, 3)])
    match([1, (2, 3, 4)])


def test_multi_target_extras_assignment():
    def multi_target_extras_assignment(value):
        a, *b, c = value
        return a, b, c

    # Python error messages might change across versions
    match = create_function_match_asserter(multi_target_extras_assignment)
    match([1])
    match([1, 2])
    match([1, 2, 3])
    match([1, 2, 3, 4])


def test_attr_assignment():
    class A: ...

    def attr_assignment(value):
        a = A()
        a.x = value
        return a.x

    match = create_function_match_asserter(attr_assignment)
    match(10)
    match(20)
    match("a")


def test_attr_deletion():
    class A:
        def __init__(self):
            self.x = 1

    def attr_delete():
        a = A()
        del a.x
        return a.x

    match = create_function_match_asserter(attr_delete)
    match()


def test_attr_inplace_add():
    class A: ...

    def attr_assignment(value):
        a = A()
        a.x = 1
        a.x += value
        return a.x

    match = create_function_match_asserter(attr_assignment)
    match(10)
    match(20)


def test_assignment_expr():
    def attr_assignment(value):
        y = (x := value)
        y = y * y
        return y // x

    match = create_function_match_asserter(attr_assignment)
    match(10)
    match(20)


def test_add():
    def add(a, b):
        return a + b

    match = create_function_match_asserter(add)
    match(1, 2)


def test_inplace_add():
    def add(a, b):
        a += b
        return a

    match = create_function_match_asserter(add)
    match(1, 2)


def test_comparison():
    def lt(a, b):
        return a < b

    match = create_function_match_asserter(lt)
    match(1, 2)


def test_chained_comparison():
    def lt(a, b, c):
        return a < b < c

    match = create_function_match_asserter(lt)
    match(1, 2, 3)
    match(1, 3, 2)
    match(3, 2, 1)


def test_is():
    def is_(a, b):
        return a is b

    a = "1"
    b = "2"
    match = create_function_match_asserter(is_)
    match(a, a)
    match(b, b)
    match(a, b)


def test_is_not():
    def is_not(a, b):
        return a is not b

    a = "1"
    b = "2"
    match = create_function_match_asserter(is_not)
    match(a, a)
    match(b, b)
    match(a, b)


def test_in():
    def in_(a, b):
        return a in b

    a = "a"
    b = "abc"
    match = create_function_match_asserter(in_)
    match(a, a)
    match(b, b)
    match(a, b)
    match(b, a)


def test_and():
    def and_(a, b):
        return a and b

    match = create_function_match_asserter(and_)
    match(1, 2)
    match(False, 2)
    match(0, False)
    match(True, 5)


def test_or():
    def or_(a, b):
        return a or b

    match = create_function_match_asserter(or_)
    match(1, 2)
    match(False, 2)
    match(1, False)
    match(True, 5)


def test_if():
    def if_(value):
        if value < 5:
            return -value
        else:
            return value

    match = create_function_match_asserter(if_)
    match(1)
    match(3)
    match(5)
    match(6)
    match(8)


def test_if_expr():
    def if_(value):
        return -value if value < 5 else value

    match = create_function_match_asserter(if_)
    match(1)
    match(3)
    match(5)
    match(6)
    match(8)


def test_while():
    def power(base, exponent):
        out = 1
        while exponent > 0:
            out *= base
            exponent -= 1
        return out

    match = create_function_match_asserter(power)
    match(2, 0)
    match(2, 1)
    match(2, 2)
    match(2, 3)

    match(3, 0)
    match(3, 1)
    match(3, 2)
    match(3, 3)


def test_while_else():
    def is_prime(number):
        tested = 2
        while tested < number:
            if number % tested == 0:
                break
            tested += 1
        else:
            return True
        return False

    match = create_function_match_asserter(is_prime)
    match(2)
    match(3)
    match(4)
    match(5)
    match(6)
    match(7)


def test_while_continue():
    def count_even(end):
        current = 0
        out = 0
        while current < end:
            current += 1
            if current % 2 != 1:
                continue
            out += 1
        return out

    match = create_function_match_asserter(count_even)
    match(0)
    match(1)
    match(2)
    match(3)
    match(4)
    match(5)


def test_for():
    def power(base, exponent):
        out = 1
        for i in range(exponent):
            out *= base
        return out

    match = create_function_match_asserter(power)
    match(2, 0)
    match(2, 1)
    match(2, 2)
    match(2, 3)

    match(3, 0)
    match(3, 1)
    match(3, 2)
    match(3, 3)


def test_for_else():
    def is_prime(number):
        for tested in range(2, number):
            if number % tested == 0:
                break
            tested += 1
        else:
            return True
        return False

    match = create_function_match_asserter(is_prime)
    match(2)
    match(3)
    match(4)
    match(5)
    match(6)
    match(7)


def test_for_continue():
    def count_even(end):
        out = 0
        for current in range(end):
            if current % 2 != 0:
                continue
            out += 1
        return out

    match = create_function_match_asserter(count_even)
    match(0)
    match(1)
    match(2)
    match(3)
    match(4)
    match(5)


def test_try():
    def try_(error):
        try:
            raise error
        except IOError:
            return 1
        except ValueError:
            return 2
        except:  # noqa
            return 3

    match = create_function_match_asserter(try_)
    match(IOError)
    match(ValueError)
    match(SystemError)


def test_try_dynamic_except():
    def try_(error, guard1, guard2):
        try:
            raise error
        except guard1:
            return 1
        except guard2:
            return 2
        except:  # noqa
            return 3

    match = create_function_match_asserter(try_)
    match(IOError, IOError, ValueError)
    match(IOError, ValueError, IOError)
    match(ValueError, IOError, ValueError)
    match(ValueError, ValueError, IOError)
    match(ValueError, 1, 2)


@pytest.mark.skipif(sys.version_info >= (3, 14), reason="Python 3.14 and above do not support return in finally")
def test_try_finally_return():
    def try_():
        try:
            return 1
        finally:
            return 2

    match = create_function_match_asserter(try_)
    match()


def test_try_finally_no_return():
    def try_():
        try:
            value = 1
            return value
        finally:
            value = 2

    match = create_function_match_asserter(try_)
    match()


def test_try_finally_return_raises():
    def try_():
        try:
            value = 1
            return value
        finally:
            raise ValueError

    match = create_function_match_asserter(try_)
    match()


def test_try_except_raises():
    def try_(error):
        failed = True
        try:
            raise error
        except IOError:
            raise ValueError
        except ValueError:
            failed = False
            raise
        finally:
            if failed:
                raise SystemError

    match = create_function_match_asserter(try_)
    match(IOError)
    match(ValueError)
    match(NameError)


def test_try_inner_try():
    def try_(error):
        try:
            raise error
        except ValueError:
            try:
                raise IOError
            except IOError:
                return 1
        except IOError:
            return 2

    match = create_function_match_asserter(try_)
    match(ValueError)
    match(IOError)


def test_list():
    def list_(arg):
        return [1, arg, "3"]

    match = create_function_match_asserter(list_)
    match(1)
    match(2)


def test_tuple():
    def tuple_(arg):
        return 1, arg, "3"

    match = create_function_match_asserter(tuple_)
    match(1)
    match(2)


def test_slice():
    def slice_(arg):
        return arg[:2]

    match = create_function_match_asserter(slice_)
    match([1, 2, 3, 4])
    match([4, 3, 2, 1, 0])


def test_set():
    def set_(arg):
        return {1, arg, "3"}

    match = create_function_match_asserter(set_)
    match(1)
    match(2)


def test_dict():
    def dict_(arg):
        return {"a": 1, "b": arg, arg: "3"}

    match = create_function_match_asserter(dict_)
    match("c")
    match("b")


def test_list_extend():
    def list_(arg):
        return [1, *arg, "3"]

    match = create_function_match_asserter(list_)
    match([1, 2])
    match([1, "3"])


def test_tuple_extend():
    def tuple_(arg):
        return 1, *arg, "3"

    match = create_function_match_asserter(tuple_)
    match([1, 2])
    match([1, "3"])


def test_set_update():
    def set_(arg):
        return {1, *arg, "3"}

    match = create_function_match_asserter(set_)
    match([1, 2])
    match([2, 4])


def test_dict_update():
    def dict_(arg):
        return {"a": 1, "b": arg, **arg}

    match = create_function_match_asserter(dict_)
    match({"c": 3, "d": 4})
    match({"c": 3, "a": 2})


def test_get_item():
    def get_item(items, index):
        return items[index]

    match = create_function_match_asserter(get_item)
    match([1, 2, 3], 0)
    match([1, 2, 3], 1)
    match([1, 2, 3], 2)
    match([1, 2, 3], -1)
    match({"a": 10}, "a")


def test_get_item_chain():
    def get_item(items, index1, index2):
        return items[index1][index2]

    match = create_function_match_asserter(get_item)
    match([[1]], 0, 0)
    match([[0], [0, 1, 2]], 1, -1)


def test_set_item():
    def set_item(index, value):
        a = [1, 2, 3]
        a[index] = value
        return a

    match = create_function_match_asserter(set_item)
    match(0, "a")
    match(1, "b")
    match(2, "c")
    match(-1, "d")


def test_set_item_chain():
    def set_item(index1, index2, value):
        a = [[1, 2, 3], [4, 5, 6]]
        a[index1][index2] = value
        return a

    match = create_function_match_asserter(set_item)
    match(0, 0, "a")
    match(1, 0, "b")
    match(0, -1, "c")


def test_aug_set_item():
    def aug_set_item(index, value):
        a = [1, 2, 3]
        a[index] += value
        return a

    match = create_function_match_asserter(aug_set_item)
    match(0, 1)
    match(1, 2)
    match(2, 3)
    match(-1, 3)


def test_aug_set_item_chain():
    def aug_set_item(index1, index2, value):
        a = [[1, 2, 3], [4, 5, 6]]
        a[index1][index2] += value
        return a

    match = create_function_match_asserter(aug_set_item)
    match(0, 0, 1)
    match(1, 0, 2)
    match(1, -1, 3)


def test_delete_item():
    def delete_item(index):
        a = [1, 2, 3]
        del a[index]
        return a

    match = create_function_match_asserter(delete_item)
    match(0)
    match(1)
    match(2)
    match(-1)


def test_delete_item_chain():
    def delete_item(index1, index2):
        a = [[1, 2, 3], [4, 5, 6]]
        del a[index1][index2]
        return a

    match = create_function_match_asserter(delete_item)
    match(0, 0)
    match(1, 0)
    match(0, -1)


def test_f_string():
    def f_string(value):
        return f"Hello {value}!"

    match = create_function_match_asserter(f_string)
    match("World")
    match("Earth")
    match(10)


# TODO: Enable class test
def test_f_string_with_str_conversion():
    class A:
        def __str__(self):
            return "A"

        def __format__(self, format_spec):
            raise NotImplementedError

    def f_string(value):
        return f"Hello {value!s}!"

    match = create_function_match_asserter(f_string)
    match(1)
    match(["a", "b", "c"])
    #match(A())


def test_f_string_with_repr_conversion():
    class A:
        def __repr__(self):
            return "A"

        def __format__(self, format_spec):
            raise NotImplementedError

    def f_string(value):
        return f"Hello {value!r}!"

    match = create_function_match_asserter(f_string)
    match(1)
    match(["a", "b", "c"])
    #match(A())


def test_f_string_with_ascii_conversion():
    class A:
        def __str__(self):
            return "Å"

        def __format__(self, format_spec):
            raise NotImplementedError

    def f_string(value):
        return f"Hello {value!a}!"

    match = create_function_match_asserter(f_string)
    match(1)
    match(["Å", "b", "c"])
    #match(A())


def test_f_string_with_spec():
    def f_string(value):
        return f"Hello {value:b}!"

    match = create_function_match_asserter(f_string)
    match(1)
    match(6)
    match(12)


def test_f_string_multiple_joined():
    def f_string(greetings, name):
        return f"{greetings} {name}!"

    match = create_function_match_asserter(f_string)
    match("Hello", "World")
    match("Goodbye", "World")


def test_list_comprehension():
    def list_comp(numbers):
        x = 5
        out = [x * 2 for x in numbers]
        return x, out

    match = create_function_match_asserter(list_comp)
    match([])
    match([1, 2, 3])


def test_set_comprehension():
    def set_comp(numbers):
        x = 5
        out = {x for x in numbers}
        return x, out

    match = create_function_match_asserter(set_comp)
    match([])
    match([1, 2, 3])
    match([1, 2, 2])


def test_dict_comprehension():
    def dict_comp(numbers):
        x = 5
        out = {x: x**2 for x in numbers}
        return x, out

    match = create_function_match_asserter(dict_comp)
    match([])
    match([1, 2, 3])
    match([1, 2, 2])


def test_list_comprehension_multiple_generators():
    def list_comp(xs, ys):
        return [(x, y, x * y) for x in xs for y in ys]

    match = create_function_match_asserter(list_comp)
    match([], [])
    match([], [1, 2, 3])
    match([1, 2, 3], [])
    match([1, 2, 3], [4, 5, 6])
    match([1, 2], [3, 4, 5, 6])


def test_generator_comprehension():
    def generator_comp(numbers):
        double = (number * 2 for number in numbers)
        total = 0
        for x in double:
            total += x
        return total

    match = create_function_match_asserter(generator_comp)
    match([])
    match([1, 2, 3])
    match([1, 2, 3, 4, 5])


def test_generator_comprehension_multiple_generators():
    def generator_comp(xs, ys):
        sum = (x + y for x in xs for y in ys)
        total = 0
        for x in sum:
            total += x
        return total

    match = create_function_match_asserter(generator_comp)
    match([], [])
    match([1], [])
    match([], [2])
    match([1, 2, 3], [4, 5, 6])
    match([1], [2, 3])


def test_inner_function():
    def outer_function(x):
        def inner_function(y):
            return x + y

        return inner_function(x * 2)

    match = create_function_match_asserter(outer_function)
    match(1)
    match(2)
    match(3)


def test_inner_function_with_defaults():
    def outer_function(x):
        def inner_function(y=x * 2):
            return x + y

        x = 10
        return inner_function()

    match = create_function_match_asserter(outer_function)
    match(1)
    match(2)
    match(3)


@pytest.mark.skipif(sys.version_info >= (3, 14), reason="Python 3.14 and above defer evaluating __annotations__")
def test_inner_function_with_annotations():
    def outer_function(x):
        def inner_function(y: x * 2):
            pass

        y = inner_function.__annotations__["y"]
        return x + y

    match = create_function_match_asserter(outer_function)
    match(1)
    match(2)
    match(3)


def test_inner_function_with_decorators():
    def decorator(func):
        def wrapper(*args, **kwargs):
            return func(1, *args, **kwargs)

        return wrapper

    def outer_function(x):
        @decorator
        def inner_function(y, z):
            return x + y + z

        return inner_function(x * 2)

    match = create_function_match_asserter(outer_function)
    match(1)
    match(2)
    match(3)


def test_lambda_function():
    def outer_function(x):
        adder = lambda y: x + y  # noqa
        return adder(x * 2)

    match = create_function_match_asserter(outer_function)
    match(1)
    match(2)
    match(3)


def test_lambda_function_with_defaults():
    def outer_function(x):
        adder = lambda y=x * 2: x + y  # noqa
        x = 10
        return adder()

    match = create_function_match_asserter(outer_function)
    match(1)
    match(2)
    match(3)


def test_context_manager_error_enter():
    class A:
        def __init__(self):
            self.called_exit = False

        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            self.called_exit = True
            return False

    def fun():
        manager = A()
        try:
            with manager, manager.missing_attribute:
                pass
        except AttributeError:
            return manager.called_exit

    match = create_function_match_asserter(fun)
    match()


def test_context_manager_call_exit_normally():
    class A:
        def __init__(self):
            self.value = 0

        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            self.value = 10
            return False

    def fun():
        manager = A()
        y = 0
        with manager:
            y = 20
        return y + manager.value

    match = create_function_match_asserter(fun)
    match()


def test_context_manager_shallow_exception_if_exit_truthful():
    class A:
        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            return 1  # 1 is truthful

    def fun():
        manager = A()
        y = 0
        with manager:
            y = 20
            raise ValueError
        return y

    match = create_function_match_asserter(fun)
    match()


def test_context_manager_shallow_exception_if_any_exit_truthful():
    class A:
        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            return 1  # 1 is truthful

    class B:
        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            return 0  # 0 is false

    def fun(a, b):
        y = 0
        with a, b:
            y = 20
            raise ValueError
        return y

    match = create_function_match_asserter(fun)

    # TODO: Find a way to convert these instances
    match(A(), B())
    match(B(), A())


def test_context_manager_reraise_exception_if_exit_false():
    class A:
        def __enter__(self):
            return self

        def __exit__(self, exc_type, exc_value, traceback):
            return 0  # 0 is false

    def fun():
        manager = A()
        y = 0
        with manager:
            y = 20
            raise ValueError
        return y

    match = create_function_match_asserter(fun)
    match()


def test_context_manager_assign_value():
    class A:
        def __enter__(self):
            return 10

        def __exit__(self, exc_type, exc_value, traceback):
            return 0  # 0 is false

    def fun():
        manager = A()
        y = 0
        with manager as x:
            y = x + 20
        return y

    match = create_function_match_asserter(fun)
    match()


def test_generator():
    def generator(x):
        total = 0
        for i in range(x):
            total = total + i
            yield total

    # TODO
    # assert_generator_bytecode_for_args(generator, 0)
    # assert_generator_bytecode_for_args(generator, 5)
    # assert_generator_bytecode_for_args(generator, 10)


def test_generator_send():
    def generator(x):
        yield (10 + (yield x))

    # Assert that when this generator is used incorrectly, it matches Python
    # TODO
    # assert_generator_bytecode_for_args(generator, 0)
    #
    # assert_generator_bytecode_for_args(
    #     generator, 0, sequence=(Skip(), Sent(20), Skip())
    # )


def test_generator_throw():
    def generator(x):
        try:
            yield x
            return
        except ValueError:
            yield 2 * x
            raise

    # No exception
    # TODO
    # assert_generator_bytecode_for_args(generator, 0, sequence=(Skip(2),))
    #
    # # ValueError
    # assert_generator_bytecode_for_args(
    #     generator, 0, sequence=(Skip(), Thrown(ValueError()), Skip())
    # )
    #
    # # NameError
    # assert_generator_bytecode_for_args(
    #     generator, 0, sequence=(Skip(), Thrown(NameError()), Skip())
    # )


def test_subgenerator():
    def generator(x):
        yield from [2 * n for n in range(x)]

    # TODO
    # assert_generator_bytecode_for_args(generator, 0)
    # assert_generator_bytecode_for_args(generator, 5)
    # assert_generator_bytecode_for_args(generator, 10)


def test_subgenerator_send():
    def coroutine(x):
        yield (10 + (yield x))

    def generator(x):
        yield from coroutine(x)

    # Assert that when this generator is used incorrectly, it matches Python
    # TODO
    # assert_generator_bytecode_for_args(generator, 0)
    #
    # assert_generator_bytecode_for_args(
    #     generator, 0, sequence=(Skip(), Sent(20), Skip())
    # )


def test_subgenerator_throw():
    def coroutine(x):
        try:
            yield x
            return
        except ValueError:
            yield 2 * x
            raise

    def generator(x):
        yield from coroutine(x)

    # TODO
    # No exception
    # assert_generator_bytecode_for_args(generator, 0, sequence=(Skip(2),))
    #
    # # ValueError
    # assert_generator_bytecode_for_args(
    #     generator, 0, sequence=(Skip(), Thrown(ValueError()), Skip())
    # )
    #
    # # NameError
    # assert_generator_bytecode_for_args(
    #     generator, 0, sequence=(Skip(), Thrown(NameError()), Skip())
    # )


def test_async():
    from types import coroutine
    from asyncio import sleep

    @coroutine
    def generator(x):
        total = 0
        for i in range(x):
            total = total + i
            yield from sleep(total)
        return total

    async def task(count):
        return await generator(count)

    # TODO
    # assert_async_bytecode_for_args(task, 0)
    # assert_async_bytecode_for_args(task, 1)


def test_async_for():
    from asyncio import sleep

    class ARange:
        def __init__(self, start, stop):
            self.current = start
            self.stop = stop

        def __aiter__(self):
            return self

        async def __anext__(self):
            if self.current >= self.stop:
                raise StopAsyncIteration()
            else:
                await sleep(0)
                self.current += 1
                return self.current

    async def task(start, end):
        total = 0
        async for value in ARange(start, end):
            total += value
        return total

    # TODO
    # assert_async_bytecode_for_args(task, 0, 10)
    # assert_async_bytecode_for_args(task, 20, 30)


def test_async_with():
    from asyncio import sleep

    class AManager:
        def __init__(self, value):
            self.value = value

        async def __aenter__(self):
            await sleep(0)
            return self.value

        async def __aexit__(self, exc_type, exc_value, traceback):
            await sleep(0)
            return False

    async def task(value):
        x = 10
        async with AManager(value):
            x += value
        return x

    # TODO
    # assert_async_bytecode_for_args(task, 0)
    # assert_async_bytecode_for_args(task, 20)


def test_import():
    def sqrt(x):
        import math

        return int(math.sqrt(x))

    match = create_function_match_asserter(sqrt)
    match(4)
    match(9)


def test_import_as():
    def sqrt(x):
        import math as m

        return int(m.sqrt(x))

    match = create_function_match_asserter(sqrt)
    match(4)
    match(9)


def test_multi_import():
    def sqrt(x):
        import math
        import string

        return string.Formatter().format("{}", int(math.sqrt(x)))

    match = create_function_match_asserter(sqrt)
    match(4)
    match(9)


def test_import_from():
    def sqrt(x):
        from math import sqrt as square_root

        return int(square_root(x))

    match = create_function_match_asserter(sqrt)
    match(4)
    match(9)


def test_import_path():
    def is_iterable(asserted):
        import collections.abc

        return isinstance(asserted, collections.abc.Iterable)

    from collections.abc import Collection, Iterable, KeysView

    match = create_function_match_asserter(is_iterable)
    match(Collection)
    match(Iterable)
    match(KeysView)
    match(10)


def test_match_sequence():
    def where_is_0(items):
        match items:
            case [0, 0]:
                return 2
            case [0, _]:
                return 0
            case [_, 0]:
                return 1
            case [_, _]:
                return -1
            case _:
                raise ValueError()

    match = create_function_match_asserter(where_is_0)
    match((0, 0))
    match((1, 0))
    match((0, 1))
    match((1, 1))
    match(10)


def test_match_mapping():
    def where_is_0(items):
        match items:
            case {"a": 0, "b": 0}:
                return "ab"
            case {"a": 0}:
                return "a"
            case {"b": 0}:
                return "b"
            case {}:
                return ""
            case _:
                raise ValueError()

    match = create_function_match_asserter(where_is_0)
    match({"a": 0, "b": 0})
    match({"a": 0, "b": 1})
    match({"a": 1, "b": 0})
    match({"a": 0})
    match(10)


def test_match_or():
    def is_even(number):
        match number:
            case 0 | 2 | 4 | 8:
                return True
            case 1 | 3 | 5 | 7 | 9:
                return False
            case _:
                raise ValueError("Value is not a true number!")

    match = create_function_match_asserter(is_even)
    for i in range(11):
        match(i)


def test_match_guard():
    def is_even(number):
        match number:
            case int(num) if num % 2 == 0:
                return True
            case int(num) if num % 2 == 1:
                return False
            case _:
                raise ValueError("Value is not an integer!")

    match = create_function_match_asserter(is_even)
    for i in range(11):
        match(i)


def test_match_class():
    class Job:
        name: str
        duration: int
        __match_args__ = ("name", "duration")

        def __init__(self, name: str, duration: int):
            self.name = name
            self.duration = duration

    def department(job):
        match job:
            case Job("Programmer"):
                return "IT"
            case Job("Supervisor", 1):
                return "Onsite"
            case Job(name="Supervisor"):
                return "Management"
            case Job(duration=duration, name="CEO"):
                return f"Executive {duration}"
            case _:
                raise ValueError("Could not find department for job")

    match = create_function_match_asserter(department)
    match(Job("Programmer", 10))
    match(Job("Supervisor", 1))
    match(Job("Supervisor", 2))
    match(Job("CEO", 2))
    match(10)


def test_match_literal():
    def function(query):
        match query:
            case int(10):
                return "int 10"
            case int():
                return "int"
            case str("name"):
                return "str name"
            case str():
                return "str"
            case _:
                raise TypeError()

    match = create_function_match_asserter(function)
    match(10)
    match(1)
    match("name")
    match("str")
    match(set())


def test_inner_class():
    def test(value):
        class C:
            age = value

        return C.age

    match = create_function_match_asserter(test)
    match(10)


def test_inner_class_with_decorator():
    def decorator(cls):
        cls.age = 10

    def test():
        @decorator
        class C:
            pass

        return C.age

    match = create_function_match_asserter(test)
    match()


def test_inner_class_custom_meta():
    class MyMeta(type):
        @classmethod
        def __prepare__(cls, *args, **kwargs):
            return {"age": 10}

    def test(value):
        class C(metaclass=MyMeta):
            pass

        return C.age

    match = create_function_match_asserter(test)
    match(10)


def test_inner_class_global_then_local():
    def test(value):
        class C:
            a = len(value)
            len = 3
            b = len + 1

        return C.a + C.b

    match = create_function_match_asserter(test)
    match([1, 2, 3])
