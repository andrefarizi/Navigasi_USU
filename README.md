# PetaUSU - Sistem Navigasi Kampus USU
## Navigation System for Universitas Sumatera Utara

### 📋 Project Overview
Sistem navigasi berbasis peta interaktif untuk Kampus Universitas Sumatera Utara menggunakan Google Maps API dengan arsitektur Object-Oriented Programming (OOP) lanjutan.

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

#### Project Structure
```
PETA_USU/
├── src/main/java/com/mycompany/peta_usu/
│   ├── config/
│   │   └── DatabaseConnection.java         # Singleton DB connection
│   ├── models/
│   │   ├── Building.java                   # Building entity with BuildingType enum
│   │   ├── Marker.java                     # Custom marker entity
│   │   ├── RoadClosure.java                # Road closure with ClosureType enum
│   │   ├── Room.java                       # Room/Class entity with RoomType enum
│   │   └── User.java                       # User entity with UserRole enum
│   ├── dao/
│   │   ├── BuildingDAO.java                # Building CRUD operations
│   │   ├── MarkerDAO.java                  # Marker CRUD + position update
│   │   ├── RoadClosureDAO.java             # Road closure CRUD
│   │   ├── RoomDAO.java                    # Room CRUD
│   │   └── UserDAO.java                    # User authentication
│   ├── utils/
│   │   ├── GoogleMapsHelper.java           # Google Maps API integration
│   │   └── IconUploadManager.java          # Icon upload handler
│   └── ui/
│       ├── AdminMainFrame.java             # Main admin window
│       ├── AdminMapPanel.java              # Marker management panel
│       ├── RoadClosurePanel.java           # Road closure management
│       ├── BuildingInfoDialog.java         # Building details dialog
│       └── UserMapFrame.java               # User map view
├── resources/
│   ├── icons/                              # Uploaded marker icons
│   ├── area_usu.geojson                    # USU area polygon
│   └── kampus_usu.geojson                  # Campus map data
├── database/
│   └── navigasi_usu_schema.sql             # Complete database schema
├── pom.xml                                 # Maven dependencies
├── QUICK_START.md                          # Quick start guide
├── INSTALLATION.md                         # Installation guide
└── README_IMPLEMENTATION.md                # Technical documentation
```

### 🗄️ Database Schema

#### Tables (9 total)
1. **users** - Admin dan user accounts
2. **buildings** - Campus buildings
3. **rooms** - Classrooms and labs
4. **markers** - Custom map markers
5. **roads** - Road network
6. **road_closures** - Road closures dan one-way
7. **facilities** - Building facilities
8. **building_facilities** - Building-facility relationships
9. **icon_uploads** - Uploaded icon metadata

#### Views
- `v_buildings_summary` - Building summary with room count
- `v_active_closures` - Currently active road closures

#### Stored Procedures
- `sp_get_active_markers` - Get active markers
- `sp_get_buildings_by_type` - Filter buildings by type

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
</dependencies>
```

### 🚀 Quick Start

#### 1. Setup Database
```bash
mysql -u root -p
CREATE DATABASE navigasi_usu;
USE navigasi_usu;
source database/navigasi_usu_schema.sql;
```

#### 2. Configure Connection
Edit `config/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String USER = "root";
private static final String PASSWORD = "your_password";
```

#### 3. Build & Run
```bash
# Build
mvn clean install

# Run Admin Panel
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.ui.AdminMainFrame"

# Run User Map
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.ui.UserMapFrame"
```

### 🔑 Default Credentials
```
Admin:
- Username: admin
- Password: admin123

