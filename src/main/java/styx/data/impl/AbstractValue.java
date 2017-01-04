package styx.data.impl;

import styx.data.Kind;
import styx.data.Value;

abstract class AbstractValue implements Value {

    /**
     * Subclasses must provide a value-based implementation consistent with equals() and hashCode()
     */
    @Override
    public abstract String toString();

    @Override
    public boolean equals(Object other) {
        if(other instanceof Value) {
            return compareTo((Value) other) == 0;
        } else {
            return false;
        }
    }

    @Override
    public int hashCode() {
        return toString().hashCode();
    }

    protected static int compare(Kind a, Kind b) {
        return Integer.compare(a.ordinal(), b.ordinal());
    }
}
