package styx.data.impl.mem;
import static styx.data.Values.complex;
import static styx.data.Values.pair;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

import styx.data.Complex;
import styx.data.InvalidAccessException;
import styx.data.Pair;
import styx.data.Value;

/**
 * A mutable object, identified by a reference.
 */
class MemoryObject {

    /**
     * The value must be queried from the parent (lazy initialization, top down).
     * <p>
     * This flag is never set for the root object.
     */
    private final static int FLAG_PARENT = 0x01;

    /**
     * The value must be updated by replacing children which have FLAG_MODIFIED set (lazy initialization, bottom up).
     * <p>
     * This flag can occur together with FLAG_PARENT.
     * In this case, the parent has to be queried first, then children have to be replaced.
     */
    private final static int FLAG_CHILD = 0x02;

    /**
     * Used when a parent has FLAG_CHILD set to mark modified children.
     * <p>
     * The flag is set when the parent's onModifiedChild() is called and is cleared when the parent is lazily initialized.
     * This flag is never set for the root object.
     */
    private final static int FLAG_MODIFIED = 0x04;

    /**
     * The parent object, never null except for root.
     */
    private final MemoryObject parent;

    /**
     * The last part of the reference of this object, never null except for root.
     */
    private final Value key;

    /**
     * The value of the object, null if there is no value or if FLAG_PARENT is set.
     */
    private Value value;

    /**
     * The current state of the object's value.
     */
    private int flags;

    /**
     * The map of child references, null if none.
     * <p>
     * The map can be non-empty for non-complex values (but child references never have values in this case).
     */
    private Map<Value, MemoryObject> children;

    /**
     * Constructs a new root object.
     *
     * @param value the value of the object, can be null.
     */
    MemoryObject(Value value) {
        this.parent = null;
        this.key = null;
        this.value = value;
        this.flags = 0;
    }

    /**
     * Constructs a new non-root object.
     *
     * @param parent the parent object, must not be null.
     * @param key last part of the reference of this object, must not be null.
     */
    private MemoryObject(MemoryObject parent, Value key) {
        this.parent = Objects.requireNonNull(parent);
        this.key = Objects.requireNonNull(key);
        this.value = null;
        this.flags = FLAG_PARENT;
    }

    MemoryObject child(Value key) {
        Objects.requireNonNull(key);
        if(children == null) {
            children = new HashMap<>();
        }
        MemoryObject child = children.get(key);
        if(child == null) {
            child = new MemoryObject(this, key);
            children.put(key, child);
        }
        return child;
    }

    Stream<Pair> browse() {
        Value value = read();
        if(value == null) {
            throw new InvalidAccessException("Attempt to browse children of a non-existing value.");
        } else if(!value.isComplex()) {
            throw new InvalidAccessException("Attempt to browse children of a non-complex value.");
        } else {
            return value.asComplex().entries().
                    map(p -> pair(p.key(), p.value().isComplex() ? complex() : p.value()));
        }
    }

    /**
     * Reads (gets) the value of the object.
     *
     * @return the value of the object, null if not existing.
     */
    Value read() {
        // If FLAG_PARENT is set, query the value from the parent and clear the flag.
        if((flags & FLAG_PARENT) != 0) {
            value = parent.getChild(key);
            flags &= ~FLAG_PARENT;
        }
        // If FLAG_CHILD is set, replace children which have FLAG_MODIFIED set and
        // clear FLAG_MODIFIED from children and FLAG_CHILD from this object.
        if((flags & FLAG_CHILD) != 0) {
            if(value == null || !value.isComplex()) {
                throw new IllegalStateException(); // should not happen: previous write to non-existing child.
            }
            Complex valc = value.asComplex(); // never called for non-complex values.
            for(MemoryObject child : children.values()) {
                if((child.flags & FLAG_MODIFIED) != 0) {
                    valc = valc.put(child.key, child.read());
                    child.flags &= ~FLAG_MODIFIED;
                }
            }
            value = valc;
            flags &= ~FLAG_CHILD;
        }
        return value;
    }

    /**
     * Writes (sets) the value of the object.
     *
     * @param value value for the object, null if to be removed.
     * @throws InvalidAccessException if the parent is non-existing or non-complex.
     */
    void write(Value value) {
        // Writing is valid only if the parent is complex.
        if(/*value != null &&*/ parent != null) {
            parent.ensureComplex();
        }
        // Propagate changes up (if not root).
        // This will set FLAG_CHILD on parents and FLAG_MODIFIED on this object and on parents (recursively).
        if(parent != null) {
            parent.onModifiedChild(this);
            flags |= FLAG_MODIFIED;
        }
        // Propagate changes down (if any children).
        // This will set FLAG_PARENT and clear FLAG_CHILD or FLAG_MODIFIED on all children (recursively).
        if(children != null) {
            for(MemoryObject child : children.values()) {
                child.onModifiedParent();
            }
        }
        this.value = value;
        flags &= ~(FLAG_PARENT | FLAG_CHILD); // clear both flags
    }

    /**
     * Ensures that the object has a complex value.
     *
     * @throws InvalidAccessException if the object does not have a value or a non-complex value.
     */
    private void ensureComplex() {
        // If FLAG_PARENT is set, query the value from the parent and clear the flag.
        if((flags & FLAG_PARENT) != 0) {
            value = parent.getChild(key);
            flags &= ~FLAG_PARENT;
        }
        if(value == null) {
            throw new InvalidAccessException("Attempt to write a child of a non-existing value.");
        } else if(!value.isComplex()) {
            throw new InvalidAccessException("Attempt to write a child of a non-complex value.");
        }
    }

    /**
     * Returns the value of the child with the given key.
     *
     * @param key the last part of the reference of the child, never null.
     * @return the value of the child, can be null.
     */
    private Value getChild(Value key) {
        // If FLAG_PARENT is set, query the value from the parent and clear the flag.
        if((flags & FLAG_PARENT) != 0) {
            value = parent.getChild(this.key);
            flags &= ~FLAG_PARENT;
        }
        if(value == null || !value.isComplex()) {
            return null;
        } else {
            return value.asComplex().get(key).orElse(null);
        }
    }

    /**
     * Indicates that a value has been written to an ancestor object.
     */
    private void onModifiedParent() {
        // If FLAG_PARENT is not set, propagate down and set it.
        // If FLAG_CHILD or FLAG_MODIFIED are set, they become obsolete and are cleared.
        if((flags & FLAG_PARENT) == 0) {
            if(children != null) {
                for(MemoryObject child : children.values()) {
                    child.onModifiedParent();
                }
            }
            value = null;
            flags = FLAG_PARENT; // clear FLAG_CHILD and FLAG_MODIFIED
        }
    }

    /**
     * Indicates that a value has been written to a descendant object.
     *
     * @param child never null.
     */
    private void onModifiedChild(MemoryObject child) {
        // If FLAG_CHILD is not set, propagate up and set it (recursively).
        // If FLAG_PARENT is set, it still applies and is left unchanged.
        if((flags & FLAG_CHILD) == 0) {
            if(parent != null) {
                parent.onModifiedChild(this);
                flags |= FLAG_MODIFIED;
            }
            flags |= FLAG_CHILD;
        }
    }
}
