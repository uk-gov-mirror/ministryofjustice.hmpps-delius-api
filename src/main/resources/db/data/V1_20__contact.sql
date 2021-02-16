
INSERT
INTO R_CONTACT_TYPE
(
    CONTACT_TYPE_ID, CODE, DESCRIPTION, SHORT_DESCRIPTION, SELECTABLE, NATIONAL_STANDARDS_CONTACT, ATTENDANCE_CONTACT,
    RECORDED_HOURS_CREDITED, SENSITIVE_CONTACT, OFFENDER_LEVEL_CONTACT, CONTACT_OUTCOME_FLAG,
    CONTACT_LOCATION_FLAG, CONTACT_ALERT_FLAG, FUTURE_SCHEDULED_CONTACTS_FLAG, APPEARS_IN_LIST_OF_CONTACTS,
    SMS_MESSAGE_TEXT, OFFENDER_EVENT_0, LEGACY_ORDERS, CJA_ORDERS, DPA_EXCLUDE, SPG_OVERRIDE, CREATED_BY_USER_ID, CREATED_DATETIME,
    LAST_UPDATED_USER_ID, LAST_UPDATED_DATETIME, ROW_VERSION, EDITABLE, SGC_FLAG, SPG_INTEREST
)
SELECT CONTACT_TYPE_ID_SEQ.NEXTVAL,
       'CRS01',
       'Referred to Commissioned Rehabilitative Service',
       null,
       'N',
       'N',
       'N',
       'N',
       'N',
       'Y',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       'N',
       1,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'
       ),
       SYSDATE,
       (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'
       ),
       SYSDATE,
       0,
       'N',
       0,
       0
FROM DUAL
WHERE NOT EXISTS
    (SELECT * FROM R_CONTACT_TYPE WHERE CODE = 'CRS01'
    );

--AL = All/Always/Forever, RA = Referrals & Assessments
insert into R_CONTACT_TYPECONTACT_CATEGORY(CONTACT_TYPE_ID, STANDARD_REFERENCE_LIST_ID, ROW_VERSION)
select
    (select CONTACT_TYPE_ID from R_CONTACT_TYPE where code = 'CRS01'),
    (select STANDARD_REFERENCE_LIST_ID from R_STANDARD_REFERENCE_LIST where REFERENCE_DATA_MASTER_ID = (
        select REFERENCE_DATA_MASTER_ID from R_REFERENCE_DATA_MASTER where CODE_SET_NAME = 'CONTACT CATEGORY')
                                                                        and CODE_VALUE = 'AL'), 0
FROM DUAL
WHERE NOT EXISTS
    (SELECT * FROM R_CONTACT_TYPECONTACT_CATEGORY WHERE CONTACT_TYPE_ID = (select CONTACT_TYPE_ID from R_CONTACT_TYPE where code = 'CRS01')
                                                    AND STANDARD_REFERENCE_LIST_ID = (select STANDARD_REFERENCE_LIST_ID from R_STANDARD_REFERENCE_LIST where REFERENCE_DATA_MASTER_ID = (
                select REFERENCE_DATA_MASTER_ID from R_REFERENCE_DATA_MASTER where CODE_SET_NAME = 'CONTACT CATEGORY')
                                                                                                                                                         and CODE_VALUE = 'AL')
    )
;

insert into R_CONTACT_TYPECONTACT_CATEGORY(CONTACT_TYPE_ID, STANDARD_REFERENCE_LIST_ID, ROW_VERSION)
select
    (select CONTACT_TYPE_ID from R_CONTACT_TYPE where code = 'CRS01'),
    (select STANDARD_REFERENCE_LIST_ID from R_STANDARD_REFERENCE_LIST where REFERENCE_DATA_MASTER_ID = (
        select REFERENCE_DATA_MASTER_ID from R_REFERENCE_DATA_MASTER where CODE_SET_NAME = 'CONTACT CATEGORY')
                                                                        and CODE_VALUE = 'RA'), 0
FROM DUAL
WHERE NOT EXISTS
    (SELECT * FROM R_CONTACT_TYPECONTACT_CATEGORY WHERE CONTACT_TYPE_ID = (select CONTACT_TYPE_ID from R_CONTACT_TYPE where code = 'CRS01')
                                                    AND STANDARD_REFERENCE_LIST_ID = (select STANDARD_REFERENCE_LIST_ID from R_STANDARD_REFERENCE_LIST where REFERENCE_DATA_MASTER_ID = (
                select REFERENCE_DATA_MASTER_ID from R_REFERENCE_DATA_MASTER where CODE_SET_NAME = 'CONTACT CATEGORY')
                                                                                                                                                         and CODE_VALUE = 'RA')
    )
;

--Insert address for Organisation
insert into ADDRESS(ADDRESS_ID, BUILDING_NAME, CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME, LAST_UPDATED_USER_ID, SOFT_DELETED, PARTITION_AREA_ID)
select ADDRESS_ID_SEQ.NEXTVAL, 'CRS', SYSDATE, (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), 0, 0
FROM DUAL
WHERE NOT EXISTS
    (select * from ADDRESS where BUILDING_NAME = 'CRS')
;

