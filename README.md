# PetaUSU - Sistem Navigasi Kampus USU

Sistem navigasi berbasis peta interaktif untuk Kampus Universitas Sumatera Utara menggunakan JXMapViewer2 dan Google Maps API dengan arsitektur Object-Oriented Programming (OOP) lanjutan.

## Deskripsi Aplikasi

**PetaUSU** adalah aplikasi desktop berbasis Java Swing yang menyediakan sistem navigasi interaktif untuk kampus Universitas Sumatera Utara. Aplikasi ini memiliki dua mode akses:

### Mode User (Tanpa Login)
- Melihat peta kampus USU dengan gedung dan marker lokasi penting
- Mencari gedung berdasarkan nama atau kode
- Melihat informasi detail gedung (alamat, koordinat, ruangan, fasilitas)
- Mencari rute dari titik awal ke titik tujuan menggunakan algoritma A*
- Navigasi interaktif dengan zoom dan pan

### Mode Admin (Dengan Login)
- Login dengan credentials admin (middleware authentication)
- Manajemen marker peta (tambah, edit, hapus, upload icon)
- Manajemen penutupan jalan (temporary, permanent, one-way)
- Visualisasi jaringan jalan
- Manajemen gedung dan ruangan
- Dashboard monitoring

## Teknologi yang Digunakan

- **Java 21 LTS** - Bahasa pemrograman
- **MySQL 8.0** - Database management system
- **JXMapViewer2 2.6** - Komponen peta interaktif
- **Google Maps API** - Layanan peta dan geocoding
- **Maven** - Build automation tool
- **Swing** - GUI framework

## Fitur Utama

### 1. Peta Interaktif
- Display gedung kampus USU dengan koordinat real
- Marker lokasi penting (parkir, kantin, taman, halte, ATM)
- Jaringan jalan kampus (main roads, secondary roads, connector roads)
- Zoom, pan, dan klik marker untuk info detail

### 2. Pencarian Rute
- Algoritma A* untuk pathfinding optimal
- Pertimbangkan penutupan jalan dan jalan satu arah
- Display rute di peta dengan garis berwarna
- Informasi jarak dan estimasi waktu

### 3. Manajemen Data (Admin)
- CRUD operations untuk buildings, markers, roads
- Upload custom icons untuk marker (PNG, JPG, SVG)
- Road closure management dengan date range
- Filter dan search functionality

### 4. Visualisasi Jalan
- Garis putus-putus biru = Jalan satu arah (one-way)
- Garis solid hitam = Jalan dua arah (two-way)
- Garis merah = Penutupan permanen
- Garis oranye = Penutupan sementara

