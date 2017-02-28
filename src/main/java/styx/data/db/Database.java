package styx.data.db;

public interface Database extends AutoCloseable {

    @Override
    public void close();

    public DatabaseTransaction openReadTransaction();

    public DatabaseTransaction openWriteTransaction();
}
