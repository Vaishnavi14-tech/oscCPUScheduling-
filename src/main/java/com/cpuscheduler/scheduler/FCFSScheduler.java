package com.cpuscheduler.scheduler;

import com.cpuscheduler.model.Process;
import com.cpuscheduler.model.SchedulerAlgorithm;
import java.util.*;

/**
 * First Come First Served (FCFS) Scheduling Algorithm
 */
public class FCFSScheduler extends SchedulerAlgorithm {
    public FCFSScheduler(List<Process> processes) {
        super(processes, 0);
    }

    @Override
    public void schedule() {
        List<Process> queue = new ArrayList<>();
        List<Process> completed = new ArrayList<>();
        List<String> readyQueue = new ArrayList<>();
        List<String> completedQueue = new ArrayList<>();
        int time = 0;
        int processIndex = 0;
        Set<Integer> arrived = new HashSet<>();

        // Sort by arrival time
        List<Process> sortedByArrival = new ArrayList<>(processes);
        sortedByArrival.sort(Comparator.comparingInt(Process::getArrivalTime));

        while (completed.size() < processes.size()) {
            // Add arrived processes
            for (Process p : sortedByArrival) {
                if (p.getArrivalTime() <= time && !arrived.contains(p.getArrivalTime() * 1000 + processes.indexOf(p))) {
                    queue.add(p);
                    arrived.add(p.getArrivalTime() * 1000 + processes.indexOf(p));
                    readyQueue.add(p.getId());
                    events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_ARRIVED,
                            p.getId(), p.getId() + " arrived", new ArrayList<>(readyQueue), new ArrayList<>(completedQueue)));
                }
            }

            // If queue is empty, fast-forward to next arrival
            if (queue.isEmpty()) {
                int nextArrival = Integer.MAX_VALUE;
                for (Process p : sortedByArrival) {
                    if (!arrived.contains(p.getArrivalTime() * 1000 + processes.indexOf(p)) && p.getArrivalTime() > time) {
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

            // Process next in queue
            Process current = queue.remove(0);
            readyQueue.remove(current.getId());
            current.setState(Process.ProcessState.RUNNING);
            current.setFirstScheduledTime(time);
            events.add(new ScheduleEvent(time, ScheduleEvent.EventType.PROCESS_STARTED,
                    current.getId(), current.getId() + " started execution", readyQueue, completedQueue));

            // Execute
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
