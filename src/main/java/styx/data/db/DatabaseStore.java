package styx.data.db;

import static styx.data.Values.generate;
import static styx.data.Values.parse;
import static styx.data.Values.text;

import java.util.Objects;
import java.util.Optional;

import styx.data.Complex;
import styx.data.Pair;
import styx.data.Reference;
import styx.data.Store;
import styx.data.Value;
import styx.data.exception.InvalidWriteException;
import styx.data.impl.CollectingHandler;

public class DatabaseStore implements Store {

    private final Database db;

    public DatabaseStore(Database db) {
        this.db = Objects.requireNonNull(db);
    }

    @Override
    public void close() {
        db.close();
    }

    @Override
    public Optional<Value> read(Reference ref) {
        Path path = Path.of();
        Row current = db.selectSingle(path, "").orElse(null);
        for(int i = 0; current != null && i < ref.partCount(); i++) {
            path = path.add(current.suffix());
            current = db.selectSingle(path, generate(ref.partAt(i))).orElse(null);
        }
        if(current != null) {
            if(current.isComplex()) {
                return Optional.of(collectDescendants(current));
            } else {
                return Optional.of(parse(current.value()));
            }
        } else {
            return Optional.empty();
        }
    }

    private Value collectDescendants(Row base) {
        CollectingHandler handler = new CollectingHandler();
        handler.open(text());
        Row previous = base;
        for(Row current : db.selectDescendants(base.fullpath()).toArray(Row[]::new)) {
            int prefixLength = current.fullpath2().prefixLength(previous.fullpath2());
            int closeCount = previous.fullpath2().length() - prefixLength;
            while(closeCount-- > 0) {
                handler.close();
            }
            if(current.isComplex()) {
                handler.open(parse(current.key()));
            } else {
                handler.value(parse(current.key()), parse(current.value()));
            }
            previous = current;
        }
        int closeCount = previous.fullpath2().length() - base.fullpath2().length();
        while(closeCount-- > 0) {
            handler.close();
        }
        handler.close();
        return handler.collect();
    }

    @Override
    public void write(Reference ref, Value value) {
        Path path = Path.of();
        Row previous = null;
        Row current = db.selectSingle(path, "").orElse(null);
        Path parent = null;
        String key = null;
        for(int i = 0; current != null && i < ref.partCount(); i++) {
            path = path.add(current.suffix());
            parent = path;
            key = generate(ref.partAt(i));
            previous = current;
            current = db.selectSingle(path, key).orElse(null);
            if(i+1 < ref.partCount() && current == null) {
                throw new InvalidWriteException("Attempt to write a child of a non-existing value.");
            }
        }
        if(ref.parent().isPresent()) {
            if(previous != null && !previous.isComplex()) {
                throw new InvalidWriteException("Attempt to write a child of a non-complex value.");
            }
        }
        if(current != null) {
            db.deleteSingle(current.parent(), current.key());
            if(current.isComplex()) {
                db.deleteDescendants(current.fullpath());
            }
        }
        if(value != null) {
            if(ref.partCount() == 0) {
                parent = Path.of();
                key = "";
            }
            if(value.isComplex()) {
                int suffix = db.allocateSuffx(parent);
                visitDescendants(parent, key, suffix, value.asComplex());
            } else {
                db.insertSimple(parent, key, generate(value));
            }
        }
    }

    private void visitDescendants(Path parent, String key, int suffix, Complex value) {
        db.insertComplex(parent, key, suffix);
        Path current = parent.add(suffix);
        int index = 0;
        for(Pair pair : value.asComplex()) {
            if(pair.value().isComplex()) {
                visitDescendants(current, generate(pair.key()), ++index, pair.value().asComplex());
            } else {
                db.insertSimple(current, generate(pair.key()), generate(pair.value()));
            }
        }
    }
}