-- sample-package-translation.sql
-- Sample Oracle package for translation engine integration tests

CREATE OR REPLACE PACKAGE ORDER_PROCESSOR IS
    PROCEDURE process_order(p_order_id IN NUMBER, p_status OUT VARCHAR2);
    FUNCTION get_discount(p_customer_id IN NUMBER) RETURN NUMBER;
END ORDER_PROCESSOR;
/

CREATE OR REPLACE PACKAGE BODY ORDER_PROCESSOR IS

    PROCEDURE process_order(p_order_id IN NUMBER, p_status OUT VARCHAR2) IS
        v_amount NUMBER;
        v_customer_id NUMBER;
    BEGIN
        SELECT amount, customer_id INTO v_amount, v_customer_id
        FROM orders WHERE order_id = p_order_id;

        IF v_amount > 1000 THEN
            p_status := 'HIGH_VALUE';
        ELSIF v_amount > 100 THEN
            p_status := 'STANDARD';
        ELSE
            p_status := 'LOW_VALUE';
        END IF;

        DBMS_OUTPUT.PUT_LINE('Processed order: ' || p_order_id);

    EXCEPTION
        WHEN NO_DATA_FOUND THEN
            p_status := 'NOT_FOUND';
        WHEN OTHERS THEN
            p_status := 'ERROR';
    END process_order;

    FUNCTION get_discount(p_customer_id IN NUMBER) RETURN NUMBER IS
        v_discount NUMBER := 0;
    BEGIN
        FOR rec IN (SELECT discount_pct FROM customer_discounts WHERE customer_id = p_customer_id) LOOP
            v_discount := rec.discount_pct;
        END LOOP;
        RETURN v_discount;
    END get_discount;

END ORDER_PROCESSOR;
/
