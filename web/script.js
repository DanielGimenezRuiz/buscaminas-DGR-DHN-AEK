const DIFFICULTIES = {
    principiante: { displayName: "Novato", rows: 8, cols: 8, mines: 10, scoreMultiplier: 1.0 },
    intermedio: { displayName: "Intermedio", rows: 16, cols: 16, mines: 40, scoreMultiplier: 1.5 },
    experto: { displayName: "Experto", rows: 16, cols: 30, mines: 99, scoreMultiplier: 2.5 }
};

const NUMBER_COLORS = [
    "", "#1565C0", "#2E7D32", "#C62828", "#4527A0",
    "#6D4C41", "#00838F", "#000000", "#546E7A"
];

let currentDifficulty = DIFFICULTIES.principiante;
let boardCells = []; 
let flagsPlaced = 0;
let revealedCount = 0;
let gameOver = false;
let gameWon = false;
let firstClick = true;

let elapsedSeconds = 0;
let timerInterval = null;

const buscador = document.getElementById('buscador');
const botonJugarJava = document.getElementById('botonJugarJava');
const espacioSelectorMedio = document.getElementById('espacio-selector-medio');
const contenedorPrincipal = document.getElementById('contenedor-principal');

botonJugarJava.addEventListener('click', () => {
    espacioSelectorMedio.innerHTML = `
        <div class="bloque-seleccion-medio">
            <select id="selector-nivel" class="select-medio-estilo">
                <option value="principiante">Principiante (8x8 - 10 minas)</option>
                <option value="intermedio">Intermedio (16x16 - 40 minas)</option>
                <option value="experto">Experto (16x30 - 99 minas)</option>
            </select>
            <button id="btn-confirmar-java" class="btn-confirmar-medio">
                Confirmar e Iniciar
            </button>
        </div>
    `;

    document.getElementById('btn-confirmar-java').addEventListener('click', () => {
        const nivelElegido = document.getElementById('selector-nivel').value;
        currentDifficulty = DIFFICULTIES[nivelElegido];
        inicializarEstructuraTablero();
    });
});

function inicializarEstructuraTablero() {
    flagsPlaced = 0;
    revealedCount = 0;
    gameOver = false;
    gameWon = false;
    firstClick = true;
    elapsedSeconds = 0;
    clearInterval(timerInterval);

    boardCells = [];
    for (let r = 0; r < currentDifficulty.rows; r++) {
        boardCells[r] = [];
        for (let c = 0; c < currentDifficulty.cols; c++) {
            boardCells[r][c] = {
                mine: false,
                revealed: false,
                flagged: false,
                adjacentMines: 0
            };
        }
    }

    contenedorPrincipal.innerHTML = `
        <div class="minesweeper-wrapper">
            <div class="game-header">
                <div class="lcd-display" id="mine-lcd">000</div>
                <button class="face-button" id="face-btn">🙂</button>
                <div class="lcd-display" id="timer-lcd">000</div>
            </div>
            <div class="game-grid" id="mines-grid" style="grid-template-columns: repeat(${currentDifficulty.cols}, 24px);"></div>
            
            <div class="score-bar-flex">
                <div class="game-score-bar">Puntuación: <span id="score-live-label" style="color: #1565C0;">0</span></div>
                <button class="btn-cambiar-diff-directo" id="btn-cambiar-diff-directo">Cambiar Dificultad</button>
            </div>
        </div>
    `;

    document.getElementById('btn-cambiar-diff-directo').addEventListener('click', () => {
        clearInterval(timerInterval);
        espacioSelectorMedio.scrollIntoView({ behavior: 'smooth' });
    });

    const gridContainer = document.getElementById('mines-grid');
    for (let r = 0; r < currentDifficulty.rows; r++) {
        for (let c = 0; c < currentDifficulty.cols; c++) {
            const btn = document.createElement('button');
            btn.classList.add('cell-btn');
            btn.dataset.row = r;
            btn.dataset.col = c;
            
            btn.addEventListener('mousedown', (e) => {
                if (e.button === 0 && !gameOver) {
                    document.getElementById('face-btn').innerText = "😮";
                }
            });

            btn.addEventListener('mouseup', (e) => {
                if (e.button === 0 && !gameOver) {
                    document.getElementById('face-btn').innerText = "🙂";
                    handleLeftClick(r, c);
                }
            });

            btn.addEventListener('contextmenu', (e) => {
                e.preventDefault();
                handleRightClick(r, c);
            });

            gridContainer.appendChild(btn);
        }
    }

    document.getElementById('face-btn').addEventListener('click', inicializarEstructuraTablero);
    actualizarMarcadoresUI();
}

