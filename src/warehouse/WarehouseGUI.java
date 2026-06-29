package warehouse;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Path2D;
import java.util.*;
import java.util.List;
import javax.swing.*;
import javax.swing.Timer;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;

/**
 * ============================================================================
 * WAREHOUSE ROBOT OPTIMIZATION GAME - POLISHED HIGH-TECH GUI
 * Responsi Akhir - Struktur Data dan Algoritma (Tema 1: Route Optimization)
 * ============================================================================
 * 
 * Desain & Peningkatan Estetika Utama:
 * 1. Cyberpunk / Dark-Tech Theme: Background slate gelap ultra modern, neon cyan, emerald green, dan amber.
 * 2. High-Contrast Text: Semua teks, tabel, dropdown, dan list memiliki render custom dengan warna kontras maksimal.
 * 3. Sleek Stats Cards: Panel atas dirancang sebagai kartu status modern terpisah dengan visual rapi dan seimbang.
 * 4. Balanced 2-Column Tab Dashboard: Kontrol manual disandingkan langsung dengan antrean pending order di Tab 1,
 *    dan upgrade tycoon disandingkan dengan list riwayat di Tab 2. Pengguna tidak perlu berpindah tab secara berlebihan.
 * 5. Smooth Linear constant Speed Animation: Robot bergerak dengan kecepatan fisik konstan yang bergantung pada jarak piksel,
 *    bukan langkah statis yang membuat pergerakan aneh. Dilengkapi booster trailing trail dan radar scan.
 * 6. Glowing Radar Tracks 2D: Peta lintasan memiliki visual double-line neon conduit, dengan pulsing beacon di titik target.
 */
public class WarehouseGUI extends JFrame implements WarehouseGUIModule {
    private final OrderManager orderManager;
    private final RouteOptimizer routeOptimizer;
    private final GameState gameState;

    private WarehouseGraph currentGraph;
    private List<Integer> currentRoute = new ArrayList<>();
    private List<String> currentAlgoLogs = new ArrayList<>();

    // Robot Visual State
    private double robotX = 80;
    private double robotY = 200;
    private double robotAngle = 0; // Sudut arah robot
    private int robotCurrentNode = 0;
    private boolean isRobotMoving = false;
    private Order currentProcessingOrder = null;

    // Robot Trail (Visual momentum ekor cahaya)
    private final java.util.List<Point2D> trailPoints = new ArrayList<>();
    private static class Point2D {
        double x, y;
        Point2D(double x, double y) { this.x = x; this.y = y; }
    }

    // Animation & Pulse Rates
    private double radarPulseRadius = 0;
    private double targetPulseRadius = 0;

    // UI Components
    private JTextArea logTextArea;
    private DefaultTableModel pendingTableModel;
    private DefaultListModel<String> historyListModel;
    private JComboBox<Integer> nodeSelector;
    private JComboBox<String> prioritySelector;
    private JTextField itemIdField;
    private JTextField itemNameField;
    private JToggleButton autoOrderToggle;
    private JPanel canvasPanel;
    private JButton buyRobotBtn;
    private JButton upgradeSpeedBtn;
    private JButton upgradeCapacityBtn;
    private JButton unlockAreaBtn;

    // Modern Dashboard Stats Display Card
    private StatsCard cashCard, robotCard, areaCard, targetCard;

    // Modern Palette
    private final Color bgDark = new Color(11, 15, 26);        // slate-950
    private final Color bgCard = new Color(22, 28, 45);        // slate-900 (Panel & Card background)
    private final Color bgInput = new Color(30, 41, 59);       // slate-800
    private final Color borderDark = new Color(51, 65, 85);    // slate-700
    private final Color textLight = new Color(248, 250, 252);  // slate-50 (White slate)
    private final Color textMuted = new Color(148, 163, 184);  // slate-400 (Soft label grey)
    private final Color accentBlue = new Color(14, 165, 233);   // Neon blue (sky-500)
    private final Color accentGreen = new Color(16, 185, 129);  // Neon emerald green
    private final Color accentOrange = new Color(249, 115, 22); // Glowing amber/orange
    private final Color accentRed = new Color(239, 68, 68);     // Warning red

    public WarehouseGUI(OrderManager om, RouteOptimizer ro, GameState gs) {
        this.orderManager = om;
        this.routeOptimizer = ro;
        this.gameState = gs;
        this.currentGraph = ro.createDefaultWarehouseLayout(gs.getUnlockedAreas());

        setTitle("▲ ADVANCED WAREHOUSE ROBOT SIMULATOR (DIJKSTRA OPTIMIZATION)");
        setSize(1200, 780);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        // Inisialisasi visual dan layout dashboard
        initUI();

        // Game loop pasif (Idle Tycoon)
        startGameLoop();

        // Timer global untuk merender aura animasi pulsing dan radar sweep
        Timer renderTimer = new Timer(30, e -> {
            // Pulse radar robot
            radarPulseRadius += 1.5;
            if (radarPulseRadius > 35) radarPulseRadius = 0;

            // Pulse beacon target node
            targetPulseRadius += 1.0;
            if (targetPulseRadius > 25) targetPulseRadius = 0;

            canvasPanel.repaint();
        });
        renderTimer.start();
    }

