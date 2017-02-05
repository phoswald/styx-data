package styx.data.impl.mem;

import java.nio.file.Paths;
import java.util.Optional;

import styx.data.Store;
import styx.data.StoreProvider;

public class FileStoreProvider implements StoreProvider {

    @Override
    public Optional<Store> openStore(String url) {
        if(url.startsWith("file:")) {
            return Optional.of(FileStore.open(Paths.get(url.substring(5))));
        }
        return Optional.empty();
    }
}
