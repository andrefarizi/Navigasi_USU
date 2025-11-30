# PetaUSU - Sistem Navigasi Kampus USU
## Navigation System for Universitas Sumatera Utara

### 📋 Project Overview
Sistem navigasi berbasis peta interaktif untuk Kampus Universitas Sumatera Utara menggunakan Google Maps API dengan arsitektur Object-Oriented Programming (OOP) lanjutan. Aplikasi ini menyediakan antarmuka terpisah untuk pengguna umum dan administrator, dengan sistem autentikasi berbasis middleware untuk keamanan akses.

### ✨ Key Features

#### Admin Features
1. **Marker Management**
   - Add/Edit/Delete markers untuk gedung
   - Upload custom icons (PNG, JPG, SVG, GIF)
   - Drag & drop markers (upcoming)
   - Set marker coordinates (latitude/longitude)
   - Activate/deactivate markers

2. **Road Closure Management**
   - Add road closures
   - Set one-way streets
   - Temporary/permanent closures
   - Date range untuk closures
   - Filter by closure type

3. **Dashboard**
   - View statistics
   - Monitor recent activity
   - Summary of markers and closures

#### User Features
1. **Interactive Map**
   - View all campus buildings
   - Google Maps integration
   - Click markers for info
   - Zoom and pan map

2. **Search & Filter**
   - Search by building name/code
   - Filter by building type
   - View building list

3. **Building Information**
   - Detailed building info
   - View room/class list
   - Floor count
   - Facilities information
   - Address and coordinates

### 🏗️ Architecture

#### Design Patterns
- **Singleton**: DatabaseConnection
- **DAO (Data Access Object)**: All database operations
- **MVC**: Separation of Model, View, Controller

## 🗂️ Project Structure

```
PETA_USU/
├── database/
│   └── navigasi.sql                    # Complete database setup script
├── src/main/java/com/mycompany/peta_usu/
│   ├── config/
│   │   └── DatabaseConnection.java     # Singleton DB connection
│   ├── models/
│   │   ├── Building.java               # Building entity dengan BuildingType enum
│   │   ├── Marker.java                 # Custom marker entity
│   │   ├── Road.java                   # Road dengan RoadType enum & distance calc
│   │   ├── RoadClosure.java            # Road closure dengan ClosureType enum
│   │   ├── Room.java                   # Room/Class dengan RoomType enum
│   │   └── User.java                   # User dengan UserRole enum
│   ├── dao/
│   │   ├── BuildingDAO.java            # Building CRUD operations
│   │   ├── MarkerDAO.java              # Marker CRUD + position update
│   │   ├── RoadDAO.java                # Road CRUD + distance calculation
│   │   ├── RoadClosureDAO.java         # Road closure CRUD
│   │   ├── RoomDAO.java                # Room CRUD
│   │   └── UserDAO.java                # User authentication
│   ├── services/
│   │   └── PathfindingService.java     # A* algorithm untuk routing
│   ├── middleware/
│   │   └── AuthMiddleware.java         # Security & session management
│   ├── utils/
│   │   ├── GoogleMapsHelper.java       # Google Maps API integration
│   │   ├── IconUploadManager.java      # Icon upload handler
│   │   ├── DatabaseDebugger.java       # DB debugging utility
│   │   └── MapRefreshUtil.java         # Map refresh helper
│   ├── ui/
│   │   ├── AdminMainFrame.java         # Main admin window
│   │   ├── AdminMapPanel.java          # Marker management panel
│   │   ├── RoadMapPanel.java           # Road visualization panel
│   │   ├── RoadClosurePanel.java       # Road closure management
│   │   ├── BuildingManagementPanel.java # Building CRUD panel
│   │   ├── RoomManagementPanel.java    # Room CRUD panel
│   │   ├── FacilityManagementPanel.java # Facility CRUD panel
│   │   ├── BuildingInfoDialog.java     # Building details dialog
│   │   └── UserMapFrame.java           # User map view
│   ├── PETA_USU.java                   # Main entry point dengan Welcome Screen
│   ├── LoginFrame.java                 # Admin login dengan middleware
│   ├── MapFrame.java                   # User map view dengan routing
│   ├── utamaadmin.java                 # Admin dashboard
│   └── profilUser.java                 # User profile
├── resources/
│   ├── icons/                          # Uploaded marker icons
│   ├── area_usu.geojson                # USU area polygon
│   └── kampus_usu.geojson              # Campus map data
├── pom.xml                             # Maven dependencies
└── README.md                           # This file
```

