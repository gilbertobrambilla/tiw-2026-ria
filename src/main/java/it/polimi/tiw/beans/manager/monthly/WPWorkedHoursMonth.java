package it.polimi.tiw.beans.manager.monthly;

public class WPWorkedHoursMonth {
    private final int month;
    private final int totalWorkedHours;

    public WPWorkedHoursMonth(int month, int totalWorkedHours) {
        this.month = month;
        this.totalWorkedHours = totalWorkedHours;
    }

    public int getMonth() {
        return month;
    }

    public int getTotalWorkedHours() {
        return totalWorkedHours;
    }
}
