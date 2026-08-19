package io.github.cdisvm.runtime;

import java.util.List;
import java.util.Map;

import io.github.cdisvm.runtime.builtin.PyStr;

public interface PyCallBuilder {
    // No possible conflict; it takes no arguments, whereas the other
    // interfaces take 1 argument
    PyObject pyCall();

    // These use $ since they can conflict with other interfaces otherwise
    PyCallBuilder $bindTo(PyObject binding);
    PyCallBuilder $returning(PyObject value);
    PyCallBuilder $appendArgument(PyObject argument);
    PyCallBuilder $putArgument(String argumentName, PyObject argument);

    // In order to support vargs, a vargs function must implement all positional interfaces
    // Since this is impossible in general, we only have dedicated methods for the first
    // 8 parameters, with the rest being set via append
    int MAX_POSITIONAL_ARG_METHOD = 8;
    default PyCallBuilder $0(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $1(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $2(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $3(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $4(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $5(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $6(PyObject argument) { return $appendArgument(argument); }
    default PyCallBuilder $7(PyObject argument) { return $appendArgument(argument); }

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
