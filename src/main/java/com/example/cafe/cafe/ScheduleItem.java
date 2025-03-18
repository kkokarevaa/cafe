package com.example.cafe.cafe;

import javafx.scene.control.ComboBox;

/**
 * Класс, представляющий элемент расписания работы кафе на один день.
 */
public class ScheduleItem {
    private String day; // День недели
    private ComboBox<String> openTimeComboBox; // Выпадающий список для выбора времени открытия
    private ComboBox<String> closeTimeComboBox; // Выпадающий список для выбора времени закрытия

    /**
     * Конструктор класса ScheduleItem
     *
     * @param day               День недели
     * @param openTimeComboBox  Комбо-бокс для времени открытия
     * @param closeTimeComboBox Комбо-бокс для времени закрытия
     */
    public ScheduleItem(String day, ComboBox<String> openTimeComboBox, ComboBox<String> closeTimeComboBox) {
        this.day = day;
        this.openTimeComboBox = openTimeComboBox;
        this.closeTimeComboBox = closeTimeComboBox;
    }

    /**
     * Получает день недели
     *
     * @return День недели
     */
    public String getDay() {
        return day;
    }

    /**
     * Получает комбо-бокс с временем открытия
     *
     * @return Комбо-бокс для выбора времени открытия
     */
    public ComboBox<String> getOpenTimeComboBox() {
        return openTimeComboBox;
    }

    /**
     * Получает комбо-бокс с временем закрытия
     *
     * @return Комбо-бокс для выбора времени закрытия
     */
    public ComboBox<String> getCloseTimeComboBox() {
        return closeTimeComboBox;
    }
}