    private void initUI() {
        // Kontainer utama dengan dark background
        JPanel mainContainer = new JPanel(new BorderLayout(10, 10));
        mainContainer.setBackground(bgDark);
        mainContainer.setBorder(new EmptyBorder(12, 12, 12, 12));

        // ==========================================
        // 1. BAGIAN UTAS: HIGH-TECH STATS PANEL
        // ==========================================
        JPanel headerPanel = new JPanel(new BorderLayout(10, 10));
        headerPanel.setOpaque(false);

        // Title Label
        JLabel titleLabel = new JLabel("▲ AUTONOMOUS ROBOT PATHFINDING CONTROLLER");
        titleLabel.setFont(new Font("Monospaced", Font.BOLD, 18));
        titleLabel.setForeground(accentBlue);
        headerPanel.add(titleLabel, BorderLayout.NORTH);

        // Grid 4 Stats Cards
        JPanel statsGrid = new JPanel(new GridLayout(1, 4, 12, 12));
        statsGrid.setOpaque(false);

        cashCard = new StatsCard("VIRTUAL CAPITAL (CASH)", "$250.00", "Passive Rate: +$0.00/s", accentGreen);
        robotCard = new StatsCard("ACTIVE ROBOT FLEET", "1 Unit", "Speed: Lvl 1 | Cargo: Lvl 1", accentBlue);
        areaCard = new StatsCard("WAREHOUSE RANGE", "Area 1", "Total Nodes: 6", accentOrange);
        targetCard = new StatsCard("CURRENT DISPATCH MISSION", "STANDBY", "System Ready", textMuted);

        statsGrid.add(cashCard);
        statsGrid.add(robotCard);
        statsGrid.add(areaCard);
        statsGrid.add(targetCard);
        headerPanel.add(statsGrid, BorderLayout.CENTER);

        mainContainer.add(headerPanel, BorderLayout.NORTH);

        // Split Pane: Kiri (Visualisasi 2D) & Kanan (Tab Dashboard Kontrol & Antrean)
        JSplitPane centerSplitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        centerSplitPane.setDividerLocation(660);
        centerSplitPane.setResizeWeight(0.55);
        centerSplitPane.setOpaque(false);
        centerSplitPane.setBorder(null);

        // ==========================================
        // 2. PANEL KIRI: VISUALISASI GUDANG 2D
        // ==========================================
        canvasPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                drawWarehouse2D((Graphics2D) g);
            }
        };
        canvasPanel.setBackground(new Color(11, 14, 23)); // Ultra black slate
        canvasPanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderDark, 1, true),
            new EmptyBorder(5, 5, 5, 5)
        ));
        centerSplitPane.setLeftComponent(canvasPanel);

        // ==========================================
        // 3. PANEL KANAN: DASHBOARD DAN KONTROL
        // ==========================================
        JPanel rightPanel = new JPanel(new BorderLayout());
        rightPanel.setOpaque(false);

        JTabbedPane tabbedPane = new JTabbedPane();
        tabbedPane.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI());
        tabbedPane.setBackground(bgCard);
        tabbedPane.setForeground(textLight);
        tabbedPane.setFont(new Font("SansSerif", Font.BOLD, 12));
        tabbedPane.setBorder(null);

        // ------------------------------------------
        // TAB 1: CONTROL & DISPATCH CENTER (Sangat Seimbang!)
        // ------------------------------------------
        JPanel dispatchTab = new JPanel(new BorderLayout(8, 8));
        dispatchTab.setBackground(bgDark);
        dispatchTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Sub-panel 1a: Form Tambah Order Manual (Symmetrical 2x4 layout)
        JPanel orderFormPanel = new JPanel(new GridBagLayout());
        orderFormPanel.setBackground(bgCard);
        orderFormPanel.setBorder(createNeonTitledBorder("MANUAL DISPATCH FORM", accentBlue));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(6, 8, 6, 8);
        gbc.weightx = 1.0;

        itemIdField = new JTextField("ORD-" + (int)(Math.random() * 900 + 100));
        styleTextField(itemIdField);

        itemNameField = new JTextField("Component Box");
        styleTextField(itemNameField);

        nodeSelector = new JComboBox<>();
        styleComboBox(nodeSelector);
        updateNodeSelector();

        prioritySelector = new JComboBox<>(new String[]{"1 - Normal", "3 - Urgent", "5 - VIP"});
        styleComboBox(prioritySelector);

        // Row 0
        gbc.gridy = 0; gbc.gridx = 0; gbc.weightx = 0.3;
        orderFormPanel.add(createFormLabel("Order ID:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        orderFormPanel.add(itemIdField, gbc);

        // Row 1
        gbc.gridy = 1; gbc.gridx = 0; gbc.weightx = 0.3;
        orderFormPanel.add(createFormLabel("Item Name:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        orderFormPanel.add(itemNameField, gbc);

        // Row 2
        gbc.gridy = 2; gbc.gridx = 0; gbc.weightx = 0.3;
        orderFormPanel.add(createFormLabel("Target Rack:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        orderFormPanel.add(nodeSelector, gbc);

        // Row 3
        gbc.gridy = 3; gbc.gridx = 0; gbc.weightx = 0.3;
        orderFormPanel.add(createFormLabel("Priority:"), gbc);
        gbc.gridx = 1; gbc.weightx = 0.7;
        orderFormPanel.add(prioritySelector, gbc);

        // Action Button: Add Order
        JButton addOrderBtn = new JButton("+ INJECT ORDER INTO HEAP");
        styleButton(addOrderBtn, accentBlue, textLight);
        addOrderBtn.addActionListener(e -> {
            String id = itemIdField.getText().trim();
            String name = itemNameField.getText().trim();
            if (name.isEmpty()) name = "Unknown Item";
            int loc = (Integer) nodeSelector.getSelectedItem();
            int prio = Integer.parseInt(((String) prioritySelector.getSelectedItem()).split(" ")[0]);

            Order order = new Order(id, name, loc, prio);
            orderManager.addOrder(order);
            updatePendingTable();
            logTextArea.append("[INFO] Order ditambahkan secara manual: " + order.toString() + "\n");
            itemIdField.setText("ORD-" + (int)(Math.random() * 900 + 100));
        });

        JPanel formAndBtnWrapper = new JPanel(new BorderLayout(5, 5));
        formAndBtnWrapper.setOpaque(false);
        formAndBtnWrapper.add(orderFormPanel, BorderLayout.CENTER);
        formAndBtnWrapper.add(addOrderBtn, BorderLayout.SOUTH);

        dispatchTab.add(formAndBtnWrapper, BorderLayout.NORTH);

        // Sub-panel 1b: Priority Queue Table (Pending Heap)
        JPanel queuePanel = new JPanel(new BorderLayout());
        queuePanel.setBackground(bgCard);
        queuePanel.setBorder(createNeonTitledBorder("ACTIVE HEAP QUEUE (PRIORITY FIRST)", accentOrange));

        String[] cols = {"ID", "Item Name", "Target Node", "Priority", "Reward"};
        pendingTableModel = new DefaultTableModel(cols, 0);
        JTable pendingTable = new JTable(pendingTableModel);
        styleTable(pendingTable);

        JScrollPane tableScroll = new JScrollPane(pendingTable);
        tableScroll.getViewport().setBackground(bgCard);
        tableScroll.setBorder(null);
        queuePanel.add(tableScroll, BorderLayout.CENTER);

        // Process Action Button at bottom
        JButton runProcessBtn = new JButton("► DISPATCH NEAREST ROUTE (DIJKSTRA)");
        styleButton(runProcessBtn, accentGreen, textLight);
        runProcessBtn.setFont(new Font("SansSerif", Font.BOLD, 14));
        runProcessBtn.addActionListener(e -> triggerNextOrderProcess(true));
        queuePanel.add(runProcessBtn, BorderLayout.SOUTH);

        dispatchTab.add(queuePanel, BorderLayout.CENTER);

        tabbedPane.addTab("<html><b style='color:#f8fafc; font-family:SansSerif;'>Control & Dispatch</b></html>", dispatchTab);

        // ------------------------------------------
        // TAB 2: IDLE TYCOON EMPIRE (Upgrades & LIFO Stack)
        // ------------------------------------------
        JPanel tycoonTab = new JPanel(new BorderLayout(8, 8));
        tycoonTab.setBackground(bgDark);
        tycoonTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        // Passive Automatic Stream Toggle at top
        JPanel togglePanel = new JPanel(new BorderLayout(10, 10));
        togglePanel.setBackground(bgCard);
        togglePanel.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderDark, 1),
            new EmptyBorder(10, 10, 10, 10)
        ));

        JLabel toggleLabel = new JLabel("AUTO-ORDER GENERATION SYSTEM");
        toggleLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
        toggleLabel.setForeground(textLight);

        autoOrderToggle = new JToggleButton("ACTIVE (STREAM ON)");
        autoOrderToggle.setSelected(true);
        styleToggleButton(autoOrderToggle, accentGreen, textLight);
        autoOrderToggle.addActionListener(e -> {
            if (autoOrderToggle.isSelected()) {
                autoOrderToggle.setText("ACTIVE (STREAM ON)");
                autoOrderToggle.setBackground(accentGreen);
                logTextArea.append("[TYCOON] Auto-order passive stream diaktifkan.\n");
            } else {
                autoOrderToggle.setText("PAUSED (STREAM OFF)");
                autoOrderToggle.setBackground(bgInput);
                logTextArea.append("[TYCOON] Auto-order passive stream dinonaktifkan.\n");
            }
        });

        togglePanel.add(toggleLabel, BorderLayout.CENTER);
        togglePanel.add(autoOrderToggle, BorderLayout.EAST);
        tycoonTab.add(togglePanel, BorderLayout.NORTH);

        // Upgrades Grid (Center-Left) & History List (Center-Right) using split panel inside tycoon tab
        JSplitPane tycoonSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        tycoonSplit.setDividerLocation(200);
        tycoonSplit.setResizeWeight(0.5);
        tycoonSplit.setOpaque(false);
        tycoonSplit.setBorder(null);

        // Upgrades 2x2 Panel
        JPanel upgradesPanel = new JPanel(new GridLayout(2, 2, 8, 8));
        upgradesPanel.setBackground(bgCard);
        upgradesPanel.setBorder(createNeonTitledBorder("TYCOON ECONOMIC UPGRADES", accentBlue));

        buyRobotBtn = new JButton("Buy Robot ($200.00)");
        styleUpgradeButton(buyRobotBtn);
        buyRobotBtn.addActionListener(e -> {
            double cost = gameState.getRobotBuyCost();
            if (gameState.spendCash(cost)) {
                gameState.incrementRobots();
                updateStatsDisplay();
                logTextArea.append("[UPGRADE] Membeli unit robot baru! Total armada: " + gameState.getTotalRobots() + "\n");
            } else {
                showLowCashError();
            }
        });

        upgradeSpeedBtn = new JButton("Upgrade Speed ($50.00)");
        styleUpgradeButton(upgradeSpeedBtn);
        upgradeSpeedBtn.addActionListener(e -> {
            double cost = gameState.getSpeedUpgradeCost();
            if (gameState.spendCash(cost)) {
                gameState.incrementSpeed();
                updateStatsDisplay();
                logTextArea.append("[UPGRADE] Mesin penggerak robot di-upgrade ke Level " + gameState.getSpeedLevel() + "\n");
            } else {
                showLowCashError();
            }
        });

        upgradeCapacityBtn = new JButton("Upgrade Cargo Capacity ($75.00)");
        styleUpgradeButton(upgradeCapacityBtn);
        upgradeCapacityBtn.addActionListener(e -> {
            double cost = gameState.getCapacityUpgradeCost();
            if (gameState.spendCash(cost)) {
                gameState.incrementCapacity();
                updateStatsDisplay();
                logTextArea.append("[UPGRADE] Lengan hidrolik kargo di-upgrade ke Level " + gameState.getCapacityLevel() + "\n");
            } else {
                showLowCashError();
            }
        });

        unlockAreaBtn = new JButton("Unlock New Zone ($500.00)");
        styleUpgradeButton(unlockAreaBtn);
        unlockAreaBtn.addActionListener(e -> {
            if (gameState.getUnlockedAreas() >= 3) {
                JOptionPane.showMessageDialog(this, "Seluruh zona pergudangan telah dibuka!");
                return;
            }
            double cost = gameState.getUnlockAreaCost();
            if (gameState.spendCash(cost)) {
                gameState.incrementUnlockedAreas();
                currentGraph = routeOptimizer.createDefaultWarehouseLayout(gameState.getUnlockedAreas());
                updateNodeSelector();
                updateStatsDisplay();
                canvasPanel.repaint();
                logTextArea.append("[ZONA] Sektor pergudangan baru diaktifkan! Total Sektor: " + gameState.getUnlockedAreas() + "\n");
            } else {
                showLowCashError();
            }
        });

        upgradesPanel.add(buyRobotBtn);
        upgradesPanel.add(upgradeSpeedBtn);
        upgradesPanel.add(upgradeCapacityBtn);
        upgradesPanel.add(unlockAreaBtn);
        tycoonSplit.setTopComponent(upgradesPanel);

        // LIFO History Stack Panel
        JPanel historyPanel = new JPanel(new BorderLayout());
        historyPanel.setBackground(bgCard);
        historyPanel.setBorder(createNeonTitledBorder("COMPLETED SHIPMENT CHRONOLOGY (LIFO)", accentGreen));

        historyListModel = new DefaultListModel<>();
        JList<String> historyList = new JList<>(historyListModel);
        historyList.setBackground(bgCard);
        historyList.setForeground(textLight);
        historyList.setFont(new Font("Monospaced", Font.PLAIN, 11));
        historyList.setCellRenderer(new CustomListCellRenderer());

        JScrollPane historyScroll = new JScrollPane(historyList);
        historyScroll.getViewport().setBackground(bgCard);
        historyScroll.setBorder(null);
        historyPanel.add(historyScroll, BorderLayout.CENTER);

        // Undo LIFO Action Button
        JButton undoBtn = new JButton("↺ VOID/UNDO LAST DISPATCH ACTION (STACK POP)");
        styleButton(undoBtn, accentRed, textLight);
        undoBtn.addActionListener(e -> {
            Order undone = orderManager.undoLastCompletedOrder();
            if (undone != null) {
                logTextArea.append("[VOID] Membatalkan rute " + undone.getOrderId() + ". Item dikembalikan ke Heap!\n");
                // Potong virtual cash sebagai penalty
                gameState.setVirtualCash(Math.max(0, gameState.getVirtualCash() - undone.getRewardCash()));
                updatePendingTable();
                updateHistoryList();
                updateStatsDisplay();
                canvasPanel.repaint();
            } else {
                JOptionPane.showMessageDialog(this, "Riwayat pengantaran kosong! Tidak ada transaksi yang bisa dibatalkan.");
            }
        });
        historyPanel.add(undoBtn, BorderLayout.SOUTH);

        tycoonSplit.setBottomComponent(historyPanel);
        tycoonTab.add(tycoonSplit, BorderLayout.CENTER);

        tabbedPane.addTab("<html><b style='color:#f8fafc; font-family:SansSerif;'>Tycoon Empire</b></html>", tycoonTab);

        // ------------------------------------------
        // TAB 3: DIJKSTRA PATHFINDING TERMINAL LOG
        // ------------------------------------------
        JPanel logTab = new JPanel(new BorderLayout());
        logTab.setBackground(bgDark);
        logTab.setBorder(new EmptyBorder(8, 8, 8, 8));

        logTextArea = new JTextArea();
        logTextArea.setEditable(false);
        logTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        logTextArea.setBackground(new Color(9, 12, 22)); // Deep terminal black-slate
        logTextArea.setForeground(new Color(56, 189, 248)); // Sky Blue glow
        logTextArea.setCaretColor(Color.WHITE);
        logTextArea.setBorder(new EmptyBorder(10, 10, 10, 10));

        JScrollPane terminalScroll = new JScrollPane(logTextArea);
        terminalScroll.setBorder(new LineBorder(borderDark, 1, true));
        logTab.add(terminalScroll, BorderLayout.CENTER);

        tabbedPane.addTab("<html><b style='color:#f8fafc; font-family:SansSerif;'>Pathfinding Logs</b></html>", logTab);

        rightPanel.add(tabbedPane, BorderLayout.CENTER);
        centerSplitPane.setRightComponent(rightPanel);

        mainContainer.add(centerSplitPane, BorderLayout.CENTER);
        add(mainContainer);

        // Isi default order
        orderManager.addOrder(new Order("ORD-101", "Microchip Substrate", 3, 3));
        orderManager.addOrder(new Order("ORD-102", "Lithium Ion Core", 1, 5));
        orderManager.addOrder(new Order("ORD-103", "Heavy Alloy Rods", 4, 1));

        updatePendingTable();
        logTextArea.append("[SYSTEM] Server pathfinding siap di port lokal. Menginisialisasi visualisasi peta 2D...\n");
    }

    // ==========================================
    // SEKSI GRAPHICS & CUSTOM ANIMATION (PERGERAKAN ROBOT SANGAT MULUS)
    // ==========================================
    private void drawWarehouse2D(Graphics2D g2) {
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);

        int width = canvasPanel.getWidth();
        int height = canvasPanel.getHeight();

        // 1. Gambar Grid Latar Belakang (Cyberpunk tech radar look)
        g2.setColor(new Color(22, 28, 45, 60)); // grid line color
        for (int i = 0; i < width; i += 40) {
            g2.drawLine(i, 0, i, height);
        }
        for (int j = 0; j < height; j += 40) {
            g2.drawLine(0, j, width, j);
        }

        // 2. Gambar Jalur Rel Logistik (Double Neon Conduit)
        for (Map.Entry<Integer, List<Edge>> entry : currentGraph.getAdjList().entrySet()) {
            int u = entry.getKey();
            NodeLocation locU = currentGraph.getLocations().get(u);
            if (locU == null) continue;

            for (Edge edge : entry.getValue()) {
                int v = edge.getDestination();
                NodeLocation locV = currentGraph.getLocations().get(v);
                if (locV == null || u > v) continue; // Hanya lukis sekali (undirected)

                // Backing track (Outer conduit line)
                g2.setColor(new Color(30, 41, 59)); // Slate border
                g2.setStroke(new BasicStroke(7f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(locU.getX(), locU.getY(), locV.getX(), locV.getY()));

                // Inner electric glow track
                g2.setColor(new Color(17, 24, 39)); // Deep dark interior
                g2.setStroke(new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
                g2.draw(new Line2D.Double(locU.getX(), locU.getY(), locV.getX(), locV.getY()));

                // Tulis bobot rute di tengah garis
                int midX = (locU.getX() + locV.getX()) / 2;
                int midY = (locU.getY() + locV.getY()) / 2;
                g2.setColor(textMuted);
                g2.setFont(new Font("Monospaced", Font.BOLD, 10));
                g2.drawString(edge.getWeight() + "m", midX - 8, midY - 3);
            }
        }

        // 3. Highlight Rute Teraktif (Glow Laser Path)
        if (!currentRoute.isEmpty()) {
            g2.setStroke(new BasicStroke(5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(249, 115, 22, 100)); // Glowing Orange Outer
            for (int i = 0; i < currentRoute.size() - 1; i++) {
                NodeLocation from = currentGraph.getLocations().get(currentRoute.get(i));
                NodeLocation to = currentGraph.getLocations().get(currentRoute.get(i+1));
                if (from != null && to != null) {
                    g2.draw(new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY()));
                }
            }

            g2.setStroke(new BasicStroke(2f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            g2.setColor(new Color(251, 191, 36)); // Glowing Yellow Core
            for (int i = 0; i < currentRoute.size() - 1; i++) {
                NodeLocation from = currentGraph.getLocations().get(currentRoute.get(i));
                NodeLocation to = currentGraph.getLocations().get(currentRoute.get(i+1));
                if (from != null && to != null) {
                    g2.draw(new Line2D.Double(from.getX(), from.getY(), to.getX(), to.getY()));
                    drawArrowOnLine(g2, from.getX(), from.getY(), to.getX(), to.getY());
                }
            }
        }

        // 4. Lukis Target Pulse Aura (Radar Ring pada target node)
        if (currentProcessingOrder != null) {
            NodeLocation targetLoc = currentGraph.getLocations().get(currentProcessingOrder.getTargetNodeId());
            if (targetLoc != null) {
                g2.setColor(new Color(239, 68, 68, (int) Math.max(0, 150 - (targetPulseRadius * 6))));
                g2.setStroke(new BasicStroke(2f));
                g2.draw(new Ellipse2D.Double(
                    targetLoc.getX() - targetPulseRadius,
                    targetLoc.getY() - targetPulseRadius,
                    targetPulseRadius * 2,
                    targetPulseRadius * 2
                ));
            }
        }

        // 5. Lukis Titik Node Pad / Rak Gudang
        for (Map.Entry<Integer, NodeLocation> entry : currentGraph.getLocations().entrySet()) {
            NodeLocation loc = entry.getValue();

            // Desain Warna Node Pad
            boolean isTarget = (currentProcessingOrder != null && loc.getId() == currentProcessingOrder.getTargetNodeId());
            boolean isRobotPos = (loc.getId() == robotCurrentNode);

            if (isTarget) {
                g2.setColor(accentRed); // Merah Terang
            } else if (isRobotPos) {
                g2.setColor(accentBlue); // Biru Terang
            } else {
                g2.setColor(bgInput); // Slate
            }

            // Fill Node Circle
            g2.fill(new Ellipse2D.Double(loc.getX() - 15, loc.getY() - 15, 30, 30));

            // Outer Steel Ring
            g2.setColor(isTarget ? accentOrange : textLight);
            g2.setStroke(new BasicStroke(2f));
            g2.draw(new Ellipse2D.Double(loc.getX() - 15, loc.getY() - 15, 30, 30));

            // Tulisan ID Node di dalam Lingkaran
            g2.setColor(textLight);
            g2.setFont(new Font("Monospaced", Font.BOLD, 12));
            String idStr = String.valueOf(loc.getId());
            FontMetrics fm = g2.getFontMetrics();
            int textW = fm.stringWidth(idStr);
            g2.drawString(idStr, loc.getX() - textW / 2, loc.getY() + 4);

            // Tulisan Label Node (Memiliki background gelap mini agar tidak tabrakan dengan rel)
            String label = loc.getName();
            g2.setFont(new Font("SansSerif", Font.BOLD, 10));
            int lblW = g2.getFontMetrics().stringWidth(label);
            g2.setColor(new Color(11, 14, 23, 200));
            g2.fillRoundRect(loc.getX() - lblW / 2 - 4, loc.getY() - 28, lblW + 8, 14, 4, 4);

            g2.setColor(isTarget ? accentOrange : textMuted);
            g2.drawString(label, loc.getX() - lblW / 2, loc.getY() - 17);
        }

        // 6. Lukis Jejak Booster Robot (Glowing Particle Trail)
        if (isRobotMoving && !trailPoints.isEmpty()) {
            for (int i = 0; i < trailPoints.size(); i++) {
                Point2D p = trailPoints.get(i);
                double ratio = (double) i / trailPoints.size();
                g2.setColor(new Color(251, 191, 36, (int) (ratio * 120)));
                double trailSize = 6 + (ratio * 10);
                g2.fill(new Ellipse2D.Double(p.x - trailSize/2, p.y - trailSize/2, trailSize, trailSize));
            }
        }

        // 7. Lukis Radar Sweep Robot
        g2.setColor(new Color(14, 165, 233, (int) Math.max(0, 120 - (radarPulseRadius * 3))));
        g2.setStroke(new BasicStroke(1.5f));
        g2.draw(new Ellipse2D.Double(robotX - radarPulseRadius, robotY - radarPulseRadius, radarPulseRadius * 2, radarPulseRadius * 2));

        // 8. Lukis Robot Sebagai Kendaraan AGV Futuristik Berarah
        g2.translate(robotX, robotY);
        g2.rotate(robotAngle);

        // Body Chassis Robot (Hexagonal / Sleek Arrow Design)
        Path2D.Double chassis = new Path2D.Double();
        chassis.moveTo(-12, -10);
        chassis.lineTo(14, 0); // Depan lancip
        chassis.lineTo(-12, 10);
        chassis.lineTo(-18, 5);
        chassis.lineTo(-18, -5);
        chassis.closePath();

        g2.setColor(new Color(15, 23, 42)); // Black Carbon
        g2.fill(chassis);

        // Glowing Outline
        g2.setColor(accentOrange);
        g2.setStroke(new BasicStroke(2.5f));
        g2.draw(chassis);

        // Thruster glow (bagian belakang)
        g2.setColor(accentRed);
        g2.fillRect(-22, -4, 4, 8);

        // Sensor Core (Neon hijau di pusat robot)
        g2.setColor(accentGreen);
        g2.fill(new Ellipse2D.Double(-4, -4, 8, 8));

        // Blinking status lights
        if ((System.currentTimeMillis() / 200) % 2 == 0) {
            g2.setColor(accentBlue);
            g2.fill(new Ellipse2D.Double(4, -6, 3, 3));
        } else {
            g2.setColor(accentRed);
            g2.fill(new Ellipse2D.Double(4, 3, 3, 3));
        }

        // Reset rotasi & translasi
        g2.rotate(-robotAngle);
        g2.translate(-robotX, -robotY);
    }

    private void drawArrowOnLine(Graphics2D g2, double x1, double y1, double x2, double y2) {
        double angle = Math.atan2(y2 - y1, x2 - x1);
        double midX = (x1 + x2) / 2;
        double midY = (y1 + y2) / 2;
        int size = 8;

        Polygon arrowHead = new Polygon();
        arrowHead.addPoint((int) midX, (int) midY);
        arrowHead.addPoint((int) (midX - size * Math.cos(angle - Math.PI / 6)), (int) (midY - size * Math.sin(angle - Math.PI / 6)));
        arrowHead.addPoint((int) (midX - size * Math.cos(angle + Math.PI / 6)), (int) (midY - size * Math.sin(angle + Math.PI / 6)));
        g2.fill(arrowHead);
    }

    // ==========================================
    // ALGORITMA ANIMASI DAN KALKULASI RUTE TERKENDALI
    // ==========================================
    private void triggerNextOrderProcess(boolean isManual) {
        if (isRobotMoving) {
            if (isManual) {
                JOptionPane.showMessageDialog(this, "Robot sedang sibuk mengambil pesanan lain!");
            }
            return;
        }

        currentProcessingOrder = orderManager.getNextPriorityOrder();
        if (currentProcessingOrder == null) {
            if (isManual) {
                JOptionPane.showMessageDialog(this, "Antrean order kosong! Sila tambahkan order baru terlebih dahulu.");
            }
            return;
        }

        targetCard.updateValue(currentProcessingOrder.getOrderId(), currentProcessingOrder.getItemName(), accentRed);
        updatePendingTable();

        // Hitung rute Dijkstra
        currentRoute = routeOptimizer.calculateShortestPath(
            currentGraph, 
            robotCurrentNode, 
            currentProcessingOrder.getTargetNodeId(), 
            currentAlgoLogs
        );

        // Cetak kalkulasi Dijkstra ke panel log
        logTextArea.setText("");
        for (String logLine : currentAlgoLogs) {
            logTextArea.append(logLine + "\n");
        }
        currentAlgoLogs.clear();

        if (currentRoute.isEmpty()) {
            logTextArea.append("[GALAT] Robot tidak menemukan rute ke lokasi barang!\n");
            completeOrderDirectly();
            return;
        }

        // Mulai pergerakan robot
        isRobotMoving = true;
        trailPoints.clear();
        animateRobotMovement(0);
    }

    private void completeOrderDirectly() {
        if (currentProcessingOrder == null) return;

        OrderManager.OrderMetrics metrics = new OrderManager.OrderMetrics();
        metrics.durationMs = System.currentTimeMillis() - currentProcessingOrder.getCreationTime();
        metrics.actualDistance = 0;
        metrics.efficiency = 1.0;
        metrics.processedByRobot = "Robot Fleet Standard";

        orderManager.archiveCompletedOrder(currentProcessingOrder, metrics);
        gameState.addCash(currentProcessingOrder.getRewardCash());
        gameState.totalOrdersCompleted++;

        logTextArea.append("[SUKSES] Order " + currentProcessingOrder.getOrderId() + " diselesaikan instan.\n");
        targetCard.updateValue("STANDBY", "System Ready", textMuted);
        updateHistoryList();
        updateStatsDisplay();
        currentProcessingOrder = null;
        isRobotMoving = false;
        canvasPanel.repaint();
    }

    private void animateRobotMovement(final int pathIndex) {
        if (pathIndex >= currentRoute.size() - 1) {
            // Robot telah sukses tiba di node target barang!
            robotCurrentNode = currentRoute.get(currentRoute.size() - 1);

            // Hitung total jarak fisik yang ditempuh berdasarkan edge weight
            double totalDistance = 0.0;
            for (int i = 0; i < currentRoute.size() - 1; i++) {
                int from = currentRoute.get(i);
                int to = currentRoute.get(i + 1);
                for (Edge edge : currentGraph.getAdjList().get(from)) {
                    if (edge.getDestination() == to) {
                        totalDistance += edge.getWeight();
                        break;
                    }
                }
            }

            long duration = System.currentTimeMillis() - currentProcessingOrder.getCreationTime();

            OrderManager.OrderMetrics metrics = new OrderManager.OrderMetrics();
            metrics.durationMs = duration;
            metrics.actualDistance = totalDistance;
            metrics.efficiency = 1.0; // Dijkstra dijamin optimal (100%)
            metrics.processedByRobot = "AGV Automated Unit";

            orderManager.archiveCompletedOrder(currentProcessingOrder, metrics);

            // Tambahkan income ke keuangan Tycoon
            gameState.addCash(currentProcessingOrder.getRewardCash());
            gameState.totalOrdersCompleted++;
            gameState.totalDistanceTraveled += totalDistance;
            gameState.totalProcessingTimeMs += duration;

            logTextArea.append(String.format("[BERHASIL] Robot mengirimkan '%s' [%s] dengan jarak total %.1fm dalam %.1fs!\n",
                currentProcessingOrder.getItemName(), currentProcessingOrder.getOrderId(), totalDistance, duration / 1000.0));

            targetCard.updateValue("STANDBY", "System Ready", textMuted);
            updateHistoryList();
            updateStatsDisplay();

            currentProcessingOrder = null;
            isRobotMoving = false;
            currentRoute.clear();
            trailPoints.clear();
            canvasPanel.repaint();
            return;
        }

        int fromNode = currentRoute.get(pathIndex);
        int toNode = currentRoute.get(pathIndex + 1);

        NodeLocation locFrom = currentGraph.getLocations().get(fromNode);
        NodeLocation locTo = currentGraph.getLocations().get(toNode);

        final double startX = locFrom.getX();
        final double startY = locFrom.getY();
        final double endX = locTo.getX();
        final double endY = locTo.getY();

        // Hitung target sudut kemudi robot
        final double targetAngle = Math.atan2(endY - startY, endX - startX);

        // Kecepatan konstan proporsional terhadap jarak fisik piksel
        double dx = endX - startX;
        double dy = endY - startY;
        double pixelDist = Math.sqrt(dx * dx + dy * dy);

        // Speed level mempengaruhi kecepatan penggerak robot secara nyata
        double speedFactor = 2.0 + (gameState.getSpeedLevel() * 0.8);
        final int totalSteps = Math.max(10, (int) (pixelDist / speedFactor));

        Timer animTimer = new Timer(25, new ActionListener() {
            int step = 0;
            @Override
            public void actionPerformed(ActionEvent e) {
                step++;
                double ratio = (double) step / totalSteps;
                robotX = startX + ratio * (endX - startX);
                robotY = startY + ratio * (endY - startY);

                // Interpolasi sudut kemudi agar berputar halus saat belok
                double angleDiff = targetAngle - robotAngle;
                while (angleDiff < -Math.PI) angleDiff += 2 * Math.PI;
                while (angleDiff > Math.PI) angleDiff -= 2 * Math.PI;
                robotAngle += angleDiff * 0.15; // Kecepatan berputar halus (15% per tick)

                // Rekam koordinat booster untuk trail cahaya
                trailPoints.add(new Point2D(robotX, robotY));
                if (trailPoints.size() > 15) {
                    trailPoints.remove(0);
                }

                canvasPanel.repaint();

                if (step >= totalSteps) {
                    ((Timer) e.getSource()).stop();
                    robotCurrentNode = toNode;
                    // Lanjut ke rute berikutnya
                    animateRobotMovement(pathIndex + 1);
                }
            }
        });
        animTimer.start();
    }

    // ==========================================
    // SEKSI SINKRONISASI GAME LOOP & STATS DISPLAY (IDLE TYCOON)
    // ==========================================
    private void startGameLoop() {
        // Timer pasif setiap 4 detik untuk pendapatan dan injeksi order otomatis
        Timer tycoonTimer = new Timer(4000, e -> {
            // Pasif income dihitung berdasarkan jumlah armada dan cargo level
            double passiveIncome = gameState.getTotalRobots() * 1.5 * gameState.getCapacityLevel();
            gameState.addCash(passiveIncome);
            updateStatsDisplay();

            // Pasif Auto Order (Injeksi otomatis jika diizinkan toggle)
            if (autoOrderToggle.isSelected() && orderManager.getAllPendingOrders().size() < 12) {
                String[] items = {"Sensor Module X", "Precious Copper Coil", "Integrated Circuit Board", "Pneumatic Valve V2", "Lithium Battery Pack", "Hydraulic Cylinder"};
                int randNode = (int) (Math.random() * currentGraph.getTotalNodes());
                int randPrio = Math.random() < 0.15 ? 5 : (Math.random() < 0.4 ? 3 : 1);
                String id = "AUTO-" + (int)(Math.random() * 900 + 100);
                String item = items[(int)(Math.random() * items.length)];

                orderManager.addOrder(new Order(id, item, randNode, randPrio));
                updatePendingTable();
            }
        });
        tycoonTimer.start();

        // Timer terpisah untuk Auto-Dispatch (Idle Tycoon Auto-Pilot)
        // Memeriksa antrean setiap 1 detik dan menjalankan robot otomatis jika standby
        Timer autoDispatchTimer = new Timer(1000, e -> {
            if (autoOrderToggle.isSelected() && !isRobotMoving && !orderManager.getAllPendingOrders().isEmpty()) {
                triggerNextOrderProcess(false);
            }
        });
        autoDispatchTimer.start();
    }

    private void updateStatsDisplay() {
        cashCard.updateValue("$" + String.format("%.2f", gameState.getVirtualCash()), 
            String.format("Passive: +$%.2f/4s", gameState.getTotalRobots() * 1.5 * gameState.getCapacityLevel()), accentGreen);
        
        robotCard.updateValue(gameState.getTotalRobots() + " Unit", 
            "Speed: Lvl " + gameState.getSpeedLevel() + " | Cargo: Lvl " + gameState.getCapacityLevel(), accentBlue);
        
        areaCard.updateValue("Sektor " + gameState.getUnlockedAreas(), 
            "Total Node: " + currentGraph.getTotalNodes(), accentOrange);

        // Update teks tombol pricing
        buyRobotBtn.setText("<html><center><b>BUY FLEET ROBOT</b><br><font color='#34d399'>Cost: $" + String.format("%.2f", gameState.getRobotBuyCost()) + "</font></center></html>");
        upgradeSpeedBtn.setText("<html><center><b>UPGRADE SPEED (Lvl " + (gameState.getSpeedLevel() + 1) + ")</b><br><font color='#34d399'>Cost: $" + String.format("%.2f", gameState.getSpeedUpgradeCost()) + "</font></center></html>");
        upgradeCapacityBtn.setText("<html><center><b>UPGRADE CARGO (Lvl " + (gameState.getCapacityLevel() + 1) + ")</b><br><font color='#34d399'>Cost: $" + String.format("%.2f", gameState.getCapacityUpgradeCost()) + "</font></center></html>");
        
        if (gameState.getUnlockedAreas() >= 3) {
            unlockAreaBtn.setText("<html><center><b>ALL ZONES ONLINE</b><br><font color='#94a3b8'>Max Level Reached</font></center></html>");
            unlockAreaBtn.setEnabled(false);
        } else {
            unlockAreaBtn.setText("<html><center><b>UNLOCK ZONE " + (gameState.getUnlockedAreas() + 1) + "</b><br><font color='#34d399'>Cost: $" + String.format("%.2f", gameState.getUnlockAreaCost()) + "</font></center></html>");
        }
    }

    private void updatePendingTable() {
        pendingTableModel.setRowCount(0);
        List<Order> sorted = orderManager.getOrdersSortedByPriority();
        for (Order o : sorted) {
            String pStr = o.getPriority() == 5 ? "VIP (5)" : (o.getPriority() == 3 ? "URGENT (3)" : "NORMAL (1)");
            pendingTableModel.addRow(new Object[]{
                o.getOrderId(),
                o.getItemName(),
                "Node " + o.getTargetNodeId(),
                pStr,
                "$" + String.format("%.2f", o.getRewardCash())
            });
        }
    }

    private void updateHistoryList() {
        historyListModel.clear();
        Stack<Order> stack = orderManager.getHistoryStack();
        @SuppressWarnings("unchecked")
        Stack<Order> temp = (Stack<Order>) stack.clone();
        while (!temp.isEmpty()) {
            Order o = temp.pop();
            OrderManager.OrderMetrics m = orderManager.getMetrics(o.getOrderId());
            String text = String.format(" [%s] %s (Node %d)", o.getOrderId(), o.getItemName(), o.getTargetNodeId());
            if (m != null) {
                text += String.format(" - %.1fm | %.1fs | Eff: 100%%", m.actualDistance, m.durationMs / 1000.0);
            }
            historyListModel.addElement(text);
        }
    }

    private void updateNodeSelector() {
        if (nodeSelector == null) return;
        nodeSelector.removeAllItems();
        for (int i = 0; i < currentGraph.getTotalNodes(); i++) {
            nodeSelector.addItem(i);
        }
    }

    private void showLowCashError() {
        JOptionPane.showMessageDialog(this, "Dana virtual kas tidak mencukupi untuk melakukan upgrade ini!", "Upgrade Gagal", JOptionPane.WARNING_MESSAGE);
    }

    // ==========================================
    // STYLING DAN ELEMEN KOSMETIK SWING CUSTOM
    // ==========================================
    private void styleButton(JButton btn, Color bg, Color fg) {
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bg.brighter(), 1, true),
            new EmptyBorder(8, 14, 8, 14)
        ));
    }

    private void styleToggleButton(JToggleButton btn, Color bg, Color fg) {
        btn.setUI(new javax.swing.plaf.basic.BasicToggleButtonUI());
        btn.setBackground(bg);
        btn.setForeground(fg);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(bg.brighter(), 1),
            new EmptyBorder(6, 12, 6, 12)
        ));
    }

    private void styleUpgradeButton(JButton btn) {
        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI());
        btn.setBackground(bgInput);
        btn.setForeground(textLight);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 11));
        btn.setOpaque(true);
        btn.setContentAreaFilled(true);
        btn.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderDark, 1, true),
            new EmptyBorder(10, 10, 10, 10)
        ));
    }

    private void styleTextField(JTextField tf) {
        tf.setBackground(bgInput);
        tf.setForeground(textLight);
        tf.setCaretColor(Color.WHITE);
        tf.setFont(new Font("SansSerif", Font.PLAIN, 12));
        tf.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderDark, 1, true),
            new EmptyBorder(4, 6, 4, 6)
        ));
    }

    private void styleComboBox(JComboBox<?> cb) {
        cb.setUI(new javax.swing.plaf.basic.BasicComboBoxUI());
        cb.setBackground(bgInput);
        cb.setForeground(textLight);
        cb.setFont(new Font("SansSerif", Font.BOLD, 12));
        cb.setRenderer(new CustomComboCellRenderer());
        cb.setBorder(BorderFactory.createCompoundBorder(
            new LineBorder(borderDark, 1, true),
            new EmptyBorder(2, 2, 2, 2)
        ));
    }

    private void styleTable(JTable table) {
        table.setBackground(bgCard);
        table.setForeground(textLight);
        table.setGridColor(borderDark);
        table.setRowHeight(26);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.setSelectionBackground(new Color(59, 130, 246, 120));
        table.setSelectionForeground(textLight);

        // Headers
        table.getTableHeader().setBackground(bgDark);
        table.getTableHeader().setForeground(textLight);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 11));
        table.getTableHeader().setBorder(new LineBorder(borderDark, 1));

        // Center renderers
        DefaultTableCellRenderer cellRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable t, Object v, boolean isSel, boolean hasF, int row, int col) {
                Component c = super.getTableCellRendererComponent(t, v, isSel, hasF, row, col);
                setBackground(row % 2 == 0 ? bgCard : new Color(30, 41, 59));
                setForeground(textLight);
                setHorizontalAlignment(SwingConstants.CENTER);
                
                // Highlight VIP / Urgent priorities
                if (col == 3) {
                    String str = String.valueOf(v);
                    if (str.contains("VIP")) {
                        setForeground(accentRed);
                        setFont(t.getFont().deriveFont(Font.BOLD));
                    } else if (str.contains("URGENT")) {
                        setForeground(accentOrange);
                        setFont(t.getFont().deriveFont(Font.BOLD));
                    } else {
                        setForeground(accentBlue);
                    }
                }
                if (col == 4) {
                    setForeground(accentGreen);
                }
                return c;
            }
        };
        for (int i = 0; i < table.getColumnCount(); i++) {
            table.getColumnModel().getColumn(i).setCellRenderer(cellRenderer);
        }
    }

    private JLabel createFormLabel(String text) {
        JLabel label = new JLabel(text);
        label.setFont(new Font("SansSerif", Font.BOLD, 11));
        label.setForeground(textLight);
        return label;
    }

    private TitledBorder createNeonTitledBorder(String title, Color color) {
        TitledBorder border = BorderFactory.createTitledBorder(
            new LineBorder(borderDark, 1, true),
            title,
            TitledBorder.LEFT,
            TitledBorder.TOP,
            new Font("Monospaced", Font.BOLD, 12),
            color
        );
        return border;
    }

    // Custom List cell renderer to secure beautiful text visibility
    private class CustomListCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBackground(index % 2 == 0 ? bgCard : new Color(30, 41, 59));
            setForeground(textLight);
            setFont(new Font("Monospaced", Font.PLAIN, 12));
            setBorder(new EmptyBorder(6, 10, 6, 10));
            if (isSelected) {
                setBackground(new Color(14, 165, 233, 100));
            }
            return c;
        }
    }

    // Custom Combo Box renderer to secure beautiful high-contrast drop downs
    private class CustomComboCellRenderer extends DefaultListCellRenderer {
        @Override
        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            Component c = super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
            setBackground(bgInput);
            setForeground(textLight);
            setBorder(new EmptyBorder(4, 8, 4, 8));
            if (isSelected) {
                setBackground(accentBlue);
                setForeground(Color.WHITE);
            }
            return c;
        }
    }

    // Custom Stats Card Component (Clean boxes with rounded glowing border)
    private static class StatsCard extends JPanel {
        private final JLabel titleLabel;
        private final JLabel valueLabel;
        private final JLabel subLabel;
        private final Color accentColor;

        public StatsCard(String title, String val, String sub, Color accent) {
            this.accentColor = accent;
            setLayout(new BorderLayout(2, 2));
            setBackground(new Color(22, 28, 45)); // Slate 900
            setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(new Color(51, 65, 85), 1, true),
                new EmptyBorder(8, 12, 8, 12)
            ));

            titleLabel = new JLabel(title.toUpperCase());
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 9));
            titleLabel.setForeground(new Color(148, 163, 184)); // muted gray

            valueLabel = new JLabel(val);
            valueLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
            valueLabel.setForeground(accentColor);

            subLabel = new JLabel(sub);
            subLabel.setFont(new Font("SansSerif", Font.PLAIN, 10));
            subLabel.setForeground(new Color(148, 163, 184));

            add(titleLabel, BorderLayout.NORTH);
            add(valueLabel, BorderLayout.CENTER);
            add(subLabel, BorderLayout.SOUTH);
        }

        public void updateValue(String val, String sub, Color newColor) {
            valueLabel.setText(val);
            valueLabel.setForeground(newColor);
            subLabel.setText(sub);
        }
    }

    // ==========================================
    // OVERRIDES DARI INTERFACE WAREHOUSEGUIMODULE
    // ==========================================
    @Override
    public void initializeMainFrame() {
        setVisible(true);
    }

    @Override
    public void triggerRobotAnimation(List<Integer> path) {
        if (path == null || path.isEmpty()) return;
        this.currentRoute = path;
        this.isRobotMoving = true;
        this.trailPoints.clear();
        animateRobotMovement(0);
    }

    @Override
    public void updateUIComponents(Collection<Order> pending, Stack<Order> history) {
        updatePendingTable();
        updateHistoryList();
    }
}