function placeMines(safeRow, safeCol) {
    let placed = 0;
    while (placed < currentDifficulty.mines) {
        let r = Math.floor(Math.random() * currentDifficulty.rows);
        let c = Math.floor(Math.random() * currentDifficulty.cols);
        if (!boardCells[r][c].mine && !isNeighbour(r, c, safeRow, safeCol)) {
            boardCells[r][c].mine = true;
            placed++;
        }
    }
    computeAdjacentCounts();
}

function isNeighbour(r, c, sr, sc) {
    return Math.abs(r - sr) <= 1 && Math.abs(c - sc) <= 1;
}

function computeAdjacentCounts() {
    for (let r = 0; r < currentDifficulty.rows; r++) {
        for (let c = 0; c < currentDifficulty.cols; c++) {
            if (!boardCells[r][c].mine) {
                boardCells[r][c].adjacentMines = countAdjacentMines(r, c);
            }
        }
    }
}

function countAdjacentMines(r, c) {
    let count = 0;
    for (let dr = -1; dr <= 1; dr++) {
        for (let dc = -1; dc <= 1; dc++) {
            let nr = r + dr;
            let nc = c + dc;
            if (nr >= 0 && nr < currentDifficulty.rows && nc >= 0 && nc < currentDifficulty.cols) {
                if (boardCells[nr][nc].mine) count++;
            }
        }
    }
    return count;
}

function handleLeftClick(row, col) {
    if (gameOver) return;
    let cell = boardCells[row][col];
    if (cell.revealed || cell.flagged) return;

    if (firstClick) {
        placeMines(row, col);
        firstClick = false;
        timerInterval = setInterval(() => {
            elapsedSeconds++;
            let displayTime = Math.min(elapsedSeconds, 999);
            document.getElementById('timer-lcd').innerText = String(displayTime).padStart(3, '0');
            actualizarPuntuacionEnVivo();
        }, 1000);
    }

    if (cell.mine) {
        cell.revealed = true;
        gameOver = true;
        revealAllMines();
        document.getElementById('face-btn').innerText = "😵";
        clearInterval(timerInterval);
        refrescarTableroUI();
        setTimeout(() => lanzarModalResultado(false), 400);
        return;
    }

    floodReveal(row, col);
    checkWin();
    refrescarTableroUI();
    actualizarPuntuacionEnVivo();
}

function floodReveal(r, c) {
    if (r < 0 || r >= currentDifficulty.rows || c < 0 || c >= currentDifficulty.cols) return;
    let cell = boardCells[r][c];
    if (cell.revealed || cell.flagged) return;

    cell.revealed = true;
    revealedCount++;

    if (cell.adjacentMines === 0 && !cell.mine) {
        for (let dr = -1; dr <= 1; dr++) {
            for (let dc = -1; dc <= 1; dc++) {
                if (dr !== 0 || dc !== 0) {
                    floodReveal(r + dr, c + dc);
                }
            }
        }
    }
}

function handleRightClick(row, col) {
    if (gameOver) return;
    let cell = boardCells[row][col];
    if (cell.revealed) return;

    if (cell.flagged) {
        cell.flagged = false;
        flagsPlaced--;
    } else {
        cell.flagged = true;
        flagsPlaced++;
    }

    actualizarMarcadoresUI();
    actualizarBotonCeldaUI(row, col);
    actualizarPuntuacionEnVivo();
}

function checkWin() {
    let safeCells = (currentDifficulty.rows * currentDifficulty.cols) - currentDifficulty.mines;
    if (revealedCount === safeCells) {
        gameWon = true;
        gameOver = true;
        document.getElementById('face-btn').innerText = "😎";
        clearInterval(timerInterval);
        setTimeout(() => lanzarModalResultado(true), 400);
    }
}

