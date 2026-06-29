package warehouse;

import java.util.*;

/**
 * Struktur data Graph untuk memetakan denah gudang.
 */
public class WarehouseGraph {
    private int totalNodes;
    private Map<Integer, List<Edge>> adjList;
    private Map<Integer, NodeLocation> locations;

    public WarehouseGraph(int totalNodes) {
        this.totalNodes = totalNodes;
        this.adjList = new HashMap<>();
        this.locations = new HashMap<>();
        for (int i = 0; i < totalNodes; i++) {
            adjList.put(i, new ArrayList<>());
        }
    }

    public void registerNode(int id, String name, int x, int y) {
        locations.put(id, new NodeLocation(id, name, x, y));
    }

    public void addEdge(int source, int destination, int weight) {
        // Undirected graph: Robot dapat melintas bolak-balik
        adjList.get(source).add(new Edge(destination, weight));
        adjList.get(destination).add(new Edge(source, weight));
    }

    public int getTotalNodes() { return totalNodes; }
    public Map<Integer, List<Edge>> getAdjList() { return adjList; }
    public Map<Integer, NodeLocation> getLocations() { return locations; }
}
