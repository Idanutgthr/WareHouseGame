package warehouse;

import java.util.List;

/**
 * Interface Modul Optimalisasi Rute (Kontribusi Teman)
 */
public interface RouteOptimizationModule {
    /**
     * LAYOUT GUDANG
     * Melakukan setup awal koordinat titik rak gudang dan jalur koneksinya.
     */
    WarehouseGraph createDefaultWarehouseLayout(int unlockedAreas);

    /**
     * SHORTEST PATH (DIJKSTRA ALGORITHM)
     * Mencari jalur terpendek dari posisi robot saat ini menuju ke node tempat barang berada.
     */
    List<Integer> calculateShortestPath(WarehouseGraph graph, int startNode, int endNode, List<String> algorithmLogs);
}
