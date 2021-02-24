-- Simple contact
INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE,
                            NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT, RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT,
                            OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG, CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG,
                            FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS, SMS_MESSAGE_TEXT,
                            OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID,
                            CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE,
                            SGC_FLAG, SPG_INTEREST)
VALUES ((SELECT MAX(CONTACT_TYPE_ID) + 1 FROM R_CONTACT_TYPE), 'TST01', 'Simple contact', null, 'Y', 'N',
        'N', 'N', 'N', 'Y', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 1,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, 0, 'N', 0, 0);

-- Attendance contact
INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE,
                            NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT, RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT,
                            OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG, CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG,
                            FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS, SMS_MESSAGE_TEXT,
                            OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID,
                            CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE,
                            SGC_FLAG, SPG_INTEREST)
VALUES ((SELECT MAX(CONTACT_TYPE_ID) + 1 FROM R_CONTACT_TYPE), 'TST02', 'Attendance contact', null, 'Y', 'N',
        'Y', 'N', 'N', 'Y', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 1,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, 0, 'N', 0, 0);

-- Alert attendance contact, requires outcome
INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE,
                            NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT, RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT,
                            OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG, CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG,
                            FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS, SMS_MESSAGE_TEXT,
                            OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID,
                            CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE,
                            SGC_FLAG, SPG_INTEREST)
VALUES ((SELECT MAX(CONTACT_TYPE_ID) + 1 FROM R_CONTACT_TYPE), 'TST03',
        'Alert attendance contact, requires outcome', null, 'Y', 'N', 'Y', 'N', 'N', 'Y', 'Y', 'Y', 'Y',
        'N', 'N', 'N', 'N', 'N', 'N', 'N', 1,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, 0, 'N', 0, 0);

-- Contact not allowed by this API (see allowed.contact-types in application-test.yml)
INSERT INTO R_CONTACT_TYPE (CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE,
                            NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT, RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT,
                            OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG, CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG,
                            FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS, SMS_MESSAGE_TEXT,
                            OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID,
                            CREATED_DATETIME, LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE,
                            SGC_FLAG, SPG_INTEREST)
VALUES ((SELECT MAX(CONTACT_TYPE_ID) + 1 FROM R_CONTACT_TYPE), 'TST04', 'Contact not allowed by this API', null, 'Y', 'N',
        'N', 'N', 'N', 'Y', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 'N', 1,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE,
        (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, 0, 'N', 0, 0);

-- Add a linked outcome (CO22 - No Action Required) for each type
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
VALUES ((SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE='TST01'), 94, 0, null);
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
VALUES ((SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE='TST02'), 94, 0, null);
INSERT INTO R_CONTACT_TYPE_OUTCOME (CONTACT_TYPE_ID, CONTACT_OUTCOME_TYPE_ID, ROW_VERSION, TRAINING_SESSION_ID)
VALUES ((SELECT CONTACT_TYPE_ID FROM R_CONTACT_TYPE WHERE CODE='TST03'), 94, 0, null);

