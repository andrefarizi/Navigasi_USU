-- ============================================
-- CLEANUP UNUSED TABLES - Navigasi USU
-- ============================================
-- Tabel yang sudah tidak digunakan karena fitur
-- Building, Room, Facility sudah merge ke Markers
-- ============================================

USE navigasi_usu;

-- Tampilkan tabel yang akan dihapus
SHOW TABLES;

-- ============================================
-- BACKUP DATA (Optional - uncomment jika perlu)
-- ============================================
-- CREATE DATABASE IF NOT EXISTS navigasi_usu_backup;
-- 
-- CREATE TABLE navigasi_usu_backup.buildings_backup AS SELECT * FROM buildings;
-- CREATE TABLE navigasi_usu_backup.rooms_backup AS SELECT * FROM rooms;
-- CREATE TABLE navigasi_usu_backup.facilities_backup AS SELECT * FROM facilities;
-- CREATE TABLE navigasi_usu_backup.icon_uploads_backup AS SELECT * FROM icon_uploads;

-- ============================================
-- HAPUS VIEW YANG DEPEND KE TABEL LAMA
-- ============================================
DROP VIEW IF EXISTS v_buildings_summary;

-- ============================================
-- HAPUS TABEL YANG TIDAK DIGUNAKAN
-- ============================================

-- 1. Hapus tabel facilities (merge ke markers)
DROP TABLE IF EXISTS facilities;
SELECT 'Table facilities dropped!' AS status;

-- 2. Hapus tabel rooms (merge ke markers)
DROP TABLE IF EXISTS rooms;
SELECT 'Table rooms dropped!' AS status;

-- 3. Hapus tabel buildings (merge ke markers)
DROP TABLE IF EXISTS buildings;
SELECT 'Table buildings dropped!' AS status;

-- 4. Hapus tabel icon_uploads (tidak dipakai, icon langsung di markers.icon_path)
DROP TABLE IF EXISTS icon_uploads;
SELECT 'Table icon_uploads dropped!' AS status;

-- ============================================
-- TAMPILKAN TABEL YANG TERSISA
-- ============================================
SHOW TABLES;

-- ============================================
-- TABEL AKTIF YANG MASIH DIGUNAKAN:
-- ============================================
-- 1. markers           - Marker utama (gedung, ruangan, fasilitas)
-- 2. roads             - Peta jalan
-- 3. road_closures     - Penutupan jalan
-- 4. users             - User login
-- 5. v_active_closures - VIEW untuk closure aktif
-- ============================================

-- Cek jumlah data di tabel aktif
SELECT 'Active Tables Summary:' AS info;
SELECT 'markers' AS table_name, COUNT(*) AS total_rows FROM markers
UNION ALL
SELECT 'roads', COUNT(*) FROM roads
UNION ALL
SELECT 'road_closures', COUNT(*) FROM road_closures
UNION ALL
SELECT 'users', COUNT(*) FROM users;

SELECT '✅ Cleanup completed successfully!' AS status;
