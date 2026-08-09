package io.github.cdisvm.runtime.builtin;

import java.util.ArrayList;
import java.util.List;
import java.util.PrimitiveIterator;

import io.github.cdisvm.runtime.PyIndexable;
import io.github.cdisvm.runtime.PyObject;
import io.github.cdisvm.runtime.PySizable;
import io.github.cdisvm.runtime.PyType;
import io.github.cdisvm.runtime.annotation.PyBuiltin;
import io.github.cdisvm.runtime.annotation.PyConstructor;
import io.github.cdisvm.runtime.annotation.PyDefault;

@PyBuiltin("slice")
public record PySlice(PyIndexable start, PyIndexable end, PyIndexable step) implements PyObject {
    public static PyType type;

    public PyType pyType() {
        return type;
    }

    @PyConstructor
    public static PySlice of(
            @PyDefault(type=PyDefault.Type.NULL, value="") PyObject start,
            @PyDefault(type=PyDefault.Type.NULL, value="") PyObject end,
            @PyDefault(type=PyDefault.Type.NULL, value="") PyObject step) {
        if (start == PyNone.INSTANCE) {
            start = null;
        }
        if (end == PyNone.INSTANCE) {
            end = null;
        }
        if (step == PyNone.INSTANCE) {
            step = null;
        }
        return new PySlice((PyIndexable) start, (PyIndexable) end, (PyIndexable) step);
    }

    public PrimitiveIterator.OfInt getIndices(PySizable sizable) {
        return getIndices(sizable.pyLength().intValue());
    }

    public PrimitiveIterator.OfInt getIndices(int containerSize) {
        int startIndex, endIndex, stepSize;

        if (step == null) {
            stepSize = 1;
        } else {
            stepSize = step.pyIndex().intValue();
        }

        if (start == null) {
            if (stepSize > 0) {
                startIndex = 0;
            } else {
                startIndex = containerSize - 1;
            }
        } else {
            startIndex = start.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = containerSize - startIndex;
            }
        }

        if (end == null) {
            if (stepSize > 0) {
                endIndex = containerSize;
            } else {
                endIndex = 0;
            }
        } else {
            endIndex = end.pyIndex().intValue();
            if (endIndex < 0) {
                endIndex = containerSize - endIndex + 1;
            }
        }

        return new SliceIterator(startIndex, endIndex, stepSize);
    }

    public <T> List<T> copySliceFromList(List<T> source) {
        if (step == null || step.pyIndex().intValue() == 1) {
            var startIndex = (start == null)? 0 :  start.pyIndex().intValue();
            var endIndex = (end == null)? source.size() :  end.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = source.size() - startIndex;
            }
            if (endIndex < 0) {
                endIndex = source.size() - endIndex;
            }
            return new ArrayList<>(source.subList(startIndex, endIndex));
        }
        if (step.pyIndex().intValue() == -1) {
            var startIndex = (end == null)? 0 :  end.pyIndex().intValue();
            var endIndex = (start == null)? source.size() :  start.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = source.size() - startIndex;
            }
            if (endIndex < 0) {
                endIndex = source.size() - endIndex;
            }
            return new ArrayList<>(source.subList(startIndex, endIndex).reversed());
        }
        var sliceIterator = getIndices(source.size());
        var out = new ArrayList<T>();
        while (sliceIterator.hasNext()) {
            out.add(source.get(sliceIterator.next()));
        }
        return out;
    }

    public <T> List<T> copyImmutableSliceFromList(List<T> source) {
        if (step == null || step.pyIndex().intValue() == 1) {
            var startIndex = (start == null)? 0 :  start.pyIndex().intValue();
            var endIndex = (end == null)? source.size() :  end.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = source.size() - startIndex;
            }
            if (endIndex < 0) {
                endIndex = source.size() - endIndex;
            }
            return source.subList(startIndex, endIndex);
        }
        if (step.pyIndex().intValue() == -1) {
            var startIndex = (end == null)? 0 :  end.pyIndex().intValue();
            var endIndex = (start == null)? source.size() :  start.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = source.size() - startIndex;
            }
            if (endIndex < 0) {
                endIndex = source.size() - endIndex;
            }
            return source.subList(startIndex, endIndex).reversed();
        }
        var sliceIterator = getIndices(source.size());
        var out = new ArrayList<T>();
        while (sliceIterator.hasNext()) {
            out.add(source.get(sliceIterator.next()));
        }
        return out;
    }

    public String copySliceFromString(String source) {
        if (step == null || step.pyIndex().intValue() == 1) {
            var startIndex = (start == null)? 0 :  start.pyIndex().intValue();
            var endIndex = (end == null)? source.length() :  end.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = source.length() - startIndex;
            }
            if (endIndex < 0) {
                endIndex = source.length() - endIndex;
            }
            return source.substring(startIndex, endIndex);
        }
        if (step.pyIndex().intValue() == -1) {
            var startIndex = (end == null)? 0 :  end.pyIndex().intValue();
            var endIndex = (start == null)? source.length() :  start.pyIndex().intValue();
            if (startIndex < 0) {
                startIndex = source.length() - startIndex;
            }
            if (endIndex < 0) {
                endIndex = source.length() - endIndex;
            }
            return new StringBuilder(source.substring(startIndex, endIndex)).reverse().toString();
        }
        var sliceIterator = getIndices(source.length());
        var out = new StringBuilder();
        while (sliceIterator.hasNext()) {
            out.append(source.charAt(sliceIterator.next()));
        }
        return out.toString();
    }

    private static class SliceIterator implements PrimitiveIterator.OfInt {
        private int current;
        private final int stepSize;
        private final int end;
        private final int stepSignNum;

        public SliceIterator(int start, int end, int stepSize) {
            this.current = start;
            this.end = end;
            this.stepSize = stepSize;
            this.stepSignNum = (stepSize > 0)? 1 : -1;
        }

        @Override
        public int nextInt() {
            var out = current;
            current += stepSize;
            return out;
        }

        @Override
        public boolean hasNext() {
            return stepSignNum * current < stepSignNum * end;
        }
    }
}
