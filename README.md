# Warehouse Robot Route Optimization Game (Quick Route)
### Responsi Akhir - Struktur Data dan Algoritma

Proyek Penugasan Responsi Akhir mata kuliah Struktur Data dan Algoritma (SDA). Proyek ini memilih **Tema 1: Route Optimization and Network-Based Systems**, dengan ide pengembangan kreatif berupa sebuah game simulasi pergudangan 2D interaktif berbasis **Pure Java (Java Swing & Command Line Interface)** dengan bumbu gameplay **Idle Tycoon**.

===========================================================

LEMBAR PENILAIAN KONTRIBUSI ANGGOTA KELOMPOK

===========================================================

Tema Proyek  : Warehouse Robot Optimization Game

Kelompok     : 1

1. Nama Anggota : Wildan Saputra Wibowo

   NIM          : L0125070
   
   Kontribusi   : Bertanggung jawab pada Modul 2, yaitu
                  Route Optimization & Dijkstra.
                  Mengimplementasikan algoritma Dijkstra,
                  mengembangkan sistem optimasi rute robot,
                  serta mengintegrasikan perhitungan jalur
                  terpendek ke dalam simulasi.

2. Nama Anggota : Muhammad Regan Azfar Aziza
   
   NIM          : L0125054
   
   Kontribusi   : Bertanggung jawab pada Modul 1, yaitu
                  Order & State Management.
                  Mengembangkan sistem pengelolaan order,
                  manajemen status pesanan dan robot,
                  serta logika dasar proses distribusi barang.

3. Nama Anggota : Ridwan Surya Pamuji
   
   NIM          : L0125131
   
   Kontribusi   : Bertanggung jawab pada Modul 3, yaitu
                  GUI Swing, Animasi, dan Integrasi Fondasi
                  Sistem Route & Order.
                  Mengembangkan antarmuka menggunakan Java Swing,
                  membuat animasi pergerakan robot, serta
                  mengintegrasikan seluruh modul menjadi
                  aplikasi yang berjalan secara terpadu.

===========================================================


## 🚀 Fitur Utama Program

1. **Tambah Order Dinamis (Push to Heap)**
   - Pengguna dapat menambahkan pesanan (Order) baru ke dalam sistem dengan menginput ID Order, Nama Barang, ID Node (lokasi rak penyimpanan), dan Tingkat Prioritas.
2. **Lihat Antrean Aktif (FIFO View)**
   - Menampilkan pesanan aktif berdasarkan urutan masuknya (First-In, First-Out) untuk memberikan gambaran waktu tunggu pesanan secara kronologis.
3. **Urutan Antrean Prioritas (Max-Heap Priority Queue)**
   - Menyusun antrean pesanan secara otomatis berdasarkan tingkat kepentingan (VIP = 5, Urgent = 3, Normal = 1). Pesanan dengan prioritas tertinggi akan diletakkan di bagian terdepan antrean untuk diproses terlebih dahulu.
4. **Layout Denah Gudang Interaktif (Graph Representasi)**
   - Tata letak gudang diwakili oleh struktur data Graf (Graph), menghubungkan Packing Station, Lorong-Lorong Rak, Storage Bay, Refueling Area, hingga Cold Storage.
5. **Kalkulasi Rute Tercepat (Algoritma Dijkstra)**
   - Menghitung rute terpendek secara real-time dari posisi robot saat ini menuju titik lokasi barang target. Menampilkan baris demi baris log perhitungan langkah-demi-langkah (edge relaxation) algoritma Dijkstra.
6. **Simulasi & Visualisasi Animasi Robot 2D**
   - Robot divisualisasikan bergerak lancar secara real-time (bukan sekadar lompat titik) menyusuri garis koordinat rute optimal. Menampilkan arah jalan dengan visualisasi panah.
7. **Arsip Riwayat & Pembatalan (LIFO Stack Undo/Void)**
   - Pesanan yang selesai diarsipkan ke dalam Stack (Last-In, First-Out). Sistem mendukung fitur **Undo/Void** di mana transaksi terakhir yang dikerjakan dapat dibatalkan, mengembalikan barang ke antrean aktif, dan menyesuaikan saldo.
8. **Gameplay Idle Tycoon & Sistem Ekonomi**
   - Pemain mendapatkan reward uang virtual untuk setiap pesanan yang berhasil diselesaikan robot. Uang tersebut dapat diinvestasikan kembali untuk:
     - Membeli robot armada baru (menambah pendapatan pasif).
     - Meng-upgrade kecepatan robot (kecepatan animasi pergerakan).
     - Meng-upgrade kapasitas kargo robot (melipatgandakan reward).
     - Membuka Area Gudang Baru (memperluas graf dari 6 node dasar menjadi hingga 14 node kompleks).
   - Memiliki fitur simulasi *Idle/Pasif*, di mana pesanan otomatis masuk secara pasif dan dikerjakan secara pasif untuk memberikan simulasi otomatisasi berkelanjutan.

---

## 🛠️ Struktur Data & Algoritma (Alasan Pemilihan & Kompleksitas)

