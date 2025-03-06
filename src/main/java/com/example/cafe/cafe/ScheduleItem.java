package com.example.cafe.cafe;

import javafx.scene.control.ComboBox;

public class ScheduleItem {
    private String day;
    private ComboBox<String> openTimeComboBox;
    private ComboBox<String> closeTimeComboBox;

    public ScheduleItem(String day, ComboBox<String> openTimeComboBox, ComboBox<String> closeTimeComboBox) {
        this.day = day;
        this.openTimeComboBox = openTimeComboBox;
        this.closeTimeComboBox = closeTimeComboBox;
    }

    public String getDay() {
        return day;
    }

    public ComboBox<String> getOpenTimeComboBox() {
        return openTimeComboBox;
    }

    public ComboBox<String> getCloseTimeComboBox() {
        return closeTimeComboBox;
    }
}
