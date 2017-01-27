package styx.data.impl.store;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

import styx.data.GeneratorOption;
import styx.data.Store;
import styx.data.Value;
import styx.data.Values;

public class FileStore extends MemoryStore {

    private final Path file;
    private final Path lock;
    private final Value initialValue;

    private FileStore(Path file) {
        this.file = file;
        this.lock = Paths.get(file.toString() + ".lock");
        run(() -> Files.createFile(lock), "Failed to aquire lock for " + file);
        if(Files.isRegularFile(file)) {
            initialValue = Values.parse(file);
            root.write(initialValue);
        } else {
            initialValue = null;
        }
    }

    public static Store open(Path file) {
        run(() -> Files.createDirectories(file.getParent()), "Invalid path: " + file);
        return new FileStore(Objects.requireNonNull(file));
    }

    @Override
    public void close() {
        try {
            Value value = root.read();
            if(value != initialValue) {
                if(value != null) {
                    Values.generate(value, file, GeneratorOption.INDENT);
                } else {
                    run(() -> Files.deleteIfExists(file), null);
                }
            }
        } finally {
            run(() -> Files.delete(lock), "Failed to release lock for " + file);
        }
    }

    private static void run(IORunnable runnable, String message) {
        try {
            runnable.run();
        } catch(IOException e) {
            throw new UncheckedIOException(message, e);
        }
    }

    private static interface IORunnable {
        void run() throws IOException;
    }
}