### 1. Max-Heap Priority Queue (`java.util.PriorityQueue`)
* **Alasan Pemilihan**: Diperlukan pengurutan pesanan berdasarkan prioritas secara dinamis. Bila pesanan baru dengan prioritas tinggi masuk di tengah jalan, sistem harus otomatis menempatkannya di depan tanpa melakukan sorting ulang seluruh array dari awal.
* **Analisis Kompleksitas**:
  * **Penyisipan (add)**: $O(\log N)$ — Memasukkan elemen ke Heap memerlukan penataan ulang (bubble up).
  * **Pengambilan (poll)**: $O(\log N)$ — Mengambil prioritas tertinggi dari root Heap memerlukan restrukturisasi (bubble down).
  * **Introspeksi (peek)**: $O(1)$ — Mengetahui elemen teratas tanpa mengambilnya.

### 2. Stack (`java.util.Stack`)
* **Alasan Pemilihan**: Sangat cocok untuk mengimplementasikan fitur riwayat pesanan yang mendukung pembatalan transaksi terakhir (**Undo/Void**). Berdasarkan prinsip LIFO (Last-In, First-Out), item terakhir yang masuk ke tumpukan adalah item pertama yang dibatalkan.
* **Analisis Kompleksitas**:
  * **Push**: $O(1)$ — Menambahkan riwayat baru di atas tumpukan.
  * **Pop**: $O(1)$ — Menghapus/mengambil riwayat terakhir untuk dibatalkan.

### 3. Graph Adjacency List (`java.util.Map<Integer, List<Edge>>`)
* **Alasan Pemilihan**: Representasi peta gudang yang terdiri dari persimpangan/rak (Node) dan lorong jalan (Edge). Menggunakan Adjacency List (daftar ketetanggaan) berbasis HashMap dan ArrayList karena lebih efisien secara memori dibanding Adjacency Matrix untuk graf gudang yang bersifat *sparse* (longgar, tidak semua rak saling terhubung langsung).
* **Analisis Kompleksitas**:
  * **Penyisipan Sisi (addEdge)**: $O(1)$.
  * **Penelusuran Tetangga (get neighbors)**: $O(\text{deg}(V))$ — Proporsional terhadap jumlah jalan yang terhubung langsung ke titik tersebut.

### 4. Algoritma Dijkstra
* **Alasan Pemilihan**: Menjamin ditemukannya lintasan terpendek (shortest path) dari posisi awal robot ke target barang pada graf berbobot non-negatif. Kami mengoptimalkannya dengan menggunakan Min-Heap Priority Queue untuk memilih node berikutnya dengan jarak terdekat secara instan.
* **Analisis Kompleksitas**: 
  * **Kompleksitas Waktu**: $O((V + E) \log V)$
    * $V$ adalah jumlah Node (rak) dan $E$ adalah jumlah Edge (jalur lorong).
    * Ekstraksi nilai minimum dari Priority Queue sebanyak $V$ kali: $O(V \log V)$.
    * Relaksasi jarak untuk setiap sisi sebanyak $E$ kali: $O(E \log V)$.
    * Total waktu: $O((V + E) \log V)$, jauh lebih cepat daripada algoritma Dijkstra standar $O(V^2)$ jika menggunakan array biasa.

---

## 💻 Panduan Instalasi & Menjalankan Program

### Persyaratan Sistem (Prerequisites)
1. **Java Development Kit (JDK)** versi **8** atau yang lebih baru (Disarankan JDK 11 atau JDK 17).
2. Perangkat terminal / command prompt atau IDE Java pilihan Anda (Eclipse, IntelliJ IDEA, NetBeans, VS Code).

### Langkah Menjalankan via Terminal / CLI

1. **Unduh atau Ekstrak Berkas Proyek**
   Pastikan struktur direktori berkas Java Anda berada di dalam package `warehouse`.
   
2. **Kompilasi Program**
   Buka terminal di direktori induk tempat folder `warehouse` berada (yaitu di dalam folder `src/`), lalu jalankan:
   ```bash
   javac warehouse/WarehouseSystem.java
   ```

3. **Menjalankan Mode GUI Swing (Rekomendasi / Live Demo)**
   Jalankan perintah berikut:
   ```bash
   java warehouse.WarehouseSystem
   ```
   *Program akan mendeteksi lingkungan layar Anda dan otomatis meluncurkan antarmuka grafis 2D yang interaktif!*

4. **Menjalankan Mode Terminal CLI (Jika diuji lewat server headless/SSH)**
   Jalankan program dengan argumen `--cli` atau `-c`:
   ```bash
   java warehouse.WarehouseSystem --cli
   ```

---

## 📦 Penggunaan Library Eksternal
* **Tidak Ada (Pure Java)**. 
* Proyek ini sepenuhnya menggunakan pustaka standar Java Development Kit bawaan (`java.util.*`, `java.awt.*`, `javax.swing.*`), sehingga **tidak memerlukan konfigurasi Gradle atau Maven eksternal**, menjamin kelancaran 100% saat live-demo di depan Dosen Penguji tanpa masalah dependensi yang rusak (broken packages).
