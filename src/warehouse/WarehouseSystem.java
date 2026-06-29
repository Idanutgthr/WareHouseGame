package warehouse;

import java.awt.*;
import java.util.*;
import java.util.List;
import javax.swing.*;

/**
 * ============================================================================
 * WAREHOUSE ROBOT OPTIMIZATION GAME
 * Responsi Akhir - Struktur Data dan Algoritma (Tema 1: Route Optimization)
 * ============================================================================
 * 
 * Jelas Komponen & Analisis Kompleksitas:
 * 
 * 1. PRIORITY QUEUE (Max-Heap) - java.util.PriorityQueue (dalam OrderManager)
 *    - Kegunaan: Mengelola pesanan (Order) berdasarkan tingkat prioritas (VIP = 5, Urgent = 3, Normal = 1).
 *    - Kompleksitas Waktu:
 *      * Insertion (addOrder): O(log N)
 *      * Extraction (getNextPriorityOrder): O(log N)
 *      * Peek: O(1)
 * 
 * 2. LIFO STACK (Last In, First Out) - java.util.Stack (dalam OrderManager)
 *    - Kegunaan: Mengarsipkan riwayat pesanan yang selesai diproses oleh robot.
 *      Mendukung fitur pembatalan (Undo/Void) dengan cepat untuk mengembalikan stok/status.
 *    - Kompleksitas Waktu:
 *      * Push (archiveCompletedOrder): O(1)
 *      * Pop (undoLastAction): O(1)
 *      * Peek: O(1)
 * 
 * 3. GRAPH (Adjacency List + HashMap) - WarehouseGraph
 *    - Kegunaan: Merepresentasikan tata letak (layout) rak dan jalur di gudang.
 *    - Kompleksitas Waktu:
 *      * Add Edge: O(1)
 *      * Traverse Neighbours: O(deg(V)) di mana deg(V) adalah jumlah tetangga dari node V.
 * 
 * 4. DIJKSTRA ALGORITHM (Shortest Path with Priority Queue) (dalam RouteOptimizer)
 *    - Kegunaan: Mencari jalur tercepat/terpendek untuk robot dari koordinat asal ke target barang.
 *    - Kompleksitas Waktu: O((V + E) log V)
 *      * V = Jumlah Node (titik rak/posisi)
 *      * E = Jumlah Edge (jalur penghubung)
 *      * Menggunakan Priority Queue untuk ekstraksi node dengan jarak terkecil, sehingga sangat efisien.
 */
public class WarehouseSystem {

