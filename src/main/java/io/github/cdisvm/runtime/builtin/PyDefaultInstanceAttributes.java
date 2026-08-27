package io.github.cdisvm.runtime.builtin;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import io.github.cdisvm.runtime.PyAttributes;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.descriptor.PyDataDescriptor;
import io.github.cdisvm.runtime.descriptor.PyDescriptor;
import io.github.cdisvm.runtime.descriptor.PyGetDescriptor;
import io.github.cdisvm.runtime.exception.PyAttributeError;

public class PyDefaultInstanceAttributes implements PyAttributes {
    private final PyObject instance;
    private final PyAttributes typeAttributes;
    private Map<String, PyObject> attributeToValue;

    public PyDefaultInstanceAttributes(PyObject instance, PyAttributes typeAttributes) {
        this.instance = instance;
        this.typeAttributes = typeAttributes;
    }

    public PyObject instance() {
        return instance;
    }

    public PyAttributes typeAttributes() {
        return typeAttributes;
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
        var typeAttribute = typeAttributes.getAttributeByNameOrNull(name);
        if (typeAttribute instanceof PyDataDescriptor || attributeToValue == null) {
            if (typeAttribute instanceof PyGetDescriptor getter) {
                return getter.pyGet(instance, instance.pyType());
            }
        }
        if (attributeToValue == null) {
            return null;
        }
        var out = attributeToValue.get(name);
        if (out == null) {
            if (typeAttribute instanceof PyGetDescriptor getter) {
                return getter.pyGet(instance, instance.pyType());
            }
            return typeAttribute;
        }
        return out;
    }

    @Override
    public void setAttributeByName(String name, PyObject value) {
        // TODO: descriptor protocol
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
        // TODO: descriptor protocol
        attributeToValue.remove(name);
    }
}
