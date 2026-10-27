package com.cpuscheduler.ui.components;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Line;
import javafx.geometry.Insets;
import javafx.scene.paint.Color;
import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.util.Duration;
import java.util.HashMap;
import java.util.Map;

/**
 * Enhanced Gantt Chart Panel for timeline visualization with live updates
 */
public class GanttChartPanel {
    private VBox view;
    private Pane ganttCanvas;
    private Label timeLabel;
    private Map<String, Color> processColors;
    private int currentTime;
    private double pixelsPerUnit = 20;

    public GanttChartPanel() {
        this.view = new VBox(10);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");

        Label title = new Label("GANTT CHART - TIMELINE VISUALIZATION");
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14; -fx-text-fill: #2c3e50;");

        HBox timeBox = new HBox(10);
        timeBox.setPadding(new Insets(5));
        Label timeDescLabel = new Label("System Time:");
        timeDescLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12;");
        timeLabel = new Label("0 ms");
        timeLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #e74c3c; -fx-font-weight: bold;");
        timeBox.getChildren().addAll(timeDescLabel, timeLabel);

        ganttCanvas = new Pane();
        ganttCanvas.setPrefHeight(180);
        ganttCanvas.setStyle("-fx-background-color: #ecf0f1; -fx-border-color: #95a5a6; -fx-border-radius: 5;");

        ScrollPane scrollPane = new ScrollPane(ganttCanvas);
        scrollPane.setFitToHeight(true);
        scrollPane.setPrefHeight(200);
        scrollPane.setStyle("-fx-control-inner-background: #ecf0f1;");

        this.processColors = new HashMap<>();
        this.currentTime = 0;

        // Draw time scale
        drawTimeScale();

        view.getChildren().addAll(title, timeBox, scrollPane);
    }

    private void drawTimeScale() {
        ganttCanvas.getChildren().clear();
        
        // Draw time markers every 5 units
        for (int i = 0; i <= 100; i += 5) {
            double x = i * pixelsPerUnit;
            
            Line tick = new Line(x, 10, x, 20);
            tick.setStroke(Color.web("#95a5a6"));
            tick.setStrokeWidth(1);
            
            Label timeMarker = new Label(String.valueOf(i));
            timeMarker.setLayoutX(x - 10);
            timeMarker.setLayoutY(0);
            timeMarker.setStyle("-fx-font-size: 9; -fx-text-fill: #7f8c8d;");
            
            ganttCanvas.getChildren().addAll(tick, timeMarker);
        }

        // Draw baseline
        Line baseline = new Line(0, 30, 2000, 30);
        baseline.setStroke(Color.web("#34495e"));
        baseline.setStrokeWidth(2);
        ganttCanvas.getChildren().add(baseline);
    }

    public void addGanttBlock(String processId, int startTime, int duration, String color) {
        double blockX = startTime * pixelsPerUnit + 10;
        double blockWidth = Math.max(duration * pixelsPerUnit, 20);
        double blockY = 50;

        // Main block
        Rectangle block = new Rectangle(blockX, blockY, blockWidth, 40);
        block.setFill(Color.web(color));
        block.setStroke(Color.BLACK);
        block.setStrokeWidth(2);

        // Gradient effect
        block.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.2), 5, 0, 0, 2);");

        // Process label
        Label label = new Label(processId);
        label.setLayoutX(blockX + 5);
        label.setLayoutY(blockY + 10);
        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 11;");

        // Duration label
        Label durationLabel = new Label(duration + "ms");
        durationLabel.setLayoutX(blockX + 5);
        durationLabel.setLayoutY(blockY + 25);
        durationLabel.setStyle("-fx-text-fill: white; -fx-font-size: 9;");

        ganttCanvas.getChildren().addAll(block, label, durationLabel);
    }

    public void addIdleBlock(int startTime, int endTime) {
        double blockX = startTime * pixelsPerUnit + 10;
        double blockWidth = (endTime - startTime) * pixelsPerUnit;
        double blockY = 50;

        Rectangle block = new Rectangle(blockX, blockY, blockWidth, 40);
        block.setFill(Color.web("#bdc3c7"));
        block.setStroke(Color.web("#7f8c8d"));
        block.setStrokeWidth(2);
        block.setOpacity(0.5);

        Label label = new Label("IDLE");
        label.setLayoutX(blockX + 5);
        label.setLayoutY(blockY + 12);
        label.setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-font-size: 10;");

        ganttCanvas.getChildren().addAll(block, label);
    }

    public void updateCurrentTimeMarker(int time) {
        // Remove old marker
        ganttCanvas.getChildren().removeIf(node -> node instanceof Line && 
                                           ((Line) node).getStrokeWidth() == 3);

        // Draw new time marker
        double markerX = time * pixelsPerUnit + 10;
        Line timeMarker = new Line(markerX, 30, markerX, 100);
        timeMarker.setStroke(Color.web("#e74c3c"));
        timeMarker.setStrokeWidth(3);
        timeMarker.setStrokeDashOffset(5);

        ganttCanvas.getChildren().add(timeMarker);
    }

    public void clearGantt() {
        ganttCanvas.getChildren().clear();
        drawTimeScale();
        currentTime = 0;
        updateTime(0);
    }

    public void updateTime(int time) {
        this.currentTime = time;
        timeLabel.setText(time + " ms");
        updateCurrentTimeMarker(time);
    }

    public VBox getView() {
        return view;
    }
}
