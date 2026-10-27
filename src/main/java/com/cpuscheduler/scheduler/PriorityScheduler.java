package com.cpuscheduler.scheduler;

import com.cpuscheduler.model.Process;
import com.cpuscheduler.model.SchedulerAlgorithm;
import java.util.*;

/**
 * Priority Preemptive Scheduling Algorithm
 */
public class PriorityScheduler extends SchedulerAlgorithm {
    public PriorityScheduler(List<Process> processes) {
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
        Process running = null;

        while (completed.size() < processes.size()) {
            // Add arrived processes
            for (Process p : processes) {
                int key = p.getArrivalTime() * 1000 + processes.indexOf(p);
                if (p.getArrivalTime() <= time && !arrived.contains(key) && p.getRemainingTime() > 0) {
                    queue.add(p);
                    arrived.add(key);
                    readyQueue.add(p.getId());
                    if (running != null && p.getPriority() < running.getPriority()) {
                        running.setState(Process.ProcessState.READY);
                        readyQueue.add(running.getId());
                        events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_PREEMPTED,
                                running.getId(), running.getId() + " preempted by higher priority " + p.getId(), readyQueue, completedQueue));
                        running = null;
                    }
                    events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_ARRIVED,
                            p.getId(), p.getId() + " (priority: " + p.getPriority() + ") arrived", readyQueue, completedQueue));
                }
            }

            if (running == null || running.getRemainingTime() == 0) {
                if (running != null && running.getRemainingTime() == 0) {
                    queue.remove(running);
                    readyQueue.remove(running.getId());
                    running.setCompletionTime(time);
                    running.setState(Process.ProcessState.COMPLETED);
                    completed.add(running);
                    completedQueue.add(running.getId());
                    events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_COMPLETED,
                            running.getId(), running.getId() + " completed", readyQueue, completedQueue));
                }

                if (queue.isEmpty()) {
                    if (completed.size() < processes.size()) {
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
                    }
                    continue;
                }

                running = queue.stream()
                        .filter(p -> p.getRemainingTime() > 0)
                        .min(Comparator.comparingInt(Process::getPriority))
                        .orElse(null);

                if (running != null) {
                    queue.remove(running);
                    readyQueue.remove(running.getId());
                    if (running.getFirstScheduledTime() == -1) {
                        running.setFirstScheduledTime(time);
                    }
                    running.setState(Process.ProcessState.RUNNING);
                    events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_STARTED,
                            running.getId(), running.getId() + " (priority: " + running.getPriority() + ") started - highest priority", readyQueue, completedQueue));
                }
            }

            if (running != null) {
                time++;
                running.setRemainingTime(running.getRemainingTime() - 1);
            } else {
                break;
            }
        }

        calculateMetrics();
    }
}
