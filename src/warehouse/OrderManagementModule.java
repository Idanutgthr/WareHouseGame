package warehouse;

import java.util.Collection;
import java.util.Stack;

/**
 * Interface Modul Manajemen Order (Kontribusi Teman)
 */
public interface OrderManagementModule {
    /**
     * [FITUR 1] - TAMBAH ORDER
     * Memasukkan order baru yang diinput user ke dalam antrean prioritas.
     */
    void addOrder(Order order);

    /**
     * [FITUR 2] - LIHAT ORDER
     * Mengembalikan seluruh daftar antrean pesanan aktif untuk ditampilkan di layar.
     */
    Collection<Order> getAllPendingOrders();

    /**
     * [FITUR 3] - PRIORITY QUEUE OPERATION
     * Mengambil (poll) pesanan yang memiliki prioritas paling tinggi untuk segera dikerjakan robot.
     */
    Order getNextPriorityOrder();

    /**
     * [FITUR 7] - RIWAYAT PESANAN
     * Menyimpan pesanan yang telah sukses diselesaikan oleh robot ke dalam riwayat.
     */
    void archiveCompletedOrder(Order completedOrder);

    /**
     * Mengambil seluruh riwayat pesanan yang sudah selesai untuk ditampilkan.
     */
    Stack<Order> getHistoryStack();
}
