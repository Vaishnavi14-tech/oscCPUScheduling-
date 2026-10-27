package com.cpuscheduler.model;

import javafx.scene.paint.Color;

/**
 * Represents a process in the CPU scheduler
 */
public class Process {
    private String id;
    private int arrivalTime;
    private int burstTime;
    private int priority;
    private int remainingTime;
    private int completionTime;
    private int waitingTime;
    private int turnaroundTime;
    private int responseTime;
    private Color color;
    private ProcessState state;
    private int firstScheduledTime = -1;

    public enum ProcessState {
        NOT_ARRIVED, READY, RUNNING, COMPLETED
    }

    public Process(String id, int arrivalTime, int burstTime, int priority) {
        this.id = id;
        this.arrivalTime = arrivalTime;
        this.burstTime = burstTime;
        this.priority = priority;
        this.remainingTime = burstTime;
        this.state = ProcessState.NOT_ARRIVED;
        this.color = generateColor();
    }

    private Color generateColor() {
        int index = Integer.parseInt(id.substring(1)) % 4;
        return switch(index) {
            case 0 -> Color.web("#3B82F6"); // Blue
            case 1 -> Color.web("#10B981"); // Green
            case 2 -> Color.web("#F59E0B"); // Amber
            default -> Color.web("#EF4444"); // Red
        };
    }

    // Getters and Setters
    public String getId() { return id; }
    public int getArrivalTime() { return arrivalTime; }
    public int getBurstTime() { return burstTime; }
    public int getPriority() { return priority; }
    public int getRemainingTime() { return remainingTime; }
    public void setRemainingTime(int time) { this.remainingTime = time; }
    public int getCompletionTime() { return completionTime; }
    public void setCompletionTime(int time) { this.completionTime = time; }
    public int getWaitingTime() { return waitingTime; }
    public void setWaitingTime(int time) { this.waitingTime = time; }
    public int getTurnaroundTime() { return turnaroundTime; }
    public void setTurnaroundTime(int time) { this.turnaroundTime = time; }
    public int getResponseTime() { return responseTime; }
    public void setResponseTime(int time) { this.responseTime = time; }
    public Color getColor() { return color; }
    public ProcessState getState() { return state; }
    public void setState(ProcessState state) { this.state = state; }
    public int getFirstScheduledTime() { return firstScheduledTime; }
    public void setFirstScheduledTime(int time) {
        if (this.firstScheduledTime == -1) {
            this.firstScheduledTime = time;
        }
    }
}
