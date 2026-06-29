package warehouse;

/**
 * Model data Pesanan (Order).
 * Mengimplementasikan Comparable untuk mendukung pengurutan otomatis di PriorityQueue.
 */
public class Order implements Comparable<Order> {
    private String orderId;
    private String itemName;
    private int targetNodeId; // Lokasi rak tempat barang berada
    private int priority;     // Tingkat prioritas (1: Normal, 3: Urgent, 5: VIP)
    private double rewardCash; // Hadiah uang virtual untuk mode Tycoon
    private long creationTime; // Waktu pembuatan order (ms)

    public Order(String orderId, String itemName, int targetNodeId, int priority) {
        this.orderId = orderId;
        this.itemName = itemName;
        this.targetNodeId = targetNodeId;
        this.priority = priority;
        this.creationTime = System.currentTimeMillis();
        // Penghitungan reward berdasarkan prioritas dan jarak dasar
        this.rewardCash = 50 + (priority * 30);
    }

    public String getOrderId() { return orderId; }
    public String getItemName() { return itemName; }
    public int getTargetNodeId() { return targetNodeId; }
    public int getPriority() { return priority; }
    public double getRewardCash() { return rewardCash; }
    public long getCreationTime() { return creationTime; }

    @Override
    public int compareTo(Order other) {
        // Max-Heap: Prioritas lebih tinggi akan diletakkan di depan antrean.
        // Jika prioritas sama, order yang lebih dulu masuk (creationTime terkecil) akan didahulukan.
        if (other.getPriority() != this.priority) {
            return Integer.compare(other.getPriority(), this.priority);
        }
        return Long.compare(this.creationTime, other.getCreationTime());
    }

    @Override
    public String toString() {
        String pStr = priority == 5 ? "VIP" : (priority == 3 ? "URGENT" : "NORMAL");
        return "[" + orderId + "] " + itemName + " (Lokasi: Node " + targetNodeId + ", Prioritas: " + pStr + ")";
    }
}
