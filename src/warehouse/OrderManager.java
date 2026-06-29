package warehouse;

import java.util.*;

/**
 * Modul Order Management (Priority Queue & Stack).
 */
public class OrderManager implements OrderManagementModule {
    private final List<Order> pendingList = new ArrayList<>(); // Urutan masuk (FIFO representasi visual)
    private final PriorityQueue<Order> priorityQueue = new PriorityQueue<>(); // Antrean prioritas (Max-Heap)
    private final Stack<Order> completedStack = new Stack<>(); // Riwayat pesanan selesai (LIFO Stack)

    // Menyimpan metrik tambahan untuk setiap pesanan selesai
    private final Map<String, OrderMetrics> orderMetricsMap = new HashMap<>();

    public static class OrderMetrics {
        public long durationMs;
        public double actualDistance;
        public double efficiency; // Jarak teoritis vs aktual
        public String processedByRobot;
    }

    /**
     * Tambah Order baru ke dalam list urutan masuk dan Heap prioritas.
     */
    public synchronized void addOrder(Order order) {
        if (order != null) {
            pendingList.add(order);
            priorityQueue.add(order);
        }
    }

    /**
     * Mendapatkan daftar semua pesanan tertunda berdasarkan urutan masuknya.
     */
    public synchronized Collection<Order> getAllPendingOrders() {
        return new ArrayList<>(pendingList);
    }

    /**
     * Mengambil order dengan prioritas tertinggi untuk dikerjakan.
     */
    public synchronized Order getNextPriorityOrder() {
        Order nextOrder = priorityQueue.poll();
        if (nextOrder != null) {
            pendingList.remove(nextOrder);
        }
        return nextOrder;
    }

    /**
     * Menyimpan order ke dalam stack riwayat selesai (LIFO).
     */
    @Override
    public synchronized void archiveCompletedOrder(Order completedOrder) {
        archiveCompletedOrder(completedOrder, null);
    }

    /**
     * Menyimpan order ke dalam stack riwayat selesai (LIFO) beserta metrik tambahannya.
     */
    public synchronized void archiveCompletedOrder(Order completedOrder, OrderMetrics metrics) {
        if (completedOrder != null) {
            completedStack.push(completedOrder);
            if (metrics != null) {
                orderMetricsMap.put(completedOrder.getOrderId(), metrics);
            }
        }
    }

    /**
     * Mengembalikan stack riwayat pesanan (LIFO).
     */
    public synchronized Stack<Order> getHistoryStack() {
        return completedStack;
    }

    public synchronized OrderMetrics getMetrics(String orderId) {
        return orderMetricsMap.get(orderId);
    }

    /**
     * Fitur pembatalan (Undo/Void) dari item terakhir yang diselesaikan.
     */
    public synchronized Order undoLastCompletedOrder() {
        if (!completedStack.isEmpty()) {
            Order undoneOrder = completedStack.pop();
            orderMetricsMap.remove(undoneOrder.getOrderId());
            // Masukkan kembali ke antrean aktif
            addOrder(undoneOrder);
            return undoneOrder;
        }
        return null;
    }

    /**
     * Helper untuk menampilkan daftar antrean yang sudah terurut berdasarkan prioritas tanpa merusak data asli.
     */
    public synchronized List<Order> getOrdersSortedByPriority() {
        List<Order> sortedList = new ArrayList<>(pendingList);
        Collections.sort(sortedList);
        return sortedList;
    }
}
