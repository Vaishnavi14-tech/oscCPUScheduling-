/**
 * CPU Scheduler UI & Animation Engine
 * Developers: Vaishnavi Satone & Bhavishka Paunikar
 */

// Global App State
const state = {
    processes: [
        { pid: 'P1', arrivalTime: 0, burstTime: 7, priority: 2, color: 'var(--color-p1)' },
        { pid: 'P2', arrivalTime: 1, burstTime: 4, priority: 1, color: 'var(--color-p2)' },
        { pid: 'P3', arrivalTime: 3, burstTime: 2, priority: 3, color: 'var(--color-p3)' },
        { pid: 'P4', arrivalTime: 5, burstTime: 5, priority: 2, color: 'var(--color-p4)' }
    ],
    colorPalette: [
        '#38bdf8', '#a855f7', '#f97316', '#10b981', 
        '#ec4899', '#eab308', '#6366f1', '#14b8a6'
    ],
    isRunning: false,
    isPaused: false,
    currentTick: 0,
    speedMultiplier: 1.0,
    baseTickMs: 600, // 600ms per tick at 1x
    timerId: null,
    simulationData: null,
    soundEnabled: true
};

// Web Audio API Synth for subtle feedback
const audioCtx = new (window.AudioContext || window.webkitAudioContext)();
function playTone(freq, type = 'sine', duration = 0.08) {
    if (!state.soundEnabled) return;
    try {
        if (audioCtx.state === 'suspended') audioCtx.resume();
        const osc = audioCtx.createOscillator();
        const gain = audioCtx.createGain();
        osc.type = type;
        osc.frequency.setValueAtTime(freq, audioCtx.currentTime);
        gain.gain.setValueAtTime(0.08, audioCtx.currentTime);
        gain.gain.exponentialRampToValueAtTime(0.001, audioCtx.currentTime + duration);
        osc.connect(gain);
        gain.connect(audioCtx.destination);
        osc.start();
        osc.stop(audioCtx.currentTime + duration);
    } catch (e) {
        // silent fail
    }
}

// DOM Elements
const DOM = {
    algorithmSelect: document.getElementById('algorithmSelect'),
    quantumGroup: document.getElementById('quantumGroup'),
    quantumInput: document.getElementById('quantumInput'),
    quantumPlus: document.getElementById('quantumPlus'),
    quantumMinus: document.getElementById('quantumMinus'),
    speedPills: document.querySelectorAll('.speed-pill'),
    startBtn: document.getElementById('startBtn'),
    startBtnText: document.getElementById('startBtnText'),
    stepBtn: document.getElementById('stepBtn'),
    resetBtn: document.getElementById('resetBtn'),
    statusIndicator: document.getElementById('statusIndicator'),
    statusText: document.getElementById('statusText'),
    simClock: document.getElementById('simClock'),
    processTableBody: document.getElementById('processTableBody'),
    addProcessBtn: document.getElementById('addProcessBtn'),
    loadSampleBtn: document.getElementById('loadSampleBtn'),
    randomizeBtn: document.getElementById('randomizeBtn'),
    readyQueueTrack: document.getElementById('readyQueueTrack'),
    readyCount: document.getElementById('readyCount'),
    cpuStage: document.getElementById('cpuStage'),
    cpuSlot: document.querySelector('.cpu-core-slot'),
    cpuStateText: document.getElementById('cpuStateText'),
    waitingQueueTrack: document.getElementById('waitingQueueTrack'),
    waitingCount: document.getElementById('waitingCount'),
    completedQueueTrack: document.getElementById('completedQueueTrack'),
    completedCount: document.getElementById('completedCount'),
    ganttBlocks: document.getElementById('ganttBlocks'),
    ganttTicks: document.getElementById('ganttTicks'),
    ganttCanvas: document.getElementById('ganttCanvas'),
    currentTimeMarker: document.getElementById('currentTimeMarker'),
    ganttLegend: document.getElementById('ganttLegend'),
    metricAvgWT: document.getElementById('metricAvgWT'),
    metricAvgTAT: document.getElementById('metricAvgTAT'),
    metricAvgRT: document.getElementById('metricAvgRT'),
    metricCpuUtil: document.getElementById('metricCpuUtil'),
    utilizationFill: document.getElementById('utilizationFill'),
    resultsTableBody: document.getElementById('resultsTableBody'),
    eventLogContainer: document.getElementById('eventLogContainer'),
    clearLogBtn: document.getElementById('clearLogBtn'),
    soundToggleBtn: document.getElementById('soundToggleBtn'),
    themeToggleBtn: document.getElementById('themeToggleBtn')
};

