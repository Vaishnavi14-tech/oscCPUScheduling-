/**
 * CPU Scheduling Algorithm Engine
 * Developers: Vaishnavi Satone & Bhavishka Paunikar
 */

class CPUScheduler {
    /**
     * Run simulation and produce timeline ticks
     * @param {string} algorithm - FCFS, SJF, SRTF, RR, PRIORITY_PRE, PRIORITY_NON
     * @param {Array} processes - Array of { pid, arrivalTime, burstTime, priority }
     * @param {number} timeQuantum - For Round Robin
     * @returns {Object} { timeline, stats, processResults }
     */
    static simulate(algorithm, rawProcesses, timeQuantum = 2) {
        // Deep copy
        const processes = rawProcesses.map(p => ({
            pid: p.pid,
            arrivalTime: parseInt(p.arrivalTime, 10),
            burstTime: parseInt(p.burstTime, 10),
            remainingTime: parseInt(p.burstTime, 10),
            priority: parseInt(p.priority, 10),
            color: p.color,
            startTime: -1,
            completionTime: 0,
            waitingTime: 0,
            turnaroundTime: 0,
            responseTime: -1
        }));

        switch (algorithm) {
            case 'FCFS':
                return this.simulateFCFS(processes);
            case 'SJF':
                return this.simulateSJF(processes);
            case 'SRTF':
                return this.simulateSRTF(processes);
            case 'RR':
                return this.simulateRR(processes, timeQuantum);
            case 'PRIORITY_PRE':
                return this.simulatePriorityPreemptive(processes);
            case 'PRIORITY_NON':
                return this.simulatePriorityNonPreemptive(processes);
            default:
                return this.simulateFCFS(processes);
        }
    }

    /**
     * First Come First Served
     */
    static simulateFCFS(processes) {
        const timeline = [];
        let currentTime = 0;
        const total = processes.length;
        let completed = 0;
        const readyQueue = [];
        const arrived = new Set();

        const maxTime = 1000; // safeguard

        while (completed < total && currentTime < maxTime) {
            // Check arrivals
            processes.forEach(p => {
                if (p.arrivalTime <= currentTime && !arrived.has(p.pid)) {
                    readyQueue.push(p);
                    arrived.add(p.pid);
                }
            });

            if (readyQueue.length === 0) {
                // CPU Idle
                timeline.push({
                    time: currentTime,
                    running: null,
                    readyQueue: [],
                    completedQueue: processes.filter(p => p.remainingTime === 0),
                    event: 'CPU is idle waiting for process arrival.'
                });
                currentTime++;
                continue;
            }

            const current = readyQueue[0];
            if (current.startTime === -1) {
                current.startTime = currentTime;
                current.responseTime = currentTime - current.arrivalTime;
            }

            timeline.push({
                time: currentTime,
                running: { ...current },
                readyQueue: readyQueue.slice(1).map(p => ({ ...p })),
                completedQueue: processes.filter(p => p.remainingTime === 0),
                event: `Executing ${current.pid} (Remaining: ${current.remainingTime}ms)`
            });

            current.remainingTime--;
            currentTime++;

            if (current.remainingTime === 0) {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                readyQueue.shift();
                completed++;
            }
        }

        return this.compileResults(processes, timeline, currentTime);
    }

    /**
     * Shortest Job First (Non-Preemptive)
     */
    static simulateSJF(processes) {
        const timeline = [];
        let currentTime = 0;
        const total = processes.length;
        let completed = 0;
        const readyQueue = [];
        const arrived = new Set();
        let currentProcess = null;

        while (completed < total && currentTime < 1000) {
            // Check arrivals
            processes.forEach(p => {
                if (p.arrivalTime <= currentTime && !arrived.has(p.pid)) {
                    readyQueue.push(p);
                    arrived.add(p.pid);
                }
            });

            if (!currentProcess) {
                if (readyQueue.length > 0) {
                    // Pick smallest burst
                    readyQueue.sort((a, b) => a.burstTime - b.burstTime || a.arrivalTime - b.arrivalTime);
                    currentProcess = readyQueue.shift();
                    if (currentProcess.startTime === -1) {
                        currentProcess.startTime = currentTime;
                        currentProcess.responseTime = currentTime - currentProcess.arrivalTime;
                    }
                }
            }

            if (!currentProcess) {
                timeline.push({
                    time: currentTime,
                    running: null,
                    readyQueue: readyQueue.map(p => ({ ...p })),
                    completedQueue: processes.filter(p => p.remainingTime === 0),
                    event: 'CPU is idle.'
                });
                currentTime++;
                continue;
            }

            timeline.push({
                time: currentTime,
                running: { ...currentProcess },
                readyQueue: readyQueue.map(p => ({ ...p })),
                completedQueue: processes.filter(p => p.remainingTime === 0),
                event: `Executing ${currentProcess.pid} (Burst: ${currentProcess.burstTime}ms)`
            });

            currentProcess.remainingTime--;
            currentTime++;

            if (currentProcess.remainingTime === 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                currentProcess = null;
                completed++;
            }
        }

        return this.compileResults(processes, timeline, currentTime);
    }

