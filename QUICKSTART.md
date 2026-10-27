# CPU Scheduler JavaFX - Quick Start Guide

## System Requirements
- Java JDK 17 or higher
- Maven 3.8.1 or higher
- 8GB RAM minimum

## Installation Steps

### Step 1: Install Java JDK 17+
Download from: https://www.oracle.com/java/technologies/downloads/

### Step 2: Install Maven
Download from: https://maven.apache.org/download.cgi

### Step 3: Clone or Extract Project
```bash
cd c:\Users\Vaishnavi Satone\Desktop\ReactJs\CPUSchedulerJavaFX
```

### Step 4: Build Project
```bash
mvn clean install
```

This will download all dependencies (JavaFX 22.0.1, etc.)

### Step 5: Run Application
```bash
mvn javafx:run
```

The application window will open automatically.

---

## Using the Simulator

### Step 1: Select Algorithm
- FCFS: First Come, First Served
- SJF: Shortest Job First
- Round Robin: Time-sharing with quantum
- Priority: Highest priority (lowest number) first

### Step 2: Configure Parameters
- Set Time Quantum for Round Robin (default: 2)
- Adjust speed (1x, 2x, 5x)

### Step 3: Start Simulation
Click **Start ▶** button

### Step 4: Watch Animation
- Left panel: Queue visualization
- Bottom: Gantt chart timeline
- Right panel: Statistics and events

### Step 5: Control Playback
- **Pause ⏸**: Freeze at current step
- **Resume**: Continue animation
- **Reset ⟲**: Start over

---

## Default Test Processes

The simulator includes a sample process set:
```
P1: Arrival = 0ms, Burst = 7ms, Priority = 2
P2: Arrival = 1ms, Burst = 4ms, Priority = 1
P3: Arrival = 3ms, Burst = 2ms, Priority = 3
P4: Arrival = 5ms, Burst = 5ms, Priority = 2
```

### Expected Results:

**FCFS**: 
- Execution: P1 → P2 → P3 → P4
- Avg WT ≈ 5.75ms

**SJF**: 
- Execution: P1 → P3 → P2 → P4
- Avg WT ≈ 4.0ms

**Round Robin (TQ=2)**:
- Each process gets 2ms time slice
- Avg WT ≈ 5.0ms

**Priority**: 
- Execution: P1 → P2 → P4 → P3
- Avg WT ≈ 4.75ms

---

## Troubleshooting

### "Maven is not recognized"
Add Maven to PATH environment variable

### "javafx: run not found"
Run: `mvn clean install` first to download dependencies

### "Cannot find Java"
Install JDK 17+ and add to PATH

### "Out of Memory"
Increase Java heap: `export MAVEN_OPTS=-Xmx2048m`

---

## Development

### Project Structure
```
CPUSchedulerJavaFX/
├── pom.xml                    # Maven config
├── README.md                  # Documentation
├── QUICKSTART.md             # This file
├── src/
│   ├── main/java/com/cpuscheduler/
│   │   ├── CPUSchedulerApp.java      # Entry point
│   │   ├── model/              # Data models
│   │   ├── scheduler/          # Algorithm implementations
│   │   └── ui/                 # JavaFX UI components
│   └── main/resources/
│       └── styles.css         # Application styling
└── target/                     # Build output
```

### To Modify Code
1. Open project in IntelliJ IDEA or Eclipse
2. Edit Java files in `src/main/java/com/cpuscheduler/`
3. Rebuild: `mvn clean install`
4. Run: `mvn javafx:run`

### To Add Custom Processes
Edit `CPUSchedulerApp.java` main method to load custom process data.

---

## Credits
**Developers**: VAISHNAVI SATONE & BHAVISHKA PAUNIKAR

**Framework**: JavaFX 22.0.1
**Build Tool**: Maven 3.8+
**Language**: Java 17+

---

**Enjoy exploring CPU scheduling algorithms! 🚀**
