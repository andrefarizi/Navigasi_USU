# Troubleshooting - PetaUSU

## Masalah: Map Abu-Abu (Tidak Muncul)

### Penyebab & Solusi:

#### 1. Internet Connection
**Gejala**: Map area abu-abu, loading indicators muncul  
**Penyebab**: JXMapViewer2 menggunakan Google Maps tiles yang memerlukan internet  
**Solusi**:
- Pastikan laptop terhubung internet
- Test connection: buka browser → `https://www.google.com/maps`
- Cek firewall tidak memblok Java aplikasi

#### 2. Database Belum Diimport
**Gejala**: Dropdown "Titik Awal" dan "Titik Tujuan" kosong  
**Penyebab**: Data buildings dan markers tidak ada di database  
**Solusi**:
```bash
# Buka MySQL Command Line atau phpMyAdmin
mysql -u root -p

# Buat database (jika belum ada)
CREATE DATABASE navigasi_usu;

# Import data
USE navigasi_usu;
source database/navigasi.sql;

# Verify data
SELECT COUNT(*) FROM buildings;  -- Harus: 10
SELECT COUNT(*) FROM markers;    -- Harus: 5
SELECT COUNT(*) FROM roads;      -- Harus: 21
```

**Atau via phpMyAdmin**:
1. Buka `http://localhost/phpmyadmin`
2. Klik "New" → Database name: `navigasi_usu`
3. Klik "Import" → Choose file: `database/navigasi.sql`
4. Klik "Go"

#### 3. Database Connection Error
**Gejala**: Error di console saat run aplikasi  
**Penyebab**: Credentials salah atau MySQL service tidak running  
**Solusi**:

**A. Cek MySQL Service**:
```powershell
# Windows
Get-Service MySQL*

# Jika stopped
Start-Service MySQL80  # atau MySQL sesuai versi
```

**B. Update Database Credentials**:
Edit `src/main/java/com/mycompany/peta_usu/config/DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // ← Ganti dengan password MySQL Anda
```

**C. Test Connection**:
```bash
mysql -u root -p
# Masukkan password, jika berhasil berarti credentials benar
```

#### 4. Rebuild Project
**Setelah fix database atau credentials**:
```bash
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"
mvn clean compile
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.PETA_USU"
```

---

## Masalah: Dropdown Titik Awal/Tujuan Kosong

### Solusi:
1. **Verify database data ada**:
   ```sql
   USE navigasi_usu;
   SELECT building_name FROM buildings WHERE is_active = 1;
   SELECT marker_name FROM markers WHERE is_active = 1;
   ```
   Harus muncul 10 buildings + 5 markers = 15 locations

2. **Check log output**:
   Saat run aplikasi, cek console output:
   ```
   INFO: Loaded 10 buildings from database
   INFO: Loaded 5 markers from database
   INFO: Combo boxes updated with 15 locations
   ```

3. **Jika log menunjukkan error "Connection refused"**:
   - MySQL service tidak running → Start service
   - Credentials salah → Update DatabaseConnection.java

---

## Masalah: Road Map Panel Abu-Abu (Admin)

### Solusi:
1. **Verify roads data**:
   ```sql
   SELECT COUNT(*) FROM roads WHERE is_active = 1;
   -- Harus: 21 roads
   ```

2. **Check painter attached**:
   Painter seharusnya sudah terpasang otomatis di RoadMapPanel.
   Jika masih abu-abu, restart aplikasi.

3. **Internet connection required**:
   Road map juga menggunakan Google Maps tiles, perlu internet.

---

## Masalah: Maven Command Not Found

### Solusi (Already Installed):
Maven sudah terinstall di `C:\Users\ZEPHYRUS G14\maven`

**Jika masih not found**, restart terminal atau VS Code:
1. Close semua terminal
2. Close VS Code
3. Open VS Code lagi
4. Test: `mvn -version`

**Jika masih error**, add manual ke PATH:
```powershell
$env:Path += ";C:\Users\ZEPHYRUS G14\maven\bin"
mvn -version
```

---

## Masalah: JAVA_HOME Error

### Solusi (Already Set):
JAVA_HOME sudah di-set ke `C:\Program Files\Zulu\zulu-24`

**Jika masih error**:
```powershell
$env:JAVA_HOME = "C:\Program Files\Zulu\zulu-24"
mvn -version
```

---

## Quick Test Checklist

Sebelum run aplikasi, pastikan:
- [x] MySQL service running
- [x] Database `navigasi_usu` sudah diimport
- [x] Internet connection aktif
- [x] Maven terinstall (`mvn -version`)
- [x] JAVA_HOME set (`echo $env:JAVA_HOME`)

**Run Test**:
```bash
mvn clean compile
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.PETA_USU"
```

**Expected Result**:
- Window "Selamat Datang di Peta USU" muncul
- Klik "Lihat Peta Kampus"
- Map muncul dengan tiles (bukan abu-abu)
- Dropdown terisi 15 locations
- Dapat zoom/pan map

---

## Contact & Support

Jika masih ada masalah setelah troubleshooting, check:
1. Console output untuk error messages
2. MySQL error log
3. Windows Event Viewer (untuk Java crashes)