// Initialize Application
document.addEventListener('DOMContentLoaded', () => {
    initProcessTable();
    initEventListeners();
    updateQuantumVisibility();
    renderGanttLegend();
    logEvent('Simulator ready. Loaded sample test suite (P1-P4).', 'system');
});

// Event Listeners
function initEventListeners() {
    DOM.algorithmSelect.addEventListener('change', () => {
        updateQuantumVisibility();
        resetSimulation();
    });

    DOM.quantumPlus.addEventListener('click', () => {
        DOM.quantumInput.value = Math.min(20, parseInt(DOM.quantumInput.value || 2) + 1);
        resetSimulation();
    });

    DOM.quantumMinus.addEventListener('click', () => {
        DOM.quantumInput.value = Math.max(1, parseInt(DOM.quantumInput.value || 2) - 1);
        resetSimulation();
    });

    DOM.speedPills.forEach(pill => {
        pill.addEventListener('click', () => {
            DOM.speedPills.forEach(p => p.classList.remove('active'));
            pill.classList.add('active');
            state.speedMultiplier = parseFloat(pill.dataset.speed);
            if (state.isRunning && !state.isPaused) {
                restartTimer();
            }
        });
    });

    DOM.startBtn.addEventListener('click', toggleStartPause);
    DOM.stepBtn.addEventListener('click', stepSimulation);
    DOM.resetBtn.addEventListener('click', resetSimulation);
    DOM.addProcessBtn.addEventListener('click', addNewProcess);
    DOM.loadSampleBtn.addEventListener('click', loadSampleData);
    DOM.randomizeBtn.addEventListener('click', randomizeProcesses);
    DOM.clearLogBtn.addEventListener('click', () => DOM.eventLogContainer.innerHTML = '');

    DOM.soundToggleBtn.addEventListener('click', () => {
        state.soundEnabled = !state.soundEnabled;
        DOM.soundToggleBtn.textContent = state.soundEnabled ? '🔊' : '🔇';
    });

    DOM.themeToggleBtn.addEventListener('click', () => {
        const isDark = document.documentElement.getAttribute('data-theme') !== 'light';
        document.documentElement.setAttribute('data-theme', isDark ? 'light' : 'dark');
        DOM.themeToggleBtn.textContent = isDark ? '☀️' : '🌙';
    });

    // Keyboard Shortcuts
    window.addEventListener('keydown', (e) => {
        if (e.target.tagName === 'INPUT' || e.target.tagName === 'SELECT') return;
        if (e.code === 'Space') {
            e.preventDefault();
            toggleStartPause();
        } else if (e.code === 'KeyR') {
            resetSimulation();
        } else if (e.code === 'KeyS') {
            stepSimulation();
        }
    });
}

function updateQuantumVisibility() {
    const algo = DOM.algorithmSelect.value;
    DOM.quantumGroup.style.display = algo === 'RR' ? 'flex' : 'none';
}

