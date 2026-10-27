package com.cpuscheduler.scheduler;

import com.cpuscheduler.model.Process;
import com.cpuscheduler.model.SchedulerAlgorithm;
import java.util.*;

/**
 * Shortest Job First (SJF) Scheduling Algorithm
 */
public class SJFScheduler extends SchedulerAlgorithm {
    public SJFScheduler(List<Process> processes) {
        super(processes, 0);
    }

    @Override
    public void schedule() {
        List<Process> queue = new ArrayList<>();
        List<Process> completed = new ArrayList<>();
        List<String> readyQueue = new ArrayList<>();
        List<String> completedQueue = new ArrayList<>();
        int time = 0;
        Set<Integer> arrived = new HashSet<>();

        while (completed.size() < processes.size()) {
            // Add arrived processes
            for (Process p : processes) {
                int key = p.getArrivalTime() * 1000 + processes.indexOf(p);
                if (p.getArrivalTime() <= time && !arrived.contains(key)) {
                    queue.add(p);
                    arrived.add(key);
                    readyQueue.add(p.getId());
                    events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_ARRIVED,
                            p.getId(), p.getId() + " arrived", new ArrayList<>(readyQueue), new ArrayList<>(completedQueue)));
                }
            }

            if (queue.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : processes) {
                    int key = p.getArrivalTime() * 1000 + processes.indexOf(p);
                    if (!arrived.contains(key) && p.getArrivalTime() > time) {
                        nextArrival = Math.min(nextArrival, p.getArrivalTime());
                    }
                }
                if (nextArrival != Integer.MAX_VALUE) {
                    time = nextArrival;
                    events.add(new ScheduleEvent(time, ScheduleEvent.EventType.CPU_IDLE,
                            "IDLE", "CPU idle until time " + time, readyQueue, completedQueue));
                }
                continue;
            }

            // Find shortest job
            Process current = queue.stream()
                    .min(Comparator.comparingInt(Process::getBurstTime))
                    .orElse(queue.get(0));
            queue.remove(current);
            readyQueue.remove(current.getId());

            current.setState(Process.ProcessState.RUNNING);
            current.setFirstScheduledTime(time);
            events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_STARTED,
                    current.getId(), current.getId() + " (burst: " + current.getBurstTime() + ") started - Shortest Job", readyQueue, completedQueue));

            time += current.getBurstTime();
            current.setCompletionTime(time);
            current.setState(Process.ProcessState.COMPLETED);
            completed.add(current);
            completedQueue.add(current.getId());
            events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_COMPLETED,
                    current.getId(), current.getId() + " completed", readyQueue, completedQueue));
        }

        calculateMetrics();
    }
}
