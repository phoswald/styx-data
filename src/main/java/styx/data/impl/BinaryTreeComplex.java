package styx.data.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import styx.data.Complex;
import styx.data.Pair;
import styx.data.Value;

public class BinaryTreeComplex extends AbstractValue implements Complex {

    public static final BinaryTreeComplex EMPTY = new BinaryTreeComplex();

    /** the key/value-pair of this node, never null except for empty node */
    private final Pair pair;

    /** the left subtree, never null but points to empty node if not used */
    private final BinaryTreeComplex left;

    /** the right subtree, never null but points to empty node if not used */
    private final BinaryTreeComplex right;

    /** the height of the tree: 0 if empty, 1 if containing one key/value-pair, ... */
    private final int height;

    private BinaryTreeComplex() {
        this.pair = null;
        this.left = this;
        this.right = this;
        this.height = 0;
    }

    private BinaryTreeComplex(Pair pair, BinaryTreeComplex left, BinaryTreeComplex right) {
        this.pair = Objects.requireNonNull(pair);
        this.left = Objects.requireNonNull(left);
        this.right = Objects.requireNonNull(right);
        this.height = 1 + Math.max(left.height, right.height);
    }

    private boolean isEmpty() {
        return height == 0;
    }

    private int balance() {
        return right.height - left.height;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append('{');
        boolean first = true;
        for(Pair pair : this) {
            if(!first) {
                sb.append(',');
            }
            first = false;
            sb.append(pair.key().toString());
            sb.append(':');
            sb.append(pair.value().toString());
        }
        sb.append('}');
        return sb.toString();
    }

    @Override
    public int compareTo(Value other) {
        if(other.isComplex()) {
            return compare(this, other.asComplex());
        } else {
            return 1; // complex sorts after all all other values.
        }
    }

    @Override
    public Iterator<Pair> iterator() {
        List<Pair> list = new ArrayList<>();
        if(!isEmpty()) {
            iterate(this, list);
        }
        return list.iterator(); // TODO (optimize): return iterator for tree, not iterator for copied list.
    }

    @Override
    public Optional<Value> get(Value key) {
        if(key == null) {
            throw new IllegalArgumentException("The key must not be null");
        }
        return get(this, key);
    }

    @Override
    public Complex put(Value key, Value value) {
        if(key == null) {
            throw new IllegalArgumentException("The key must not be null");
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

    private static void iterate(BinaryTreeComplex node, List<Pair> list) {
        Objects.requireNonNull(node.pair); // must not be called for empty nodes
        if(!node.left.isEmpty()) {
            iterate(node.left, list);
        }
        list.add(node.pair);
        if(!node.right.isEmpty()) {
            iterate(node.right, list);
        }
    }

    private static Optional<Value> get(BinaryTreeComplex node, Value key) {
        while(!node.isEmpty()) {
            int order = key.compareTo(node.pair.key());
            if(order == 0) {
                return Optional.of(node.pair.value());
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
                return new BinaryTreeComplex(new Pair(key, value), node /* empty */, node /* empty */);
            }
        } else {
            int order = key.compareTo(node.pair.key());
            if(order == 0) {
                if(value == null) {
                    return merge(node.left, node.right); // remove
                } else {
                    return new BinaryTreeComplex(new Pair(key, value), node.left, node.right); // replace
                }
            } else if(order < 0) {
                return balance(new BinaryTreeComplex(node.pair,
                        put(node.left, key, value),
                        node.right));
            } else /* order > 0 */ {
                return balance(new BinaryTreeComplex(node.pair,
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
            return balance(new BinaryTreeComplex(succ.pair,
                    left,
                    put(right, succ.pair.key(), null)));
        }
    }

    private static BinaryTreeComplex balance(BinaryTreeComplex node) {
        if(node.balance() > 1) {
            if(node.right.balance() <= -1) {
                node = rotateLeft(new BinaryTreeComplex(node.pair,
                        node.left,
                        rotateRight(node.right)));
            } else {
                node = rotateLeft(node);
            }
        } else if(node.balance() < -1) {
            if(node.left.balance() >= 1) {
                node = rotateRight(new BinaryTreeComplex(node.pair,
                        rotateLeft(node.left),
                        node.right));
            } else {
                node = rotateRight(node);
            }
        }
        return node;
    }

    private static BinaryTreeComplex rotateLeft(BinaryTreeComplex node) {
        return new BinaryTreeComplex(node.right.pair,
                new BinaryTreeComplex(node.pair, node.left, node.right.left),
                node.right.right);
    }

    private static BinaryTreeComplex rotateRight(BinaryTreeComplex node) {
        return new BinaryTreeComplex(node.left.pair,
                node.left.left,
                new BinaryTreeComplex(node.pair, node.left.right, node.right));
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
}
