CREATE
    DATABASE IF NOT EXISTS "tcp" DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE
    "tcp";
-- --------------------------------------------------------

--
-- Table structure for table `user`
--

DROP TABLE IF EXISTS `user`;
CREATE TABLE `user`
(
    `user_id`  int(11) UNSIGNED NOT NULL AUTO_INCREMENT,
    `name`     varchar(255)     NOT NULL,
    `email`    varchar(255)     NOT NULL,
    `password` varchar(255)     NOT NULL,
    PRIMARY KEY (`user_id`)
) ENGINE = INNODB
  DEFAULT CHARSET = utf8
  COLLATE = utf8_unicode_ci;


-- --------------------------------------------------------

--
-- Dumping data for table `users`
--

INSERT INTO `user` (`user_id`, `name`, `email`, `password`)
VALUES (1, 'khang', 'vankhang@gmail.com', '123456'),
       (2, 'Nguyen Van A', 'a@gmail.com', '123456'),
       (3, 'Nguyen Van B', 'b@gmail.com', '123456'),
       (4, 'Nguyen Van C', 'c@gmail.com', '123456'),
       (5, 'Nguyen Van D', 'd@gmail.com', '123456'),
       (6, 'Nguyen Van E', 'e@gmail.com', '123456');