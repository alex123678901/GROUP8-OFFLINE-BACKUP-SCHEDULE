-- phpMyAdmin SQL Dump
-- version 5.2.1
-- https://www.phpmyadmin.net/
--
-- Host: 127.0.0.1
-- Generation Time: Nov 19, 2025 at 02:23 PM
-- Server version: 10.4.32-MariaDB
-- PHP Version: 8.2.12

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `offline_backup_scheduler`
--

-- --------------------------------------------------------

--
-- Table structure for table `admin_actions`
--

CREATE TABLE `admin_actions` (
  `action_id` int(11) NOT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `action_type` varchar(100) DEFAULT NULL,
  `description` text DEFAULT NULL,
  `ip_address` varchar(45) DEFAULT NULL,
  `timestamp` timestamp NOT NULL DEFAULT current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `admin_actions`
--
DELIMITER $$
CREATE TRIGGER `trg_admin_log` AFTER INSERT ON `admin_actions` FOR EACH ROW BEGIN
    INSERT INTO backup_logs (user_id, action, status, message)
    VALUES (NEW.admin_id, 'AdminAction', 'Success', CONCAT('Admin performed action: ', NEW.action_type));
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `backup_files`
--

CREATE TABLE `backup_files` (
  `file_id` int(11) NOT NULL AUTO_INCREMENT,
  `task_id` int(11) NOT NULL,
  `file_name` varchar(255) NOT NULL,
  `file_path` varchar(500) NOT NULL,
  `file_size` bigint(20) DEFAULT NULL,
  `checksum` varchar(64) DEFAULT NULL,
  `version_no` int(11) DEFAULT 1,
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  PRIMARY KEY (`file_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `backup_logs`
--

CREATE TABLE `backup_logs` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `timestamp` datetime NOT NULL,
  `backup_name` varchar(255) DEFAULT NULL,
  `backup_type` varchar(50) NOT NULL,
  `status` varchar(50) NOT NULL,
  `details` text DEFAULT NULL,
  `performed_by` varchar(100) NOT NULL,
  `department` varchar(100) DEFAULT NULL,
  `task_id` int(11) DEFAULT NULL,
  `user_id` bigint(20) DEFAULT NULL,
  PRIMARY KEY (`id`),
  KEY `idx_timestamp` (`timestamp`),
  KEY `idx_performed_by` (`performed_by`),
  KEY `idx_department` (`department`),
  KEY `idx_status` (`status`),
  KEY `fk_logs_task` (`task_id`),
  KEY `fk_logs_user` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `backup_tasks`
--

CREATE TABLE `backup_tasks` (
  `task_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) DEFAULT NULL,
  `schedule_id` int(11) DEFAULT NULL,
  `task_type` enum('Manual','Scheduled') NOT NULL,
  `start_time` datetime DEFAULT current_timestamp(),
  `end_time` datetime DEFAULT NULL,
  `status` enum('Pending','Running','Completed','Failed') DEFAULT 'Pending',
  `remarks` text DEFAULT NULL,
  PRIMARY KEY (`task_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Triggers `backup_tasks`
--
DELIMITER $$
CREATE TRIGGER `trg_backup_complete` BEFORE UPDATE ON `backup_tasks` FOR EACH ROW BEGIN
    IF NEW.status = 'Completed' AND OLD.status <> 'Completed' THEN
        SET NEW.end_time = NOW();
    END IF;
END
$$
DELIMITER ;

-- --------------------------------------------------------

--
-- Table structure for table `cloud_uploads`
--

CREATE TABLE `cloud_uploads` (
  `upload_id` int(11) NOT NULL,
  `admin_id` bigint(20) DEFAULT NULL,
  `backup_task_id` int(11) DEFAULT NULL,
  `upload_time` datetime DEFAULT current_timestamp(),
  `status` enum('Pending','Completed','Failed') DEFAULT 'Pending',
  `cloud_link` varchar(500) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `roles`
--

CREATE TABLE `roles` (
  `Id` varchar(90) NOT NULL,
  `RoleName` varchar(90) DEFAULT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `roles`
--

INSERT INTO `roles` (`Id`, `RoleName`) VALUES
('ROLE01', 'Admin'),
('ROLE02', 'Staff'),
('ROLE03', 'HOD');

-- --------------------------------------------------------

--
-- Table structure for table `schedules`
--

CREATE TABLE `schedules` (
  `schedule_id` int(11) NOT NULL AUTO_INCREMENT,
  `user_id` bigint(20) NOT NULL,
  `name` varchar(100) DEFAULT NULL,
  `frequency` varchar(20) DEFAULT 'Daily',
  `time_of_day` varchar(10) DEFAULT NULL,
  `department` varchar(100) DEFAULT NULL,
  `source_path` text DEFAULT NULL,
  `destination_path` text DEFAULT NULL,
  `cloud_enabled` tinyint(1) DEFAULT 0,
  `next_run` datetime NOT NULL,
  `last_run` datetime DEFAULT NULL,
  `status` enum('Active','Inactive') DEFAULT 'Active',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp(),
  PRIMARY KEY (`schedule_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `system_alerts`
--

CREATE TABLE `system_alerts` (
  `alert_id` int(11) NOT NULL,
  `alert_type` enum('BackupFailure','LowStorage','NetworkError','UnauthorizedAccess') NOT NULL,
  `message` text NOT NULL,
  `severity` enum('Info','Warning','Critical') DEFAULT 'Info',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `resolved` enum('Yes','No') DEFAULT 'No'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` bigint(20) NOT NULL,
  `username` varchar(50) NOT NULL,
  `password_hash` varchar(255) NOT NULL,
  `full_name` varchar(100) NOT NULL,
  `department` varchar(100) DEFAULT NULL,
  `role` enum('Admin','HOD','Staff') NOT NULL,
  `email` varchar(120) DEFAULT NULL,
  `status` enum('Active','Inactive') DEFAULT 'Inactive',
  `created_at` timestamp NOT NULL DEFAULT current_timestamp(),
  `updated_at` timestamp NOT NULL DEFAULT current_timestamp() ON UPDATE current_timestamp()
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_general_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `username`, `password_hash`, `full_name`, `department`, `role`, `email`, `status`, `created_at`, `updated_at`) VALUES
(1000000000000000, 'admin', '240be518fabd2724ddb6f04eeb1da5967448d7e831c08c8fa822809f74c720a9', 'System Administrators', 'ALL DEPARTMENTS', 'Admin', 'null', 'Active', '2025-11-10 16:25:03', '2025-11-10 16:34:26'),
(1200370198982048, 'tuyizerenaomie', '9cc4ef38c2dc068961b709d4bdbfff09528da7f59314a6dfdb9c468237b6d03d', 'TUYIZERE Naomi', 'Administration', 'Admin', 'naomie@gmail.com', 'Active', '2025-11-10 16:04:13', '2025-11-10 16:04:13'),
(1200460064662069, 'mwizerwalex', '9cc4ef38c2dc068961b709d4bdbfff09528da7f59314a6dfdb9c468237b6d03d', 'MWIZERWA Alex', 'ICT Department', 'Staff', 'alexmwizerwa7@gmail.com', 'Active', '2025-11-10 15:29:37', '2025-11-15 08:08:03');

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_failed_backups`
-- (See below for the actual view)
--
CREATE TABLE `v_failed_backups` (
`task_id` int(11)
,`username` varchar(50)
,`details` text
,`timestamp` datetime
);

-- --------------------------------------------------------

--
-- Stand-in structure for view `v_recent_backups`
-- (See below for the actual view)
--
CREATE TABLE `v_recent_backups` (
`task_id` int(11)
,`username` varchar(50)
,`status` enum('Pending','Running','Completed','Failed')
,`start_time` datetime
,`end_time` datetime
,`total_files` bigint(21)
);

-- --------------------------------------------------------

--
-- Structure for view `v_failed_backups`
--
DROP TABLE IF EXISTS `v_failed_backups`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_failed_backups`  AS SELECT `l`.`task_id` AS `task_id`, `u`.`username` AS `username`, `l`.`details` AS `details`, `l`.`timestamp` AS `timestamp` FROM (`backup_logs` `l` left join `users` `u` on(`l`.`user_id` = `u`.`user_id`)) WHERE `l`.`status` = 'FAILED' ORDER BY `l`.`timestamp` DESC ;

-- --------------------------------------------------------

--
-- Structure for view `v_recent_backups`
--
DROP TABLE IF EXISTS `v_recent_backups`;

CREATE ALGORITHM=UNDEFINED DEFINER=`root`@`localhost` SQL SECURITY DEFINER VIEW `v_recent_backups`  AS SELECT `t`.`task_id` AS `task_id`, `u`.`username` AS `username`, `t`.`status` AS `status`, `t`.`start_time` AS `start_time`, `t`.`end_time` AS `end_time`, count(`f`.`file_id`) AS `total_files` FROM ((`backup_tasks` `t` left join `users` `u` on(`t`.`user_id` = `u`.`user_id`)) left join `backup_files` `f` on(`t`.`task_id` = `f`.`task_id`)) WHERE `t`.`status` = 'Completed' GROUP BY `t`.`task_id`, `u`.`username`, `t`.`status`, `t`.`start_time`, `t`.`end_time` ORDER BY `t`.`end_time` DESC LIMIT 0, 50 ;

--
-- Indexes for dumped tables
--

--
-- Indexes for table `admin_actions`
--
ALTER TABLE `admin_actions`
  ADD PRIMARY KEY (`action_id`),
  ADD KEY `fk_admin_actions_user` (`admin_id`),
  ADD KEY `idx_admin_action_type` (`action_type`);

--
-- Indexes for table `backup_files`
--
ALTER TABLE `backup_files`
  ADD KEY `fk_task` (`task_id`),
  ADD KEY `idx_checksum` (`checksum`);


--
-- Indexes for table `backup_logs`
-- (Foreign keys already defined in CREATE TABLE statement)
--

--
-- Indexes for table `backup_tasks`
--
ALTER TABLE `backup_tasks`
  ADD KEY `fk_user_task` (`user_id`),
  ADD KEY `fk_schedule_task` (`schedule_id`),
  ADD KEY `idx_tasks_schedule` (`schedule_id`);

--
-- Indexes for table `cloud_uploads`
--
ALTER TABLE `cloud_uploads`
  ADD PRIMARY KEY (`upload_id`),
  ADD KEY `fk_cloud_admin` (`admin_id`),
  ADD KEY `fk_cloud_task` (`backup_task_id`),
  ADD KEY `idx_cloud_status` (`status`);


--
-- Indexes for table `roles`
--
ALTER TABLE `roles`
  ADD PRIMARY KEY (`Id`);

--
-- Indexes for table `schedules`
--
ALTER TABLE `schedules`
  ADD KEY `fk_user_schedule` (`user_id`),
  ADD KEY `idx_schedules_status` (`status`);


--
-- Indexes for table `system_alerts`
--
ALTER TABLE `system_alerts`
  ADD PRIMARY KEY (`alert_id`),
  ADD KEY `idx_alerts_type` (`alert_type`),
  ADD KEY `idx_alerts_severity` (`severity`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `username` (`username`),
  ADD KEY `idx_users_department` (`department`),
  ADD KEY `idx_users_role` (`role`),
  ADD KEY `idx_users_status` (`status`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `admin_actions`
--
ALTER TABLE `admin_actions`
  MODIFY `action_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `backup_files`
--
ALTER TABLE `backup_files`
  MODIFY `file_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `backup_logs`
--
ALTER TABLE `backup_logs`
  MODIFY `id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `backup_tasks`
--
ALTER TABLE `backup_tasks`
  MODIFY `task_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `cloud_uploads`
--
ALTER TABLE `cloud_uploads`
  MODIFY `upload_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `schedules`
--
ALTER TABLE `schedules`
  MODIFY `schedule_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- AUTO_INCREMENT for table `system_alerts`
--
ALTER TABLE `system_alerts`
  MODIFY `alert_id` int(11) NOT NULL AUTO_INCREMENT;

--
-- Constraints for dumped tables
-- All foreign keys added here after tables are created
--

--
-- Constraints for table `admin_actions`
--
ALTER TABLE `admin_actions`
  ADD CONSTRAINT `fk_admin_actions_user` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `backup_files`
--
ALTER TABLE `backup_files`
  ADD CONSTRAINT `fk_files_task` FOREIGN KEY (`task_id`) REFERENCES `backup_tasks` (`task_id`) ON DELETE CASCADE;

--
-- Constraints for table `backup_logs`
--
ALTER TABLE `backup_logs`
  ADD CONSTRAINT `fk_logs_task` FOREIGN KEY (`task_id`) REFERENCES `backup_tasks` (`task_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_logs_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `backup_tasks`
--
ALTER TABLE `backup_tasks`
  ADD CONSTRAINT `fk_tasks_schedule` FOREIGN KEY (`schedule_id`) REFERENCES `schedules` (`schedule_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_tasks_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `cloud_uploads`
--
ALTER TABLE `cloud_uploads`
  ADD CONSTRAINT `fk_cloud_admin` FOREIGN KEY (`admin_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL,
  ADD CONSTRAINT `fk_cloud_task` FOREIGN KEY (`backup_task_id`) REFERENCES `backup_tasks` (`task_id`) ON DELETE SET NULL;

--
-- Constraints for table `schedules`
--
ALTER TABLE `schedules`
  ADD CONSTRAINT `fk_schedules_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE;

COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;

