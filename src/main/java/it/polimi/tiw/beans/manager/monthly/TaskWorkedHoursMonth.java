package it.polimi.tiw.beans.manager.monthly;

public class TaskWorkedHoursMonth {
    private final int month;
    private final int workedHours;

    public TaskWorkedHoursMonth(int month, int workedHours) {
        this.month = month;
        this.workedHours = workedHours;
    }

    public int getMonth() {
        return month;
    }

    public int getWorkedHours() {
        return workedHours;
    }
}
