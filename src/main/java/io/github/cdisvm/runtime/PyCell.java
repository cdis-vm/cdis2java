package io.github.cdisvm.runtime;

public final class PyCell {
    public static final String CELL_PACKAGE = "io.github.cdisvm.codegen.cell";
    public static final String CELL_FIELD_NAME = "cell";

    private final long cellId;
    private PyObject value;

    /**
     * Anonymous cell, scoped to function call
     */
    public PyCell() {
        this.cellId = -1L;
        this.value = null;
    }

    public PyCell(long cellId, PyObject value) {
        this.cellId = cellId;
        this.value = value;
    }

    public long getCellId() {
        return cellId;
    }

    public String getClassName() {
        return getClassName(cellId);
    }

    public static String getClassName(long cellId) {
        return "%s.PyCell%d".formatted(CELL_PACKAGE, cellId);
    }

    public PyObject getValue() {
        return value;
    }

    public void setValue(PyObject value) {
        this.value = value;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof PyCell pyCell))
            return false;
        return cellId == pyCell.cellId;
    }

    @Override
    public int hashCode() {
        return Long.hashCode(cellId);
    }
}
