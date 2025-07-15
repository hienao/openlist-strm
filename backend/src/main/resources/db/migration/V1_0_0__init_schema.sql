CREATE TABLE `user`
(
    `id`          INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    `username`    VARCHAR(255) NOT NULL UNIQUE,
    `create_time` TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `password`    VARCHAR(255) NOT NULL,
    `enable`      BOOLEAN      NOT NULL DEFAULT true
);

CREATE TABLE `permission`
(
    `id`   INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    `code` VARCHAR(255) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE `role`
(
    `id`   INTEGER      NOT NULL PRIMARY KEY AUTOINCREMENT,
    `code` VARCHAR(255) NOT NULL UNIQUE,
    `name` VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE `role_permission_map`
(
    `role_id`       INTEGER NOT NULL,
    `permission_id` INTEGER NOT NULL,
    PRIMARY KEY (`role_id`, `permission_id`),
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`permission_id`) REFERENCES `permission` (`id`) ON DELETE CASCADE
);

CREATE TABLE `user_role_map`
(
    `user_id` INTEGER NOT NULL,
    `role_id` INTEGER NOT NULL,
    PRIMARY KEY (`user_id`, `role_id`),
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`) ON DELETE CASCADE,
    FOREIGN KEY (`role_id`) REFERENCES `role` (`id`) ON DELETE CASCADE
);
