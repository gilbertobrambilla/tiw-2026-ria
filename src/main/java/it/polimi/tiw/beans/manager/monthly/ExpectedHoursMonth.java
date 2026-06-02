package it.polimi.tiw.beans.manager.monthly;

public class ExpectedHoursMonth {
    private final int month;
    private final Integer expectedHours;

    public ExpectedHoursMonth(int month, Integer expectedHours) {
        this.month = month;
        this.expectedHours = expectedHours;
    }

    public int getMonth() {
        return month;
    }

    public Integer getExpectedHours() {
        return expectedHours;
    }
}
