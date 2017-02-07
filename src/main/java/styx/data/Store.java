package styx.data;

import java.util.Optional;
import java.util.ServiceLoader;
import java.util.stream.Stream;

public interface Store extends AutoCloseable {

    public static Store open(String url) {
        for(StoreProvider provider : ServiceLoader.load(StoreProvider.class)) {
            Optional<Store> store = provider.openStore(url);
            if(store.isPresent()) {
                return store.get();
            }
        }
        throw new IllegalArgumentException("Invalid url: " + url);
    }

    @Override
    public void close();

    public Stream<Pair> browse(Reference ref);

    public Optional<Value> read(Reference ref);

    public void write(Reference ref, Value value);
}
