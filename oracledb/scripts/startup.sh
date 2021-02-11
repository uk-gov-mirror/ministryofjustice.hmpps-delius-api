export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv

# Start Oracle
sqlplus / as sysdba <<EOF
STARTUP;
EXIT;
EOF

# Start listener
lsnrctl start
echo -n Waiting for listener to start
until lsnrctl status | grep -q 'READY'; do echo -n .; sleep 1; done