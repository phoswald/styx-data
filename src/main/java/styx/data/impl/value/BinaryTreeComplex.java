package styx.data.impl.value;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Optional;

import styx.data.Complex;
import styx.data.Kind;
import styx.data.Pair;
import styx.data.Value;

/**
 * An implementation of a complex value as an immutable, balanced binary tree.
 * <p>
 * Users never create instances directly. Instead, trees can be constructed by starting with the
 * empty node (which is exposed by the public static field EMPTY) and using the put() and putAll()
 * or add() and addAll() methods to insert entries.
 * <p>
 * Note that instances of this class can be regarded as a whole tree (i.e. as a complex value)
 * or as a node of the tree (i.e. as a key/value-pair) at the same time.
 * <p>
 * For this reason, this class also implements the Pair interface, which is used when iterating
 * or streaming over a complex value's entries. The Pair interface, however, should be regarded
 * as an implementation detail, which is not directly exposed.
 * <p>
 * <u>Important</u>: The Pair interface requires its key and value to be non-null, therefore the
 * node for the empty tree (which is also referenced from every unused left or right pointer of
 * every regular tree node) must not be regarded as a pair. The iterator implementation is
 * consistent with this restriction: The empty node is never retured when iterating or streaming
 * over a complex value's entries.
 */
public class BinaryTreeComplex extends AbstractValue implements Complex, Pair {

    /** the starting point for working with complex values */
    public static final Complex EMPTY = new BinaryTreeComplex();

    /** the key of this node, never null except for empty node */
    private final Value key;

    /** the value of this node, never null except for empty node */
    private final Value value;

    /** the left subtree, never null but points to empty node if not used */
    private final BinaryTreeComplex left;

    /** the right subtree, never null but points to empty node if not used */
    private final BinaryTreeComplex right;

    /** the height of the tree: 0 if empty, 1 if containing one key/value-pair, ... */
    private final int height;

    private BinaryTreeComplex() {
        this.key = null;
        this.value = null;
        this.left = this;
        this.right = this;
        this.height = 0;
    }

    private BinaryTreeComplex(Value key, Value value, BinaryTreeComplex left, BinaryTreeComplex right) {
        this.key = Objects.requireNonNull(key);
        this.value = Objects.requireNonNull(value);
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
        this.height = 1 + Math.max(left.height, right.height);
    }

    @Override
    public Value key() {
        return Objects.requireNonNull(key);
    }

    @Override
    public Value value() {
        return Objects.requireNonNull(value);
    }

    private boolean isEmpty() {
        return key == null;
    }

    private int balance() {
        return right.height - left.height;
    }

    @Override
    public int compareTo(Value other) {
        if(other.isComplex()) {
            return compare(this, other.asComplex());
        } else {
            return compare(kind(), other.kind());
        }
    }

    @Override
    public Kind kind() {
        return Kind.COMPLEX;
    }

    @Override
    public Iterator<Pair> iterator() {
        return new TreeIterator(this);
    }

    @Override
    public Optional<Value> get(Value key) {
        if(key == null) {
            throw new IllegalArgumentException("The key must not be null.");
        }
        return get(this, key);
    }

    @Override
    public Complex put(Value key, Value value) {
        if(key == null) {
            throw new IllegalArgumentException("The key must not be null.");
        }
        return put(this, key, value);
    }

    @Override
    public long nextIndex() {
        if(isEmpty()) {
            return 1;
        } else {
            throw new UnsupportedOperationException();
        }
    }

    private static Optional<Value> get(BinaryTreeComplex node, Value key) {
        while(!node.isEmpty()) {
            int order = key.compareTo(node.key);
            if(order == 0) {
                return Optional.of(node.value);
            } else if(order < 0) {
                node = node.left;
            } else /* order > 0 */ {
                node = node.right;
            }
        }
        return Optional.empty();
    }