function revealAllMines() {
    for (let r = 0; r < currentDifficulty.rows; r++) {
        for (let c = 0; c < currentDifficulty.cols; c++) {
            if (boardCells[r][c].mine) boardCells[r][c].revealed = true;
        }
    }
}

function refrescarTableroUI() {
    for (let r = 0; r < currentDifficulty.rows; r++) {
        for (let c = 0; c < currentDifficulty.cols; c++) {
            actualizarBotonCeldaUI(r, c);
        }
    }
}

function actualizarBotonCeldaUI(r, c) {
    const btn = document.querySelector(`.cell-btn[data-row="${r}"][data-col="${c}"]`);
    if (!btn) return;
    let cell = boardCells[r][c];

    if (!cell.revealed) {
        btn.innerText = cell.flagged ? "🚩" : "";
        return;
    }

    btn.classList.add('revealed');
    if (cell.mine) {
        btn.innerText = "💣";
        btn.classList.add('mine-exploded');
        return;
    }

    let adj = cell.adjacentMines;
    if (adj === 0) {
        btn.innerText = "";
    } else {
        btn.innerText = adj;
        btn.style.color = NUMBER_COLORS[adj];
    }
}

function actualizarMarcadoresUI() {
    let remainingMines = Math.max(0, currentDifficulty.mines - flagsPlaced);
    document.getElementById('mine-lcd').innerText = String(remainingMines).padStart(3, '0');
}

function calcularPuntuacionMatematica(wonState) {
    let safeCells = (currentDifficulty.rows * currentDifficulty.cols) - currentDifficulty.mines;
    let baseScore = revealedCount * 10.0;
    if (wonState) baseScore += 500;
    let timePenalty = Math.max(0, elapsedSeconds - 10);
    baseScore -= timePenalty;
    let progress = safeCells > 0 ? revealedCount / safeCells : 0;
    baseScore *= (0.5 + progress * 0.5);
    baseScore *= currentDifficulty.scoreMultiplier;
    return Math.max(0, Math.round(baseScore));
}

function actualizarPuntuacionEnVivo() {
    const liveLabel = document.getElementById('score-live-label');
    if (liveLabel) liveLabel.innerText = calcularPuntuacionMatematica(gameWon);
}

function lanzarModalResultado(won) {
    const score = calcularPuntuacionMatematica(won);
    document.getElementById('modal-emoji').innerText = won ? "🎉" : "💥";
    document.getElementById('modal-title').innerText = won ? "¡Has ganado!" : "¡Boom! Has perdido";
    document.getElementById('modal-title').style.color = won ? "#00f2fe" : "#ff007f";
    
    document.getElementById('stat-diff').innerText = currentDifficulty.displayName;
    
    let m = Math.floor(elapsedSeconds / 60);
    let s = elapsedSeconds % 60;
    document.getElementById('stat-time').innerText = `${String(m).padStart(2, '0')}:${String(s).padStart(2, '0')}`;
    document.getElementById('stat-cells').innerText = revealedCount;
    document.getElementById('stat-score').innerText = score;

    document.getElementById('result-modal').classList.add('active');
}

document.getElementById('modal-btn-restart').addEventListener('click', () => {
    document.getElementById('result-modal').classList.remove('active');
    inicializarEstructuraTablero();
});

document.getElementById('modal-btn-menu').addEventListener('click', () => {
    document.getElementById('result-modal').classList.remove('active');
    // En lugar de reiniciar la página, hacemos un scroll suave de vuelta arriba al selector intermedio
    espacioSelectorMedio.scrollIntoView({ behavior: 'smooth' });
});

buscador.addEventListener('keydown', (event) => {
    if (event.key === 'Enter') {
        const query = buscador.value.trim().toLowerCase();
        if (query === "") return;
        if (query.includes('versiones') || query.includes('versio')) {
            window.open('https://www.google.com/search?q=buscaminas+versiones+juegos', '_blank');
        } else if (query.includes('tutorial') || query.includes('video') || query.includes('como jugar')) {
            window.open('https://youtu.be/PqA7yQcAZVc?si=y2-xhyVf-1pLzHSZ', '_blank');
        } else {
            window.open(`https://www.google.com/search?q=buscaminas+${encodeURIComponent(query)}`, '_blank');
        }
    }
});