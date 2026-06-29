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

    public static class OrderManager implements OrderManagementModule {
        private final List<Order> pendingList = new ArrayList<>();
        private final PriorityQueue<Order> priorityQueue = new PriorityQueue<>();
        private final Stack<Order> completedOrders = new Stack<>();

        @Override
        public void addOrder(Order order) {
            if (order != null) {
                pendingList.add(order);
                priorityQueue.add(order);
            }
        }

        @Override
        public Collection<Order> getAllPendingOrders() {
            // Mengembalikan seluruh daftar antrean pesanan aktif dalam urutan masuk (Fitur
            // 2 - Lihat Order)
            return new ArrayList<>(pendingList);
        }

        @Override
        public Order getNextPriorityOrder() {
            // Mengambil pesanan dengan prioritas tertinggi (Fitur 3 - Priority Queue)
            Order nextOrder = priorityQueue.poll();
            if (nextOrder != null) {
                pendingList.remove(nextOrder);
            }
            return nextOrder;
        }

        @Override
        public void archiveCompletedOrder(Order completedOrder) {
            if (completedOrder != null) {
                completedOrders.push(completedOrder);
            }
        }

        @Override
        public Stack<Order> getHistoryStack() {
            return completedOrders;
        }

        // Helper method untuk menampilkan antrean yang sudah diurutkan berdasarkan
        // prioritas tanpa memodifikasi data
        public Collection<Order> getOrdersSortedByPriority() {
            List<Order> sortedList = new ArrayList<>(pendingList);
            Collections.sort(sortedList);
            return sortedList;
        }
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
         * @param graph Denah gudang aktif
         * @param startNode Posisi node robot saat ini
         * @param endNode Node tujuan (lokasi barang dari data Order)
         * @return List Urutan ID Node yang harus dilewati robot dari start sampai
         *         finish (Lintasan Terpendek).
         */
        List<Integer> calculateShortestPath(WarehouseGraph graph, int startNode, int endNode);
    }

    public static class NodeDistance implements Comparable<NodeDistance> {
        int nodeId;
        int distance;

        public NodeDistance(int nodeId, int distance) {
            this.nodeId = nodeId;
            this.distance = distance;
        }

        @Override
        public int compareTo(NodeDistance other) {
            return Integer.compare(this.distance, other.distance);
        }
    }

    public static class RouteOptimizer implements RouteOptimizationModule {
        @Override
        public WarehouseGraph createDefaultWarehouseLayout() {
            WarehouseGraph graph = new WarehouseGraph(10);
            
            // Tambahkan jalur antartitik rak/lorong dengan bobot jarak
            graph.addEdge(0, 1, 2);
            graph.addEdge(0, 2, 5);
            graph.addEdge(1, 3, 4);
            graph.addEdge(1, 4, 3);
            graph.addEdge(2, 4, 1);
            graph.addEdge(2, 5, 6);
            graph.addEdge(3, 6, 2);
            graph.addEdge(4, 6, 3);
            graph.addEdge(4, 7, 5);
            graph.addEdge(5, 7, 2);
            graph.addEdge(6, 8, 3);
            graph.addEdge(7, 9, 4);
            graph.addEdge(8, 9, 1);
            
            return graph;
        }

        @Override
        public List<Integer> calculateShortestPath(WarehouseGraph graph, int startNode, int endNode) {
            int totalNodes = graph.getTotalNodes();
            
            if (startNode < 0 || startNode >= totalNodes || endNode < 0 || endNode >= totalNodes) {
                return new ArrayList<>();
            }
            
            int[] dist = new int[totalNodes];
            int[] parent = new int[totalNodes];
            Arrays.fill(dist, Integer.MAX_VALUE);
            Arrays.fill(parent, -1);
            
            PriorityQueue<NodeDistance> pq = new PriorityQueue<>();
            dist[startNode] = 0;
            pq.add(new NodeDistance(startNode, 0));
            
            boolean[] visited = new boolean[totalNodes];
            
            while (!pq.isEmpty()) {
                NodeDistance curr = pq.poll();
                int u = curr.nodeId;
                
                if (visited[u]) continue;
                visited[u] = true;
                
                if (u == endNode) break;
                
                List<Edge> edges = graph.getAdjList().get(u);
                if (edges != null) {
                    for (Edge edge : edges) {
                        int v = edge.getDestination();
                        int weight = edge.getWeight();
                        if (!visited[v] && dist[u] != Integer.MAX_VALUE && dist[u] + weight < dist[v]) {
                            dist[v] = dist[u] + weight;
                            parent[v] = u;
                            pq.add(new NodeDistance(v, dist[v]));
                        }
                    }
                }
            }
            
            List<Integer> path = new ArrayList<>();
            if (dist[endNode] == Integer.MAX_VALUE) {
                return path; // No path found
            }
            
            int currNode = endNode;
            while (currNode != -1) {
                path.add(0, currNode);
                currNode = parent[currNode];
            }
            return path;
        }
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
        Scanner scanner = new Scanner(System.in);
        OrderManager manager = new OrderManager();
        RouteOptimizer routeOptimizer = new RouteOptimizer();
        WarehouseGraph graph = routeOptimizer.createDefaultWarehouseLayout();
        int robotPosition = 0; // Posisi awal robot di Node 0

        System.out.println("=======================================================");
        System.out.println(" WAREHOUSE ROBOT OPTIMIZATION GAME - CODE BLUEPRINT");
        System.out.println("=======================================================");

        while (true) {
            System.out.println("\n=== MENU UTAMA SISTEM GUDANG ===");
            System.out.println("1. Tambah Order   (Menambahkan pesanan baru)");
            System.out.println("2. Lihat Order    (Melihat antrean pesanan - Urutan Masuk)");
            System.out.println("3. Priority Queue (Mengurutkan pesanan & proses berdasarkan prioritas)");
            System.out.println("4. Layout Gudang  (Menampilkan graph gudang)");
            System.out.println("5. Shortest Path  (Mencari rute tercepat)");
            System.out.println("6. Simulasi Robot (Robot mengambil barang)");
            System.out.println("7. Riwayat        (Melihat order yang selesai - LIFO Stack)");
            System.out.println("8. Keluar");
            System.out.print("Pilih opsi (1-8): ");

            int choice = -1;
            if (scanner.hasNextInt()) {
                choice = scanner.nextInt();
                scanner.nextLine(); // consume newline
            } else {
                scanner.nextLine(); // clear invalid input
            }

            switch (choice) {
                case 1:
                    System.out.print("Masukkan ID Order: ");
                    String orderId = scanner.nextLine().trim();
                    System.out.print("Masukkan Nama Barang: ");
                    String itemName = scanner.nextLine().trim();
                    System.out.print("Masukkan ID Node Lokasi Barang (Angka): ");
                    int nodeId = 0;
                    if (scanner.hasNextInt()) {
                        nodeId = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("ID Node harus berupa angka. Gagal menambahkan.");
                        scanner.nextLine();
                        break;
                    }
                    System.out.print("Masukkan Prioritas (1 = Biasa, 3 = Penting, 5 = VIP): ");
                    int priority = 1;
                    if (scanner.hasNextInt()) {
                        priority = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("Prioritas harus berupa angka. Gagal menambahkan.");
                        scanner.nextLine();
                        break;
                    }
                    manager.addOrder(new Order(orderId, itemName, nodeId, priority));
                    System.out.println("Order berhasil ditambahkan ke antrean!");
                    break;

                case 2:
                    System.out.println("\n--- Daftar Semua Pesanan Tertunda (Urutan Masuk) ---");
                    Collection<Order> pending = manager.getAllPendingOrders();
                    if (pending.isEmpty()) {
                        System.out.println("(Tidak ada pesanan tertunda)");
                    } else {
                        for (Order o : pending) {
                            System.out.println(" - " + o);
                        }
                    }
                    break;

                case 3:
                    System.out.println("\n--- Daftar Antrean Pesanan (Urutan Prioritas) ---");
                    Collection<Order> sortedPending = manager.getOrdersSortedByPriority();
                    if (sortedPending.isEmpty()) {
                        System.out.println("(Tidak ada pesanan tertunda)");
                        break;
                    }

                    for (Order o : sortedPending) {
                        System.out.println(" [Prioritas " + o.getPriority() + "] " + o);
                    }

                    System.out
                            .print("\nApakah Anda ingin memproses order dengan prioritas tertinggi sekarang? (y/n): ");
                    String answer = scanner.nextLine().trim().toLowerCase();
                    if (answer.equals("y") || answer.equals("ya")) {
                        Order nextOrder = manager.getNextPriorityOrder();
                        if (nextOrder != null) {
                            System.out.println("Memproses: " + nextOrder);
                            manager.archiveCompletedOrder(nextOrder);
                            System.out.println(" -> Selesai dan diarsipkan ke riwayat (Fitur 7).");
                        }
                    }
                    break;

                case 4:
                    System.out.println("\n--- DENAH LAYOUT GUDANG ---");
                    for (int i = 0; i < graph.getTotalNodes(); i++) {
                        System.out.print("Node " + i);
                        if (i == robotPosition) {
                            System.out.print(" [Robot Saat Ini]");
                        }
                        System.out.print(" terhubung ke: ");
                        List<Edge> edges = graph.getAdjList().get(i);
                        for (int j = 0; j < edges.size(); j++) {
                            Edge e = edges.get(j);
                            System.out.print("Node " + e.getDestination() + " (Jarak: " + e.getWeight() + "m)");
                            if (j < edges.size() - 1) {
                                System.out.print(", ");
                            }
                        }
                        System.out.println();
                    }
                    break;

                case 5:
                    System.out.println("\n--- PENCARIAN RUTE TERCEPAT ---");
                    System.out.print("Masukkan Node Asal (0-" + (graph.getTotalNodes() - 1) + "): ");
                    int startNode = 0;
                    if (scanner.hasNextInt()) {
                        startNode = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("Node Asal harus berupa angka.");
                        scanner.nextLine();
                        break;
                    }
                    System.out.print("Masukkan Node Tujuan (0-" + (graph.getTotalNodes() - 1) + "): ");
                    int endNode = 0;
                    if (scanner.hasNextInt()) {
                        endNode = scanner.nextInt();
                        scanner.nextLine();
                    } else {
                        System.out.println("Node Tujuan harus berupa angka.");
                        scanner.nextLine();
                        break;
                    }
                    List<Integer> path = routeOptimizer.calculateShortestPath(graph, startNode, endNode);
                    if (path.isEmpty()) {
                        System.out.println("Tidak ada jalur yang ditemukan dari Node " + startNode + " ke Node " + endNode);
                    } else {
                        System.out.print("Jalur terpendek: ");
                        for (int i = 0; i < path.size(); i++) {
                            System.out.print(path.get(i));
                            if (i < path.size() - 1) {
                                System.out.print(" -> ");
                            }
                        }
                        System.out.println();
                    }
                    break;

                case 6:
                    Order activeOrder = manager.getNextPriorityOrder();
                    if (activeOrder == null) {
                        System.out.println("\nTidak ada pesanan tertunda untuk disimulasikan.");
                        break;
                    }
                    System.out.println("\n--- SIMULASI ROBOT MENGAMBIL BARANG ---");
                    System.out.println("Pesanan Aktif: " + activeOrder);
                    System.out.println("Posisi Robot Saat Ini: Node " + robotPosition);
                    System.out.println("Lokasi Barang: Node " + activeOrder.getTargetNodeId());

                    List<Integer> route = routeOptimizer.calculateShortestPath(graph, robotPosition, activeOrder.getTargetNodeId());
                    if (route.isEmpty()) {
                        System.out.println("Robot Gagal: Rute ke lokasi barang tidak ditemukan!");
                        manager.addOrder(activeOrder); // Kembalikan order jika gagal
                    } else {
                        System.out.println("Robot bergerak menyusuri rute:");
                        for (int i = 0; i < route.size(); i++) {
                            System.out.print("Node " + route.get(i));
                            if (i < route.size() - 1) {
                                System.out.print(" -> ");
                            }
                            try {
                                Thread.sleep(500); // Simulasi waktu pergerakan robot
                            } catch (InterruptedException e) {
                                Thread.currentThread().interrupt();
                            }
                        }
                        System.out.println("\nRobot sampai di Node " + activeOrder.getTargetNodeId() + " dan mengambil " + activeOrder.getItemName());
                        robotPosition = activeOrder.getTargetNodeId();
                        manager.archiveCompletedOrder(activeOrder);
                        System.out.println("Pesanan selesai dan diarsipkan.");
                    }
                    break;

                case 7:
                    System.out.println("\n--- Riwayat Pesanan Selesai (LIFO - Terbaru Paling Atas) ---");
                    Stack<Order> history = manager.getHistoryStack();
                    if (history.isEmpty()) {
                        System.out.println("(Riwayat kosong)");
                    } else {
                        @SuppressWarnings("unchecked")
                        Stack<Order> tempStack = (Stack<Order>) history.clone();
                        while (!tempStack.isEmpty()) {
                            System.out.println(" - " + tempStack.pop());
                        }
                    }
                    break;

                case 8:
                    System.out.println("Keluar dari program. Terima kasih!");
                    scanner.close();
                    return;

                default:
                    System.out.println("Pilihan tidak valid. Silakan pilih 1-8.");
            }
        }
    }
}
