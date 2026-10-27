package com.cpuscheduler.ui.components;

import javafx.scene.layout.VBox;
import javafx.scene.control.Label;
import javafx.geometry.Insets;

/**
 * Statistics Panel displaying metrics
 */
public class StatisticsPanel {
    private VBox view;
    private Label avgWtLabel, avgTatLabel, avgRtLabel, utilizationLabel, throughputLabel;

    public StatisticsPanel() {
        this.view = new VBox(8);
        view.setPadding(new Insets(10));
        view.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #bdc3c7; -fx-border-radius: 5;");

        avgWtLabel = createStatLabel("Avg WT:", "0.00");
        avgTatLabel = createStatLabel("Avg TAT:", "0.00");
        avgRtLabel = createStatLabel("Avg RT:", "0.00");
        utilizationLabel = createStatLabel("CPU Util:", "0%");
        throughputLabel = createStatLabel("Throughput:", "0.00");

        view.getChildren().addAll(avgWtLabel, avgTatLabel, avgRtLabel, utilizationLabel, throughputLabel);
    }

    private Label createStatLabel(String name, String value) {
        Label label = new Label(name + " " + value);
        label.setStyle("-fx-font-size: 11; -fx-font-family: monospace;");
        return label;
    }

    public void updateStatistics(double avgWt, double avgTat, double avgRt, double utilization, double throughput) {
        avgWtLabel.setText(String.format("Avg WT: %.2f", avgWt));
        avgTatLabel.setText(String.format("Avg TAT: %.2f", avgTat));
        avgRtLabel.setText(String.format("Avg RT: %.2f", avgRt));
        utilizationLabel.setText(String.format("CPU Util: %.1f%%", utilization));
        throughputLabel.setText(String.format("Throughput: %.2f", throughput));
    }

    public void reset() {
        updateStatistics(0, 0, 0, 0, 0);
    }

    public VBox getView() {
        return view;
    }
}
