package styx.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import styx.data.impl.AbstractNumeric;
import styx.data.impl.ByteArrayBinary;
import styx.data.impl.ConcreteReference;
import styx.data.impl.StringText;

public class Values {

//    public static Value empty() {
//        throw new UnsupportedOperationException();
//    }

//    public static Value value(Object object) {
//        throw new UnsupportedOperationException();
//    }

    public static Numeric number(long value) {
        return AbstractNumeric.valueOf(value);
    }

    public static Numeric number(double value) {
        return AbstractNumeric.valueOf(value);
    }

    public static Text text(String value) {
        return StringText.valueOf(value);
    }

    public static Binary binary(byte[] value) {
        return ByteArrayBinary.valueOf(value);
    }

    public static Reference root() {
        return ConcreteReference.ROOT;
    }

    public static Reference reference(Value... parts) {
        return root().child(parts);
    }

    public static Reference reference(List<? extends Value> parts) {
        return root().child(parts);
    }

    public static Complex complex() {
        throw new UnsupportedOperationException();
    }

    public static Complex complex(Value value) {
        throw new UnsupportedOperationException();
    }

//    public static Complex complex(Value key, Value value) {
//        throw new UnsupportedOperationException();
//    }

//    public static Complex complex(Value... values) {
//        throw new UnsupportedOperationException();
//    }

    public static Complex complex(Collection<?> values) {
        throw new UnsupportedOperationException();
    }

    public static Complex complex(Map<?, ?> values) {
        throw new UnsupportedOperationException();
    }
}
