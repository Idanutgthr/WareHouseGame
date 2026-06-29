package warehouse;

import java.util.*;

/**
 * Modul Route Optimization menggunakan algoritma Dijkstra.
 */
public class RouteOptimizer implements RouteOptimizationModule {

    /**
     * Overload default layout dengan 10 node sesuai kodingan teman (dengan koordinat visual).
     */
    public WarehouseGraph createDefaultWarehouseLayout() {
        WarehouseGraph graph = new WarehouseGraph(10);

        // Daftarkan koordinat node untuk visualisasi GUI
        graph.registerNode(0, "Packing Station", 80, 200);
        graph.registerNode(1, "Shelf Row A1", 220, 100);
        graph.registerNode(2, "Shelf Row A2", 220, 300);
        graph.registerNode(3, "Shelf Row B1", 380, 100);
        graph.registerNode(4, "Shelf Row B2", 380, 300);
        graph.registerNode(5, "Shipping Dock", 520, 200);
        graph.registerNode(6, "Heavy Cargo C1", 220, 480);
        graph.registerNode(7, "Heavy Cargo C2", 380, 480);
        graph.registerNode(8, "Hazardous D1", 520, 400);
        graph.registerNode(9, "Refueling Stn", 80, 400);

        // Jalur dari kodingan teman
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

    /**
     * Memetakan node-node rak gudang beserta koordinat X,Y dan jalur penghubungnya.
     */
    public WarehouseGraph createDefaultWarehouseLayout(int unlockedAreas) {
        // Area 1 (Terbuka secara default): 6 Node
        // Area 2 (Premium): Menambah 4 Node tambahan
        // Area 3 (Mega-Warehouse): Menambah 4 Node lagi
        int totalNodes = 6;
        if (unlockedAreas >= 2) totalNodes += 4;
        if (unlockedAreas >= 3) totalNodes += 4;

        WarehouseGraph graph = new WarehouseGraph(totalNodes);

        // Daftarkan koordinat node untuk visualisasi GUI
        // Area 1 Nodes (0 sampai 5)
        graph.registerNode(0, "Packing Station", 80, 200);
        graph.registerNode(1, "Shelf Row A1", 220, 100);
        graph.registerNode(2, "Shelf Row A2", 220, 300);
        graph.registerNode(3, "Shelf Row B1", 380, 100);
        graph.registerNode(4, "Shelf Row B2", 380, 300);
        graph.registerNode(5, "Shipping Dock", 520, 200);

        // Area 1 Edges
        graph.addEdge(0, 1, 6); // Jalur dari packing ke rak A1 (bobot=6)
        graph.addEdge(0, 2, 7); // Jalur dari packing ke rak A2 (bobot=7)
        graph.addEdge(1, 2, 4); // Jalur penghubung lorong A1 - A2
        graph.addEdge(1, 3, 8); // Lorong atas dari A1 ke B1
        graph.addEdge(2, 4, 8); // Lorong bawah dari A2 ke B2
        graph.addEdge(3, 4, 4); // Jalur penghubung lorong B1 - B2
        graph.addEdge(3, 5, 5); // Jalur dari B1 ke shipping dock
        graph.addEdge(4, 5, 6); // Jalur dari B2 ke shipping dock

        // Area 2 Nodes (6 sampai 9)
        if (unlockedAreas >= 2) {
            graph.registerNode(6, "Heavy Cargo C1", 220, 480);
            graph.registerNode(7, "Heavy Cargo C2", 380, 480);
            graph.registerNode(8, "Hazardous Shelves D1", 520, 400);
            graph.registerNode(9, "Refueling Station", 80, 400);

            // Area 2 Edges
            graph.addEdge(2, 6, 5);
            graph.addEdge(4, 7, 5);
            graph.addEdge(6, 7, 6);
            graph.addEdge(7, 8, 4);
            graph.addEdge(5, 8, 5);
            graph.addEdge(0, 9, 4);
            graph.addEdge(9, 6, 8);
        }

        // Area 3 Nodes (10 sampai 13)
        if (unlockedAreas >= 3) {
            graph.registerNode(10, "Cold Storage E1", 300, 30);
            graph.registerNode(11, "Cold Storage E2", 460, 30);
            graph.registerNode(12, "Valuable Items Vault V1", 640, 100);
            graph.registerNode(13, "Express Dispatch Bay", 640, 300);

            // Area 3 Edges
            graph.addEdge(1, 10, 4);
            graph.addEdge(3, 11, 4);
            graph.addEdge(10, 11, 5);
            graph.addEdge(11, 12, 6);
            graph.addEdge(5, 12, 8);
            graph.addEdge(5, 13, 5);
            graph.addEdge(12, 13, 4);
        }

        return graph;
    }

    /**
     * Overload algoritma Dijkstra tiga parameter sesuai kodingan teman.
     */
    public List<Integer> calculateShortestPath(WarehouseGraph graph, int startNode, int endNode) {
        return calculateShortestPath(graph, startNode, endNode, null);
    }

    /**
     * Algoritma Dijkstra untuk pencarian rute terpendek dengan logging opsional.
     */
    public List<Integer> calculateShortestPath(WarehouseGraph graph, int startNode, int endNode, List<String> algorithmLogs) {
        int totalNodes = graph.getTotalNodes();
        int[] distances = new int[totalNodes];
        int[] parent = new int[totalNodes];
        boolean[] visited = new boolean[totalNodes];

        Arrays.fill(distances, Integer.MAX_VALUE);
        Arrays.fill(parent, -1);

        distances[startNode] = 0;

        // Priority Queue menyimpan pasangan [nodeId, distance] terurut menaik berdasarkan distance
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(a -> a[1]));
        pq.add(new int[]{startNode, 0});

        if (algorithmLogs != null) {
            algorithmLogs.add("--- DIJKSTRA PATHFINDING LOG ---");
            algorithmLogs.add("Mulai pencarian rute: Node " + startNode + " -> Node " + endNode);
        }

        while (!pq.isEmpty()) {
            int[] current = pq.poll();
            int u = current[0];
            int distU = current[1];

            if (visited[u]) continue;
            visited[u] = true;

            if (algorithmLogs != null) {
                algorithmLogs.add(String.format("Eksplorasi Node %d (Jarak saat ini: %d)", u, distU));
            }

            if (u == endNode) {
                if (algorithmLogs != null) {
                    algorithmLogs.add("Target Node " + endNode + " tercapai!");
                }
                break;
            }

            // Ambil semua tetangga dari node u
            List<Edge> edges = graph.getAdjList().get(u);
            if (edges != null) {
                for (Edge edge : edges) {
                    int v = edge.getDestination();
                    int weight = edge.getWeight();

                    // Jika ditemukan jalur yang lebih pendek menuju v melalui u
                    if (!visited[v] && distances[u] + weight < distances[v]) {
                        distances[v] = distances[u] + weight;
                        parent[v] = u;
                        pq.add(new int[]{v, distances[v]});

                        if (algorithmLogs != null) {
                            algorithmLogs.add(String.format("  -> Relaksasi jalur ke Node %d: bobot baru = %d", v, distances[v]));
                        }
                    }
                }
            }
        }

        // Rekonstruksi rute dari target ke start (menggunakan backtracking via array parent)
        List<Integer> path = new ArrayList<>();
        if (distances[endNode] == Integer.MAX_VALUE) {
            if (algorithmLogs != null) {
                algorithmLogs.add("Rute TIDAK ditemukan!");
            }
            return path; // Jalur buntu / tidak terhubung
        }

        int curr = endNode;
        while (curr != -1) {
            path.add(0, curr); // Tambahkan ke depan list
            curr = parent[curr];
        }

        if (algorithmLogs != null) {
            algorithmLogs.add("Rute optimal berhasil dirumuskan: " + path.toString() + " dengan total bobot: " + distances[endNode]);
        }

        return path;
    }
}
