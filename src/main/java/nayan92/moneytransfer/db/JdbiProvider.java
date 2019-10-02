package nayan92.moneytransfer.db;

import nayan92.moneytransfer.db.mapper.DbAccountMapper;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

public class JdbiProvider {

    private Jdbi jdbi;

    public void initialise() {
        // TODO pull this into a config file + in the tests too
        jdbi = Jdbi.create("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", "");
        jdbi.registerRowMapper(new DbAccountMapper());

        Flyway flyway = Flyway.configure()
                .dataSource("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", "", "")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    public Jdbi get() {
        return jdbi;
    }
}
