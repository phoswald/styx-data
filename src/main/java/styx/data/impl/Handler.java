package styx.data.impl;

import styx.data.Value;

public interface Handler {

    public void open(Value key);

    public void value(Value key, Value value);

    public void close();
}
