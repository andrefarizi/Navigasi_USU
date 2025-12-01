# 🗺️ Google Maps Integration - PETA USU

## 📋 Ringkasan Perubahan

Sistem routing di PETA USU telah ditingkatkan untuk menggunakan **Google Maps API** sehingga rute mengikuti jalan sebenarnya di kampus, bukan lagi garis lurus berdasarkan latitude dan longitude.

### ✨ Fitur Baru

1. **Routing yang Akurat** 🛣️
   - Rute mengikuti jalan sebenarnya dari Google Maps
   - Polyline detail dengan ratusan titik koordinat
   - Nama jalan otomatis dari Google Maps API (contoh: "Jl. Alumni", "Jl. Perpustakaan")

2. **Visualisasi Penutupan Jalan** 🚧
   - Jalan tertutup ditampilkan dengan warna **MERAH** dari ujung ke ujung
   - Jalan tertutup sementara dengan warna **ORANGE**
   - Jalan satu arah dengan warna **BIRU** (garis putus-putus)
   - Jalan normal dengan warna **HITAM**

3. **Button "Fetch dari Google Maps"** 🗺️
   - Admin bisa mengambil polyline dan nama jalan langsung dari Google Maps
   - Data disimpan ke database untuk performa lebih baik
   - Menampilkan progress dialog saat fetching

## 🗄️ Perubahan Database

### Tabel `roads` - Kolom Baru:

```sql
-- Jalankan script ini untuk update database
-- File: database/update_roads_gmaps.sql

ALTER TABLE roads 
ADD COLUMN polyline_points TEXT COMMENT 'Encoded polyline from Google Maps API',
ADD COLUMN google_road_name VARCHAR(200) COMMENT 'Road name from Google Maps API',
ADD COLUMN road_segments JSON COMMENT 'Array of road segments with detailed polylines',
ADD COLUMN last_gmaps_update TIMESTAMP NULL COMMENT 'Last time Google Maps data was fetched',
ADD INDEX idx_google_road_name (google_road_name);
```

**Cara menjalankan:**
1. Buka MySQL Workbench atau command line
2. Connect ke database `navigasi_usu`
3. Run script: `database/update_roads_gmaps.sql`

## 📦 File-File Baru

### 1. **GoogleMapsRoadService.java**
Service untuk mengambil informasi jalan dari Google Maps API:
- `getRoadInfo()` - Mendapatkan polyline dan nama jalan
- `snapToRoad()` - Snap koordinat ke jalan terdekat
- `getRoadNameAtCoordinate()` - Mendapatkan nama jalan di koordinat tertentu
- `decodePolyline()` / `encodePolyline()` - Encode/decode Google Maps polyline

**Location:** `src/main/java/com/mycompany/peta_usu/services/GoogleMapsRoadService.java`

### 2. **update_roads_gmaps.sql**
SQL script untuk update database schema.

**Location:** `database/update_roads_gmaps.sql`

## 🔧 File-File yang Dimodifikasi

### 1. **Road.java** (Model)
Tambah field:
- `polylinePoints` - Encoded polyline dari Google Maps
- `googleRoadName` - Nama jalan dari Google Maps (contoh: "Jl. Alumni")
- `roadSegments` - JSON array untuk detail segments
- `lastGmapsUpdate` - Timestamp terakhir update dari Google Maps

### 2. **RoadDAO.java** (Data Access)
- Update `insertRoad()` dan `updateRoad()` untuk handle field baru
- Update `mapResultSetToRoad()` dengan backward compatibility (try-catch untuk kolom baru)

### 3. **DirectionsService.java** (Service)
- Extract nama jalan dari HTML instructions Google Maps
- Return `roadName` dan `encodedPolyline` di result
- List semua nama jalan yang dilalui (`roadNames`)

### 4. **RoadMapPanel.java** (Admin UI)
**Fitur Baru:**
- Button **"🗺️ Fetch dari Google Maps"** di button panel
- Method `fetchGoogleMapsData()` - Ambil data dari Google Maps API dengan progress dialog
- Update rendering menggunakan polyline (bukan garis lurus)
- Jalan tertutup tampil **MERAH** dari ujung ke ujung mengikuti polyline

**Cara pakai:**
1. Pilih jalan di tabel
2. Klik "Fetch dari Google Maps"
3. Tunggu proses (muncul progress dialog)
4. Data polyline dan nama jalan disimpan otomatis
5. Peta akan refresh dengan polyline baru

### 5. **MapFrame.java** (User Map)
- Update rendering untuk support polyline dari Google Maps
- Priority: Jalan tertutup = **MERAH**, lebih tebal (strokeWidth = 6)
- Fallback: Jika belum ada polyline, tetap tampilkan garis lurus
- Add method `decodePolyline()` untuk decode Google Maps polyline

## 🎨 Warna Jalan di Peta

