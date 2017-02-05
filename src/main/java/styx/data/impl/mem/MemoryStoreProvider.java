package styx.data.impl.mem;

import java.util.Optional;

import styx.data.Store;
import styx.data.StoreProvider;

public class MemoryStoreProvider implements StoreProvider {

    @Override
    public Optional<Store> openStore(String url) {
        if(url.equals("memory")) {
            return Optional.of(MemoryStore.open(null));
        }
        if(url.startsWith("memory:")) {
            return Optional.of(MemoryStore.open(url.substring(7)));
        }
        return Optional.empty();
    }
}
