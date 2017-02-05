package styx.data;

import java.util.Optional;

public interface StoreProvider {

    public Optional<Store> openStore(String url);
}
