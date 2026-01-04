package io.github.cdisvm.compiler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

public class CDisClassLoader extends ClassLoader {
    private final Map<String, byte[]> classNameToBytecode;

    public CDisClassLoader() {
        super(Thread.currentThread().getContextClassLoader());
        classNameToBytecode = new LinkedHashMap<>();
    }

    public void dumpClasses(Path dumpLocation) {
        for (var entry : classNameToBytecode.entrySet()) {
            var parts = Arrays.asList(entry.getKey().split("\\."));
            var entryPath = dumpLocation.resolve(Paths.get(parts.getFirst(), parts.subList(1, parts.size()).toArray(new String[0])));
            var filePath = entryPath.resolveSibling(entryPath.getFileName() + ".class");
            try {
                Files.createDirectories(entryPath.getParent());
                Files.write(filePath, entry.getValue());
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public boolean isClassDefined(String className) {
        return classNameToBytecode.containsKey(className);
    }

    public void registerClass(String name, byte[] bytecode) {
        classNameToBytecode.put(name, bytecode);
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        var bytecode = classNameToBytecode.get(name);
        if (bytecode == null) {
            return super.findClass(name);
        }
        return defineClass(name, bytecode, 0, bytecode.length);
    }
}
