package com.cpuscheduler.ui.components;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.geometry.Insets;

/**
 * Event Log Panel for displaying step-by-step events
 */
public class EventLogPanel {
    private VBox view;
    private VBox eventList;
    private ScrollPane scrollPane;

    public EventLogPanel() {
        this.view = new VBox(10);
        view.setPadding(new Insets(10));
        view.setPrefHeight(200);

        eventList = new VBox(5);
        eventList.setStyle("-fx-background-color: #f9f9f9; -fx-padding: 5;");

        scrollPane = new ScrollPane(eventList);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-control-inner-background: #f9f9f9;");

        view.getChildren().add(scrollPane);
    }

    public void addEvent(String time, String description) {
        Label eventLabel = new Label("[" + time + "] " + description);
        eventLabel.setStyle("-fx-font-size: 10; -fx-font-family: monospace; -fx-text-wrap: true; -fx-text-fill: #2c3e50;");
        eventLabel.setWrapText(true);
        eventList.getChildren().add(0, eventLabel);

        // Keep only last 20 events
        if (eventList.getChildren().size() > 20) {
            eventList.getChildren().remove(eventList.getChildren().size() - 1);
        }

        scrollPane.setVvalue(0);
    }

    public void clear() {
        eventList.getChildren().clear();
    }

    public VBox getView() {
        return view;
    }
}
