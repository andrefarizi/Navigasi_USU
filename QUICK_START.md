# Quick Start Guide - PetaUSU Navigation System

## Prerequisites
1. Java JDK 20 installed
2. MySQL 8.0 installed dan running
3. Maven installed
4. NetBeans IDE (recommended)

## Installation Steps

### 1. Setup Database
```bash
# Login to MySQL
mysql -u root -p

# Create database
CREATE DATABASE navigasi_usu;
USE navigasi_usu;

# Import schema
source c:/Users/ZEPHYRUS G14/Documents/NetBeansProjects/PETA_USU/database/navigasi_usu_schema.sql
```

### 2. Configure Database Connection
Edit `src/main/java/com/mycompany/peta_usu/config/DatabaseConnection.java`:
```java
private static final String URL = "jdbc:mysql://localhost:3306/navigasi_usu";
private static final String USER = "root";
private static final String PASSWORD = ""; // Your MySQL password
```

### 3. Build Project
```bash
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"
mvn clean install
```

## Running the Application

### Option 1: Run Admin Panel
```bash
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.ui.AdminMainFrame"
```

Or in NetBeans:
1. Open `AdminMainFrame.java`
2. Right-click → Run File

**Default Admin Login:**
- Username: `admin`
- Password: `admin123`

### Option 2: Run User Map View
```bash
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.ui.UserMapFrame"
```

Or in NetBeans:
1. Open `UserMapFrame.java`
2. Right-click → Run File

## Features Overview

### Admin Panel Features
1. **Map Markers Management**
   - Add new markers with custom icons
   - Upload icon files (PNG, JPG, SVG)
   - Edit marker positions and details
   - Delete markers

2. **Road Closure Management**
   - Add road closures
   - Mark one-way streets
   - Set temporary/permanent closures
   - Specify closure dates

3. **Dashboard**
   - View statistics
   - Recent activity log

### User Features
1. **Interactive Map**
   - View all campus buildings
   - Click markers for info
   
2. **Building Search**
   - Search by name or code
   - Filter by building type
   
3. **Building Details**
   - View building information
   - See room list
   - Check facilities

## Project Structure
```
PETA_USU/
├── src/main/java/com/mycompany/peta_usu/
│   ├── config/          # Database connection
│   ├── models/          # Entity classes
│   ├── dao/             # Data Access Objects
│   ├── utils/           # Helper utilities
│   └── ui/              # User interface
├── resources/
│   ├── icons/           # Uploaded marker icons
│   ├── area_usu.geojson
│   └── kampus_usu.geojson
└── database/
    └── navigasi_usu_schema.sql
```

## Testing Features

### Test Admin Panel
1. Run `AdminMainFrame`
2. Click "Map Markers" tab
3. Click "Add Marker"
4. Fill form:
   - Name: Test Building
   - Type: Academic
   - Latitude: 3.5690
   - Longitude: 98.6560
5. Upload icon (optional)
6. Click Save

### Test Road Closure
1. Go to "Road Closures" tab
2. Click "Add Closure"
3. Fill form:
   - Road ID: 1
   - Type: Temporary
   - Reason: Maintenance
   - Dates: 2025-12-01 to 2025-12-31
4. Click Save

### Test User Map
1. Run `UserMapFrame`
2. Search for "FK" or "Fakultas"
3. Select building from list
4. Click "Show Details"
5. View building info and rooms

## Troubleshooting

### Database Connection Failed
- Check MySQL service is running
- Verify database credentials in `DatabaseConnection.java`
- Ensure database `navigasi_usu` exists

### Map Not Displaying
- Check internet connection (Google Maps API requires internet)
- Verify API key in `GoogleMapsHelper.java`
- Try refreshing the map

### Icon Upload Failed
- Check `resources/icons/` directory exists
- Verify file size < 5MB
- Use supported formats: PNG, JPG, JPEG, GIF, SVG

### Build Errors
```bash
# Clean and rebuild
mvn clean install -U

# Skip tests if failing
mvn clean install -DskipTests
```

## Default Test Data

### Users
- Admin: `admin` / `admin123`
- User: `user` / `user123`

### Buildings
- FK - Fakultas Kedokteran
- FT - Fakultas Teknik
- FMIPA - Fakultas MIPA
- FH - Fakultas Hukum
- FEB - Fakultas Ekonomi dan Bisnis
- Stadium USU
- Perpustakaan USU
- Masjid USU

## Next Steps
1. Customize marker icons
2. Add more buildings
3. Configure road network
4. Set up road closures
5. Add facility information

## Support
For issues or questions:
- Check `README_IMPLEMENTATION.md` for technical details
- Review `INSTALLATION.md` for setup instructions
- Check database schema in `navigasi_usu_schema.sql`