// Process Table Rendering & Editing
function initProcessTable() {
    DOM.processTableBody.innerHTML = '';
    state.processes.forEach((p, idx) => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td>
                <span class="pid-tag" style="background:${p.color}25; color:${p.color}; border: 1px solid ${p.color}50">
                    ${p.pid}
                </span>
            </td>
            <td>
                <input type="number" class="table-input" value="${p.arrivalTime}" min="0" max="100" data-field="arrivalTime" data-idx="${idx}">
            </td>
            <td>
                <input type="number" class="table-input" value="${p.burstTime}" min="1" max="100" data-field="burstTime" data-idx="${idx}">
            </td>
            <td>
                <input type="number" class="table-input" value="${p.priority}" min="1" max="20" data-field="priority" data-idx="${idx}">
            </td>
            <td>
                <button class="delete-btn" title="Delete Process" data-idx="${idx}">✕</button>
            </td>
        `;
        DOM.processTableBody.appendChild(tr);
    });

    // Attach row input listeners
    DOM.processTableBody.querySelectorAll('.table-input').forEach(input => {
        input.addEventListener('change', (e) => {
            const idx = parseInt(e.target.dataset.idx, 10);
            const field = e.target.dataset.field;
            state.processes[idx][field] = Math.max(0, parseInt(e.target.value, 10) || 0);
            resetSimulation();
        });
    });

    DOM.processTableBody.querySelectorAll('.delete-btn').forEach(btn => {
        btn.addEventListener('click', (e) => {
            const idx = parseInt(e.target.dataset.idx, 10);
            if (state.processes.length <= 1) {
                alert('At least one process is required.');
                return;
            }
            state.processes.splice(idx, 1);
            initProcessTable();
            renderGanttLegend();
            resetSimulation();
        });
    });

    renderGanttLegend();
}

function addNewProcess() {
    const nextNum = state.processes.length + 1;
    const color = state.colorPalette[(nextNum - 1) % state.colorPalette.length];
    state.processes.push({
        pid: `P${nextNum}`,
        arrivalTime: Math.min(10, nextNum * 2 - 2),
        burstTime: Math.floor(Math.random() * 6) + 2,
        priority: Math.floor(Math.random() * 4) + 1,
        color
    });
    initProcessTable();
    resetSimulation();
    logEvent(`Added new process P${nextNum}`, 'system');
}

function loadSampleData() {
    state.processes = [
        { pid: 'P1', arrivalTime: 0, burstTime: 7, priority: 2, color: 'var(--color-p1)' },
        { pid: 'P2', arrivalTime: 1, burstTime: 4, priority: 1, color: 'var(--color-p2)' },
        { pid: 'P3', arrivalTime: 3, burstTime: 2, priority: 3, color: 'var(--color-p3)' },
        { pid: 'P4', arrivalTime: 5, burstTime: 5, priority: 2, color: 'var(--color-p4)' }
    ];
    initProcessTable();
    resetSimulation();
    logEvent('Restored standard textbook sample processes (P1-P4).', 'system');
}

function randomizeProcesses() {
    state.processes.forEach((p, i) => {
        p.arrivalTime = i === 0 ? 0 : Math.floor(Math.random() * 6);
        p.burstTime = Math.floor(Math.random() * 8) + 2;
        p.priority = Math.floor(Math.random() * 4) + 1;
    });
    initProcessTable();
    resetSimulation();
    logEvent('Randomized arrival, burst, and priorities.', 'system');
}

function renderGanttLegend() {
    DOM.ganttLegend.innerHTML = '';
    state.processes.forEach(p => {
        const item = document.createElement('div');
        item.className = 'legend-item';
        item.innerHTML = `
            <span class="legend-color" style="background:${p.color}"></span>
            <span>${p.pid}</span>
        `;
        DOM.ganttLegend.appendChild(item);
    });
}

// Simulation Control Logic
function toggleStartPause() {
    if (!state.isRunning) {
        startSimulation();
    } else if (state.isPaused) {
        resumeSimulation();
    } else {
        pauseSimulation();
    }
}

function startSimulation() {
    const algorithm = DOM.algorithmSelect.value;
    const timeQuantum = parseInt(DOM.quantumInput.value, 10) || 2;

    // Run simulation algorithm
    state.simulationData = CPUScheduler.simulate(algorithm, state.processes, timeQuantum);
    state.currentTick = 0;
    state.isRunning = true;
    state.isPaused = false;

    DOM.startBtnText.textContent = 'Pause';
    DOM.statusIndicator.className = 'status-indicator running';
    DOM.statusText.textContent = `Running (${algorithm})`;

    logEvent(`Simulation started with ${algorithm} algorithm.`, 'system');
    playTone(520, 'sine', 0.1);

    prepareGanttChart();
    restartTimer();
}

function pauseSimulation() {
    state.isPaused = true;
    clearInterval(state.timerId);
    DOM.startBtnText.textContent = 'Resume';
    DOM.statusIndicator.className = 'status-indicator';
    DOM.statusText.textContent = 'Paused';
    logEvent('Simulation paused.', 'system');
    playTone(350, 'sine', 0.08);
}

function resumeSimulation() {
    state.isPaused = false;
    DOM.startBtnText.textContent = 'Pause';
    DOM.statusIndicator.className = 'status-indicator running';
    DOM.statusText.textContent = 'Running';
    logEvent('Simulation resumed.', 'system');
    playTone(580, 'sine', 0.08);
    restartTimer();
}

function restartTimer() {
    clearInterval(state.timerId);
    const interval = state.baseTickMs / state.speedMultiplier;
    state.timerId = setInterval(() => {
        stepSimulation();
    }, interval);
}

function stepSimulation() {
    if (!state.simulationData) {
        startSimulation();
        pauseSimulation();
        return;
    }

    if (state.currentTick >= state.simulationData.timeline.length) {
        completeSimulation();
        return;
    }

    renderTick(state.currentTick);
    state.currentTick++;
}

function resetSimulation() {
    clearInterval(state.timerId);
    state.isRunning = false;
    state.isPaused = false;
    state.currentTick = 0;
    state.simulationData = null;

    DOM.startBtnText.textContent = 'Start';
    DOM.statusIndicator.className = 'status-indicator';
    DOM.statusText.textContent = 'Ready';
    DOM.simClock.textContent = '0';

    // Clear Queues & Stages
    DOM.readyQueueTrack.innerHTML = '<div class="empty-placeholder">Queue is empty</div>';
    DOM.readyCount.textContent = '0 processes';
    DOM.waitingQueueTrack.innerHTML = '<div class="empty-placeholder">No waiting processes</div>';
    DOM.waitingCount.textContent = '0 processes';
    DOM.completedQueueTrack.innerHTML = '<div class="empty-placeholder">No completed processes yet</div>';
    DOM.completedCount.textContent = '0 processes';

    // Reset CPU stage
    DOM.cpuSlot.classList.remove('active');
    DOM.cpuStateText.textContent = 'IDLE';
    DOM.cpuStage.innerHTML = `
        <div class="cpu-idle-graphic">
            <div class="chip-graphic">CPU</div>
            <span>Waiting for process...</span>
        </div>
    `;

    // Reset Gantt & Marker
    DOM.ganttBlocks.innerHTML = '';
    DOM.ganttTicks.innerHTML = '';
    DOM.currentTimeMarker.style.left = '16px';

    // Reset Metrics
    DOM.metricAvgWT.innerHTML = '0.00 <small>ms</small>';
    DOM.metricAvgTAT.innerHTML = '0.00 <small>ms</small>';
    DOM.metricAvgRT.innerHTML = '0.00 <small>ms</small>';
    DOM.metricCpuUtil.innerHTML = '0.0 <small>%</small>';
    DOM.utilizationFill.style.width = '0%';
    DOM.resultsTableBody.innerHTML = '';

    logEvent('Simulator reset.', 'system');
    playTone(280, 'sine', 0.1);
}

function completeSimulation() {
    clearInterval(state.timerId);
    state.isRunning = false;
    DOM.startBtnText.textContent = 'Start';
    DOM.statusIndicator.className = 'status-indicator';
    DOM.statusText.textContent = 'Completed';

    DOM.cpuSlot.classList.remove('active');
    DOM.cpuStateText.textContent = 'COMPLETED';

    // Finalize metrics
    const data = state.simulationData;
    DOM.metricAvgWT.innerHTML = `${data.stats.avgWT} <small>ms</small>`;
    DOM.metricAvgTAT.innerHTML = `${data.stats.avgTAT} <small>ms</small>`;
    DOM.metricAvgRT.innerHTML = `${data.stats.avgRT} <small>ms</small>`;
    DOM.metricCpuUtil.innerHTML = `${data.stats.cpuUtil} <small>%</small>`;
    DOM.utilizationFill.style.width = `${data.stats.cpuUtil}%`;

    renderResultsTable(data.processResults);

    logEvent(`All processes executed successfully! Total Time: ${data.totalTime}ms, Avg WT: ${data.stats.avgWT}ms.`, 'finish');
    playTone(784, 'triangle', 0.25);
}

// Tick-by-Tick UI Update
function renderTick(tickIndex) {
    const tick = state.simulationData.timeline[tickIndex];
    if (!tick) return;

    DOM.simClock.textContent = tick.time;

    // Update Event Log
    if (tick.event) {
        const type = tick.running ? 'run' : 'system';
        logEvent(`[T=${tick.time}ms] ${tick.event}`, type);
    }

    // Sound effect on dispatch
    if (tick.running) {
        playTone(440 + parseInt(tick.running.pid.replace('P', ''), 10) * 40, 'sine', 0.04);
    }

    // 1. Ready Queue Update
    renderReadyQueue(tick.readyQueue);

    // 2. CPU Core Update
    renderCPU(tick.running);

    // 3. Completed Queue Update
    renderCompletedQueue(tick.completedQueue);

    // 4. Update Gantt Marker
    updateGanttMarker(tickIndex);
}

function renderReadyQueue(readyProcesses) {
    DOM.readyCount.textContent = `${readyProcesses.length} process${readyProcesses.length === 1 ? '' : 'es'}`;
    if (readyProcesses.length === 0) {
        DOM.readyQueueTrack.innerHTML = '<div class="empty-placeholder">Queue is empty</div>';
        return;
    }

    DOM.readyQueueTrack.innerHTML = '';
    readyProcesses.forEach(p => {
        const card = document.createElement('div');
        card.className = 'p-card';
        card.style.background = p.color;
        card.innerHTML = `
            <div class="p-card-top">
                <span>${p.pid}</span>
                <span class="p-card-bt">${p.remainingTime}ms</span>
            </div>
            <div>Pri: ${p.priority} | Arr: ${p.arrivalTime}ms</div>
            <div class="p-card-progress">
                <div class="p-card-fill" style="width:${((p.burstTime - p.remainingTime) / p.burstTime) * 100}%"></div>
            </div>
        `;
        DOM.readyQueueTrack.appendChild(card);
    });
}

function renderCPU(runningProcess) {
    if (!runningProcess) {
        DOM.cpuSlot.classList.remove('active');
        DOM.cpuStateText.textContent = 'IDLE';
        DOM.cpuStage.innerHTML = `
            <div class="cpu-idle-graphic">
                <div class="chip-graphic">CPU</div>
                <span>Idle - No process in ready queue</span>
            </div>
        `;
        return;
    }

    DOM.cpuSlot.classList.add('active');
    DOM.cpuStateText.textContent = 'RUNNING';

    const pct = ((runningProcess.burstTime - runningProcess.remainingTime) / runningProcess.burstTime) * 100;

    DOM.cpuStage.innerHTML = `
        <div class="cpu-active-process">
            <div class="cpu-active-card" style="background:${runningProcess.color}">
                ${runningProcess.pid}
            </div>
            <div class="cpu-active-details">
                <div class="cpu-active-stats">
                    <span>Remaining: <strong>${runningProcess.remainingTime}ms</strong></span>
                    <span>Burst: ${runningProcess.burstTime}ms</span>
                    <span>Pri: ${runningProcess.priority}</span>
                </div>
                <div class="cpu-countdown-bar">
                    <div class="cpu-countdown-fill" style="width:${pct}%"></div>
                </div>
            </div>
        </div>
    `;
}

function renderCompletedQueue(completedProcesses) {
    DOM.completedCount.textContent = `${completedProcesses.length} process${completedProcesses.length === 1 ? '' : 'es'}`;
    if (completedProcesses.length === 0) {
        DOM.completedQueueTrack.innerHTML = '<div class="empty-placeholder">No completed processes yet</div>';
        return;
    }

    DOM.completedQueueTrack.innerHTML = '';
    completedProcesses.forEach(p => {
        const card = document.createElement('div');
        card.className = 'p-card';
        card.style.background = `${p.color}cc`;
        card.innerHTML = `
            <div class="p-card-top">
                <span>✓ ${p.pid}</span>
                <span class="p-card-bt">CT: ${p.completionTime || 0}ms</span>
            </div>
            <div>TAT: ${p.turnaroundTime || 0}ms | WT: ${p.waitingTime || 0}ms</div>
        `;
        DOM.completedQueueTrack.appendChild(card);
    });
}

// Gantt Chart Rendering
const TICK_WIDTH = 34; // pixels per ms tick

function prepareGanttChart() {
    const timeline = state.simulationData.timeline;
    const totalTime = timeline.length;
    DOM.ganttBlocks.innerHTML = '';
    DOM.ganttTicks.innerHTML = '';

    // Group adjacent ticks by process
    const blocks = [];
    let currentBlock = null;

    timeline.forEach((tick, i) => {
        const pid = tick.running ? tick.running.pid : 'IDLE';
        const color = tick.running ? tick.running.color : 'transparent';

        if (!currentBlock || currentBlock.pid !== pid) {
            if (currentBlock) blocks.push(currentBlock);
            currentBlock = {
                pid,
                color,
                start: i,
                duration: 1
            };
        } else {
            currentBlock.duration++;
        }
    });
    if (currentBlock) blocks.push(currentBlock);

    // Build Gantt Blocks
    blocks.forEach(block => {
        const div = document.createElement('div');
        div.className = `gantt-block ${block.pid === 'IDLE' ? 'gantt-block-idle' : ''}`;
        div.style.width = `${block.duration * TICK_WIDTH}px`;
        if (block.pid !== 'IDLE') {
            div.style.background = block.color;
        }
        div.title = `${block.pid}: ${block.start}ms → ${block.start + block.duration}ms (${block.duration}ms)`;
        div.innerHTML = `<span>${block.pid}</span>`;
        DOM.ganttBlocks.appendChild(div);
    });

    // Build Ticks (Time numbers)
    for (let t = 0; t <= totalTime; t++) {
        const tickDiv = document.createElement('div');
        tickDiv.className = 'gantt-tick';
        tickDiv.style.left = `${t * TICK_WIDTH + 16}px`;
        tickDiv.textContent = t;
        DOM.ganttTicks.appendChild(tickDiv);
    }

    DOM.currentTimeMarker.style.left = '16px';
}

function updateGanttMarker(tickIndex) {
    const markerX = tickIndex * TICK_WIDTH + 16;
    DOM.currentTimeMarker.style.left = `${markerX}px`;

    // Auto-scroll Gantt if advancing beyond viewport
    const wrapper = document.getElementById('ganttWrapper');
    if (markerX > wrapper.scrollLeft + wrapper.clientWidth - 100) {
        wrapper.scrollTo({ left: markerX - 100, behavior: 'smooth' });
    }
}

// Final Results Table
function renderResultsTable(results) {
    DOM.resultsTableBody.innerHTML = '';
    results.forEach(p => {
        const tr = document.createElement('tr');
        tr.innerHTML = `
            <td><strong style="color:${p.color}">${p.pid}</strong></td>
            <td>${p.arrivalTime}</td>
            <td>${p.burstTime}</td>
            <td>${p.completionTime}</td>
            <td>${p.turnaroundTime}</td>
            <td>${p.waitingTime}</td>
            <td>${p.responseTime}</td>
        `;
        DOM.resultsTableBody.appendChild(tr);
    });
}

// Event Logging Helper
function logEvent(msg, type = 'system') {
    const entry = document.createElement('div');
    entry.className = `log-entry log-entry-${type}`;
    const now = new Date();
    const timeStr = `${String(now.getMinutes()).padStart(2, '0')}:${String(now.getSeconds()).padStart(2, '0')}`;
    entry.innerHTML = `
        <span class="log-time">[${timeStr}]</span>
        <span class="log-msg">${msg}</span>
    `;
    DOM.eventLogContainer.appendChild(entry);
    DOM.eventLogContainer.scrollTop = DOM.eventLogContainer.scrollHeight;
}
