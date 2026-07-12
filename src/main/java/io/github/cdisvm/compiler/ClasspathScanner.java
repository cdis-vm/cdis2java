package io.github.cdisvm.compiler;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.jar.JarFile;

import io.github.cdisvm.runtime.PyObject;

public final class ClasspathScanner {
    public static Class<?>[] getRuntimeClasses() {
        try {
            return getClasses(PyObject.class.getPackageName());
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Scans all classes accessible from the context class loader which belong to the given package and subpackages.
     *
     * @param packageName The base package
     * @return The classes
     * @throws ClassNotFoundException
     * @throws IOException
     */
    private static Class<?>[] getClasses(String packageName)
            throws ClassNotFoundException, IOException {
        var classLoader = PyObject.class.getClassLoader();
        var path = packageName.replace('.', '/');
        var resources = classLoader.getResources(path);
        var dirs = new ArrayList<File>();
        var classes = new ArrayList<Class<?>>();
        var visitedJars = new HashSet<String>();
        while (resources.hasMoreElements()) {
            var resource = resources.nextElement();
            if (resource.getFile().contains(".jar!/io/github/cdisvm/")) {
                var pathOnSystem = resource.getFile().substring(5, resource.getPath().lastIndexOf(".jar!/io/github/cdisvm/") + 4);
                if (visitedJars.add(pathOnSystem)) {
                    addClassesFromJar(pathOnSystem, classes);
                }
            } else {
                dirs.add(new File(resource.getFile()));
            }
        }
        for (var directory : dirs) {
            classes.addAll(findClasses(directory, packageName));
        }

        return classes.toArray(new Class[0]);
    }

    /**
     * Recursive method used to find all classes in a given directory and subdirs.
     *
     * @param directory   The base directory
     * @param packageName The package name for classes found inside the base directory
     * @return The classes
     * @throws ClassNotFoundException
     */
    private static List<Class<?>> findClasses(File directory, String packageName) throws ClassNotFoundException {
        var classes = new ArrayList<Class<?>>();
        if (!directory.exists()) {
            return classes;
        }
        var fileList = directory.listFiles();
        for (var file : fileList) {
            if (file.isDirectory()) {
                assert !file.getName().contains(".");
                classes.addAll(findClasses(file, "%s.%s".formatted(packageName, file.getName())));
            } else if (file.getName().endsWith(".class")) {
                classes.add(PyObject.class.getClassLoader().loadClass(
                        "%s.%s".formatted(packageName, file.getName().substring(0, file.getName().length() - 6))));
            }
        }
        return classes;
    }

    private static void addClassesFromJar(String jarFile, List<Class<?>> classes) {
        try (var jar = new JarFile(jarFile)) {
            var enumEntries = jar.entries();
            while (enumEntries.hasMoreElements()) {
                var file = enumEntries.nextElement();
                if (file.isDirectory()) { // if its a directory, create it
                    continue;
                }
                if (file.getName().endsWith(".class")) {
                    classes.add(PyObject.class.getClassLoader().loadClass(
                            file.getName().substring(0, file.getName().length() - 6).replace('/', '.')
                    ));
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }
}
