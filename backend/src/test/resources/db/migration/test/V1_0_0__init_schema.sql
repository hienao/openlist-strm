CREATE TABLE openlist2strm.`user`
(
    `id`          BIGINT       NOT NULL AUTO_INCREMENT,
    `username`    VARCHAR(255) NOT NULL UNIQUE,
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `password`    VARCHAR(255) NOT NULL,
    `enable`      BOOLEAN      NOT NULL DEFAULT true,
    PRIMARY KEY (`id`)
);

CREATE TABLE openlist2strm.`permission`
(
    `id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(255) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (`id`)
);

CREATE TABLE openlist2strm.`role`
(
    `id`   BIGINT       NOT NULL AUTO_INCREMENT,
    `code` VARCHAR(255) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL UNIQUE,
    PRIMARY KEY (`id`)
);

CREATE TABLE openlist2strm.`role_permission_map`
(
    `role_id`       BIGINT NOT NULL,
    `permission_id` BIGINT NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES openlist2strm.`role` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (`permission_id`) REFERENCES openlist2strm.`permission` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);

CREATE TABLE openlist2strm.`user_role_map`
(
    `user_id` BIGINT NOT NULL,
    `role_id` BIGINT NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES openlist2strm.`user` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES openlist2strm.`role` (`id`) ON UPDATE NO ACTION ON DELETE CASCADE
);
