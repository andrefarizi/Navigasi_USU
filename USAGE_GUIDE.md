# USAGE GUIDE - PetaUSU Navigation System

## 🚀 Getting Started

### Launching the Application

#### Option 1: Maven
```bash
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"
mvn clean compile
mvn exec:java -Dexec.mainClass="com.mycompany.peta_usu.PETA_USU"
```

#### Option 2: NetBeans IDE
1. Open project di NetBeans
2. Right-click `PETA_USU.java`
3. Select "Run File"

#### Option 3: JAR
```bash
mvn clean package
java -jar target/PETA_USU-1.0-SNAPSHOT.jar
```

---

## 📱 Application Flow

### Welcome Screen
Ketika aplikasi dijalankan, akan muncul Welcome Screen dengan 2 pilihan:

```
┌─────────────────────────────────────┐
│   Selamat Datang di PetaUSU         │
│   Sistem Navigasi Kampus USU        │
├─────────────────────────────────────┤
│                                     │
│   [🗺️  Lihat Peta Kampus]          │  ← USER (No Login)
│                                     │
│   [🔐  Login Admin]                 │  ← ADMIN (Perlu Login)
│                                     │
└─────────────────────────────────────┘
```

---

## 👥 User Flow (Tanpa Login)

### 1. Klik "Lihat Peta Kampus"
- **Tidak perlu login**
- Langsung masuk ke `MapFrame`
- Akses penuh ke peta dan informasi

### 2. Fitur yang Tersedia:
✅ View peta interaktif USU
✅ Lihat semua gedung dan fasilitas
✅ Klik building untuk detail info
✅ Search gedung by name/code
✅ Filter by building type
✅ View room/class information
✅ Responsive design (auto maximize)

### 3. Navigasi:
- **Pan**: Drag map dengan mouse
- **Zoom**: Mouse wheel
- **Search**: Ketik nama gedung di search box
- **Info**: Klik marker untuk detail
- **Back**: Tombol "Kembali" untuk ke Welcome Screen

---

## 🔐 Admin Flow (Dengan Login & Middleware)

### 1. Klik "Login Admin"
Akan membuka `LoginFrame`

### 2. Login Credentials
```
Default Admin:
Username: admin
Password: admin123
```

### 3. Middleware Check
- **AuthMiddleware.authenticateAdmin()** akan validate:
  ✓ Username dan password benar
  ✓ User role = ADMIN
  ✗ Jika bukan admin → Login ditolak

### 4. Setelah Login Berhasil
- Masuk ke `utamaadmin` (Admin Dashboard)
- Middleware menyimpan session di `AuthMiddleware.currentUser`

### 5. Menu Admin Panel

#### A. Management Menu
```
📍 Map Markers
   ├─ View all markers
   ├─ Add new marker
   ├─ Edit marker position
   ├─ Upload custom icon
   ├─ Set lat/lng coordinates
   └─ Delete marker

🚧 Road Closures
   ├─ View active closures
   ├─ Add new closure
   ├─ Set one-way streets
   ├─ Temporary/Permanent closure
   ├─ Date range
   └─ Filter by type

🔄 Refresh
   └─ Reload dashboard
```

#### B. Account Menu
```
👤 Profile
   └─ View/Edit admin profile

🚪 Logout
   └─ Logout dan kembali ke Welcome Screen
```

---

## 🛡️ Security & Middleware

### Middleware Protection
Semua admin pages dilindungi oleh `AuthMiddleware`:

```java
// Di utamaadmin.java
public utamaadmin() {
    try {
        AuthMiddleware.requireAdmin(); // ✅ Check admin
    } catch (SecurityException e) {
        // ❌ Access denied jika bukan admin
        JOptionPane.showMessageDialog(null, "Access Denied!");
        System.exit(0);
    }
}
```

### Access Rules
| Page | User Access | Admin Access | Login Required |
|------|-------------|--------------|----------------|
| Welcome Screen | ✅ | ✅ | ❌ |
| MapFrame | ✅ | ✅ | ❌ |
| LoginFrame | ❌ | ✅ | ❌ |
| utamaadmin | ❌ | ✅ | ✅ |
| AdminMapPanel | ❌ | ✅ | ✅ |
| RoadClosurePanel | ❌ | ✅ | ✅ |
| profilUser | ❌ | ✅ | ✅ |

---

## 📋 Step-by-Step Usage

### For Regular Users:

1. **Launch App** → Welcome Screen muncul
2. **Klik "Lihat Peta Kampus"** → MapFrame terbuka (maximized)
3. **Explore Map:**
   - Drag untuk pan
   - Scroll untuk zoom
   - Klik marker untuk info gedung
4. **Search Building:**
   - Ketik di search box
   - Pilih dari dropdown
   - Info otomatis muncul
5. **View Details:**
   - Klik "Show Details" button
   - Dialog dengan 3 tabs muncul:
     * Building Info (alamat, koordinat, dll)
     * Rooms/Classes (daftar ruangan)
     * Facilities (fasilitas gedung)
