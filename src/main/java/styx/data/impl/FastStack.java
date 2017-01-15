package styx.data.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

class FastStack <T> {

    private final Supplier<T> allocator;
    private final Consumer<T> initializer;
    private final List<T> elements = new ArrayList<>();
    private int topIndex = -1;

    FastStack(Supplier<T> allocator, Consumer<T> initializer) {
        this.allocator = Objects.requireNonNull(allocator);
        this.initializer = Objects.requireNonNull(initializer);
    }

    T push() {
        if(topIndex + 1 == elements.size()) {
            elements.add(allocator.get());
        }
        topIndex++;
        T element = elements.get(topIndex);
        initializer.accept(element);
        return element;
    }

    T pop() {
        T element = elements.get(topIndex);
        topIndex--;
        return element;
    }

    T peek() {
        return elements.get(topIndex);
    }

    int size() {
        return topIndex + 1;
    }
}
