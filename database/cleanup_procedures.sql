-- ============================================
-- Cleanup Script - Drop All Stored Procedures
-- ============================================

USE navigasi_usu;

-- Drop stored procedures
DROP PROCEDURE IF EXISTS sp_get_active_markers;
DROP PROCEDURE IF EXISTS sp_get_buildings_by_type;
DROP PROCEDURE IF EXISTS sp_check_road_status;

-- Show remaining procedures (should be empty)
SHOW PROCEDURE STATUS WHERE Db = 'navigasi_usu';