| Status Jalan | Warna | Keterangan |
|-------------|-------|------------|
| 🔴 **Tertutup Permanen** | Merah (#DC143C) | Stroke width = 6, solid line |
| 🟠 **Tertutup Sementara** | Orange (#FF8C00) | Stroke width = 6, solid line |
| 🔵 **Satu Arah** | Biru (#0064C8) | Stroke width = 5, dashed line |
| ⚫ **Normal (Dua Arah)** | Hitam | Stroke width = 5, solid line |

## 🚀 Cara Menggunakan

### Admin Dashboard - Manajemen Jalan

1. **Menambah Jalan Baru:**
   - Klik menu "Peta Jalan"
   - Klik "➕ Tambah Jalan"
   - Isi nama jalan, tipe, koordinat awal dan akhir
   - Klik "Simpan"

2. **Fetch Polyline dari Google Maps:**
   - Pilih jalan di tabel
   - Klik "🗺️ Fetch dari Google Maps"
   - Tunggu sampai selesai (muncul notifikasi sukses)
   - Jalan sekarang akan mengikuti rute Google Maps!

3. **Menutup Jalan:**
   - Pilih jalan di tabel atau klik di peta
   - Klik "🚧 Atur Penutupan"
   - Pilih status:
     - **Normal** - Jalan dibuka
     - **Tertutup Sementara** - Orange
     - **Tertutup Permanen** - Merah
   - Jalan akan langsung berubah warna di peta!

### User Map - Melihat Peta

- Jalan tertutup akan tampil **MERAH** dari ujung ke ujung
- Jalan normal tampil hitam
- Saat cari rute, sistem akan hindari jalan yang tertutup
- Rute akan mengikuti jalan sebenarnya (bukan garis lurus)

## 🔑 API Key Google Maps

API Key sudah terkonfigurasi di:
- `DirectionsService.java`
- `GoogleMapsHelper.java`
- `GoogleMapsRoadService.java`

**API Key:** `AIzaSyBy-ugy58EBTMwG2TqtBVlPhR8oF3LeMhA`

**APIs yang digunakan:**
1. **Directions API** - Untuk routing dan polyline
2. **Roads API** - Untuk snap to road
3. **Geocoding API** - Untuk mendapatkan nama jalan

## 📊 Contoh Data

### Sebelum Fetch Google Maps:
```java
Road road = new Road();
road.setRoadName("Jl. Alumni (Gerbang Utama)");
road.setStartLat(3.5650);
road.setStartLng(98.6575);
road.setEndLat(3.5660);
road.setEndLng(98.6580);
road.setPolylinePoints(null); // Belum ada polyline
```
**Tampilan:** Garis lurus dari titik A ke titik B

### Setelah Fetch Google Maps:
```java
road.setPolylinePoints("efu}@wcxiSAA@?@?@?@?@?B?@?...");  // 200+ points
road.setGoogleRoadName("Jl. Alumni");
road.setDistance(1234.56);  // Meters
road.setLastGmapsUpdate(new Timestamp(...));
```
**Tampilan:** Garis mengikuti jalan sebenarnya di Google Maps!

## 🐛 Troubleshooting

### Jalan tidak muncul polyline dari Google Maps
**Solusi:**
1. Pastikan database sudah di-update (run `update_roads_gmaps.sql`)
2. Pilih jalan dan klik "Fetch dari Google Maps"
3. Periksa log untuk error API

### Warna jalan tidak berubah saat ditutup
**Solusi:**
1. Klik "🔄 Refresh" di RoadMapPanel
2. Restart aplikasi
3. Periksa `road_closures` table di database

### Google Maps API error
**Solusi:**
1. Periksa API key masih valid
2. Periksa quota API di Google Cloud Console
3. Periksa koordinat valid (dalam range USU)

## 🎯 Best Practices

1. **Selalu fetch polyline untuk jalan utama** - Jalan utama seperti Jl. Alumni harus di-fetch untuk akurasi maksimal
2. **Update nama jalan** - Setelah fetch, nama jalan otomatis update ke nama dari Google Maps
3. **Refresh peta setelah perubahan** - Klik refresh untuk melihat perubahan terbaru
4. **Tutup jalan dengan bijak** - Pastikan ada rute alternatif sebelum menutup jalan utama

## 📝 Changelog

### Version 2.0 - Google Maps Integration
- ✅ Database schema update (4 kolom baru)
- ✅ GoogleMapsRoadService untuk API integration
- ✅ DirectionsService enhancement dengan road name extraction
- ✅ RoadMapPanel dengan "Fetch dari Google Maps" button
- ✅ MapFrame rendering dengan polyline support
- ✅ Jalan tertutup tampil MERAH dari ujung ke ujung
- ✅ Road model & DAO update untuk polyline fields

## 👥 Credits

**Developer:** PETA_USU Team  
**Google Maps API:** Directions API, Roads API, Geocoding API  
**Date:** December 2025

---

## 🚦 Next Steps

Untuk menggunakan fitur baru:

1. ✅ **Update Database**
   ```bash
   mysql -u root -p navigasi_usu < database/update_roads_gmaps.sql
   ```

2. ✅ **Compile Project**
   ```bash
   mvn clean compile
   ```

3. ✅ **Run Application**
   - Login sebagai admin
   - Buka "Peta Jalan"
   - Pilih jalan dan klik "Fetch dari Google Maps"
   - Lihat hasilnya di peta!

4. ✅ **Test Road Closure**
   - Pilih jalan
   - Klik "Atur Penutupan" → "Tertutup Permanen"
   - Jalan akan tampil **MERAH** di peta!

---

**Selamat menggunakan PETA USU dengan Google Maps Integration! 🎉**
