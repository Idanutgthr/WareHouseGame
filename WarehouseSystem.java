package warehouse;

import java.util.*;

public class WarehouseSystem {
    // MODEL DATA UTAMA

    /**
     * Merepresentasikan pesanan barang di gudang.
     * Mengimplementasikan Comparable agar otomatis terurut di PriorityQueue.
     */
    public static class Order implements Comparable<Order> {
        private String orderId;
        private String itemName;
        private int targetNodeId; // ID Lokasi barang berada di dalam graf gudang
        private int priority; // Tingkat urgensi (Contoh: 1 = Biasa, 3 = Penting, 5 = VIP)

        public Order(String orderId, String itemName, int targetNodeId, int priority) {
            this.orderId = orderId;
            this.itemName = itemName;
            this.targetNodeId = targetNodeId;
            this.priority = priority;
        }

        public String getOrderId() {
            return orderId;
        }

        public String getItemName() {
            return itemName;
        }

        public int getTargetNodeId() {
            return targetNodeId;
        }

        public int getPriority() {
            return priority;
        }

        @Override
        public int compareTo(Order other) {
            // Pengurutan Max-Heap: Prioritas tinggi akan berada di antrean paling depan
            return Integer.compare(other.getPriority(), this.priority);
        }

        @Override
        public String toString() {
            return "[" + orderId + "] " + itemName + " (Lokasi: Node " + targetNodeId + ", Prioritas: " + priority
                    + ")";
        }
    }

    /**
     * Jalur penghubung antartitik (rak/posisi) di dalam gudang beserta jaraknya.
     */
    public static class Edge {
        private int destination;
        private int weight; // Representasi jarak atau waktu tempuh (misal dalam meter/detik)

        public Edge(int destination, int weight) {
            this.destination = destination;
            this.weight = weight;
        }

        public int getDestination() {
            return destination;
        }

        public int getWeight() {
            return weight;
        }
    }

    /**
     * Struktur data Graph untuk memetakan denah/layout gudang secara keseluruhan.
     */
    public static class WarehouseGraph {
        private int totalNodes;
        // Menggunakan Adjacency List (Map berisi List of Edges) untuk efisiensi memori
        private Map<Integer, List<Edge>> adjList;

        public WarehouseGraph(int totalNodes) {
            this.totalNodes = totalNodes;
            this.adjList = new HashMap<>();
            for (int i = 0; i < totalNodes; i++) {
                adjList.put(i, new ArrayList<>());
            }
        }

        public void addEdge(int source, int destination, int weight) {
            adjList.get(source).add(new Edge(destination, weight));
            adjList.get(destination).add(new Edge(source, weight)); // Undirected: Robot bisa bolak-balik
        }

        public int getTotalNodes() {
            return totalNodes;
        }

        public Map<Integer, List<Edge>> getAdjList() {
            return adjList;
        }
    }

    // MODUL 1: ORDER & STATE MANAGEMENT

    public interface OrderManagementModule {

        /**
         * [FITUR 1] - TAMBAH ORDER
         * Memasukkan order baru yang diinput user ke dalam antrean prioritas.
         * 
         * @param order Objek pesanan baru
         */
        void addOrder(Order order);

        /**
         * [FITUR 2] - LIHAT ORDER
         * Mengembalikan seluruh daftar antrean pesanan aktif untuk ditampilkan di
         * layar.
         * 
         * @return Collection berisi semua order yang belum diproses.
         */
        Collection<Order> getAllPendingOrders();

        /**
         * [FITUR 3] - PRIORITY QUEUE OPERATION
         * Mengambil (poll) pesanan yang memiliki prioritas paling tinggi untuk segera
         * dikerjakan robot.
         * 
         * @return Order dengan prioritas tertinggi, atau null jika antrean kosong.
         */
        Order getNextPriorityOrder();

        /**
         * [FITUR 7] - RIWAYAT PESANAN
         * Menyimpan pesanan yang telah sukses diselesaikan oleh robot ke dalam riwayat.
         * Struktur Data disarankan: Stack (LIFO) agar order yang baru selesai muncul
         * paling atas.
         * 
         * @param completedOrder Order yang sudah selesai diproses
         */
        void archiveCompletedOrder(Order completedOrder);

        /**
         * Mengambil seluruh riwayat pesanan yang sudah selesai untuk ditampilkan.
         * 
         * @return Stack berisi order-order yang telah selesai.
         */
        Stack<Order> getHistoryStack();
    }

    // MODUL 2: CORE ENGINE & ROUTE OPTIMIZATION

    public interface RouteOptimizationModule {

        /**
         * LAYOUT GUDANG
         * Melakukan setup awal koordinat titik rak gudang dan jalur koneksinya.
         * 
         * @return Objek WarehouseGraph yang sudah terisi data node dan edge.
         */
        WarehouseGraph createDefaultWarehouseLayout();

        /**
         * SHORTEST PATH (DIJKSTRA ALGORITHM)
         * Mencari jalur terpendek dari posisi robot saat ini menuju ke node tempat
         * barang berada.
         * Analisis Kompleksitas Waktu wajib dijelaskan saat Demo (O((V + E) log V)
         * dengan PriorityQueue).
         * * @param graph Denah gudang aktif
         * 
         * @param startNode Posisi node robot saat ini
         * @param endNode   Node tujuan (lokasi barang dari data Order)
         * @return List Urutan ID Node yang harus dilewati robot dari start sampai
         *         finish (Lintasan Terpendek).
         */
        List<Integer> calculateShortestPath(WarehouseGraph graph, int startNode, int endNode);
    }

    // MODUL 3: GUI, SIMULATION & INTEGRATION

    public interface WarehouseGUIModule {

        /**
         * Menginisialisasi frame utama game (bisa menggunakan Java Swing JFrame atau
         * JavaFX).
         * Menampilkan layout denah gudang berupa visual titik graf dan tabel order.
         */
        void initializeMainFrame();

        /**
         * [FITUR 6] - SIMULASI ROBOT
         * Menerima hasil rute lintasan berupa List of Nodes dari Anggota 1,
         * lalu menjalankan animasi visual pergerakan robot pada panel GUI
         * langkah-demi-langkah.
         * * Tips: Gunakan javax.swing.Timer untuk animasi pergerakan berkala tanpa
         * membekukan (freeze) UI.
         * 
         * @param path List urutan node lintasan terpendek yang harus dijalani robot.
         */
        void triggerRobotAnimation(List<Integer> path);

        /**
         * Melakukan sinkronisasi data dari Modul 1 (Anggota 2) ke komponen tabel di
         * GUI.
         * Dipanggil setiap kali ada order bertambah, diproses, atau selesai.
         */
        void updateUIComponents(Collection<Order> pending, Stack<Order> history);
    }

    // MAIN ENTRY POINT (Simulasi Alur Kerja Aplikasi / Mock Test)

    public static void main(String[] args) {
        System.out.println("=======================================================");
        System.out.println(" WAREHOUSE ROBOT OPTIMIZATION GAME - CODE BLUEPRINT");
        System.out.println("=======================================================");
        System.out.println("File template sukses di-load.");
        System.out.println("Gunakan struktur interface di atas sebagai 'jembatan' integrasi kelompok.");
        System.out.println("Silakan bagikan file ini ke Anggota 1, 2, dan 3.");
    }
}
