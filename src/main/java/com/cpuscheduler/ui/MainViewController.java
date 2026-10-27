package com.cpuscheduler.ui;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.control.*;
import javafx.geometry.Insets;
import com.cpuscheduler.model.Process;
import com.cpuscheduler.ui.components.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Main View Controller for the CPU Scheduler UI
 */
public class MainViewController {
    private BorderPane root;
    private ControlPanel controlPanel;
    private QueuePanel queuePanel;
    private GanttChartPanel ganttPanel;
    private StatisticsPanel statsPanel;
    private EventLogPanel eventLogPanel;
    private SchedulerEngine schedulerEngine;

    public MainViewController() {
        this.root = new BorderPane();
        initializeUI();
    }

    private void initializeUI() {
        // Top control panel
        controlPanel = new ControlPanel(
                this::onSimulationStarted,
                this::onSimulationPaused,
                this::onSimulationReset,
                this::onSpeedChanged
        );
        root.setTop(controlPanel.getView());

        // Main content area
        HBox mainContent = new HBox(10);
        mainContent.setPadding(new Insets(10));
        mainContent.setStyle("-fx-background-color: #ecf0f1;");

        // Left panel: Queues (larger)
        queuePanel = new QueuePanel();
        VBox leftPanel = new VBox(10);
        leftPanel.getChildren().add(queuePanel.getView());
        leftPanel.setStyle("-fx-padding: 0;");
        HBox.setHgrow(leftPanel, Priority.ALWAYS);

        // Right panel: Stats and Events
        statsPanel = new StatisticsPanel();
        eventLogPanel = new EventLogPanel();
        
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(10));
        rightPanel.setStyle("-fx-background-color: white; -fx-border-color: #bdc3c7; -fx-border-radius: 8;");
        
        Label statsTitle = new Label("📊 STATISTICS");
        statsTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #2c3e50;");
        
        Label eventTitle = new Label("📝 EVENT LOG");
        eventTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: #2c3e50;");
        
        Separator separator = new Separator();
        separator.setStyle("-fx-padding: 5;");

        VBox statsContainer = new VBox(5);
        statsContainer.getChildren().addAll(statsTitle, statsPanel.getView());
        statsContainer.setPrefHeight(180);

        VBox eventContainer = new VBox(5);
        eventContainer.getChildren().addAll(eventTitle, eventLogPanel.getView());
        eventContainer.setPrefHeight(300);
        VBox.setVgrow(eventContainer, Priority.ALWAYS);

        rightPanel.getChildren().addAll(statsContainer, separator, eventContainer);
        rightPanel.setPrefWidth(300);

        mainContent.getChildren().addAll(leftPanel, rightPanel);

        // Center layout with Gantt chart at bottom
        BorderPane centerLayout = new BorderPane();
        centerLayout.setCenter(mainContent);
        
        VBox ganttContainer = new VBox(10);
        ganttContainer.setPadding(new Insets(10));
        ganttPanel = new GanttChartPanel();
        ganttContainer.getChildren().add(ganttPanel.getView());
        ganttContainer.setStyle("-fx-background-color: #ecf0f1;");
        
        centerLayout.setBottom(ganttContainer);
        root.setCenter(centerLayout);

        // Footer
        HBox footer = createFooter();
        root.setBottom(footer);

        // Initialize scheduler engine
        this.schedulerEngine = new SchedulerEngine(queuePanel, ganttPanel, statsPanel, eventLogPanel);
    }

    private HBox createFooter() {
        HBox footer = new HBox();
        footer.setPadding(new Insets(10, 15, 10, 15));
        footer.setStyle("-fx-background-color: #2c3e50; -fx-text-fill: white;");

        Label creditsLabel = new Label("CPU Scheduler Simulator | Developers: VAISHNAVI SATONE & BHAVISHKA PAUNIKAR");
        creditsLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10;");

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label versionLabel = new Label("v1.0.0");
        versionLabel.setStyle("-fx-text-fill: #95a5a6; -fx-font-size: 10;");

        footer.getChildren().addAll(creditsLabel, spacer, versionLabel);
        return footer;
    }

    private void onSimulationStarted() {
        String algorithm = controlPanel.getSelectedAlgorithm();
        int timeQuantum = controlPanel.getTimeQuantum();
        List<Process> processes = controlPanel.getSampleProcesses();
        
        schedulerEngine.startSimulation(algorithm, new ArrayList<>(processes), timeQuantum);
    }

    private void onSimulationPaused(boolean paused) {
        schedulerEngine.setPaused(paused);
    }

    private void onSimulationReset() {
        schedulerEngine.reset();
    }

    private void onSpeedChanged(double speed) {
        schedulerEngine.setAnimationSpeed(speed);
    }

    public BorderPane getView() {
        return root;
    }
}

