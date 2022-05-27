INSERT INTO USER (ID, PROVIDER_ID, PASSWORD, NICKNAME, ACTIVATED, AUTHORITY) VALUES (1, 'admin', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'admin', 1, 'ROLE_ADMIN');
INSERT INTO USER (ID, PROVIDER_ID, PASSWORD, NICKNAME, ACTIVATED, AUTHORITY, EMAIL, JOB, CAREER , KNOWN_PATH) VALUES (2, 'user', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'user', 1, 'ROLE_USER', 'user@naver.com','백엔드','LESS_THAN_3YEARS','SEARCH');
INSERT INTO USER (ID, PROVIDER_ID, PASSWORD, NICKNAME, ACTIVATED, AUTHORITY, EMAIL) VALUES (3, 'unSignedUser', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'user', 1, 'ROLE_NEED_MORE_INFO','unSignedUser@naver.com');
INSERT INTO USER (ID, PROVIDER_ID, PASSWORD, NICKNAME, ACTIVATED, AUTHORITY, EMAIL, JOB, CAREER , KNOWN_PATH) VALUES (4, 'nonActiveUser', '$2a$08$lDnHPz7eUkSi6ao14Twuau08mzhWrL4kyZGGU5xfiGALO/Vxd5DOi', 'nonActiveUser', 0, 'ROLE_USER', 'nonActiveUser@naver.com','프론트엔드','LESS_THAN_3YEARS','SEARCH');