--Insert Organisation
insert into ORGANISATION(ORGANISATION_ID, CODE, DESCRIPTION, START_DATE, PRIVATE, ADDRESS_ID, ACTIVE_FLAG, CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME, LAST_UPDATED_USER_ID)
select ORGANISATION_ID_SEQ.NEXTVAL, 'CRS', 'Commissioned Rehabilitive Services Providers', TO_DATE('01/01/2021', 'DD/MM/YYYY'), 1,
       (select address_id from address where BUILDING_NAME='CRS'), 1,
       SYSDATE, (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER')
FROM DUAL
WHERE NOT EXISTS
    (select * from ORGANISATION where CODE = 'CRS')
;


--Insert Probation Area
insert into PROBATION_AREA(PROBATION_AREA_ID, CODE, DESCRIPTION, SELECTABLE, PRIVATE, ORGANISATION_ID, ADDRESS_ID, START_DATE, SPG_ACTIVE_ID, CREATED_DATETIME, CREATED_BY_USER_ID, LAST_UPDATED_DATETIME, LAST_UPDATED_USER_ID, DIVISION_ID)
select PROBATION_AREA_ID_SEQ.nextval, 'CRS', 'Commissioned Rehabilitive Services Provider', 'N', 1, (select ORGANISATION_ID from ORGANISATION where CODE = 'CRS'), (select address_id from address where BUILDING_NAME='CRS'), TO_DATE('01/01/2021', 'DD/MM/YYYY'),
       (select STANDARD_REFERENCE_LIST_ID from R_STANDARD_REFERENCE_LIST where REFERENCE_DATA_MASTER_ID = (select REFERENCE_DATA_MASTER_ID from R_REFERENCE_DATA_MASTER where CODE_SET_NAME = 'SPG_ACTIVE') and CODE_VALUE='N'),
       SYSDATE, (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'), SYSDATE, (SELECT USER_ID FROM USER_ WHERE DISTINGUISHED_NAME = 'DELIUS_SYSTEM_USER'),
       (select PROBATION_AREA_ID from PROBATION_AREA where CODE = 'N41')
FROM DUAL
WHERE NOT EXISTS
    (select * from PROBATION_AREA where CODE = 'CRS')
;

--Insert Unallocated Borough
insert into borough (borough_id, code, description, selectable, row_version, created_by_user_id, created_datetime, last_updated_user_id, last_updated_datetime, probation_area_id)
select borough_id_seq.nextval, ''||pa.code || 'UAT', 'Unallocated Cluster', 'Y',1, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate,
       (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate, probation_area_id from probation_area pa where pa.code = 'CRS'
                                                                                                                                        and not exists (select * from borough b where b.code = ''||pa.code || 'UAT');

--Insert Unallocated District
insert into district (district_id, code, description, selectable, borough_id, row_version, created_by_user_id, created_datetime, last_updated_user_id, last_updated_datetime)
select district_id_seq.nextval, ''||pa.code || 'UAT', 'Unallocated LDU', 'Y', (select borough_id from borough where code = ''||pa.code || 'UAT'), 1,
       (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'),
       sysdate from probation_area pa where pa.code = 'CRS'
                                        and not exists (select * from district b where b.code = ''||pa.code || 'UAT');

--Insert Unallocated LDU
insert into local_delivery_unit (local_delivery_unit_id, code, description, selectable, row_version, created_by_user_id, created_datetime, last_updated_user_id, last_updated_datetime, probation_area_id)
select local_delivery_unit_id_seq.nextval, ''||pa.code || 'UAT', 'Unallocated Team Type', 'Y', 1, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate,
       (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate, probation_area_id from probation_area pa where pa.code = 'CRS'
                                                                                                                                        and not exists (select * from local_delivery_unit b where b.code = ''||pa.code || 'UAT');

--Insert Unallocated Team
insert into team (team_id, code, description, district_id, local_delivery_unit_id, unpaid_work_team, row_version, start_date, created_by_user_id, created_datetime, last_updated_user_id, last_updated_datetime, probation_area_id, private)
select team_id_seq.nextval, ''||pa.code || 'UAT', 'Unallocated', (select district_id from district where code = ''||pa.code||'UAT'), (select local_delivery_unit_id from local_delivery_unit where code = ''||pa.code||'UAT'), 'Y', 1,
       sysdate, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate,
       probation_area_id, 1 from probation_area pa where pa.code = 'CRS'
;

--Insert Unallocated Staff                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                     and not exists (select * from team b where b.code = ''||pa.code || 'UAT');
insert into staff (staff_id, start_date, surname, forename, row_version, officer_code, created_by_user_id, created_datetime, last_updated_user_id, last_updated_datetime, private, probation_area_id)
select staff_id_seq.nextval, trunc(sysdate), 'Unallocated', 'Unallocated', 1, ''||pa.code||'UATU', (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'),
       sysdate, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate, 1, probation_area_id from probation_area pa where pa.code = 'CRS'
                                                                                                                                                    and not exists (select * from staff b where b.officer_code = ''||pa.code || 'UATU');

--Insert Staff Team Link
insert into staff_team (staff_id, team_id, row_version, created_by_user_id, created_datetime, last_updated_user_id, last_updated_datetime)
select (select staff_id from staff where officer_code = ''||pa.code||'UATU'), (select team_id from team where code = ''||pa.code||'UAT'), 1,
       (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'), sysdate, (select user_id from user_ where distinguished_name = 'DELIUS_SYSTEM_USER'),
       sysdate from probation_area pa where pa.code = 'CRS'
;