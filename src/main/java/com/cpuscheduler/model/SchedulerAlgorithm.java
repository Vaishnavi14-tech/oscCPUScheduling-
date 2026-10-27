package com.cpuscheduler.model;

import java.util.*;

/**
 * Abstract base class for scheduling algorithms
 */
public abstract class SchedulerAlgorithm {
    protected List<Process> processes;
    protected List<ScheduleEvent> events;
    protected int timeQuantum;

    public SchedulerAlgorithm(List<Process> processes, int timeQuantum) {
        this.processes = new ArrayList<>(processes);
        this.timeQuantum = timeQuantum;
        this.events = new ArrayList<>();
    }

    public abstract void schedule();

    public List<ScheduleEvent> getEvents() { return events; }

    protected void calculateMetrics() {
        for (Process p : processes) {
            p.setTurnaroundTime(p.getCompletionTime() - p.getArrivalTime());
            p.setWaitingTime(p.getTurnaroundTime() - p.getBurstTime());
            p.setResponseTime(p.getFirstScheduledTime() - p.getArrivalTime());
        }
    }

    public static class ScheduleEvent {
        public int time;
        public EventType type;
        public String processId;
        public String description;
        public List<String> readyQueue;
        public List<String> completedQueue;

        public enum EventType {
            PROCESS_ARRIVED, PROCESS_STARTED, PROCESS_PREEMPTED, PROCESS_COMPLETED, CPU_IDLE
        }

        public ScheduleEvent(int time, EventType type, String processId, String description,
                           List<String> readyQueue, List<String> completedQueue) {
            this.time = time;
            this.type = type;
            this.processId = processId;
            this.description = description;
            this.readyQueue = new ArrayList<>(readyQueue);
            this.completedQueue = new ArrayList<>(completedQueue);
        }
    }
}
