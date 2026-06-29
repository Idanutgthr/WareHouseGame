package warehouse;

/**
 * Representasi Koordinat 2D untuk visualisasi graf gudang di GUI.
 */
public class NodeLocation {
    private int id;
    private String name;
    private int x; // Koordinat X untuk GUI
    private int y; // Koordinat Y untuk GUI

    public NodeLocation(int id, String name, int x, int y) {
        this.id = id;
        this.name = name;
        this.x = x;
        this.y = y;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public int getX() { return x; }
    public int getY() { return y; }
}
