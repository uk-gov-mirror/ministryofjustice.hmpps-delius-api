export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv

# Stop Oracle
sqlplus / as sysdba <<EOF
SHUTDOWN;
EXIT;
EOF

# Stop listener
lsnrctl stop