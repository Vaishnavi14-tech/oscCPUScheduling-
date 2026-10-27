package com.cpuscheduler.scheduler;

import com.cpuscheduler.model.Process;
import com.cpuscheduler.model.SchedulerAlgorithm;
import java.util.*;

/**
 * Round Robin (RR) Scheduling Algorithm
 */
public class RoundRobinScheduler extends SchedulerAlgorithm {
    public RoundRobinScheduler(List<Process> processes, int timeQuantum) {
        super(processes, timeQuantum);
    }

    @Override
    public void schedule() {
        Queue<Process> queue = new LinkedList<>();
        List<Process> completed = new ArrayList<>();
        List<String> readyQueue = new ArrayList<>();
        List<String> completedQueue = new ArrayList<>();
        int time = 0;
        Set<Integer> arrived = new HashSet<>();

        while (completed.size() < processes.size()) {
            // Add arrived processes
            for (Process p : processes) {
                int key = p.getArrivalTime() * 1000 + processes.indexOf(p);
                if (p.getArrivalTime() <= time && !arrived.contains(key) && p.getRemainingTime() > 0) {
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

            Process current = queue.poll();
            readyQueue.remove(current.getId());

            if (current.getFirstScheduledTime() == -1) {
                current.setFirstScheduledTime(time);
            }

            int timeSlice = Math.min(timeQuantum, current.getRemainingTime());
            current.setState(Process.ProcessState.RUNNING);
            events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_STARTED,
                    current.getId(), current.getId() + " executing for " + timeSlice + " ms", readyQueue, completedQueue));

            time += timeSlice;
            current.setRemainingTime(current.getRemainingTime() - timeSlice);

            if (current.getRemainingTime() == 0) {
                current.setCompletionTime(time);
                current.setState(Process.ProcessState.COMPLETED);
                completed.add(current);
                completedQueue.add(current.getId());
                events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_COMPLETED,
                        current.getId(), current.getId() + " completed", readyQueue, completedQueue));
            } else {
                queue.add(current);
                readyQueue.add(current.getId());
                events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_PREEMPTED,
                        current.getId(), current.getId() + " time quantum expired, moved back to ready queue", readyQueue, completedQueue));
            }
        }

        calculateMetrics();
    }
}
