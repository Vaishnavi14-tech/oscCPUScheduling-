package com.cpuscheduler;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import com.cpuscheduler.ui.MainViewController;

/**
 * Main JavaFX Application for CPU Scheduler Simulator
 */
public class CPUSchedulerApp extends Application {
    @Override
    public void start(Stage primaryStage) throws Exception {
        BorderPane root = new BorderPane();
        MainViewController controller = new MainViewController();
        root.setCenter(controller.getView());

        Scene scene = new Scene(root, 1400, 900);
        scene.getStylesheets().add(getClass().getResource("/styles.css").toExternalForm());

        primaryStage.setTitle("CPU Scheduler Simulator - VAISHNAVI SATONE & BHAVISHKA PAUNIKAR");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
