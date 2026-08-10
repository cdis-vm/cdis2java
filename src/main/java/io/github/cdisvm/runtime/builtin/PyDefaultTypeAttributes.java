package io.github.cdisvm.runtime.builtin;

import java.util.LinkedHashMap;
import java.util.Map;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.exception.PyAttributeError;

public class PyDefaultTypeAttributes implements PyAttributes {
    private Map<String, PyObject> attributeToValue;

    public PyDefaultTypeAttributes() {}

    @Override
    public PyObject getAttributeByName(String name) {
        if (attributeToValue == null) {
            throw new PyAttributeError("object does not have attribute '%s'".formatted(name));
        }
        // TODO: descriptor protocol
        var out = attributeToValue.get(name);
        if (out == null) {
            throw new PyAttributeError("object does not have attribute '%s'".formatted(name));
        }
        return out;
    }

    @Override
    public void setAttributeByName(String name, PyObject value) {
        if (attributeToValue == null) {
            attributeToValue = new LinkedHashMap<>();
        }
        // TODO: descriptor protocol
        attributeToValue.put(name, value);
    }

    @Override
    public void deleteAttributeByName(String name) {
        if (attributeToValue == null) {
            return;
        }
        // TODO: descriptor protocol
        attributeToValue.remove(name);
    }
}
