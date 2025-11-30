# 🗺️ Navigasi USU - Installation Guide

## 📋 Prerequisites

Sebelum menjalankan project, pastikan sudah install:

1. **Java Development Kit (JDK) 11 atau lebih tinggi**
2. **Apache Maven 3.6+**
3. **MySQL Server 8.0+**
4. **NetBeans IDE** (optional, tapi direkomendasikan)

## 🔧 Setup Database

### Step 1: Buat Database

```bash
# Login ke MySQL
mysql -u root -p

# Buat database
CREATE DATABASE navigasi_usu;
```

### Step 2: Import Schema

```bash
# Import file SQL
mysql -u root -p navigasi_usu < database/navigasi_usu_schema.sql
```

**Atau** buka phpMyAdmin:
1. Buka `http://localhost/phpmyadmin`
2. Buat database baru: `navigasi_usu`
3. Import file: `database/navigasi_usu_schema.sql`

### Step 3: Konfigurasi Database Connection

Edit file: `src/main/java/com/mycompany/peta_usu/config/DatabaseConnection.java`

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String DB_USER = "root";
private static final String DB_PASSWORD = ""; // Password MySQL Anda
```

## 📦 Install Dependencies

### Menggunakan Maven

```bash
# Di root project directory
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"

# Download & install dependencies
mvn clean install
```

### Manual Download (jika Maven error)

Download JAR files berikut dan letakkan di folder `lib/`:

1. **MySQL Connector** - https://dev.mysql.com/downloads/connector/j/
2. **JSON Library** - https://mvnrepository.com/artifact/org.json/json
3. **Google Maps Services** - https://mvnrepository.com/artifact/com.google.maps/google-maps-services
4. **Apache Commons IO** - https://mvnrepository.com/artifact/commons-io/commons-io

## 🚀 Cara Menjalankan

### Method 1: Menggunakan Maven

```bash
# Compile project
mvn compile

# Run LoginFrame (untuk user)
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.LoginFrame"

# Run utamaadmin (untuk admin)
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.utamaadmin"
```

### Method 2: Menggunakan Java Command

```bash
# Compile
javac -d target/classes -cp "lib/*" src/main/java/com/mycompany/peta_usu/**/*.java

# Run
java -cp "target/classes;lib/*" com.mycompany.peta_usu.LoginFrame
```

### Method 3: Menggunakan NetBeans

1. Buka NetBeans
2. File → Open Project → Pilih folder `PETA_USU`
3. Klik kanan project → Run

## 🔑 Default Login

### Admin Account
- **NIM:** `admin`
- **Password:** `admin123`

### User Account
- **NIM:** `2205181001`
- **Password:** `user123`

## 📁 Struktur Project

```
PETA_USU/
├── database/
│   └── navigasi_usu_schema.sql      # Database schema
├── src/main/java/com/mycompany/peta_usu/
│   ├── config/
│   │   └── DatabaseConnection.java   # Koneksi database (Singleton)
│   ├── models/                       # Entity classes
│   │   ├── Building.java
│   │   ├── Marker.java
│   │   ├── RoadClosure.java
│   │   ├── Room.java
│   │   └── User.java
│   ├── dao/                          # Data Access Objects
│   │   ├── BuildingDAO.java
│   │   ├── MarkerDAO.java
│   │   ├── RoadClosureDAO.java
│   │   ├── RoomDAO.java
│   │   └── UserDAO.java
│   ├── utils/                        # Utilities
│   │   ├── IconUploadManager.java    # Upload icon handler
│   │   └── GoogleMapsHelper.java     # Google Maps helper
│   ├── LoginFrame.java               # Login window
│   ├── MapFrame.java                 # User map view
│   ├── utamaadmin.java              # Admin dashboard
│   └── profilUser.java              # User profile
├── resources/
│   ├── icons/                        # Uploaded icons storage
│   └── area_usu.geojson             # USU area data
├── lib/                              # JAR libraries
├── pom.xml                           # Maven configuration
└── README_IMPLEMENTATION.md
```

## 🎯 Fitur yang Sudah Diimplementasi

### ✅ Database & Backend
- [x] Database schema dengan 9 tabel
- [x] Model classes (OOP entities)
- [x] DAO Layer untuk CRUD operations
- [x] Database connection (Singleton pattern)

### ✅ Utilities
- [x] Icon Upload Manager
  - File chooser untuk pilih icon
  - Validation (size & extension)
  - Upload dengan unique filename
  - Resize image
- [x] Google Maps Helper
  - Generate static map URL
  - Generate embed map URL
  - Generate interactive HTML map
  - Support multiple markers
  - Calculate distance

### 🔄 Yang Perlu Dilengkapi
- [ ] Admin Panel UI (drag-drop markers)
- [ ] MapFrame integration dengan Google Maps
- [ ] Road closure management UI
- [ ] Building info panel
- [ ] Navigation/routing

## 🛠️ Troubleshooting

### Error: Could not find or load main class

**Solusi:**
```bash
# Pastikan compile dari root directory
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"

# Compile dengan classpath yang benar
javac -d target/classes -cp "lib/*" src/main/java/com/mycompany/peta_usu/**/*.java

# Run dengan full package name
java -cp "target/classes;lib/*" com.mycompany.peta_usu.LoginFrame
```

### Error: Connection refused (MySQL)

**Solusi:**
1. Pastikan MySQL server sudah running
2. Check username & password di `DatabaseConnection.java`
3. Pastikan database `navigasi_usu` sudah dibuat

### Error: ClassNotFoundException: com.mysql.cj.jdbc.Driver

**Solusi:**
```bash
# Download MySQL Connector dan letakkan di lib/
# Atau install via Maven:
mvn dependency:copy-dependencies
```

## 📞 Support

Jika ada error atau pertanyaan:
1. Check error log di console
2. Lihat file `README_IMPLEMENTATION.md` untuk detail teknis
3. Pastikan semua dependencies sudah terinstall

## 🔐 Security Note

⚠️ **PENTING untuk Production:**
- Gunakan password hashing (BCrypt) untuk user passwords
- Jangan commit API Key ke repository public
- Gunakan environment variables untuk sensitive data
- Implement proper input validation

## 📝 License

Project ini dibuat untuk tugas Pemrograman Berorientasi Objek Lanjutan.

---
**Navigasi USU** © 2025
