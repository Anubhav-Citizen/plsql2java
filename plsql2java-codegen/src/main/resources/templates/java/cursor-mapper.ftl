package ${packageName}.cursor;

import ${packageName}.entity.Customer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Translated from Oracle REF CURSOR / BULK COLLECT pattern.
 * PKG_CUSTOMER.GET_CUSTOMERS → queryForList via JdbcTemplate.
 */
@Component
public class CustomerCursorMapper {

    private final JdbcTemplate jdbcTemplate;

    public CustomerCursorMapper(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    /**
     * Translated from: OPEN P_CURSOR FOR SELECT * FROM CUSTOMER
     */
    public List<Customer> getCustomers() {
        return jdbcTemplate.query(
            "SELECT * FROM CUSTOMER",
            (rs, rowNum) -> {
                Customer c = new Customer();
                c.setId(rs.getLong("CUSTOMER_ID"));
                c.setCustomerName(rs.getString("CUSTOMER_NAME"));
                c.setEmail(rs.getString("EMAIL"));
                c.setPhone(rs.getString("PHONE"));
                c.setStatus(rs.getString("STATUS"));
                return c;
            }
        );
    }
}
