import cdis2java as c2j

class A:
    pass


class B:
    x: A


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

    c2j.customize_class(B, customize)
    c2j.compiler.dumpClasses()
