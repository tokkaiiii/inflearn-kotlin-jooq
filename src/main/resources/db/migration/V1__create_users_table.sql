CREATE TABLE users
(
    id       BIGINT AUTO_INCREMENT PRIMARY KEY,
    email    VARCHAR(50)  NOT NULL,
    username VARCHAR(50)  NOT NULL,
    password VARCHAR(100) NOT NULL
);
