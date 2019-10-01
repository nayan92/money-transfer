package nayan92.moneytransfer.db;

import nayan92.moneytransfer.db.mapper.DbAccountMapper;
import org.jdbi.v3.core.Jdbi;

public class JdbiProvider {

    private Jdbi jdbi;

    public void initialise() {
        jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", "");
        jdbi.registerRowMapper(new DbAccountMapper());
    }

    public Jdbi get() {
        return jdbi;
    }
}
