package warehouse;

/**
 * Representasi Edge (jalur antar node) beserta bobotnya (jarak/waktu).
 */
public class Edge {
    private int destination;
    private int weight; // Dalam meter atau detik

    public Edge(int destination, int weight) {
        this.destination = destination;
        this.weight = weight;
    }

    public int getDestination() { return destination; }
    public int getWeight() { return weight; }
}