    public static void main(String[] args) {
        OrderManager manager = new OrderManager();
        RouteOptimizer optimizer = new RouteOptimizer();
        GameState state = new GameState();

        // Cek argumen CLI atau deteksi headless environment
        boolean forceCLI = false;
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--cli") || arg.equalsIgnoreCase("-c")) {
                forceCLI = true;
                break;
            }
        }

        if (GraphicsEnvironment.isHeadless() || forceCLI) {
            // Jalankan mode Terminal CLI interaktif jika sistem headless (tanpa layar)
            runCLIMode(manager, optimizer, state);
        } else {
            // Jalankan UI Swing modern jika layar tersedia
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception ignored) {}

            SwingUtilities.invokeLater(() -> {
                WarehouseGUI gui = new WarehouseGUI(manager, optimizer, state);
                gui.setVisible(true);
            });
        }
    }

    /**
     * Mode CLI Interaktif (Memenuhi spesifikasi minimal dosen)
     */
    private static void runCLIMode(OrderManager manager, RouteOptimizer optimizer, GameState state) {
        Scanner scanner = new Scanner(System.in);
        WarehouseGraph graph = optimizer.createDefaultWarehouseLayout(state.getUnlockedAreas());

        System.out.println("=====================================================================");
        System.out.println("  WAREHOUSE ROBOT OPTIMIZATION GAME - TERMINAL MODE (PURE JAVA)");
        System.out.println("  Tema 1: Route Optimization (Dijkstra, PriorityQueue, Stack)");
        System.out.println("=====================================================================");

        while (true) {
            System.out.println("\n=== STATS EKONOMI & GUDANG ===");
            System.out.printf("Cash: $%.2f | Robot: %d unit | Area Terbuka: %d/3\n", 
                state.getVirtualCash(), state.getTotalRobots(), state.getUnlockedAreas());
            
            System.out.println("\n=== DAFTAR MENU SISTEM GUDANG ===");
            System.out.println("1. Tambah Order Manual   (Fitur 1 - Push to Heap)");
            System.out.println("2. Lihat Antrean Order   (Fitur 2 - Urutan Masuk)");
            System.out.println("3. Urutkan Prioritas     (Fitur 3 - View Priority Heap)");
            System.out.println("4. Proses Order Utama    (Fitur 5 & 6 - Dijkstra Pathfinding & Dispatch)");
            System.out.println("5. Layout Graf Gudang    (Fitur 4 - View Adjacency List)");
            System.out.println("6. Riwayat Pesanan       (Fitur 7 - LIFO Stack View)");
            System.out.println("7. Pembatalan Item       (Undo/Void Last Order via Stack Pop)");
            System.out.println("8. Toko Upgrade Tycoon   (Ulangi Beli Robot, Upgrade Speed, dll.)");
            System.out.println("9. Keluar");
            System.out.print("Pilih menu (1-9): ");

            int choice = -1;
            try {
                if (scanner.hasNextInt()) {
                    choice = scanner.nextInt();
                    scanner.nextLine();
                } else {
                    scanner.nextLine();
                }
            } catch (Exception e) {
                scanner.nextLine();
            }

            switch (choice) {
                case 1:
                    System.out.print("ID Order (contoh ORD-99): ");
                    String orderId = scanner.nextLine().trim();
                    System.out.print("Nama Barang: ");
                    String itemName = scanner.nextLine().trim();
                    System.out.printf("Pilih ID Node Rak Lokasi Barang (0-%d): ", graph.getTotalNodes() - 1);
                    int nodeId = 0;
                    if (scanner.hasNextInt()) {
                        nodeId = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("ID Node harus angka!");
                        scanner.nextLine();
                        break;
                    }
                    if (nodeId < 0 || nodeId >= graph.getTotalNodes()) {
                        System.out.println("Error: Lokasi tidak valid di luar kapasitas area gudang.");
                        break;
                    }
                    System.out.print("Tingkat Prioritas (1: Normal, 3: Urgent, 5: VIP): ");
                    int priority = 1;
                    if (scanner.hasNextInt()) {
                        priority = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("Prioritas harus angka!");
                        scanner.nextLine();
                        break;
                    }

                    manager.addOrder(new Order(orderId, itemName, nodeId, priority));
                    System.out.println("Sukses: Pesanan ditambahkan ke dalam antrean!");
                    break;

                case 2:
                    System.out.println("\n--- Antrean Pesanan Masuk (Urutan FIFO) ---");
                    Collection<Order> pending = manager.getAllPendingOrders();
                    if (pending.isEmpty()) {
                        System.out.println("(Tidak ada pesanan tertunda)");
                    } else {
                        for (Order o : pending) {
                            System.out.println(" -> " + o);
                        }
                    }
                    break;

                case 3:
                    System.out.println("\n--- Tampilan Heap (Urutan Prioritas) ---");
                    List<Order> sorted = manager.getOrdersSortedByPriority();
                    if (sorted.isEmpty()) {
                        System.out.println("(Tidak ada pesanan di heap)");
                    } else {
                        int index = 1;
                        for (Order o : sorted) {
                            System.out.println(index++ + ". " + o);
                        }
                    }
                    break;

                case 4:
                    System.out.println("\n=== MENGEKUTASI ANTRIAN PRIORITAS GUDANG ===");
                    Order next = manager.getNextPriorityOrder();
                    if (next == null) {
                        System.out.println("Tidak ada pesanan untuk diproses!");
                        break;
                    }

                    System.out.println("Memproses Order Prioritas Tertinggi: " + next);
                    System.out.println("Mengkalkulasi Rute Optimal via Dijkstra...");
                    
                    List<String> logs = new ArrayList<>();
                    List<Integer> path = optimizer.calculateShortestPath(graph, 0, next.getTargetNodeId(), logs);
                    
                    // Cetak logs Dijkstra
                    for (String line : logs) {
                        System.out.println("[DIJKSTRA-LOG] " + line);
                    }

                    System.out.println("\nAnimasi Pergerakan Robot: ");
                    for (int i = 0; i < path.size(); i++) {
                        System.out.print("Robot tiba di Node " + path.get(i));
                        if (i < path.size() - 1) System.out.print(" -> ");
                        try { Thread.sleep(300); } catch (Exception ignored) {}
                    }
                    System.out.println("\n[Selesai] Barang diambil dan dikirim ke Packing Station.");

                    // Simpan ke riwayat (Stack LIFO)
                    long timeSpent = System.currentTimeMillis() - next.getCreationTime();
                    OrderManager.OrderMetrics m = new OrderManager.OrderMetrics();
                    m.durationMs = timeSpent;
                    m.actualDistance = path.size() * 5.0; // estimasi kasar jarak
                    m.efficiency = 1.0;
                    m.processedByRobot = "Robot 1";

                    manager.archiveCompletedOrder(next, m);
                    state.addCash(next.getRewardCash());
                    System.out.printf("Bonus Cash Ditambahkan: +$%.2f!\n", next.getRewardCash());
                    break;

                case 5:
                    System.out.println("\n--- Layout Graph Gudang (Adjacency List) ---");
                    for (Map.Entry<Integer, List<Edge>> entry : graph.getAdjList().entrySet()) {
                        int u = entry.getKey();
                        System.out.print("Node " + u + " (" + graph.getLocations().get(u).getName() + ") -> ");
                        for (Edge edge : entry.getValue()) {
                            System.out.print("[Node " + edge.getDestination() + ", Bobot: " + edge.getWeight() + "m] ");
                        }
                        System.out.println();
                    }
                    break;

                case 6:
                    System.out.println("\n--- Riwayat Pesanan Selesai (LIFO Stack - Terbaru Paling Atas) ---");
                    Stack<Order> stack = manager.getHistoryStack();
                    if (stack.isEmpty()) {
                        System.out.println("(Belum ada riwayat transaksi)");
                    } else {
                        @SuppressWarnings("unchecked")
                        Stack<Order> clone = (Stack<Order>) stack.clone();
                        while (!clone.isEmpty()) {
                            System.out.println(" [SELESAI] " + clone.pop());
                        }
                    }
                    break;

                case 7:
                    System.out.println("\n=== MEMBATALKAN TRANSAKSI TERAKHIR (Undo/Void via Stack Pop) ===");
                    Order undone = manager.undoLastCompletedOrder();
                    if (undone != null) {
                        System.out.println("BERHASIL: Transaksi [" + undone.getOrderId() + "] telah dibatalkan.");
                        System.out.println("Uang dikurangi, dan barang dikembalikan ke Antrean Aktif!");
                        state.setVirtualCash(Math.max(0, state.getVirtualCash() - undone.getRewardCash()));
                    } else {
                        System.out.println("Gagal: Riwayat kosong, tidak ada transaksi yang bisa di-undo.");
                    }
                    break;

                case 8:
                    System.out.println("\n=== IDLE TYCOON UPGRADE SHOP ===");
                    System.out.println("Uang Anda: $" + String.format("%.2f", state.getVirtualCash()));
                    System.out.println("1. Tambah Armada Robot      (Harga: $" + state.getRobotBuyCost() + ")");
                    System.out.println("2. Upgrade Kecepatan Robot  (Harga: $" + state.getSpeedUpgradeCost() + ")");
                    System.out.println("3. Upgrade Kapasitas Kargo  (Harga: $" + state.getCapacityUpgradeCost() + ")");
                    System.out.println("4. Buka Area Gudang Baru    (Harga: $" + state.getUnlockAreaCost() + ")");
                    System.out.println("5. Kembali");
                    System.out.print("Pilih opsi upgrade: ");
                    
                    int shopChoice = -1;
                    if (scanner.hasNextInt()) shopChoice = scanner.nextInt(); scanner.nextLine();
                    
                    if (shopChoice == 1) {
                        double cost = state.getRobotBuyCost();
                        if (state.spendCash(cost)) {
                            state.incrementRobots();
                            System.out.println("Sukses membeli robot! Total unit: " + state.getTotalRobots());
                        } else {
                            System.out.println("Uang tidak cukup!");
                        }
                    } else if (shopChoice == 2) {
                        double cost = state.getSpeedUpgradeCost();
                        if (state.spendCash(cost)) {
                            state.incrementSpeed();
                            System.out.println("Sukses upgrade kecepatan robot!");
                        } else {
                            System.out.println("Uang tidak cukup!");
                        }
                    } else if (shopChoice == 3) {
                        double cost = state.getCapacityUpgradeCost();
                        if (state.spendCash(cost)) {
                            state.incrementCapacity();
                            System.out.println("Sukses upgrade kapasitas kargo!");
                        } else {
                            System.out.println("Uang tidak cukup!");
                        }
                    } else if (shopChoice == 4) {
                        if (state.getUnlockedAreas() >= 3) {
                            System.out.println("Semua area sudah terbuka!");
                            break;
                        }
                        double cost = state.getUnlockAreaCost();
                        if (state.spendCash(cost)) {
                            state.incrementUnlockedAreas();
                            graph = optimizer.createDefaultWarehouseLayout(state.getUnlockedAreas());
                            System.out.println("Sukses membuka Area " + state.getUnlockedAreas() + "!");
                        } else {
                            System.out.println("Uang tidak cukup!");
                        }
                    }
                    break;

                case 9:
                    System.out.println("Terima kasih telah menggunakan simulator gudang. Keluar...");
                    scanner.close();
                    return;

                default:
                    System.out.println("Opsi tidak terdaftar. Silakan masukkan angka 1-9.");
            }
        }
    }
}
