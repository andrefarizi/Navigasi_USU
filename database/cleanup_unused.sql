-- ============================================
-- Cleanup Script: Hapus Tabel dan View yang Tidak Dipakai
-- Database: navigasi_usu
-- ============================================

USE navigasi_usu;

-- Disable foreign key checks untuk hapus tabel
SET FOREIGN_KEY_CHECKS = 0;

-- ============================================
-- 1. HAPUS TABEL YANG TIDAK DIPAKAI
-- ============================================

-- Tabel facilities tidak digunakan di aplikasi
DROP TABLE IF EXISTS facilities;

-- Tabel icon_uploads tidak digunakan (icon path langsung disimpan di markers)
DROP TABLE IF EXISTS icon_uploads;

-- ============================================
-- 2. HAPUS VIEW YANG TIDAK DIPAKAI
-- ============================================

-- View v_buildings_summary tidak digunakan
DROP VIEW IF EXISTS v_buildings_summary;

-- View v_active_closures tidak digunakan
DROP VIEW IF EXISTS v_active_closures;

-- ============================================
-- 3. RESTORE FOREIGN KEY CHECKS
-- ============================================

SET FOREIGN_KEY_CHECKS = 1;

-- ============================================
-- 4. VERIFIKASI TABEL YANG TERSISA
-- ============================================

-- Tabel yang masih digunakan di aplikasi:
-- 1. users (UserDAO)
-- 2. buildings (BuildingDAO)
-- 3. rooms (RoomDAO)
-- 4. markers (MarkerDAO)
-- 5. roads (RoadDAO)
-- 6. road_closures (RoadClosureDAO)

SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024), 2) AS 'Size (MB)'
FROM 
    information_schema.TABLES
WHERE 
    TABLE_SCHEMA = 'navigasi_usu'
    AND TABLE_TYPE = 'BASE TABLE'
ORDER BY 
    TABLE_NAME;

-- ============================================
-- 5. SUCCESS MESSAGE
-- ============================================

SELECT '✅ Cleanup selesai! Tabel dan view yang tidak dipakai sudah dihapus.' AS Status;
SELECT 'Tabel aktif: users, buildings, rooms, markers, roads, road_closures' AS Info;
