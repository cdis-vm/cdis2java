import cdis2java as c2j
from cdis2java import compile_function, java_value, py_value


def _bootstrap():
    # Make sure the JVM and the compiler are initialized
    def bootstrap_function():
        return 1

    compile_function(bootstrap_function)


def test_equals_delegates_to_eq():
    _bootstrap()

    class Point:
        def __init__(self, x, y):
            self.x = x
            self.y = y

        def __eq__(self, other):
            return self.x == other.x and self.y == other.y

    j1 = java_value(Point(1, 2))
    j1b = java_value(Point(1, 2))
    j2 = java_value(Point(3, 4))

    assert j1.equals(j1b) is True
    assert j1.equals(j2) is False
    assert j1.equals(None) is False
    assert j1.equals(42) is False


def test_hashcode_delegates_to_hash():
    _bootstrap()

    class Point:
        def __init__(self, x, y):
            self.x = x
            self.y = y

        def __hash__(self):
            return self.x * 10000 + self.y

    j1 = java_value(Point(1, 2))
    j1b = java_value(Point(1, 2))
    j2 = java_value(Point(3, 4))

    assert j1.hashCode() == 10002
    assert j1.hashCode() == j1b.hashCode()
    assert j1.hashCode() != j2.hashCode()


def test_compareto_delegates_to_comparison_dunders():
    _bootstrap()

    class Point:
        def __init__(self, x, y):
            self.x = x
            self.y = y

        def __eq__(self, other):
            return self.x == other.x and self.y == other.y

        def __lt__(self, other):
            return self.x < other.x or (self.x == other.x and self.y < other.y)

        def __gt__(self, other):
            return self.x > other.x or (self.x == other.x and self.y > other.y)

    j1 = java_value(Point(1, 2))
    j1b = java_value(Point(1, 2))
    j2 = java_value(Point(3, 4))

    assert j1.compareTo(j1b) == 0
    assert j1.compareTo(j2) == -1
    assert j2.compareTo(j1) == 1


def test_compareto_uses_le_and_ge_when_defined():
    _bootstrap()

    class LeGeValue:
        def __init__(self, value):
            self.value = value

        def __le__(self, other):
            return self.value <= other.value

        def __ge__(self, other):
            return self.value >= other.value

    j1 = java_value(LeGeValue(1))
    j1b = java_value(LeGeValue(1))
    j2 = java_value(LeGeValue(2))

    assert j1.compareTo(j1b) == 0
    assert j1.compareTo(j2) == -1
    assert j2.compareTo(j1) == 1


def test_compiled_comparison_uses_instance_interfaces():
    _bootstrap()

    class Point:
        def __init__(self, x, y):
            self.x = x
            self.y = y

        def __eq__(self, other):
            return self.x == other.x and self.y == other.y

        def __lt__(self, other):
            return self.x < other.x or (self.x == other.x and self.y < other.y)

    def points_equal(a, b):
        return a == b

    def points_lt(a, b):
        return a < b

    equal_function = compile_function(points_equal)
    lt_function = compile_function(points_lt)

    j1 = java_value(Point(1, 2))
    j1b = java_value(Point(1, 2))
    j2 = java_value(Point(3, 4))

    call_builder = equal_function.pyCallBuilder()
    getattr(call_builder, '$appendArgument')(j1)
    getattr(call_builder, '$appendArgument')(j1b)
    assert py_value(equal_function.pyCall(call_builder)) is True

    call_builder = equal_function.pyCallBuilder()
    getattr(call_builder, '$appendArgument')(j1)
    getattr(call_builder, '$appendArgument')(j2)
    assert py_value(equal_function.pyCall(call_builder)) is False

    call_builder = lt_function.pyCallBuilder()
    getattr(call_builder, '$appendArgument')(j1)
    getattr(call_builder, '$appendArgument')(j2)
    assert py_value(lt_function.pyCall(call_builder)) is True

    call_builder = lt_function.pyCallBuilder()
    getattr(call_builder, '$appendArgument')(j2)
    getattr(call_builder, '$appendArgument')(j1)
    assert py_value(lt_function.pyCall(call_builder)) is False


def test_py_interfaces_implemented_for_dunder_methods():
    _bootstrap()

    PyHashable = c2j._jclass("io.github.cdisvm.runtime.PyHashable")
    PyHasEquals = c2j._jclass("io.github.cdisvm.runtime.comparison.PyHasEquals")
    PyHasLessThan = c2j._jclass("io.github.cdisvm.runtime.comparison.PyHasLessThan")
    PyHasGreaterThan = c2j._jclass("io.github.cdisvm.runtime.comparison.PyHasGreaterThan")
    PyHasNotEquals = c2j._jclass("io.github.cdisvm.runtime.comparison.PyHasNotEquals")

    class Point:
        def __init__(self, x, y):
            self.x = x
            self.y = y

        def __eq__(self, other):
            return self.x == other.x and self.y == other.y

        def __ne__(self, other):
            return not (self.x == other.x and self.y == other.y)

        def __lt__(self, other):
            return self.x < other.x or (self.x == other.x and self.y < other.y)

        def __gt__(self, other):
            return self.x > other.x or (self.x == other.x and self.y > other.y)

        def __hash__(self):
            return self.x * 10000 + self.y

    j1 = java_value(Point(1, 2))
    j2 = java_value(Point(3, 4))

    assert isinstance(j1, PyHashable)
    assert isinstance(j1, PyHasEquals)
    assert isinstance(j1, PyHasLessThan)
    assert isinstance(j1, PyHasGreaterThan)
    assert isinstance(j1, PyHasNotEquals)

    assert j1.pyEquals(j2).value() is False
    assert j1.pyEquals(java_value(Point(1, 2))).value() is True
    assert j1.pyLessThan(j2).value() is True
    assert j1.pyGreaterThan(j2).value() is False
    assert j1.pyNotEquals(j2).value() is True
    assert j1.pyHash().intValue() == 10002


def test_without_dunders_uses_identity_semantics():
    _bootstrap()

    class Plain:
        def __init__(self, value):
            self.value = value

    p1 = java_value(Plain(1))
    p1b = java_value(Plain(1))

    assert p1.equals(p1b) is False
    assert p1.equals(p1) is True

    declared_methods = [m.getName() for m in p1.getClass().getDeclaredMethods()]
    assert "equals" not in declared_methods
    assert "hashCode" not in declared_methods
    assert "compareTo" not in declared_methods
    assert "pyEquals" not in declared_methods
    assert "pyHash" not in declared_methods