---

## 📊 Sample Data Included

### Buildings (10)
1. REKTORAT - Gedung Rektorat USU
2. PERPUS - Perpustakaan Universitas
3. FMIPA - Fakultas MIPA
4. FT - Fakultas Teknik
5. BIOTE - Gedung Bioteknologi
6. STADIUM - Stadium Mini USU
7. MASJID - Masjid Al-Makmur USU
8. AUDITORIUM - Auditorium USU
9. POLTEK - Politeknik Negeri Medan
10. GERBANG - Gerbang Utama USU

### Markers (5)
1. Parkir Gedung Rektorat (parking)
2. Kantin Fakultas MIPA (kantin)
3. Taman Kampus (taman)
4. Halte Bus Kampus (transportasi)
5. ATM Center (fasilitas)

### Roads (21)
- 3 Jalan Utama (main roads)
- 15 Jalan Sekunder (secondary roads)
- 3 Jalan Penghubung (connector roads)

### Rooms (7)
- PERPUS-101: Ruang Baca Utama
- PERPUS-201: Ruang Koleksi
- MIPA-101: Lab Fisika
- MIPA-201: Ruang Kuliah Matematika
- FT-101: Ruang Kuliah Teknik 1
- FT-201: Lab Komputer 1
- FT-301: Ruang Seminar

---

## 🔧 Installation Guide

### Prerequisites
1. **Java JDK 21** (LTS Version)
2. **Apache Maven 3.6+**
3. **MySQL Server 8.0+**
4. **NetBeans IDE 23** (recommended) atau IDE lain

### Step-by-Step Installation

#### 1. Clone Repository (jika dari GitHub)
```bash
git clone https://github.com/andrefarizi/Navigasi_USU.git
cd Navigasi_USU
```

#### 2. Setup Database
```bash
# Login ke MySQL
mysql -u root -p

# Import database script
mysql -u root -p < database/navigasi.sql
```

**Atau menggunakan phpMyAdmin:**
1. Buka phpMyAdmin di browser
2. Klik "New" untuk create database
3. Nama database: `navigasi_usu`
4. Klik "Import"
5. Choose file: `database/navigasi.sql`
6. Klik "Go"

#### 3. Configure Database Connection
Edit file `src/main/java/com/mycompany/peta_usu/config/DatabaseConnection.java`:
```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // Ganti dengan password MySQL Anda
```

#### 4. Configure Google Maps API
Edit file `src/main/java/com/mycompany/peta_usu/utils/GoogleMapsHelper.java`:
```java
private static final String API_KEY = "YOUR_API_KEY_HERE";
```

