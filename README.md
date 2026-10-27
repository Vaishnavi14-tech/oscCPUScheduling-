# CPU Scheduler Simulator - JavaFX

Modern and interactive CPU scheduling simulator built with JavaFX featuring:

## Features

✅ **Four Scheduling Algorithms**
- First Come First Served (FCFS)
- Shortest Job First (SJF)
- Round Robin (RR)
- Priority Preemptive

✅ **Live Queue Animation**
- Ready Queue visualization
- CPU execution display
- Waiting Queue (I/O operations)
- Completed Queue

✅ **Real-time Gantt Chart**
- Timeline visualization
- Process execution blocks
- Live time updates

✅ **Statistics Panel**
- Average Waiting Time
- Average Turnaround Time
- Average Response Time
- CPU Utilization
- Throughput

✅ **Event Log**
- Step-by-step event tracking
- Real-time descriptions
- Queue status updates

✅ **Playback Controls**
- Start/Pause/Resume
- Reset simulation
- Speed control (1x, 2x, 5x)

✅ **Visual Effects**
- Smooth animations (500-1000ms)
- Color-coded processes
- Hover tooltips
- Modern gradient UI

## Project Structure

```
CPUSchedulerJavaFX/
├── pom.xml                          # Maven configuration
├── src/
│   ├── main/
│   │   ├── java/com/cpuscheduler/
│   │   │   ├── CPUSchedulerApp.java          # Main application
│   │   │   ├── model/
│   │   │   │   ├── Process.java              # Process model
│   │   │   │   └── SchedulerAlgorithm.java   # Base algorithm
│   │   │   ├── scheduler/
│   │   │   │   ├── FCFSScheduler.java        # FCFS implementation
│   │   │   │   ├── SJFScheduler.java         # SJF implementation
│   │   │   │   ├── RoundRobinScheduler.java  # RR implementation
│   │   │   │   └── PriorityScheduler.java    # Priority implementation
│   │   │   └── ui/
│   │   │       ├── MainViewController.java   # Main view controller
│   │   │       ├── SchedulerEngine.java      # Animation engine
│   │   │       └── components/
│   │   │           ├── ControlPanel.java     # Control buttons
│   │   │           ├── QueuePanel.java       # Queue visualization
│   │   │           ├── GanttChartPanel.java  # Timeline chart
│   │   │           ├── StatisticsPanel.java  # Metrics display
│   │   │           └── EventLogPanel.java    # Event tracker
│   │   └── resources/
│   │       └── styles.css            # Application styling
│   └── test/                         # Unit tests

```

## Requirements

- **Java 17+**
- **JavaFX 22.0.1**
- **Maven 3.8+**

## Setup & Running

### 1. Open in IDE
Open this folder in IntelliJ IDEA, Eclipse, or VS Code with Java extension

### 2. Install Dependencies (Maven)
```bash
cd CPUSchedulerJavaFX
mvn clean install
```

### 3. Run the Application
```bash
mvn javafx:run
```

Or directly from IDE:
- Right-click `CPUSchedulerApp.java`
- Select "Run"

## How to Use

1. **Select Algorithm**: Choose from FCFS, SJF, Round Robin, or Priority
2. **Set Time Quantum**: For Round Robin (default: 2ms)
3. **Click Start**: Begin the simulation
4. **Watch Animation**: See processes move between queues
5. **View Results**: Check statistics and event log
6. **Control Playback**: Pause, resume, or reset anytime

## Animation Features

- 🎬 Smooth 500-1000ms transitions
- 🎨 Color-coded process visualization
- 📊 Live Gantt chart updates
- 📝 Real-time event descriptions
- ⏱️ Speed control (1x, 2x, 5x)

## Example Scenario

```
Process Set:
P1: Arrival = 0, Burst = 7, Priority = 2
P2: Arrival = 1, Burst = 4, Priority = 1
P3: Arrival = 3, Burst = 2, Priority = 3
P4: Arrival = 5, Burst = 5, Priority = 2
```

**FCFS Result**: Processes execute in arrival order
**SJF Result**: Shortest processes run first
**RR (TQ=2)**: Each process gets 2ms time slices
**Priority**: Highest priority (lowest number) runs first

## Educational Features

Each step explains:
- "P2 moved from Ready Queue to CPU because it has the shortest burst time."
- "P1 returned to Ready Queue because Time Quantum expired."
- "P3 completed execution and moved to Completed Queue."

## Credits

**Developers**: VAISHNAVI SATONE & BHAVISHKA PAUNIKAR

## License

MIT License - Open source educational project
