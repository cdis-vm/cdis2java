package io.github.cdisvm.runtime.builtin;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;

public class PyDefaultAttributes implements PyAttributes {
    private Map<String, PyObject> attributeToValue;

    public PyDefaultAttributes() {}

    @Override
    public PyObject getAttributeByName(String name) {
        if (attributeToValue == null) {
            return null;
        }
        return attributeToValue.get(name);
    }

    @Override
    public void setAttributeByName(String name, PyObject value) {
        if (attributeToValue == null) {
            attributeToValue = new LinkedHashMap<>();
        }
        attributeToValue.put(name, value);
    }

    @Override
    public void deleteAttributeByName(String name) {
        if (attributeToValue == null) {
            return;
        }
        attributeToValue.remove(name);
    }
}
