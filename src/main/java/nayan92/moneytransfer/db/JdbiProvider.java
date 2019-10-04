package nayan92.moneytransfer.db;

import nayan92.moneytransfer.db.mapper.DbAccountMapper;
import org.flywaydb.core.Flyway;
import org.jdbi.v3.core.Jdbi;

import java.io.IOException;
import java.util.Properties;

public class JdbiProvider {

    private Jdbi jdbi;

    public void initialise() throws IOException {
        Properties properties = new Properties();
        properties.load(getClass().getClassLoader().getResourceAsStream("app.properties"));

        String dbUrl = properties.getProperty("db.url");

        jdbi = Jdbi.create(dbUrl, "", "");
        jdbi.registerRowMapper(new DbAccountMapper());

        Flyway flyway = Flyway.configure()
                .dataSource(dbUrl, "", "")
                .locations("classpath:db/migration")
                .load();
        flyway.migrate();
    }

    public Jdbi get() {
        return jdbi;
    }
}
