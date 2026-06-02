package it.polimi.tiw.beans.manager.monthly;

public class TaskHoursMonth {
    private final int month;
    private final Integer expectedHours;
    private final int workedHours;

    public TaskHoursMonth(int month, Integer expectedHours, int workedHours) {
        this.month = month;
        this.expectedHours = expectedHours;
        this.workedHours = workedHours;
    }

    public int getMonth() {
        return month;
    }

    public Integer getExpectedHours() {
        return expectedHours;
    }

    public int getWorkedHours() {
        return workedHours;
    }
}
