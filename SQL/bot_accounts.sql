-- Bot Player Accounts
-- Create test accounts for bot automation
-- Usage: import this into your MariaDB/MySQL database

INSERT INTO `users` (`username`, `password`, `online`, `luong`, `status`)
VALUES
    ('bot1', 'bot123', 0, 999999, 1),
    ('bot2', 'bot123', 0, 999999, 1),
    ('bot3', 'bot123', 0, 999999, 1),
    ('bot4', 'bot123', 0, 999999, 1),
    ('bot5', 'bot123', 0, 999999, 1)
ON DUPLICATE KEY UPDATE `password`=VALUES(`password`);

-- Get the user IDs for bot accounts
-- Then create corresponding player records:
-- INSERT INTO `player` (`id`, `user_id`, `name`, `class_id`, `level`, `type_pk`, `head`, `body`, `leg`, `weapon`, `map_id`, `x`, `y`, `save_coordinate`, `hp`, `max_hp`, `mp`, `max_mp`, `yen`, `coin`, `task_id`)
-- VALUES (101, 1, 'Bot1', 0, 10, 0, 1, 1, 1, 0, 1, 100, 100, 1, 100, 100, 100, 100, 1000, 1000, 1);
