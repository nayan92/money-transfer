package nayan92.moneytransfer.db.mapper;

import nayan92.moneytransfer.db.entity.DbAccount;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class DbAccountMapper implements RowMapper<DbAccount> {
    @Override
    public DbAccount map(ResultSet rs, StatementContext ctx) throws SQLException {
        return new DbAccount(rs.getInt("account_id"), rs.getInt("balance"));
    }
}