    /**
     * Shortest Remaining Time First (Preemptive SJF)
     */
    static simulateSRTF(processes) {
        const timeline = [];
        let currentTime = 0;
        const total = processes.length;
        let completed = 0;
        const readyQueue = [];
        const arrived = new Set();

        while (completed < total && currentTime < 1000) {
            processes.forEach(p => {
                if (p.arrivalTime <= currentTime && !arrived.has(p.pid)) {
                    readyQueue.push(p);
                    arrived.add(p.pid);
                }
            });

            if (readyQueue.length === 0) {
                timeline.push({
                    time: currentTime,
                    running: null,
                    readyQueue: [],
                    completedQueue: processes.filter(p => p.remainingTime === 0),
                    event: 'CPU is idle.'
                });
                currentTime++;
                continue;
            }

            // Pick process with shortest remaining time
            readyQueue.sort((a, b) => a.remainingTime - b.remainingTime || a.arrivalTime - b.arrivalTime);
            const current = readyQueue[0];

            if (current.startTime === -1) {
                current.startTime = currentTime;
                current.responseTime = currentTime - current.arrivalTime;
            }

            timeline.push({
                time: currentTime,
                running: { ...current },
                readyQueue: readyQueue.slice(1).map(p => ({ ...p })),
                completedQueue: processes.filter(p => p.remainingTime === 0),
                event: `Executing ${current.pid} (Remaining: ${current.remainingTime}ms)`
            });

            current.remainingTime--;
            currentTime++;

            if (current.remainingTime === 0) {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                readyQueue.shift();
                completed++;
            }
        }

        return this.compileResults(processes, timeline, currentTime);
    }

    /**
     * Round Robin
     */
    static simulateRR(processes, timeQuantum) {
        const timeline = [];
        let currentTime = 0;
        const total = processes.length;
        let completed = 0;
        const readyQueue = [];
        const arrived = new Set();
        let currentProcess = null;
        let currentQuantum = 0;

        while (completed < total && currentTime < 1000) {
            // Check new arrivals
            processes.forEach(p => {
                if (p.arrivalTime <= currentTime && !arrived.has(p.pid)) {
                    readyQueue.push(p);
                    arrived.add(p.pid);
                }
            });

            // If time slice expired on current process and it still has time
            if (currentProcess && currentQuantum >= timeQuantum) {
                if (currentProcess.remainingTime > 0) {
                    readyQueue.push(currentProcess);
                }
                currentProcess = null;
                currentQuantum = 0;
            }

            if (!currentProcess && readyQueue.length > 0) {
                currentProcess = readyQueue.shift();
                currentQuantum = 0;
                if (currentProcess.startTime === -1) {
                    currentProcess.startTime = currentTime;
                    currentProcess.responseTime = currentTime - currentProcess.arrivalTime;
                }
            }

            if (!currentProcess) {
                timeline.push({
                    time: currentTime,
                    running: null,
                    readyQueue: readyQueue.map(p => ({ ...p })),
                    completedQueue: processes.filter(p => p.remainingTime === 0),
                    event: 'CPU is idle waiting for quantum.'
                });
                currentTime++;
                continue;
            }

            timeline.push({
                time: currentTime,
                running: { ...currentProcess },
                readyQueue: readyQueue.map(p => ({ ...p })),
                completedQueue: processes.filter(p => p.remainingTime === 0),
                event: `Executing ${currentProcess.pid} [Quantum ${currentQuantum + 1}/${timeQuantum}] (Remaining: ${currentProcess.remainingTime}ms)`
            });

            currentProcess.remainingTime--;
            currentQuantum++;
            currentTime++;

            if (currentProcess.remainingTime === 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                currentProcess = null;
                currentQuantum = 0;
                completed++;
            }
        }

        return this.compileResults(processes, timeline, currentTime);
    }

