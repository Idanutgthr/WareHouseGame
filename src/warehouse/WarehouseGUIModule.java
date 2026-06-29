package warehouse;

import java.util.Collection;
import java.util.Stack;
import java.util.List;

/**
 * Interface Modul Graphical User Interface (Kontribusi Teman)
 */
public interface WarehouseGUIModule {
    /**
     * Menginisialisasi frame utama game.
     * Menampilkan layout denah gudang berupa visual titik graf dan tabel order.
     */
    void initializeMainFrame();

    /**
     * [FITUR 6] - SIMULASI ROBOT
     * Menerima hasil rute lintasan berupa List of Nodes dari modul optimalisasi rute,
     * lalu menjalankan animasi visual pergerakan robot pada panel GUI langkah-demi-langkah.
     */
    void triggerRobotAnimation(List<Integer> path);

    /**
     * Melakukan sinkronisasi data dari Modul Manajemen Order ke komponen tabel di GUI.
     */
    void updateUIComponents(Collection<Order> pending, Stack<Order> history);
}
