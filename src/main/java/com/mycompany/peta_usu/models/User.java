package com.mycompany.peta_usu.models;

import java.sql.Timestamp;

/**
 * User Model Class
 * Merepresentasikan user (mahasiswa dan admin)
 * 
 * === 4 PILAR OOP YANG DITERAPKAN ===
 * 
 * 1. ENCAPSULATION (Enkapsulasi):
 *    - Semua atribut (userId, nim, password, dll) bersifat PRIVATE
 *    - Data hanya bisa diakses melalui GETTER dan SETTER
 *    - Tujuan: Melindungi data dari akses langsung, memberi kontrol penuh ke class ini
 *    - Contoh: password tidak bisa diubah sembarangan, harus lewat setPassword()
 * 
 * 2. INHERITANCE (Pewarisan):
 *    - Class ini TIDAK mewarisi class lain (hanya Object dari Java)
 *    - Tapi enum UserRole DI DALAM class ini mewarisi konsep enum Java
 *    - Tujuan: Mengorganisir kode dengan hierarki yang jelas
 * 
 * 3. POLYMORPHISM (Polimorfisme):
 *    - Method toString() di-OVERRIDE dari class Object
 *    - Saat print User object, akan tampil "nama (nim)" bukan alamat memori
 *    - Tujuan: Satu method bisa punya perilaku berbeda tergantung konteks
 *    - Contoh: System.out.println(user) akan memanggil toString() otomatis
 * 
 * 4. ABSTRACTION (Abstraksi):
 *    - Class ini adalah MODEL sederhana yang merepresentasikan user
 *    - Menyembunyikan detail kompleks (database, query) dari pengguna class
 *    - User hanya butuh tahu: "ini ada object User dengan data X Y Z"
 *    - Tidak perlu tahu data berasal dari MySQL, PostgreSQL, atau file
 *    - Tujuan: Memudahkan penggunaan tanpa perlu tahu detail implementasi
 * 
 * @author PETA_USU Team
 */
public class User {
    
    private int userId;
    private String nim;
    private String password;
    private String name;
    private String email;
    private UserRole role;
    private Timestamp createdAt;
    private Timestamp updatedAt;
    
    // === ENCAPSULATION: Semua field PRIVATE ===
    // Tidak bisa diakses langsung dari luar, harus pakai getter/setter
    // Contoh SALAH: user.userId = 99 (error karena private)
    // Contoh BENAR: user.setUserId(99) (lewat method public)
    
    // Enum untuk role user
    // === INHERITANCE: UserRole mewarisi kemampuan enum dari Java ===
    public enum UserRole {
        ADMIN("admin"),
        USER("user");
        
        private final String value;
        
        UserRole(String value) {
            this.value = value;
        }
        
        public String getValue() {
            return value;
        }
        
        public static UserRole fromString(String text) {
            for (UserRole role : UserRole.values()) {
                if (role.value.equalsIgnoreCase(text)) {
                    return role;
                }
            }
            return USER;
        }
    }
    
    // Constructors
    public User() {
        this.role = UserRole.USER;
    }
    
    public User(String nim, String password, String name, String email) {
        this.nim = nim;
        this.password = password;
        this.name = name;
        this.email = email;
        this.role = UserRole.USER;
    }
    
    // ========== ENCAPSULATION: Getters and Setters ==========
    // Akses ke field PRIVATE harus lewat method PUBLIC ini
    // Bisa tambah validasi di setter (contoh: password min 8 karakter)
    // Getters and Setters
    public int getUserId() {
        return userId;
    }
    
    public void setUserId(int userId) {
        this.userId = userId;
    }
    
    public String getNim() {
        return nim;
    }
    
    public void setNim(String nim) {
        this.nim = nim;
    }
    
    public String getPassword() {
        return password;
    }
    
    public void setPassword(String password) {
        this.password = password;
    }
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
    
    public UserRole getRole() {
        return role;
    }
    
    public void setRole(UserRole role) {
        this.role = role;
    }
    
    public Timestamp getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
    
    public Timestamp getUpdatedAt() {
        return updatedAt;
    }
    
    public void setUpdatedAt(Timestamp updatedAt) {
        this.updatedAt = updatedAt;
    }
    
    /**
     * Check if user is admin
     */
    public boolean isAdmin() {
        return this.role == UserRole.ADMIN;
    }
    
    // === POLYMORPHISM: Override method toString() dari class Object ===
    // Method ini mengubah perilaku default toString()
    // Default toString() tampilkan: User@15db9742 (alamat memori)
    // Setelah override tampilkan: "John Doe (2205181001)" (lebih readable)
    // Manfaat: Saat print object, otomatis panggil method ini
    @Override
    public String toString() {
        return name + " (" + nim + ")";
    }
}
