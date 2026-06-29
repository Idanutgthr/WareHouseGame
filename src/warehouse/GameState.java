package warehouse;

/**
 * Modul State & Ekonomi (Tycoon).
 */
public class GameState {
    // Keuangan & Ekonomi
    private double virtualCash = 250.0; // Modal awal
    private double passiveIncomeRate = 0.0; // Uang pasif per detik

    // Kepemilikan Robot
    private int totalRobots = 1;
    private int robotSpeedUpgradeLevel = 1; // Meningkatkan kecepatan animasi robot
    private int robotCapacityUpgradeLevel = 1; // Kapasitas angkut
    
    // Status Gudang
    private int unlockedAreas = 1; // Berapa area gudang yang terbuka (maksimal 3)
    private boolean isIdleActive = true; // Apakah pesanan masuk otomatis (idle) aktif

    // Statistik Kumulatif
    public int totalOrdersCompleted = 0;
    public double totalRevenueEarned = 0.0;
    public double totalDistanceTraveled = 0.0;
    public long totalProcessingTimeMs = 0;

    // Upgrade Cost Constants
    public double getRobotBuyCost() { return 200 * totalRobots; }
    public double getSpeedUpgradeCost() { return 50 * robotSpeedUpgradeLevel; }
    public double getCapacityUpgradeCost() { return 75 * robotCapacityUpgradeLevel; }
    public double getUnlockAreaCost() { return 500 * unlockedAreas; }

    public void addCash(double amount) {
        this.virtualCash += amount;
        this.totalRevenueEarned += amount;
    }

    public boolean spendCash(double amount) {
        if (this.virtualCash >= amount) {
            this.virtualCash -= amount;
            return true;
        }
        return false;
    }

    public double getVirtualCash() { return virtualCash; }
    public int getTotalRobots() { return totalRobots; }
    public int getSpeedLevel() { return robotSpeedUpgradeLevel; }
    public int getCapacityLevel() { return robotCapacityUpgradeLevel; }
    public int getUnlockedAreas() { return unlockedAreas; }
    public boolean isIdleActive() { return isIdleActive; }
    
    public void setVirtualCash(double cash) { this.virtualCash = cash; }
    public void incrementRobots() { this.totalRobots++; }
    public void incrementSpeed() { this.robotSpeedUpgradeLevel++; }
    public void incrementCapacity() { this.robotCapacityUpgradeLevel++; }
    public void incrementUnlockedAreas() { this.unlockedAreas++; }
}
