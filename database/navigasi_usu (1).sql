-- phpMyAdmin SQL Dump
-- version 5.2.2
-- https://www.phpmyadmin.net/
--
-- Host: localhost:3306
-- Generation Time: Dec 02, 2025 at 04:31 PM
-- Server version: 8.4.3
-- PHP Version: 8.3.16

SET SQL_MODE = "NO_AUTO_VALUE_ON_ZERO";
START TRANSACTION;
SET time_zone = "+00:00";


/*!40101 SET @OLD_CHARACTER_SET_CLIENT=@@CHARACTER_SET_CLIENT */;
/*!40101 SET @OLD_CHARACTER_SET_RESULTS=@@CHARACTER_SET_RESULTS */;
/*!40101 SET @OLD_COLLATION_CONNECTION=@@COLLATION_CONNECTION */;
/*!40101 SET NAMES utf8mb4 */;

--
-- Database: `navigasi_usu`
--

-- --------------------------------------------------------

--
-- Table structure for table `buildings`
--

CREATE TABLE `buildings` (
  `building_id` int NOT NULL,
  `building_code` varchar(20) NOT NULL,
  `building_name` varchar(100) NOT NULL,
  `building_type` enum('fakultas','gedung','musholla','perpustakaan','stadion','masjid') NOT NULL,
  `description` text,
  `latitude` decimal(10,8) NOT NULL,
  `longitude` decimal(11,8) NOT NULL,
  `address` text,
  `floor_count` int DEFAULT '1',
  `icon_path` varchar(255) DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `markers`
--

CREATE TABLE `markers` (
  `marker_id` int NOT NULL,
  `marker_name` varchar(100) NOT NULL,
  `marker_type` varchar(50) NOT NULL,
  `description` text,
  `latitude` decimal(10,8) NOT NULL,
  `longitude` decimal(11,8) NOT NULL,
  `icon_path` varchar(255) DEFAULT NULL,
  `icon_name` varchar(100) DEFAULT NULL,
  `created_by` int DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `markers`
--

INSERT INTO `markers` (`marker_id`, `marker_name`, `marker_type`, `description`, `latitude`, `longitude`, `icon_path`, `icon_name`, `created_by`, `is_active`, `created_at`, `updated_at`) VALUES
(6, 'hahah', 'Building', 'Marker test halah - Jl. Alumni USU (Updated)', 3.56790516, 98.65379333, 'resources\\icons\\fe7a9ca8-8966-41ce-b63b-afa1b55d1f5c.png', 'building_eiffel_landmark_paris_tower_icon_123148.png', 1, 1, '2025-12-01 07:57:59', '2025-12-02 16:24:14'),
(7, 'aldrik', 'Building', 'Marker test aldrik - Jl. Dr. Mansyur (Updated)', 3.56032384, 98.65952253, 'resources\\icons\\2a81beb0-804f-46bd-8388-af64b753a661.png', 'apartmentbuilding_apartamento_3490.png', 1, 0, '2025-12-01 10:13:50', '2025-12-02 11:45:55'),
(8, 'gedung c', 'Building', 'halooo', 3.55779672, 98.65533829, 'resources\\icons\\695548db-92e1-4393-8524-68ba128ec9bd.png', 'apartmentbuilding_apartamento_3490.png', 1, 1, '2025-12-02 12:13:32', '2025-12-02 13:23:40'),
(9, 'GDP', 'Fakultas', 'Drag to position', 3.56130899, 98.65965128, 'resources\\icons\\d8574999-669d-4fcf-9a04-3b8635fb4b40.png', 'apartmentbuilding_apartamento_3490.png', 1, 1, '2025-12-02 14:44:28', '2025-12-02 14:44:43');

-- --------------------------------------------------------

--
-- Table structure for table `reports`
--

CREATE TABLE `reports` (
  `report_id` int NOT NULL,
  `user_nim` varchar(20) COLLATE utf8mb4_unicode_ci NOT NULL,
  `user_name` varchar(100) COLLATE utf8mb4_unicode_ci NOT NULL,
  `location` varchar(255) COLLATE utf8mb4_unicode_ci NOT NULL,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `description` text COLLATE utf8mb4_unicode_ci NOT NULL,
  `report_type` enum('JALAN_RUSAK','JALAN_TERTUTUP','RAMBU_HILANG','LAINNYA') COLLATE utf8mb4_unicode_ci NOT NULL DEFAULT 'LAINNYA',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `is_read` tinyint(1) DEFAULT '0'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

--
-- Dumping data for table `reports`
--

INSERT INTO `reports` (`report_id`, `user_nim`, `user_name`, `location`, `latitude`, `longitude`, `description`, `report_type`, `created_at`, `is_read`) VALUES
(1, 'GUEST', 'Guest User', 'sasdsq', 3.5688000000000075, 98.66179999999999, 'affasfvwefw', 'JALAN_RUSAK', '2025-12-02 16:11:24', 1);

-- --------------------------------------------------------

--
-- Table structure for table `roads`
--

CREATE TABLE `roads` (
  `road_id` int NOT NULL,
  `road_name` varchar(100) NOT NULL,
  `road_type` varchar(20) NOT NULL DEFAULT 'main',
  `start_lat` decimal(10,8) NOT NULL,
  `start_lng` decimal(11,8) NOT NULL,
  `end_lat` decimal(10,8) NOT NULL,
  `end_lng` decimal(11,8) NOT NULL,
  `is_one_way` tinyint(1) DEFAULT '0',
  `direction` enum('normal','reverse') DEFAULT 'normal',
  `is_active` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  `polyline_points` text COMMENT 'Encoded polyline from Google Maps API',
  `google_road_name` varchar(200) DEFAULT NULL COMMENT 'Road name from Google Maps API (e.g., Jl. Alumni)',
  `road_segments` json DEFAULT NULL COMMENT 'Array of road segments with detailed polylines',
  `last_gmaps_update` timestamp NULL DEFAULT NULL COMMENT 'Last time Google Maps data was fetched',
  `distance` double DEFAULT '0' COMMENT 'Jarak dalam meter (Haversine atau dari Google Maps)',
  `description` varchar(500) DEFAULT NULL COMMENT 'Deskripsi jalan'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci COMMENT='Jalan-jalan di USU dengan integrasi Google Maps API untuk routing akurat';

--
-- Dumping data for table `roads`
--

INSERT INTO `roads` (`road_id`, `road_name`, `road_type`, `start_lat`, `start_lng`, `end_lat`, `end_lng`, `is_one_way`, `direction`, `is_active`, `created_at`, `updated_at`, `polyline_points`, `google_road_name`, `road_segments`, `last_gmaps_update`, `distance`, `description`) VALUES
(48, 'Jl. politeknik', 'closed', 3.56237980, 98.65304232, 3.56259396, 98.65615368, 0, 'normal', 1, '2025-12-02 14:38:20', '2025-12-02 14:38:44', '{wvTwdcyQQ?CmBEiDMsH', 'Jl. Tri Dharma', NULL, '2025-12-02 14:38:44', 337, '');

-- --------------------------------------------------------

--
-- Table structure for table `road_closures`
--

CREATE TABLE `road_closures` (
  `closure_id` int NOT NULL,
  `road_id` int DEFAULT NULL,
  `closure_type` enum('temporary','permanent','one_way') NOT NULL,
  `reason` varchar(255) DEFAULT NULL,
  `start_date` date DEFAULT NULL,
  `end_date` date DEFAULT NULL,
  `start_time` time DEFAULT NULL,
  `end_time` time DEFAULT NULL,
  `is_active` tinyint(1) DEFAULT '1',
  `created_by` int DEFAULT NULL,
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `rooms`
--

CREATE TABLE `rooms` (
  `room_id` int NOT NULL,
  `building_id` int NOT NULL,
  `room_code` varchar(50) NOT NULL,
  `room_name` varchar(100) DEFAULT NULL,
  `floor_number` int NOT NULL,
  `room_type` enum('classroom','laboratory','office','auditorium','other') DEFAULT 'classroom',
  `capacity` int DEFAULT NULL,
  `description` text,
  `is_available` tinyint(1) DEFAULT '1',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- --------------------------------------------------------

--
-- Table structure for table `users`
--

CREATE TABLE `users` (
  `user_id` int NOT NULL,
  `nim` varchar(20) NOT NULL,
  `password` varchar(255) NOT NULL,
  `name` varchar(100) NOT NULL,
  `email` varchar(100) DEFAULT NULL,
  `role` enum('admin','user') DEFAULT 'user',
  `created_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

--
-- Dumping data for table `users`
--

INSERT INTO `users` (`user_id`, `nim`, `password`, `name`, `email`, `role`, `created_at`, `updated_at`) VALUES
(1, 'admin', 'admin123', 'Administrator', 'admin@usu.ac.id', 'admin', '2025-11-30 10:21:43', '2025-11-30 10:44:02'),
(2, '2205181001', 'user123', 'User Example', 'user@usu.ac.id', 'user', '2025-11-30 10:21:43', '2025-11-30 10:21:43');

--
-- Indexes for dumped tables
--

--
-- Indexes for table `buildings`
--
ALTER TABLE `buildings`
  ADD PRIMARY KEY (`building_id`),
  ADD UNIQUE KEY `building_code` (`building_code`),
  ADD KEY `idx_type` (`building_type`),
  ADD KEY `idx_active` (`is_active`),
  ADD KEY `idx_buildings_location` (`latitude`,`longitude`);

--
-- Indexes for table `markers`
--
ALTER TABLE `markers`
  ADD PRIMARY KEY (`marker_id`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `idx_type` (`marker_type`),
  ADD KEY `idx_active` (`is_active`),
  ADD KEY `idx_markers_location` (`latitude`,`longitude`);

--
-- Indexes for table `reports`
--
ALTER TABLE `reports`
  ADD PRIMARY KEY (`report_id`),
  ADD KEY `idx_created_at` (`created_at`),
  ADD KEY `idx_is_read` (`is_read`),
  ADD KEY `idx_user_nim` (`user_nim`);

--
-- Indexes for table `roads`
--
ALTER TABLE `roads`
  ADD PRIMARY KEY (`road_id`),
  ADD KEY `idx_active` (`is_active`),
  ADD KEY `idx_roads_location` (`start_lat`,`start_lng`,`end_lat`,`end_lng`),
  ADD KEY `idx_google_road_name` (`google_road_name`);

--
-- Indexes for table `road_closures`
--
ALTER TABLE `road_closures`
  ADD PRIMARY KEY (`closure_id`),
  ADD KEY `road_id` (`road_id`),
  ADD KEY `created_by` (`created_by`),
  ADD KEY `idx_active` (`is_active`),
  ADD KEY `idx_dates` (`start_date`,`end_date`);

--
-- Indexes for table `rooms`
--
ALTER TABLE `rooms`
  ADD PRIMARY KEY (`room_id`),
  ADD KEY `idx_building` (`building_id`),
  ADD KEY `idx_floor` (`floor_number`);

--
-- Indexes for table `users`
--
ALTER TABLE `users`
  ADD PRIMARY KEY (`user_id`),
  ADD UNIQUE KEY `nim` (`nim`);

--
-- AUTO_INCREMENT for dumped tables
--

--
-- AUTO_INCREMENT for table `buildings`
--
ALTER TABLE `buildings`
  MODIFY `building_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=11;

--
-- AUTO_INCREMENT for table `markers`
--
ALTER TABLE `markers`
  MODIFY `marker_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=10;

--
-- AUTO_INCREMENT for table `reports`
--
ALTER TABLE `reports`
  MODIFY `report_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=2;

--
-- AUTO_INCREMENT for table `roads`
--
ALTER TABLE `roads`
  MODIFY `road_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=49;

--
-- AUTO_INCREMENT for table `road_closures`
--
ALTER TABLE `road_closures`
  MODIFY `closure_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=15;

--
-- AUTO_INCREMENT for table `rooms`
--
ALTER TABLE `rooms`
  MODIFY `room_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=6;

--
-- AUTO_INCREMENT for table `users`
--
ALTER TABLE `users`
  MODIFY `user_id` int NOT NULL AUTO_INCREMENT, AUTO_INCREMENT=3;

--
-- Constraints for dumped tables
--

--
-- Constraints for table `markers`
--
ALTER TABLE `markers`
  ADD CONSTRAINT `markers_ibfk_1` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `road_closures`
--
ALTER TABLE `road_closures`
  ADD CONSTRAINT `road_closures_ibfk_1` FOREIGN KEY (`road_id`) REFERENCES `roads` (`road_id`) ON DELETE CASCADE,
  ADD CONSTRAINT `road_closures_ibfk_2` FOREIGN KEY (`created_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL;

--
-- Constraints for table `rooms`
--
ALTER TABLE `rooms`
  ADD CONSTRAINT `rooms_ibfk_1` FOREIGN KEY (`building_id`) REFERENCES `buildings` (`building_id`) ON DELETE CASCADE;
COMMIT;

/*!40101 SET CHARACTER_SET_CLIENT=@OLD_CHARACTER_SET_CLIENT */;
/*!40101 SET CHARACTER_SET_RESULTS=@OLD_CHARACTER_SET_RESULTS */;
/*!40101 SET COLLATION_CONNECTION=@OLD_COLLATION_CONNECTION */;
