package styx.data.impl;

import static styx.data.Values.complex;
import static styx.data.Values.pair;

import java.util.ArrayList;
import java.util.List;

import styx.data.Pair;
import styx.data.Value;

public class CollectingHandler implements Handler {

    private final FastStack<Context> context = new FastStack<>(Context::new, Context::init);

    public CollectingHandler() {
        context.push();
    }

    @Override
    public void open(Value key) {
        context.push().key = key;
    }

    @Override
    public void value(Value key, Value value) {
        context.peek().pairs.add(pair(key, value));
    }

    @Override
    public void close() {
        Pair pair = pair(context.peek().key, complex(context.peek().pairs));
        context.pop();
        context.peek().pairs.add(pair);
    }

    public Value collect() {
        return context.peek().pairs.get(0).value();
    }

    private static class Context {
        private Value key;
        private List<Pair> pairs;
        private void init() {
            key = null;
            pairs = new ArrayList<>();
        }
    }
}