    private static BinaryTreeComplex put(BinaryTreeComplex node, Value key, Value value) {
        if(node.isEmpty()) {
            if(value == null) {
                return node /* empty */; // removing from empty tree, result is empty
            } else {
                return new BinaryTreeComplex(key, value, node /* empty */, node /* empty */);
            }
        } else {
            int order = key.compareTo(node.key);
            if(order == 0) {
                if(value == null) {
                    return merge(node.left, node.right); // remove
                } else {
                    return new BinaryTreeComplex(key, value, node.left, node.right); // replace
                }
            } else if(order < 0) {
                return balance(new BinaryTreeComplex(node.key, node.value,
                        put(node.left, key, value),
                        node.right));
            } else /* order > 0 */ {
                return balance(new BinaryTreeComplex(node.key, node.value,
                        node.left,
                        put(node.right, key, value)));
            }
        }
    }

    private static BinaryTreeComplex merge(BinaryTreeComplex left, BinaryTreeComplex right) {
        if(left.isEmpty() && right.isEmpty()) {
            return left /* empty */; // trivial: nothing to merge
        } else if(left.isEmpty()) {
            return right; // trivial: only one side
        } else if(right.isEmpty()) {
            return left; // trivial: only one side
        } else {
            // Nontrivial merge: pick successor
            // TODO (optimize): pick successor from other subtree if more optimal
            BinaryTreeComplex succ = right;
            while(!succ.left.isEmpty()) {
                succ = succ.left;
            }
            return balance(new BinaryTreeComplex(succ.key, succ.value,
                    left,
                    put(right, succ.key, null)));
        }
    }

    private static BinaryTreeComplex balance(BinaryTreeComplex node) {
        if(node.balance() > 1) {
            if(node.right.balance() <= -1) {
                node = rotateLeft(new BinaryTreeComplex(node.key, node.value,
                        node.left,
                        rotateRight(node.right)));
            } else {
                node = rotateLeft(node);
            }
        } else if(node.balance() < -1) {
            if(node.left.balance() >= 1) {
                node = rotateRight(new BinaryTreeComplex(node.key, node.value,
                        rotateLeft(node.left),
                        node.right));
            } else {
                node = rotateRight(node);
            }
        }
        return node;
    }

    private static BinaryTreeComplex rotateLeft(BinaryTreeComplex node) {
        return new BinaryTreeComplex(node.right.key, node.right.value,
                new BinaryTreeComplex(node.key, node.value, node.left, node.right.left),
                node.right.right);
    }

    private static BinaryTreeComplex rotateRight(BinaryTreeComplex node) {
        return new BinaryTreeComplex(node.left.key, node.left.value,
                node.left.left,
                new BinaryTreeComplex(node.key, node.value, node.left.right, node.right));
    }

    private static int compare(Complex a, Complex b) {
        Iterator<Pair> iteratorA = a.iterator();
        Iterator<Pair> iteratorB = b.iterator();
        while(iteratorA.hasNext() && iteratorB.hasNext()) {
            Pair pairA = iteratorA.next();
            Pair pairB = iteratorB.next();
            int keyOrder = pairA.key().compareTo(pairB.key());
            if(keyOrder != 0) {
                return keyOrder;
            }
            int valueOrder = pairA.value().compareTo(pairB.value());
            if(valueOrder != 0) {
                return valueOrder;
            }
        }
        return Boolean.compare(iteratorA.hasNext(), iteratorB.hasNext());
    }

    private static class TreeIterator implements Iterator<Pair> {

        private final BinaryTreeComplex[] path;
        private int pos;

        private TreeIterator(BinaryTreeComplex tree) {
            path = new BinaryTreeComplex[tree.height];
            pos = -1;
            walkLeft(tree);
        }

        @Override
        public boolean hasNext() {
            return pos >= 0;
        }

        @Override
        public BinaryTreeComplex next() {
            if(!hasNext()) {
                throw new NoSuchElementException();
            }
            BinaryTreeComplex node = path[pos];
            if(!node.right.isEmpty()) {
                pos--;
                walkLeft(node.right);
            } else {
                pos--;
            }
            return node;
        }

        private void walkLeft(BinaryTreeComplex node) {
            while(!node.isEmpty()) {
                path[++pos] = node;
                node = node.left;
            }
        }
    }
}