User:
- Username: user  
- Password: user123
```

### 📍 Google Maps Configuration
API Key sudah dikonfigurasi di `GoogleMapsHelper.java`:
```java
private static final String API_KEY = "AIzaSyBy-ugy58EBTMwG2TqtBVlPhR8oF3LeMhA";
```

USU Center Coordinates:
```java
public static final double USU_CENTER_LAT = 3.5690;
public static final double USU_CENTER_LNG = 98.6560;
```

### 🧪 Testing

#### Test Admin Features
1. Launch `AdminMainFrame`
2. Navigate to "Map Markers" tab
3. Click "Add Marker"
4. Fill form dan upload icon
5. Save dan refresh map

#### Test User Features
1. Launch `UserMapFrame`
2. Search for "FK" atau "Fakultas"
3. Select building dari list
4. Click "Show Details"
5. View building info, rooms, dan facilities

### 📊 Sample Data

#### Buildings (8)
- FK - Fakultas Kedokteran
- FT - Fakultas Teknik
- FMIPA - Fakultas MIPA
- FH - Fakultas Hukum
- FEB - Fakultas Ekonomi dan Bisnis
- Stadium USU
- Perpustakaan USU
- Masjid USU

#### Roads (3)
- Jalan Dr. Mansyur
- Jalan Universitas
- Jalan Perpustakaan

#### Sample Closures
- Jalan Dr. Mansyur (One Way)
- Jalan Perpustakaan (Temporary)

### 🛠️ Technical Specifications

#### Technologies
- Java 20
- MySQL 8.0
- Google Maps API
- Maven 3.x
- Swing GUI

#### OOP Concepts Applied
1. **Encapsulation**: Private fields dengan getters/setters
2. **Inheritance**: DAO classes structure
3. **Polymorphism**: Method overloading di GoogleMapsHelper
4. **Abstraction**: Interface-based design
5. **Composition**: Model relationships

#### Advanced Features
1. **Enum Usage**: BuildingType, ClosureType, RoomType, UserRole
2. **Business Logic Methods**: 
   - `Building.distanceFrom()` - Haversine formula
   - `RoadClosure.isCurrentlyActive()` - Date validation
3. **Utility Classes**: GoogleMapsHelper, IconUploadManager
4. **Static Methods**: Helper functions
5. **Error Handling**: Try-catch blocks dengan logging

### 📝 Class Descriptions

#### Models
- **Building**: Represents campus buildings dengan coordinates, type, dan floor count
- **Marker**: Custom markers dengan icon support
- **RoadClosure**: Road closures dan one-way streets
- **Room**: Classrooms dan laboratories
- **User**: Authentication dengan role-based access

#### DAO Layer
- **BuildingDAO**: CRUD + search, nearby, filter by type
- **MarkerDAO**: CRUD + update position (drag-drop), filter by type
- **RoadClosureDAO**: CRUD + active closures, filter by type
- **RoomDAO**: CRUD + rooms by building, search
- **UserDAO**: Authentication, registration, password management

#### Utils
- **GoogleMapsHelper**: Static map URL, HTML generation, directions API, distance calculation
- **IconUploadManager**: File selection, validation (5MB max), resize, unique naming (UUID)

#### UI Components
- **AdminMainFrame**: Tabbed interface untuk admin management
- **AdminMapPanel**: Table view + CRUD untuk markers
- **RoadClosurePanel**: Road closure management dengan filtering
- **BuildingInfoDialog**: 3-tab dialog (info, rooms, facilities)
- **UserMapFrame**: Split pane dengan sidebar search dan map display

### 🔮 Future Enhancements
1. ✅ Drag-and-drop markers (UI ready, awaiting JavaScript integration)
2. ✅ Real-time map updates
3. ✅ Route planning dan directions
4. ✅ Mobile responsive design
5. ✅ Export data (CSV, PDF)
6. ✅ Analytics dan reporting
7. ✅ Multi-language support
8. ✅ Offline map caching

### 📞 Support
Untuk pertanyaan atau issues:
- Lihat `QUICK_START.md` untuk petunjuk cepat
- Baca `INSTALLATION.md` untuk setup detail
- Review `README_IMPLEMENTATION.md` untuk dokumentasi teknis

### 📄 License
© 2025 PetaUSU Team - Universitas Sumatera Utara

---
**Version**: 1.0
**Last Updated**: December 2025
**Status**: Production Ready ✅
