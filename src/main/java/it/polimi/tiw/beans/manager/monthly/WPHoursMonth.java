package it.polimi.tiw.beans.manager.monthly;

public class WPHoursMonth {
    private final int month;
    private final Integer totalExpectedHours;
    private final int totalWorkedHours;

    public WPHoursMonth(int month, Integer totalExpectedHours, int totalWorkedHours) {
        this.month = month;
        this.totalExpectedHours = totalExpectedHours;
        this.totalWorkedHours = totalWorkedHours;
    }

    public int getMonth() {
        return month;
    }

    public Integer getTotalExpectedHours() {
        return totalExpectedHours;
    }

    public int getTotalWorkedHours() {
        return totalWorkedHours;
    }
}
