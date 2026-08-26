package io.github.cdisvm.runtime.builtin;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.exception.PyAttributeError;

public class PyDefaultTypeAttributes implements PyAttributes {
    private Map<String, PyObject> attributeToValue;

    public PyDefaultTypeAttributes() {}

    @Override
    public Collection<String> attributeNames() {
        return attributeToValue.keySet();
    }

    @Override
    public PyObject getAttributeByNameOrNull(String name) {
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
