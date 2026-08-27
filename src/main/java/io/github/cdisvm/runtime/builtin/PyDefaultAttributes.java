package io.github.cdisvm.runtime.builtin;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.exception.PyAttributeError;

public class PyDefaultAttributes implements PyAttributes {
    private Map<String, PyObject> attributeToValue;

    public PyDefaultAttributes() {}

    public PyDefaultAttributes(Map<String, PyObject> attributeToValue) {
        this.attributeToValue = attributeToValue;
    }

    @Override
    public Collection<String> attributeNames() {
        if (attributeToValue == null) {
            return Collections.emptyList();
        }
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
