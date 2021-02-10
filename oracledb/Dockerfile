FROM oracle/database:18.4.0-xe

ARG ORACLE_PWD=NDelius1
ARG ORACLE_CHARACTERSET=AL32UTF8
ENV ORACLE_PWD=$ORACLE_PWD \
    ORACLE_CHARACTERSET=$ORACLE_CHARACTERSET \
    XE_LOG_FILE=/opt/oracle/cfgtoollogs/dbca/XE/XE.log

# Initialise database
COPY scripts/shutdown.sh scripts/startup.sh /scripts/
RUN exec $ORACLE_BASE/$RUN_FILE & \
    until [[ -f $XE_LOG_FILE ]] && [[ $(grep 'Database creation complete' $XE_LOG_FILE) ]]; do sleep 1; done && \
    chmod +x /scripts/*.sh && \
    su - oracle -c '/scripts/shutdown.sh;'

# Create tablespaces and directories, then import the datapump file (from Unilink ST9)
# TODO curl the import.dmp file from somewhere so it isn't stored in a docker layer
COPY scripts/setup.sql scripts/post-import.sql /scripts/
COPY import.dmp /datapump/import.dmp
RUN chmod +x /scripts/*.sh && \
    chown oracle:oinstall /datapump && \
    su - oracle -c "/scripts/startup.sh"; \
    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; sqlplus system/${ORACLE_PWD}@xepdb1 @/scripts/setup.sql"; \
    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; impdp system/${ORACLE_PWD}@xepdb1 full=Y directory=IMPORT_DIR dumpfile=import.dmp logfile=import.log"; \
    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; sqlplus system/${ORACLE_PWD}@xepdb1 @/scripts/post-import.sql ${ORACLE_PWD}"; \
    su - oracle -c "/scripts/shutdown.sh"; \
    rm /datapump/import.dmp

# Uplift PDM version
#COPY scripts/uplift.sql /scripts/
#RUN su - oracle -c "/scripts/startup.sh" && \
#    su - oracle -c "export ORACLE_SID=XE; ORAENV_ASK=NO . oraenv; sqlplus system/${ORACLE_PWD}@xepdb1 @/scripts/uplift.sql ${ORACLE_PWD}" && \
#    su - oracle -c "/scripts/shutdown.sh"

EXPOSE 1521
EXPOSE 5500
CMD exec $ORACLE_BASE/$RUN_FILE