**Cara mendapatkan API Key:**
1. Kunjungi [Google Cloud Console](https://console.cloud.google.com/)
2. Create new project atau pilih existing project
3. Enable **Maps JavaScript API**
4. Navigate ke Credentials
5. Create credentials → API key
6. Copy API key dan paste ke GoogleMapsHelper.java
7. (Optional) Restrict API key untuk security

#### 5. Install Maven Dependencies
```bash
# Di root project directory
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"

# Download & install dependencies
mvn clean install
```

#### 6. Run Application

**Option A: Using Maven**
```bash
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.PETA_USU"
```

**Option B: Using NetBeans**
1. Open project di NetBeans
2. Klik kanan `PETA_USU.java`
3. Select "Run File" (Shift+F6)

**Option C: Using JAR**
```bash
mvn clean package
java -jar target/PETA_USU-1.0-SNAPSHOT.jar
```

---

## 🛠️ Troubleshooting

### Problem: Database Connection Failed
**Solution:**
- Pastikan MySQL service sedang running
- Verify credentials di `DatabaseConnection.java`
- Ensure database `navigasi_usu` exists
- Check port 3306 tidak diblok firewall

### Problem: Map Not Displaying (Blank/Gray)
**Solution:**
- Check internet connection (Google Maps requires internet)
- Verify API key valid dan tidak expired
- Check API key restrictions di Google Cloud Console
- Enable Maps JavaScript API di project
- Try refresh map atau restart application

### Problem: Icon Upload Failed
**Solution:**
- Check folder `resources/icons/` exists dan writable
- Verify file size < 5MB
- Use supported formats: PNG, JPG, JPEG, GIF, SVG
- Check file permissions

### Problem: Maven Build Errors
**Solution:**
```bash
# Clean and rebuild dengan force update
mvn clean install -U

# Skip tests jika test failing
mvn clean install -DskipTests

# Clear Maven cache
mvn dependency:purge-local-repository
```

### Problem: "Access Denied" di Admin Panel
**Solution:**
- Login via LoginFrame dengan admin credentials
- Check middleware session: `AuthMiddleware.getCurrentUser()`
- Pastikan role = ADMIN di database

### Problem: Roads Tidak Muncul di Map
**Solution:**
- Check database ada 21 roads dengan `SELECT * FROM roads`
- Verify RoadMapPanel painter terpasang
- Check road coordinates valid (latitude/longitude)
- Try refresh road map panel

### Problem: Dropdown "Titik Awal/Tujuan" Kosong
**Solution:**
- Verify buildings & markers ada di database
- Check `loadBuildingsFromDatabase()` dipanggil
- Ensure `updateLocationComboBoxes()` di EDT thread
- Try restart application

---

## 🧪 Testing Guide

### Test User Features
1. **Launch Application**
   ```bash
   mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.PETA_USU"
   ```

2. **Klik "Lihat Peta Kampus"**
   - Map akan maximize otomatis
   - Verify 10 buildings + 5 markers muncul

3. **Test Search**
   - Ketik "FMIPA" di search box
   - Select dari dropdown
   - Info building akan muncul

4. **Test Routing**
   - Titik Awal: Gerbang Utama USU
   - Titik Tujuan: Fakultas Teknik
   - Klik "Cari Rute"
   - Route path akan ditampilkan di map

5. **Test Building Info**
   - Klik marker building
   - Dialog dengan 3 tabs muncul:
     - Info: building details
     - Rooms: list of rooms
     - Facilities: list of facilities

### Test Admin Features
1. **Login Admin**
   - Klik "Login Admin"
   - Username: `admin`
   - Password: `admin123`
   - Dashboard akan terbuka

2. **Test Marker Management**
   - Menu → Management → Map Markers
   - Klik "Add Marker"
   - Fill form:
     - Name: Test Marker
     - Type: parking
     - Latitude: 3.5670
     - Longitude: 98.6585
   - Upload icon (optional)
   - Save → verify masuk database

3. **Test Road Closure**
   - Menu → Management → Road Closures
   - Klik "Add Closure"
   - Select road: Jl. Alumni (Gerbang Utama)
   - Type: Temporary
   - Reason: Perbaikan jalan
   - Dates: 2025-12-01 to 2025-12-31
   - Save → verify closure active

4. **Test Road Visualization**
   - Open RoadMapPanel
   - Verify roads display:
     - Blue dashed = one-way
     - Black solid = two-way
     - Red = permanent closure
     - Orange = temporary closure

---

## 💡 Tips & Best Practices

### For Users
- Use search untuk quick find gedung
- Maximize window untuk view lebih luas
- Klik marker untuk detailed info
- Save frequent routes untuk akses cepat

### For Admins
- Logout setelah selesai manage
- Upload icons dengan size < 5MB
- Set coordinates dengan precision (6-8 decimal places)
- Test changes di user view setelah update
- Backup database regularly

### For Developers
- Follow OOP principles
- Use DAO pattern untuk database operations
- Implement error handling dengan try-catch
- Add logging untuk debugging
- Comment kode dengan jelas
- Test setiap feature setelah update

---

## 📈 Performance Optimization

### Database
- Indexes pada frequently queried columns
- Use views untuk complex queries
- Connection pooling (future enhancement)

### UI
- SwingUtilities.invokeLater() untuk EDT operations
- Lazy loading untuk large datasets
- Cache frequently accessed data

### Maps
- Use JXMapViewer2 untuk efficient rendering
- Painter pattern untuk custom overlays
- Optimize marker rendering

---

## 🔒 Security Features

### Authentication
- Password hashing dengan BCrypt
- Session management via AuthMiddleware
- Role-based access control (RBAC)

### Authorization
- Middleware protection untuk admin pages
- User role validation
- Session timeout (future enhancement)

### Data Validation
- Input sanitization
- SQL injection prevention (PreparedStatement)
- File upload validation (size, type)

---

## 🚀 Future Enhancements

### Planned Features
1. ✅ Real-time GPS tracking
2. ✅ Mobile app (Android/iOS)
3. ✅ Push notifications untuk road closures
4. ✅ Multi-language support (EN, ID)
5. ✅ Export routes as PDF/Image
6. ✅ Analytics dashboard
7. ✅ User feedback system
8. ✅ Offline map support
9. ✅ Voice navigation
10. ✅ Accessibility features

### Technical Improvements
1. Implement connection pooling
2. Add caching layer (Redis)
3. RESTful API untuk mobile integration
4. WebSocket untuk real-time updates
5. Docker containerization
6. CI/CD pipeline
7. Automated testing (JUnit)
8. Code coverage reporting

---

## 📞 Support & Contact

### Issues & Questions
- **GitHub Issues**: [Report bugs atau request features](https://github.com/andrefarizi/Navigasi_USU/issues)
- **Email**: admin@usu.ac.id

### Documentation
- **Quick Start**: Setup cepat dalam 5 menit
- **API Documentation**: JavaDoc generated docs
- **Database Schema**: Complete ER diagram

### Contributing
Pull requests are welcome! For major changes, please open an issue first.

---

## 📄 License

© 2025 PetaUSU Team - Universitas Sumatera Utara

Project ini dibuat untuk keperluan akademik dan pengembangan sistem navigasi kampus.

---

## 👥 Credits

### Development Team
- **Backend Development**: DAO, Services, Models
- **Frontend Development**: Swing UI, Map Integration
- **Database Design**: MySQL Schema & Optimization
- **API Integration**: Google Maps API

### Technologies Used
- **Java 21 LTS** - Programming Language
- **MySQL 8.0** - Database Management System
- **Google Maps API** - Map Services
- **JXMapViewer2** - Java Map Component
- **Maven** - Build Automation
- **NetBeans 23** - IDE

### Special Thanks
- Universitas Sumatera Utara
- Google Maps Platform
- JXMapViewer2 Community
- Open Source Contributors

---

**Version**: 2.0  
**Last Updated**: November 30, 2025  
**Status**: Production Ready ✅  
**Java Version**: 21 (LTS)  
**Database**: navigasi_usu (MySQL 8.0)

---



### 🗄️ Database Schema

#### Tables (9 total)
1. **users** - Admin dan user accounts
2. **buildings** - Campus buildings (10 gedung)
3. **rooms** - Classrooms and labs (7 ruangan)
4. **markers** - Custom map markers (5 markers)
5. **roads** - Road network (21 jalan)
6. **road_closures** - Road closures dan one-way
7. **facilities** - Building facilities
8. **building_facilities** - Building-facility relationships
9. **icon_uploads** - Uploaded icon metadata

#### Database File
Gunakan `database/navigasi.sql` untuk setup lengkap database dengan semua data USU.

#### Views
- `v_buildings_summary` - Building summary with room count
- `v_active_closures` - Currently active road closures

### 📦 Dependencies
```xml
<dependencies>
    <!-- MySQL Connector -->
    <dependency>
        <groupId>mysql</groupId>
        <artifactId>mysql-connector-java</artifactId>
        <version>8.0.33</version>
    </dependency>
    
    <!-- Google Maps Services -->
    <dependency>
        <groupId>com.google.maps</groupId>
        <artifactId>google-maps-services</artifactId>
        <version>2.2.0</version>
    </dependency>
    
    <!-- JSON -->
    <dependency>
        <groupId>org.json</groupId>
        <artifactId>json</artifactId>
        <version>20231013</version>
    </dependency>
    
    <!-- Apache Commons IO -->
    <dependency>
        <groupId>commons-io</groupId>
        <artifactId>commons-io</artifactId>
        <version>2.15.1</version>
    </dependency>
    
    <!-- SLF4J Simple Logger -->
    <dependency>
        <groupId>org.slf4j</groupId>
        <artifactId>slf4j-simple</artifactId>
        <version>2.0.9</version>
    </dependency>
    
    <!-- JXMapViewer2 -->
    <dependency>
        <groupId>org.jxmapviewer</groupId>
        <artifactId>jxmapviewer2</artifactId>
        <version>2.6</version>
    </dependency>
</dependencies>
```

### 🚀 Quick Start

#### 1. Setup Database
```bash
mysql -u root -p
CREATE DATABASE navigasi_usu;
USE navigasi_usu;
source database/navigasi.sql;
```

**Atau gunakan phpMyAdmin:**
1. Buka phpMyAdmin
2. Klik "New" untuk buat database baru
3. Nama: `navigasi_usu`
4. Import file: `database/navigasi.sql`

#### 2. Configure Connection
Edit `src/main/java/com/mycompany/peta_usu/config/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String USER = "root";
private static final String PASSWORD = "your_mysql_password";
```

#### 3. Configure Google Maps API
Edit `src/main/java/com/mycompany/peta_usu/utils/GoogleMapsHelper.java`:
```java
private static final String API_KEY = "YOUR_GOOGLE_MAPS_API_KEY";
```

**Cara mendapatkan Google Maps API Key:**
1. Kunjungi [Google Cloud Console](https://console.cloud.google.com/)
2. Buat project baru atau pilih project yang ada
3. Enable **Maps JavaScript API**
4. Buat credentials → API Key
5. (Opsional) Restrict API key untuk keamanan

#### 4. Build & Run
```bash
# Build project
mvn clean install

# Run aplikasi
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.PETA_USU"
```

**Atau di NetBeans:**
1. Open project di NetBeans
2. Right-click `PETA_USU.java` → Run File

### 🔑 Default Credentials
```
Admin:
- Username: admin
- Password: admin123

User:
- Username: user  
- Password: user123
```

---

## 📱 Application Flow & Usage Guide

### Welcome Screen
Ketika aplikasi dijalankan, muncul Welcome Screen dengan 2 pilihan:

```
┌─────────────────────────────────────┐
│   Selamat Datang di PetaUSU         │
│   Sistem Navigasi Kampus USU        │
├─────────────────────────────────────┤
│   [🗺️  Lihat Peta Kampus]          │  ← USER (No Login)
│   [🔐  Login Admin]                 │  ← ADMIN (Perlu Login)
└─────────────────────────────────────┘
```

### 👥 User Mode (Tanpa Login)

#### Akses Fitur:
✅ View peta interaktif USU dengan JXMapViewer2
✅ Lihat 10 gedung dan 5 marker lokasi penting
✅ Cari gedung by name/code
✅ Filter by building type
✅ Klik marker untuk info detail
✅ Pencarian rute dari titik A ke B
✅ View daftar ruangan per gedung

#### Navigasi Map:
- **Pan**: Drag map dengan mouse
- **Zoom**: Mouse wheel atau tombol +/-
- **Search**: Ketik nama gedung di search box
- **Route**: Pilih titik awal & tujuan, klik "Cari Rute"
- **Info**: Klik marker untuk detail building

### 🔐 Admin Mode (Dengan Login)

#### 1. Login Admin
- Klik "Login Admin" di Welcome Screen
- Masukkan credentials admin
- **Middleware `AuthMiddleware`** akan validate:
  - Username dan password benar
  - User role = ADMIN
  - Jika valid → masuk Admin Dashboard

#### 2. Admin Dashboard Features

##### A. Map Markers Management
- View all markers dalam table
- Add new marker dengan koordinat custom
- Upload icon (PNG, JPG, SVG) max 5MB
- Edit marker position
- Delete marker
- Filter by marker type

##### B. Road Closures Management
- View active road closures
- Add temporary/permanent closures
- Mark one-way streets
- Set closure date range
- Filter by closure type
- Integrate dengan routing system

##### C. Road Management
- View 21 jalan di USU
- Visualisasi peta jalan:
  - Garis putus-putus biru = One-way road
  - Garis solid hitam = Two-way road
  - Garis merah = Permanent closure
  - Garis oranye = Temporary closure
- Edit road properties
- Manage is_one_way flag

##### D. Building Management
- View 10 buildings
- Add/Edit building info
- Set koordinat dengan precision
- Upload building icon
- Manage building status (active/inactive)

##### E. Room Management
- View 7 rooms di berbagai gedung
- Add classroom/lab/auditorium
- Set capacity dan floor
- Link ke building

#### 3. Security & Middleware

**Middleware Protection:**
```java
// AuthMiddleware.java - Protect admin pages
public static void requireAdmin() throws SecurityException {
    if (currentUser == null || currentUser.getRole() != UserRole.ADMIN) {
        throw new SecurityException("Admin access required");
    }
}
```

**Access Control Matrix:**
| Page | User Access | Admin Access | Login Required |
|------|-------------|--------------|----------------|
| Welcome Screen | ✅ | ✅ | ❌ |
| MapFrame | ✅ | ✅ | ❌ |
| LoginFrame | ❌ | ✅ | ❌ |
| Admin Dashboard | ❌ | ✅ | ✅ |
| Map Markers Panel | ❌ | ✅ | ✅ |
| Road Closures Panel | ❌ | ✅ | ✅ |
| Building Management | ❌ | ✅ | ✅ |

---

## 🏗️ Technical Architecture

### Design Patterns Implemented
1. **Singleton**: DatabaseConnection
2. **DAO Pattern**: All database operations
3. **MVC**: Separation of Model, View, Controller
4. **Middleware Pattern**: AuthMiddleware for security
5. **Factory Pattern**: Icon creation and upload

### OOP Concepts Applied
1. **Encapsulation**: Private fields dengan getters/setters
2. **Inheritance**: DAO classes structure
3. **Polymorphism**: Method overloading
4. **Abstraction**: Interface-based design
5. **Composition**: Model relationships

### Advanced Java Features
1. **Enum Usage**: BuildingType, RoadType, ClosureType, RoomType, UserRole
2. **Business Logic Methods**: 
   - `Building.distanceFrom()` - Haversine formula
   - `RoadClosure.isCurrentlyActive()` - Date validation
   - `Road.calculateDistance()` - Road length calculation
3. **Utility Classes**: GoogleMapsHelper, IconUploadManager, MapRefreshUtil
4. **Static Methods**: Helper functions
5. **Error Handling**: Try-catch blocks dengan logging

### Road Map Rendering System
```java
// RoadMapPanel.java - Render roads dengan painter
Painter<JXMapViewer> roadPainter = new Painter<JXMapViewer>() {
    @Override
    public void paint(Graphics2D g, JXMapViewer map, int w, int h) {
        for (Road road : allRoads) {
            // Determine color berdasarkan closure status
            Color roadColor = Color.BLACK;
            
            // One-way = dashed blue line
            if (road.isOneWay()) {
                roadColor = new Color(0, 100, 200);
                stroke = new BasicStroke(3, CAP_ROUND, JOIN_ROUND, 
                    0, new float[]{10, 5}, 0);
            }
            // Two-way = solid line
            else {
                stroke = new BasicStroke(3);
            }
            
            // Draw road pada map
            g.setColor(roadColor);
            g.setStroke(stroke);
            g.drawLine(x1, y1, x2, y2);
        }
    }
};
```

### Pathfinding System (A* Algorithm)
```java
// PathfindingService.java - Route finding
public List<GeoPosition> findPath(GeoPosition start, GeoPosition end) {
    // Build graph dari roads
    Map<GeoPosition, List<Edge>> graph = buildGraph();
    
    // A* algorithm
    // Consider: road closures, one-way streets, distance
    // Return: optimal path
}
```

---

## 🗂️ Project Structure
