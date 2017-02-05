package styx.data.db;

import java.util.Optional;

import styx.data.Store;
import styx.data.StoreProvider;

public class MemoryDatabaseStoreProvider implements StoreProvider {

    @Override
    public Optional<Store> openStore(String url) {
        if(url.equals("memorydb")) {
            return Optional.of(new DatabaseStore(MemoryDatabase.open(null)));
        }
        if(url.startsWith("memorydb:")) {
            return Optional.of(new DatabaseStore(MemoryDatabase.open(url.substring(9))));
        }
        return Optional.empty();
    }
}
