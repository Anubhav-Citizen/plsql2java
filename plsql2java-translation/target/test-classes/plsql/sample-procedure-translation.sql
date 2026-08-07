-- sample-procedure-translation.sql
-- Sample standalone procedure for translation engine integration tests

CREATE OR REPLACE PROCEDURE BULK_UPDATE_PRICES(p_category IN VARCHAR2, p_increase_pct IN NUMBER) IS
    TYPE price_list IS TABLE OF NUMBER;
    v_prices price_list;
    v_ids    price_list;
BEGIN
    SELECT product_id, price
    BULK COLLECT INTO v_ids, v_prices
    FROM products WHERE category = p_category;

    FORALL i IN 1..v_ids.COUNT
        UPDATE products SET price = v_prices(i) * (1 + p_increase_pct/100)
        WHERE product_id = v_ids(i);

    DBMS_OUTPUT.PUT_LINE('Updated ' || v_ids.COUNT || ' products');
EXCEPTION
    WHEN OTHERS THEN
        RAISE;
END BULK_UPDATE_PRICES;
/
