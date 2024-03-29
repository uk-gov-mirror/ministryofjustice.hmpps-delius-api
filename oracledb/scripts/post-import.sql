ALTER SESSION SET CONTAINER = XEPDB1;

-- Fix grants
GRANT CREATE ANY TABLE TO DELIUS_APP_SCHEMA;
GRANT CREATE SEQUENCE  TO DELIUS_APP_SCHEMA;
GRANT CREATE JOB       TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_session   TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_output    TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_flashback TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_rls       TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_crypto    TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_aqadm     TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_aq        TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_pipe      TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_scheduler TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON SYS.dbms_lock  TO DELIUS_APP_SCHEMA;
GRANT EXECUTE ON dbms_result_cache TO DELIUS_APP_SCHEMA;
call dbms_java.grant_permission( 'DELIUS_APP_SCHEMA', 'SYS:java.lang.RuntimePermission', 'getClassLoader', '' );
call dbms_java.grant_permission( 'DELIUS_APP_SCHEMA', 'SYS:java.util.PropertyPermission', '*', 'read,write' );
call dbms_java.grant_permission( 'DELIUS_APP_SCHEMA', 'SYS:java.net.SocketPermission', '*', 'listen,resolve');
call dbms_java.grant_permission( 'DELIUS_APP_SCHEMA', 'SYS:java.net.SocketPermission', '*', 'accept,resolve');
call dbms_java.grant_permission( 'DELIUS_APP_SCHEMA', 'SYS:java.net.SocketPermission', '*', 'connect,resolve');
GRANT SELECT ON dba_datapump_sessions TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$database     TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$mystat       TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$statname     TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$sqlarea      TO DELIUS_APP_SCHEMA;
GRANT SELECT ON gv_$sqlarea     TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$session      TO DELIUS_APP_SCHEMA;
GRANT SELECT ON gv_$session     TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$instance     TO DELIUS_APP_SCHEMA;
GRANT SELECT ON gv_$instance    TO DELIUS_APP_SCHEMA;
GRANT SELECT ON v_$parameter    TO DELIUS_APP_SCHEMA;
GRANT EXEMPT ACCESS POLICY      TO DELIUS_APP_SCHEMA;
GRANT datapump_imp_full_database TO DELIUS_APP_SCHEMA;
GRANT SELECT ON gv_$sqlarea     TO DELIUS_APP_SCHEMA;
GRANT SELECT ON dba_data_files  TO DELIUS_APP_SCHEMA;

-- Set user passwords
ALTER USER DELIUS_APP_SCHEMA IDENTIFIED BY &1;
ALTER USER DELIUS_APP_SCHEMA ACCOUNT UNLOCK;
ALTER USER DELIUS_POOL IDENTIFIED BY &1;
ALTER USER DELIUS_POOL ACCOUNT UNLOCK;
COMMIT;

-- Recompile schema
CONNECT DELIUS_APP_SCHEMA/&1@XEPDB1;
BEGIN DBMS_UTILITY.compile_schema(USER,FALSE); END;
/
SHOW ERRORS

EXIT;