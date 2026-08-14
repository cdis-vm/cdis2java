package io.github.cdisvm.runtime.descriptor;

public sealed interface PyDataDescriptor extends PyDescriptor permits PySetDescriptor, PyDeleteDescriptor {
}
