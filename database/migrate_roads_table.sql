-- ============================================
-- MIGRATION: Add Google Maps fields to roads table
-- Menambahkan kolom untuk menyimpan data dari Google Maps API
-- ============================================

USE navigasi_usu;

-- Add missing columns to roads table
-- Note: MySQL doesn't support IF NOT EXISTS for ALTER TABLE ADD COLUMN
-- So we'll ignore errors if column already exists

-- Add distance column (skip if error - column might exist)
ALTER TABLE roads ADD COLUMN distance DOUBLE DEFAULT 0.0 COMMENT 'Jarak dalam meter (Haversine atau dari Google Maps)';

-- Add description column (skip if error - column might exist)
ALTER TABLE roads ADD COLUMN description VARCHAR(500) DEFAULT NULL COMMENT 'Deskripsi jalan';

-- Add polyline_points column (skip if error - column might exist)
ALTER TABLE roads ADD COLUMN polyline_points TEXT DEFAULT NULL COMMENT 'Encoded polyline dari Google Maps Directions API';

-- Add google_road_name column (skip if error - column might exist)
ALTER TABLE roads ADD COLUMN google_road_name VARCHAR(200) DEFAULT NULL COMMENT 'Nama jalan dari Google Maps (e.g., Jl. Alumni)';

-- Add road_segments column (skip if error - column might exist)
ALTER TABLE roads ADD COLUMN road_segments TEXT DEFAULT NULL COMMENT 'JSON array dari segmen jalan';

-- Add last_gmaps_update column (skip if error - column might exist)
ALTER TABLE roads ADD COLUMN last_gmaps_update TIMESTAMP NULL DEFAULT NULL COMMENT 'Timestamp terakhir fetch dari Google Maps API';

-- Migration completed
SELECT '✅ Migration completed! Tabel roads telah ditambahkan kolom untuk Google Maps integration.' AS Status;