    /**
     * Priority Preemptive
     */
    static simulatePriorityPreemptive(processes) {
        const timeline = [];
        let currentTime = 0;
        const total = processes.length;
        let completed = 0;
        const readyQueue = [];
        const arrived = new Set();

        while (completed < total && currentTime < 1000) {
            processes.forEach(p => {
                if (p.arrivalTime <= currentTime && !arrived.has(p.pid)) {
                    readyQueue.push(p);
                    arrived.add(p.pid);
                }
            });

            if (readyQueue.length === 0) {
                timeline.push({
                    time: currentTime,
                    running: null,
                    readyQueue: [],
                    completedQueue: processes.filter(p => p.remainingTime === 0),
                    event: 'CPU is idle.'
                });
                currentTime++;
                continue;
            }

            // Lower priority number = Higher priority
            readyQueue.sort((a, b) => a.priority - b.priority || a.arrivalTime - b.arrivalTime);
            const current = readyQueue[0];

            if (current.startTime === -1) {
                current.startTime = currentTime;
                current.responseTime = currentTime - current.arrivalTime;
            }

            timeline.push({
                time: currentTime,
                running: { ...current },
                readyQueue: readyQueue.slice(1).map(p => ({ ...p })),
                completedQueue: processes.filter(p => p.remainingTime === 0),
                event: `Executing ${current.pid} (Priority: ${current.priority}, Rem: ${current.remainingTime}ms)`
            });

            current.remainingTime--;
            currentTime++;

            if (current.remainingTime === 0) {
                current.completionTime = currentTime;
                current.turnaroundTime = current.completionTime - current.arrivalTime;
                current.waitingTime = current.turnaroundTime - current.burstTime;
                readyQueue.shift();
                completed++;
            }
        }

        return this.compileResults(processes, timeline, currentTime);
    }

    /**
     * Priority Non-Preemptive
     */
    static simulatePriorityNonPreemptive(processes) {
        const timeline = [];
        let currentTime = 0;
        const total = processes.length;
        let completed = 0;
        const readyQueue = [];
        const arrived = new Set();
        let currentProcess = null;

        while (completed < total && currentTime < 1000) {
            processes.forEach(p => {
                if (p.arrivalTime <= currentTime && !arrived.has(p.pid)) {
                    readyQueue.push(p);
                    arrived.add(p.pid);
                }
            });

            if (!currentProcess && readyQueue.length > 0) {
                readyQueue.sort((a, b) => a.priority - b.priority || a.arrivalTime - b.arrivalTime);
                currentProcess = readyQueue.shift();
                if (currentProcess.startTime === -1) {
                    currentProcess.startTime = currentTime;
                    currentProcess.responseTime = currentTime - currentProcess.arrivalTime;
                }
            }

            if (!currentProcess) {
                timeline.push({
                    time: currentTime,
                    running: null,
                    readyQueue: readyQueue.map(p => ({ ...p })),
                    completedQueue: processes.filter(p => p.remainingTime === 0),
                    event: 'CPU is idle.'
                });
                currentTime++;
                continue;
            }

            timeline.push({
                time: currentTime,
                running: { ...currentProcess },
                readyQueue: readyQueue.map(p => ({ ...p })),
                completedQueue: processes.filter(p => p.remainingTime === 0),
                event: `Executing ${currentProcess.pid} (Priority: ${currentProcess.priority})`
            });

            currentProcess.remainingTime--;
            currentTime++;

            if (currentProcess.remainingTime === 0) {
                currentProcess.completionTime = currentTime;
                currentProcess.turnaroundTime = currentProcess.completionTime - currentProcess.arrivalTime;
                currentProcess.waitingTime = currentProcess.turnaroundTime - currentProcess.burstTime;
                currentProcess = null;
                completed++;
            }
        }

        return this.compileResults(processes, timeline, currentTime);
    }

    /**
     * Compute statistics and package results
     */
    static compileResults(processes, timeline, totalTime) {
        const n = processes.length;
        const totalWT = processes.reduce((acc, p) => acc + p.waitingTime, 0);
        const totalTAT = processes.reduce((acc, p) => acc + p.turnaroundTime, 0);
        const totalRT = processes.reduce((acc, p) => acc + p.responseTime, 0);
        const busyTicks = timeline.filter(t => t.running !== null).length;

        const avgWT = (totalWT / n).toFixed(2);
        const avgTAT = (totalTAT / n).toFixed(2);
        const avgRT = (totalRT / n).toFixed(2);
        const cpuUtil = totalTime > 0 ? ((busyTicks / totalTime) * 100).toFixed(1) : 0;
        const throughput = totalTime > 0 ? (n / totalTime).toFixed(3) : 0;

        return {
            timeline,
            totalTime,
            stats: {
                avgWT,
                avgTAT,
                avgRT,
                cpuUtil,
                throughput
            },
            processResults: processes.map(p => ({
                pid: p.pid,
                arrivalTime: p.arrivalTime,
                burstTime: p.burstTime,
                priority: p.priority,
                completionTime: p.completionTime,
                turnaroundTime: p.turnaroundTime,
                waitingTime: p.waitingTime,
                responseTime: p.responseTime,
                color: p.color
            }))
        };
    }
}

window.CPUScheduler = CPUScheduler;