6. **Back** → Kembali ke Welcome Screen

### For Admin:

1. **Launch App** → Welcome Screen muncul
2. **Klik "Login Admin"** → LoginFrame terbuka
3. **Enter Credentials:**
   ```
   Username: admin
   Password: admin123
   ```
4. **Submit Login** → Middleware check
   - ✅ Success → utamaadmin opens
   - ❌ Failed → "Username/password salah atau bukan admin"
5. **Admin Dashboard Opens:**
   - Menu bar dengan Management & Account menu
   - Original utamaadmin GUI tetap ada

6. **Manage Markers:**
   - Menu → Management → Map Markers
   - New window opens dengan AdminMapPanel
   - **Add Marker:**
     * Click "Add Marker"
     * Fill form (name, type, lat, lng)
     * Upload icon (optional)
     * Save → masuk database
   - **Edit/Delete:**
     * Select dari table
     * Click Edit/Delete

7. **Manage Road Closures:**
   - Menu → Management → Road Closures
   - New window opens dengan RoadClosurePanel
   - **Add Closure:**
     * Click "Add Closure"
     * Fill form (road, type, dates)
     * Save → masuk database
   - **Filter:**
     * Dropdown: All, Temporary, Permanent, One-Way

8. **Profile:**
   - Menu → Account → Profile
   - profilUser window opens

9. **Logout:**
   - Menu → Account → Logout
   - Confirm dialog
   - Session cleared
   - Kembali ke Welcome Screen

---

## 🎨 UI Files yang Digunakan

### Existing Files (Modified):
1. **PETA_USU.java** - Main entry point dengan Welcome Screen
2. **LoginFrame.java** - Admin login dengan middleware
3. **MapFrame.java** - User map view (responsive, integrated with DB)
4. **utamaadmin.java** - Admin dashboard dengan menu bar
5. **profilUser.java** - Admin profile (no changes needed)

### New UI Files (Added):
6. **AdminMapPanel.java** - Marker management (launched from utamaadmin menu)
7. **RoadClosurePanel.java** - Road closure management (launched from utamaadmin menu)
8. **BuildingInfoDialog.java** - Building details dialog

---

## 📊 Database Integration

### MapFrame (User)
```java
// Load buildings dari database
BuildingDAO buildingDAO = new BuildingDAO();
List<Building> buildings = buildingDAO.getAllBuildings();

// Add ke map sebagai markers
for (Building building : buildings) {
    CustomWaypoint wp = new CustomWaypoint(
        building.getBuildingName(),
        new GeoPosition(building.getLatitude(), building.getLongitude()),
        building.getBuildingType().getValue()
    );
    waypoints.add(wp);
}
```

### AdminMapPanel
```java
// CRUD operations
MarkerDAO markerDAO = new MarkerDAO();

// Create
Marker marker = new Marker();
marker.setName("New Building");
marker.setLatitude(3.5690);
marker.setLongitude(98.6560);
markerDAO.insertMarker(marker);

// Read
List<Marker> markers = markerDAO.getAllMarkers();

// Update
markerDAO.updateMarkerPosition(markerId, newLat, newLng);

// Delete
markerDAO.deleteMarker(markerId);
```

---

## 🔧 Troubleshooting

### Problem: "Access Denied" saat buka utamaadmin
**Solution:** Login dulu via LoginFrame, pastikan credentials benar

### Problem: Map tidak muncul/blank
**Solution:** 
- Check internet connection (Google Maps API)
- Check GeoJSON files ada di resources/

### Problem: Database connection error
**Solution:**
- Pastikan MySQL running
- Check credentials di `DatabaseConnection.java`
- Import database schema

### Problem: Window tidak responsive
**Solution:**
- MapFrame auto-maximize saat dibuka
- Resize window untuk test responsiveness

---

## 💡 Tips & Best Practices

### For Users:
- Use search untuk quick find gedung
- Maximize window untuk view lebih luas
- Klik marker untuk detailed info

### For Admin:
- Logout setelah selesai manage
- Upload icon dengan size < 5MB
- Set coordinates dengan presisi 6 decimal places
- Test markers di MapFrame (user view)

---

## 🎯 Feature Highlights

### 1. No Login for Users ✅
- User langsung ke map
- Tidak perlu create account
- Public access ke informasi

### 2. Middleware Protection ✅
- Hanya admin yang bisa login
- Session management
- Secure admin panel

### 3. Responsive Design ✅
- Auto maximize
- Component listener
- Adaptive layout

### 4. Database Integration ✅
- Real-time data dari MySQL
- CRUD operations
- Synchronized views

### 5. Dual Interface ✅
- User: MapFrame (view only, responsive)
- Admin: utamaadmin + panels (full management)

---

## 📞 Support

Jika ada masalah:
1. Check `QUICK_START.md` untuk setup
2. Review `README.md` untuk overview
3. Check database di MySQL Workbench
4. Verify middleware session: `AuthMiddleware.getCurrentUser()`

---

**Version:** 1.0  
**Last Updated:** November 30, 2025  
**Status:** Production Ready ✅
