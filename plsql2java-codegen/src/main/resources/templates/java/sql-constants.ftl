package ${packageName}.util;

/**
 * SQL query constants translated from PL/SQL package PKG_CUSTOMER.
 */
public final class SqlConstants {

    private SqlConstants() {}

    public static final String INSERT_CUSTOMER =
        "INSERT INTO CUSTOMER (CUSTOMER_NAME, EMAIL, PHONE, ANNUAL_INCOME) VALUES (?, ?, ?, ?)";

    public static final String UPDATE_CUSTOMER_STATUS =
        "UPDATE CUSTOMER SET STATUS=?, UPDATED_DATE=NOW() WHERE CUSTOMER_ID=?";

    public static final String SELECT_CUSTOMER_COUNT =
        "SELECT COUNT(*) FROM CUSTOMER";

    public static final String SELECT_CUSTOMER_STATUS =
        "SELECT STATUS FROM CUSTOMER WHERE CUSTOMER_ID=?";

    public static final String SELECT_ALL_CUSTOMERS =
        "SELECT * FROM CUSTOMER";
}
