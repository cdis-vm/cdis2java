import cdis2java as c2j

class A:
    pass


class B:
    x: A
    y: list[A]


def test_customizer():
    def customize(customizer):
        customizer.add_type_annotations({
            'annotationType': 'java.lang.Deprecated',
            'since': '2.0.0'
        })
        customizer.add_getter_setter('x', c2j.lookup_class(A),
                                     {
                                         'annotationType': 'java.lang.Deprecated',
                                         'since': '1.0.0'
                                     })
        customizer.add_list_getter_setter('y', c2j.lookup_class(A),
                                     {
                                         'annotationType': 'java.lang.Deprecated',
                                         'since': '1.0.0'
                                     })

    c2j.customize_class(B, customize)
    c2j.compiler.dumpClasses()


def test_to_java_interface():
    def predicate(x):
        return x > 5

    java_predicate = c2j.as_interface(predicate, "java.util.function.IntPredicate")
    assert java_predicate.test(1) is False
    assert java_predicate.test(6) is True
    c2j.compiler.dumpClasses()


def test_export_jar():
    def predicate(x):
        return x > 5

    c2j.as_interface(predicate, "java.util.function.IntPredicate")
    c2j.export_jar("target/test.jar")