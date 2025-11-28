-- Fix AUTO_INCREMENT for backup_tasks and backup_files tables
-- Run this SQL script in your database to fix the "Field doesn't have a default value" errors

-- For backup_tasks table
ALTER TABLE `backup_tasks` 
  CHANGE `task_id` `task_id` INT(11) NOT NULL AUTO_INCREMENT;

-- For backup_files table
ALTER TABLE `backup_files`
  CHANGE `file_id` `file_id` INT(11) NOT NULL AUTO_INCREMENT;

-- Verify the changes
SHOW CREATE TABLE backup_tasks;
SHOW CREATE TABLE backup_files;
