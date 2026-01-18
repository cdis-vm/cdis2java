package io.github.cdisvm.runtime;

import java.util.List;
import java.util.Map;

import io.github.cdisvm.runtime.builtin.PyStr;

public interface PyCallBuilder {
    // No possible conflict; it takes no arguments, whereas the other
    // interfaces take 1 argument
    PyObject pyCall();

    // These use $ since they can conflict with other interfaces otherwise
    PyCallBuilder $appendArgument(PyObject argument);
    PyCallBuilder $putArgument(String argumentName, PyObject argument);

    default PyCallBuilder $extendArguments(Iterable<PyObject> arguments) {
        for (var argument : arguments) {
            $appendArgument(argument);
        }
        return this;
    }

    default PyCallBuilder $mergeArguments(Map<PyObject, PyObject> arguments) {
        for (var argument : arguments.entrySet()) {
            var key = argument.getKey();
            var value = argument.getValue();
            if (key instanceof PyStr str) {
                $putArgument(str.toString(), value);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return this;
    }
}
