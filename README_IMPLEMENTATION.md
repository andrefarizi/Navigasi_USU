# Navigasi USU - Panduan Implementasi

## 📋 Status Implementasi

### ✅ Yang Sudah Dibuat:

1. **Database Schema** (`database/navigasi_usu_schema.sql`)
   - 9 tabel utama: users, buildings, rooms, markers, roads, road_closures, facilities, icon_uploads
   - Views dan stored procedures
   - Sample data untuk testing
   
2. **Config Layer**
   - `DatabaseConnection.java` - Singleton pattern untuk koneksi database

3. **Model Layer** (OOP Classes)
   - `User.java` - Model untuk user/admin
   - `Building.java` - Model untuk gedung dengan method distance calculation
   - `Room.java` - Model untuk ruangan/kelas
   - `Marker.java` - Model untuk custom markers
   - `RoadClosure.java` - Model untuk penutupan jalan

### 🔄 Yang Perlu Dilanjutkan:

4. **DAO (Data Access Object) Layer**
   - BuildingDAO.java
   - MarkerDAO.java
   - RoadClosureDAO.java
   - UserDAO.java

5. **Service Layer**
   - MapService.java (untuk logika bisnis peta)
   - AdminService.java (untuk operasi admin)

6. **UI Layer - Google Maps Integration**
   - Modifikasi MapFrame.java untuk Google Maps API
   - AdminMapPanel.java (untuk admin manage markers)
   - BuildingInfoPanel.java (untuk tampilkan info gedung)

7. **Utilities**
   - IconUploadManager.java (untuk upload & manage icons)
   - CoordinateConverter.java
   - MapRenderer.java

## 🗄️ Setup Database

1. Buka phpMyAdmin
2. Import file `database/navigasi_usu_schema.sql`
3. Database `navigasi_usu` akan otomatis terbuat dengan semua tabel dan sample data

**Default Login:**
- Admin: nim=`admin`, password=`admin123`
- User: nim=`2205181001`, password=`user123`

## 🔧 Konfigurasi

### Database Connection
Edit file `config/DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // Sesuaikan password MySQL Anda
```

### Google Maps API
API Key Anda: `AIzaSyBy-ugy58EBTMwG2TqtBVlPhR8oF3LeMhA`

## 📦 Dependencies yang Diperlukan

Tambahkan ke `pom.xml`:

```xml
<dependencies>
    <!-- MySQL Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Google Maps API (untuk Java Swing) -->
    <dependency>
        <groupId>com.google.maps</groupId>
        <artifactId>google-maps-services</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- JxBrowser (untuk embed Google Maps di Swing) -->
    <!-- Alternatif: Gunakan JavaFX WebView atau JxBrowser -->
    
    <!-- JSON Processing -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20230227</version>
    </dependency>
    
    <!-- Apache Commons IO (untuk file upload) -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.11.0</version>
    </dependency>
</dependencies>
```

## 🎯 Fitur Utama yang Akan Diimplementasi

### Untuk Admin (`utamaadmin.java`):
1. ✅ Upload icon gedung/marker dari laptop
2. ✅ Drag & drop icon ke posisi di map
3. ✅ Save koordinat latitude/longitude ke database
4. ✅ Manage road closures (jalan ditutup/satu arah)
5. ✅ View semua markers dan buildings
6. ✅ Edit/Delete markers

### Untuk User (MapFrame.java):
1. ✅ View peta USU dengan Google Maps
2. ✅ Click pada gedung untuk lihat info detail
3. ✅ Lihat daftar ruangan/kelas dalam gedung
4. ✅ Search gedung/lokasi
5. ✅ View road status (ditutup/satu arah)
6. ✅ Navigation/routing

## 🏗️ Arsitektur (OOP Pattern)

```
Models (Entity)
   ↓
DAO (Data Access)
   ↓
Service (Business Logic)
   ↓
Controller/UI (Presentation)
```

**Design Patterns yang Digunakan:**
- Singleton Pattern (DatabaseConnection)
- DAO Pattern (Data Access Objects)
- MVC Pattern (Model-View-Controller)
- Factory Pattern (untuk create objects)

## 📁 Struktur Project

```
PETA_USU/
├── database/
│   └── navigasi_usu_schema.sql
├── src/main/java/com/mycompany/peta_usu/
│   ├── config/
│   │   └── DatabaseConnection.java
│   ├── models/
│   │   ├── Building.java
│   │   ├── Marker.java
│   │   ├── RoadClosure.java
│   │   ├── Room.java
│   │   └── User.java
│   ├── dao/
│   │   ├── BuildingDAO.java (BELUM)
│   │   ├── MarkerDAO.java (BELUM)
│   │   └── ... (BELUM)
│   ├── services/
│   │   ├── MapService.java (BELUM)
│   │   └── AdminService.java (BELUM)
│   ├── utils/
│   │   ├── IconUploadManager.java (BELUM)
│   │   └── ... (BELUM)
│   ├── LoginFrame.java
│   ├── MapFrame.java (PERLU UPDATE)
│   ├── utamaadmin.java (PERLU UPDATE)
│   └── profilUser.java
└── resources/
    ├── icons/ (folder untuk uploaded icons)
    └── area_usu.geojson
```

## 🚀 Next Steps

Saya akan lanjutkan dengan membuat:
1. DAO classes untuk database operations
2. Service classes untuk business logic
3. Update MapFrame dengan Google Maps
4. Buat AdminMapPanel untuk drag-drop markers
5. Utilities untuk icon upload

Apakah Anda ingin saya lanjutkan membuat file-file tersebut?
