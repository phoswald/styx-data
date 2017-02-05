package styx.data.db;

public class MemoryDatabaseTest extends GenericDatabaseTest {

    public MemoryDatabaseTest() {
        super(MemoryDatabase.open(null));
    }
}
