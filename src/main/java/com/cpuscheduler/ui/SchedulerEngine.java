package com.cpuscheduler.ui;

import javafx.animation.Timeline;
import javafx.animation.KeyFrame;
import javafx.animation.PauseTransition;
import javafx.util.Duration;
import javafx.application.Platform;
import com.cpuscheduler.model.Process;
import com.cpuscheduler.model.SchedulerAlgorithm;
import com.cpuscheduler.scheduler.*;
import com.cpuscheduler.ui.components.*;
import java.util.List;
import java.util.HashMap;
import java.util.Map;

/**
 * Engine for running the scheduler simulation with advanced animations
 */
public class SchedulerEngine {
    private QueuePanel queuePanel;
    private GanttChartPanel ganttPanel;
    private StatisticsPanel statsPanel;
    private EventLogPanel eventLogPanel;
    private SchedulerAlgorithm currentScheduler;
    private Timeline animationTimeline;
    private double animationSpeed = 1.0;
    private boolean isPaused = false;
    private List<Process> currentProcesses;
    private Map<String, String> processColors;

    public SchedulerEngine(QueuePanel queuePanel, GanttChartPanel ganttPanel,
                          StatisticsPanel statsPanel, EventLogPanel eventLogPanel) {
        this.queuePanel = queuePanel;
        this.ganttPanel = ganttPanel;
        this.statsPanel = statsPanel;
        this.eventLogPanel = eventLogPanel;
        this.processColors = new HashMap<>();
    }

    public void startSimulation(String algorithm, List<Process> processes, int timeQuantum) {
        queuePanel.clearQueues();
        ganttPanel.clearGantt();
        eventLogPanel.clear();
        statsPanel.reset();

        this.currentProcesses = processes;
        
        // Initialize process colors
        for (int i = 0; i < processes.size(); i++) {
            String color = switch(i % 4) {
                case 0 -> "#3498db";  // Blue
                case 1 -> "#27ae60";  // Green
                case 2 -> "#f39c12";  // Orange
                default -> "#e74c3c"; // Red
            };
            processColors.put(processes.get(i).getId(), color);
        }

        // Create scheduler based on algorithm
        switch (algorithm) {
            case "FCFS":
                currentScheduler = new FCFSScheduler(processes);
                break;
            case "SJF":
                currentScheduler = new SJFScheduler(processes);
                break;
            case "Round Robin":
                currentScheduler = new RoundRobinScheduler(processes, timeQuantum);
                break;
            case "Priority":
                currentScheduler = new PriorityScheduler(processes);
                break;
            default:
                currentScheduler = new FCFSScheduler(processes);
        }

        currentScheduler.schedule();
        var events = currentScheduler.getEvents();

        // Create animation timeline
        if (animationTimeline != null) {
            animationTimeline.stop();
        }

        animationTimeline = new Timeline();
        animationTimeline.setCycleCount(1);

        double delayMs = 0;
        for (var event : events) {
            final int time = event.time;
            final String eventDesc = event.description;
            final String processId = event.processId;
            final SchedulerAlgorithm.ScheduleEvent.EventType eventType = event.type;
            final List<String> readyQueue = event.readyQueue;
            final List<String> completedQueue = event.completedQueue;

            KeyFrame frame = new KeyFrame(
                    Duration.millis(delayMs),
                    evt -> {
                        Platform.runLater(() -> displayEvent(time, eventType, processId, eventDesc, readyQueue, completedQueue));
                    }
            );
            animationTimeline.getKeyFrames().add(frame);
            
            // Increment delay based on animation speed
            delayMs += (800 / animationSpeed); // 800ms base delay per event
        }

        animationTimeline.play();
    }

    private void displayEvent(int time, SchedulerAlgorithm.ScheduleEvent.EventType eventType,
                             String processId, String description, List<String> readyQueue,
                             List<String> completedQueue) {
        ganttPanel.updateTime(time);
        eventLogPanel.addEvent(String.format("%02d", time) + "ms", description);

        Process process = currentProcesses.stream()
                .filter(p -> p.getId().equals(processId))
                .findFirst()
                .orElse(null);

        if (process == null && !processId.equals("IDLE")) return;

        String color = processColors.getOrDefault(processId, "#3498db");

        // Update queue visualization based on event type
        switch (eventType) {
            case PROCESS_ARRIVED:
                if (process != null) {
                    queuePanel.addProcessToReady(processId, process.getArrivalTime(),
                            process.getBurstTime(), process.getRemainingTime(), color);
                }
                break;

            case PROCESS_STARTED:
                if (process != null) {
                    queuePanel.removeProcessFromReady(processId);
                    double progress = (double) (process.getBurstTime() - process.getRemainingTime()) / process.getBurstTime();
                    queuePanel.addProcessToCPU(processId, process.getArrivalTime(),
                            process.getBurstTime(), process.getRemainingTime(), color, progress);
                }
                break;

            case PROCESS_COMPLETED:
                if (process != null) {
                    queuePanel.addProcessToCompleted(processId, process.getArrivalTime(),
                            process.getBurstTime(), 0, color);
                    
                    // Update statistics
                    calculateAndDisplayStatistics();
                }
                break;

            case PROCESS_PREEMPTED:
                if (process != null) {
                    queuePanel.addProcessToReady(processId, process.getArrivalTime(),
                            process.getBurstTime(), process.getRemainingTime(), color);
                }
                break;

            case CPU_IDLE:
                // Show idle state in gantt chart
                break;
        }
    }

    private void calculateAndDisplayStatistics() {
        if (currentProcesses == null || currentProcesses.isEmpty()) return;

        double totalWT = 0, totalTAT = 0, totalRT = 0;
        int completedCount = 0;

        for (Process p : currentProcesses) {
            if (p.getState() == Process.ProcessState.COMPLETED) {
                totalWT += p.getWaitingTime();
                totalTAT += p.getTurnaroundTime();
                totalRT += p.getResponseTime();
                completedCount++;
            }
        }

        if (completedCount > 0) {
            double avgWT = totalWT / completedCount;
            double avgTAT = totalTAT / completedCount;
            double avgRT = totalRT / completedCount;
            double utilization = 100.0;
            double throughput = (double) completedCount / (currentProcesses.size() * 10);

            statsPanel.updateStatistics(avgWT, avgTAT, avgRT, utilization, throughput);
        }
    }

    public void setPaused(boolean paused) {
        this.isPaused = paused;
        if (animationTimeline != null) {
            if (paused) {
                animationTimeline.pause();
            } else {
                animationTimeline.play();
            }
        }
    }

    public void reset() {
        if (animationTimeline != null) {
            animationTimeline.stop();
        }
        queuePanel.clearQueues();
        ganttPanel.clearGantt();
        eventLogPanel.clear();
        statsPanel.reset();
    }

    public void setAnimationSpeed(double speed) {
        this.animationSpeed = speed;
        if (animationTimeline != null && animationTimeline.getStatus().equals(javafx.animation.Animation.Status.RUNNING)) {
            animationTimeline.setRate(speed);
        }
    }
}

