-- sample-unsupported-constructs.sql
-- PL/SQL with constructs that should be FLAGGED by the translation engine

CREATE OR REPLACE PROCEDURE UNSUPPORTED_DEMO IS
    v_cursor SYS_REFCURSOR;
BEGIN
    -- GOTO: always flagged
    GOTO skip_section;

    -- REF CURSOR: always flagged
    OPEN v_cursor FOR SELECT * FROM orders;

    <<skip_section>>
    -- FORALL with SAVE EXCEPTIONS: always flagged
    FORALL i IN 1..10 SAVE EXCEPTIONS
        INSERT INTO audit_log VALUES (i, SYSDATE);

END UNSUPPORTED_DEMO;
/
