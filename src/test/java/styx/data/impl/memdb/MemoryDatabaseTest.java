package styx.data.impl.memdb;

import styx.data.db.GenericDatabaseTest;
import styx.data.impl.memdb.MemoryDatabase;

public class MemoryDatabaseTest extends GenericDatabaseTest {

    public MemoryDatabaseTest() {
        super(MemoryDatabase.open(null));
    }
}
