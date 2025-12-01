# ⚡ QUICK FIX - Koordinat Marker

## 🎯 YANG HARUS DILAKUKAN (3 LANGKAH):

### 1️⃣ JALANKAN SQL FIX (WAJIB!)
```bash
cd "c:\Users\ZEPHYRUS G14\Documents\NetBeansProjects\PETA_USU"
mysql -u root -p navigasi_usu < database\fix_marker_coordinates.sql
```

### 2️⃣ JALANKAN APLIKASI
```bash
mvn clean compile exec:java
```

### 3️⃣ TEST ROUTING
- Login sebagai user
- From: **aldrik**  
- To: **halah**
- Klik **Show Route**

---

## ✅ HASIL YANG BENAR:

| Aspek | Nilai yang Benar |
|-------|------------------|
| **Jarak** | ~0.8-1.5 KM (bukan 5,604 KM!) |
| **Polyline** | 50-500 titik (bukan 286,045!) |
| **Rute** | Muncul di peta (kuning + merah) |
| **Performance** | Tidak lag/freeze |

---

## ❌ JIKA MASIH SALAH:

Check log console:
```
INFO: Route distance: 0.85 KM        <-- Harus < 2 KM
INFO: Polyline points: 142           <-- Harus < 1000
```

Jika distance masih 5,604 KM → **SQL belum dijalankan!**

---

📖 Dokumentasi lengkap: `PERBAIKAN_KOORDINAT.md`
