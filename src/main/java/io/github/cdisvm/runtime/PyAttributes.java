package io.github.cdisvm.runtime;

public interface PyAttributes {
    PyObject getAttributeByName(String name);
    void setAttributeByName(String name, PyObject value);
    void deleteAttributeByName(String name);
}
