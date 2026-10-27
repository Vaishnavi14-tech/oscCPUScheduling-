package com.cpuscheduler.ui.components;

import javafx.scene.layout.VBox;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.Label;
import javafx.scene.control.Tooltip;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.collections.FXCollections;
import javafx.scene.Cursor;
import com.cpuscheduler.model.Process;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Control panel for simulation controls with modern UI
 */
public class ControlPanel {
    private VBox view;
    private Button startBtn, pauseBtn, resetBtn;
    private ComboBox<String> algorithmCombo;
    private Spinner<Integer> quantumSpinner;
    private ComboBox<String> speedCombo;
    private List<Process> sampleProcesses;
    private Runnable onStart;
    private Consumer<Boolean> onPause;
    private Runnable onReset;
    private Consumer<Double> onSpeed;

    public ControlPanel(
            Runnable onStart,
            Consumer<Boolean> onPause,
            Runnable onReset,
            Consumer<Double> onSpeed) {
        
        this.onStart = onStart;
        this.onPause = onPause;
        this.onReset = onReset;
        this.onSpeed = onSpeed;
        this.view = new VBox(12);
        view.setPadding(new Insets(15));
        view.setStyle("-fx-background: linear-gradient(to right, #2c3e50, #34495e); " +
                      "-fx-text-fill: white;");

        initializeSampleProcesses();
        buildUI();
    }

    private void initializeSampleProcesses() {
        sampleProcesses = new ArrayList<>();
        sampleProcesses.add(new Process("P1", 0, 7, 2));
        sampleProcesses.add(new Process("P2", 1, 4, 1));
        sampleProcesses.add(new Process("P3", 3, 2, 3));
        sampleProcesses.add(new Process("P4", 5, 5, 2));
    }

    private void buildUI() {
        // Title
        Label title = new Label("🖥️ CPU SCHEDULER SIMULATOR");
        title.setStyle("-fx-font-size: 16; -fx-font-weight: bold; -fx-text-fill: white;");

        // Algorithm Section
        HBox algorithmBox = createSectionBox("Algorithm:", buildAlgorithmControls());

        // Parameters Section
        HBox parameterBox = createSectionBox("Parameters:", buildParameterControls());

        // Control Buttons Section
        HBox controlBox = createSectionBox("Controls:", buildControlButtons());

        // Speed Control Section
        HBox speedBox = createSectionBox("Playback Speed:", buildSpeedControls());

        view.getChildren().addAll(title, algorithmBox, parameterBox, controlBox, speedBox);
    }

    private HBox createSectionBox(String label, HBox content) {
        HBox section = new HBox(10);
        section.setAlignment(Pos.CENTER_LEFT);
        Label sectionLabel = new Label(label);
        sectionLabel.setStyle("-fx-font-weight: bold; -fx-font-size: 11; " +
                             "-fx-text-fill: #ecf0f1; -fx-min-width: 110;");
        section.getChildren().addAll(sectionLabel, content);
        HBox.setHgrow(content, Priority.ALWAYS);
        return section;
    }

    private HBox buildAlgorithmControls() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        
        algorithmCombo = new ComboBox<>(FXCollections.observableArrayList(
                "FCFS", "SJF", "Round Robin", "Priority"
        ));
        algorithmCombo.setValue("FCFS");
        algorithmCombo.setStyle("-fx-padding: 6 10; -fx-font-size: 11;");
        algorithmCombo.setPrefWidth(140);
        Tooltip.install(algorithmCombo, new Tooltip("Select scheduling algorithm"));

        box.getChildren().add(algorithmCombo);
        return box;
    }

    private HBox buildParameterControls() {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        
        // Time Quantum for RR
        Label quantumLabel = new Label("Time Quantum (ms):");
        quantumLabel.setStyle("-fx-text-fill: white; -fx-font-size: 10;");
        
        quantumSpinner = new Spinner<>(1, 10, 2);
        quantumSpinner.setPrefWidth(70);
        quantumSpinner.setStyle("-fx-font-size: 10;");
        Tooltip.install(quantumSpinner, new Tooltip("Time slice for Round Robin"));

        box.getChildren().addAll(quantumLabel, quantumSpinner);
        return box;
    }

    private HBox buildControlButtons() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        
        startBtn = createButton("▶ Start", "#27ae60", "Begin simulation");
        pauseBtn = createButton("⏸ Pause", "#f39c12", "Pause/Resume simulation");
        resetBtn = createButton("⟲ Reset", "#e74c3c", "Reset to initial state");

        startBtn.setOnAction(evt -> {
            String algo = algorithmCombo.getValue();
            int quantum = quantumSpinner.getValue();
            if (onStart != null) {
                onStart.run();
            }
        });

        pauseBtn.setOnAction(evt -> {
            if (onPause != null) {
                onPause.accept(pauseBtn.getText().contains("▶"));
                pauseBtn.setText(pauseBtn.getText().contains("▶") ? "▶ Resume" : "⏸ Pause");
            }
        });

        resetBtn.setOnAction(evt -> {
            if (onReset != null) {
                onReset.run();
            }
            pauseBtn.setText("⏸ Pause");
        });

        box.getChildren().addAll(startBtn, pauseBtn, resetBtn);
        return box;
    }

    private HBox buildSpeedControls() {
        HBox box = new HBox(8);
        box.setAlignment(Pos.CENTER_LEFT);
        
        speedCombo = new ComboBox<>(FXCollections.observableArrayList(
                "1x (Normal)", "2x (Fast)", "5x (Very Fast)"
        ));
        speedCombo.setValue("1x (Normal)");
        speedCombo.setStyle("-fx-padding: 6 10; -fx-font-size: 11;");
        speedCombo.setPrefWidth(140);

        speedCombo.setOnAction(evt -> {
            double speed = switch(speedCombo.getValue()) {
                case "1x (Normal)" -> 1.0;
                case "2x (Fast)" -> 2.0;
                case "5x (Very Fast)" -> 5.0;
                default -> 1.0;
            };
            if (onSpeed != null) {
                onSpeed.accept(speed);
            }
        });

        Tooltip.install(speedCombo, new Tooltip("Adjust animation speed"));
        box.getChildren().add(speedCombo);
        return box;
    }

    private Button createButton(String text, String color, String tooltip) {
        Button btn = new Button(text);
        btn.setStyle("-fx-font-size: 11; -fx-padding: 8 16; " +
                    "-fx-background-color: " + color + "; -fx-text-fill: white; " +
                    "-fx-font-weight: bold; -fx-border-radius: 4; -fx-cursor: hand;");
        btn.setOnMouseEntered(evt -> btn.setStyle(btn.getStyle() + "; -fx-opacity: 0.9;"));
        btn.setOnMouseExited(evt -> btn.setStyle(btn.getStyle().replace("; -fx-opacity: 0.9;", "")));
        Tooltip.install(btn, new Tooltip(tooltip));
        return btn;
    }

    public String getSelectedAlgorithm() {
        return algorithmCombo.getValue();
    }

    public int getTimeQuantum() {
        return quantumSpinner.getValue();
    }

    public List<Process> getSampleProcesses() {
        return sampleProcesses;
    }

    public VBox getView() {
        return view;
    }
}

