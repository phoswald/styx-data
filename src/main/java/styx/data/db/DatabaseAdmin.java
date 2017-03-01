package styx.data.db;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import styx.data.Store;

public class DatabaseAdmin {

    public static void main(String[] args) throws IOException {
        if(args.length == 3 && args[0].equals("-export")) {
            String databaseUrl = args[1];
            Path destinationFile = Paths.get(args[2]);
            try(Store store = Store.open(databaseUrl)) {
                long count = new DatabaseAdmin().exportDatabase(store, destinationFile);
                System.out.println("Exported " + count + " rows from " + databaseUrl + " into " + destinationFile);
            }
        } else if(args.length == 3 && args[0].equals("-import")) {
            String databaseUrl = args[1];
            Path sourceFile = Paths.get(args[2]);
            try(Store store = Store.open(databaseUrl)) {
                long count = new DatabaseAdmin().importDatabase(store, sourceFile);
                System.out.println("Imported " + count + " rows from " + sourceFile + " into " + databaseUrl);
            }
        } else {
            printUsage();
        }
    }

    private static void printUsage() {
        System.out.println("Usage:");
        System.out.println("  $ java " + DatabaseAdmin.class.getName() + " -export <database-url> <destination-file>");
        System.out.println("  $ java " + DatabaseAdmin.class.getName() + " -import <database-url> <source-file>");
    }

    public long exportDatabase(Store store, Path destinationFile) throws IOException {
        AtomicLong counter = new AtomicLong();
        try(DatabaseTransaction txn = getDatabase(store).openReadTransaction()) {
            Files.createDirectories(destinationFile.getParent());
            Files.write(destinationFile, toIterable(txn.selectAll().
                    peek(r -> counter.incrementAndGet()).
                    map(Row::encode)));
        }
        return counter.get();
    }

    public long importDatabase(Store store, Path sourceFile) throws IOException {
        AtomicLong counter = new AtomicLong();
        try(DatabaseTransaction txn = getDatabase(store).openWriteTransaction()) {
            txn.deleteAll();
            Files.lines(sourceFile).
                    map(Row::decode).
                    peek(r -> counter.incrementAndGet()).
                    forEach(txn::insert);
            txn.markCommit();
        }
        return counter.get();
    }

    private static Database getDatabase(Store store) {
        if(store instanceof DatabaseStore) {
            return ((DatabaseStore) store).getDatabase();
        } else {
            throw new IllegalArgumentException("Not a database URL.");
        }
    }

    private static <T> Iterable<T> toIterable(Stream<T> stream) {
        return stream::iterator;
    }
}
