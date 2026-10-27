package com.cpuscheduler.ui.components;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.StackPane;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tooltip;
import javafx.scene.shape.Rectangle;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.animation.TranslateTransition;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * Panel displaying the queues (Ready, CPU, Waiting, Completed) with enhanced animations
 */
public class QueuePanel {
    private VBox view;
    private FlowPane readyQueue, cpuQueue, waitingQueue, completedQueue;

    public QueuePanel() {
        this.view = new VBox(15);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background-color: #f8f9fa;");

        // Ready Queue
        readyQueue = createQueuePanel("#3498db");
        
        // CPU Queue  
        cpuQueue = createQueuePanel("#e74c3c");
        
        // Waiting Queue
        waitingQueue = createQueuePanel("#f39c12");
        
        // Completed Queue
        completedQueue = createQueuePanel("#27ae60");

        HBox queuesLayout = new HBox(15);
        queuesLayout.setPadding(new Insets(10));
        queuesLayout.getChildren().addAll(
                createQueueContainer(readyQueue, "READY QUEUE", "#3498db"),
                createQueueContainer(cpuQueue, "CPU", "#e74c3c"),
                createQueueContainer(waitingQueue, "WAITING (I/O)", "#f39c12"),
                createQueueContainer(completedQueue, "COMPLETED", "#27ae60")
        );

        view.getChildren().add(queuesLayout);
    }

    private FlowPane createQueuePanel(String color) {
        FlowPane pane = new FlowPane();
        pane.setPrefWrapLength(140);
        pane.setStyle("-fx-background-color: " + color + "15; -fx-border-color: " + color + 
                      "; -fx-border-radius: 8; -fx-border-width: 2;");
        pane.setPadding(new Insets(12));
        pane.setHgap(8);
        pane.setVgap(8);
        pane.setMinHeight(140);
        pane.setStyle(pane.getStyle() + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 8, 0, 0, 2);");
        return pane;
    }

    private VBox createQueueContainer(FlowPane queue, String title, String color) {
        VBox container = new VBox(8);
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 13; -fx-text-fill: " + color + ";");
        container.getChildren().addAll(titleLabel, queue);
        container.setPrefWidth(160);
        return container;
    }

    public void addProcessToReady(String processId, int arrivalTime, int burstTime, int remainingTime, String color) {
        addAnimatedProcessCard(readyQueue, processId, arrivalTime, burstTime, remainingTime, color);
    }

    public void addProcessToCPU(String processId, int arrivalTime, int burstTime, int remainingTime, String color, double progress) {
        cpuQueue.getChildren().clear();
        addProcessCardWithProgress(cpuQueue, processId, arrivalTime, burstTime, remainingTime, color, progress, true);
    }

    public void addProcessToWaiting(String processId, int arrivalTime, int burstTime, int remainingTime, String color) {
        addAnimatedProcessCard(waitingQueue, processId, arrivalTime, burstTime, remainingTime, color);
    }

    public void addProcessToCompleted(String processId, int arrivalTime, int burstTime, int remainingTime, String color) {
        addAnimatedProcessCard(completedQueue, processId, arrivalTime, burstTime, remainingTime, color);
    }

    private void addAnimatedProcessCard(FlowPane queue, String processId, int arrivalTime, int burstTime, 
                                        int remainingTime, String color) {
        VBox card = createProcessCard(processId, arrivalTime, burstTime, remainingTime, color, 0.0, false);
        
        // Slide in animation
        TranslateTransition transition = new TranslateTransition(Duration.millis(600), card);
        transition.setFromX(-50);
        transition.setFromY(-30);
        transition.setToX(0);
        transition.setToY(0);
        transition.play();
        
        queue.getChildren().add(card);
    }

    private void addProcessCardWithProgress(FlowPane queue, String processId, int arrivalTime, int burstTime,
                                            int remainingTime, String color, double progress, boolean showGlow) {
        VBox card = createProcessCard(processId, arrivalTime, burstTime, remainingTime, color, progress, showGlow);
        queue.getChildren().add(card);
    }

    private VBox createProcessCard(String processId, int arrivalTime, int burstTime, 
                                   int remainingTime, String color, double progress, boolean showGlow) {
        VBox card = new VBox(4);
        card.setStyle("-fx-background-color: " + color + "; -fx-text-fill: white; " +
                      "-fx-padding: 8; -fx-border-radius: 6; -fx-font-size: 11;");
        
        if (showGlow) {
            card.setStyle(card.getStyle() + "; -fx-effect: dropshadow(gaussian, " + color + ", 15, 0.8, 0, 0);");
        }

        Label idLabel = new Label(processId);
        idLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 12; -fx-text-fill: white;");
        
        Label timeLabel = new Label("AT: " + arrivalTime);
        timeLabel.setStyle("-fx-font-size: 9; -fx-text-fill: white;");
        
        Label burstLabel = new Label("BT: " + burstTime);
        burstLabel.setStyle("-fx-font-size: 9; -fx-text-fill: white;");
        
        Label remainingLabel = new Label("Rem: " + remainingTime);
        remainingLabel.setStyle("-fx-font-size: 9; -fx-text-fill: white;");

        card.getChildren().addAll(idLabel, timeLabel, burstLabel, remainingLabel);

        // Add progress bar if showing progress
        if (progress > 0) {
            ProgressBar progressBar = new ProgressBar(progress);
            progressBar.setPrefWidth(80);
            progressBar.setStyle("-fx-accent: white;");
            card.getChildren().add(progressBar);
        }

        // Add tooltip
        Tooltip tooltip = new Tooltip(
            "Process: " + processId + "\n" +
            "Arrival: " + arrivalTime + "ms\n" +
            "Burst: " + burstTime + "ms\n" +
            "Remaining: " + remainingTime + "ms"
        );
        Tooltip.install(card, tooltip);

        return card;
    }

    public void clearQueues() {
        readyQueue.getChildren().clear();
        cpuQueue.getChildren().clear();
        waitingQueue.getChildren().clear();
        completedQueue.getChildren().clear();
    }

    public void removeProcessFromReady(String processId) {
        removeProcessCard(readyQueue, processId);
    }

    private void removeProcessCard(FlowPane queue, String processId) {
        queue.getChildren().removeIf(node -> {
            if (node instanceof VBox) {
                for (var child : ((VBox) node).getChildren()) {
                    if (child instanceof Label && ((Label) child).getText().equals(processId)) {
                        return true;
                    }
                }
            }
            return false;
        });
    }

    public VBox getView() {
        return view;
    }
}
