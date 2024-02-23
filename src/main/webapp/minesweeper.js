class Minesweeper {
    constructor(rows, cols, mines) {
        this.rows = rows;
        this.cols = cols;
        this.mines = mines;
        this.board = [];
        this.initBoard();
        this.generateMines();
        this.calculateNumbers();
    }

    initBoard() {
        for (let row = 0; row < this.rows; row++) {
            this.board.push([]);
            for (let col = 0; col < this.cols; col++) {
                this.board[row].push({ isMine: false, number: 0, revealed: false, flagged: false });
            }
        }
    }

    generateMines() {
        let mineCount = 0;
        while (mineCount < this.mines) {
            const row = Math.floor(Math.random() * this.rows);
            const col = Math.floor(Math.random() * this.cols);
            if (!this.board[row][col].isMine) {
                this.board[row][col].isMine = true;
                mineCount++;
            }
        }
    }

    calculateNumbers() {
        for (let row = 0; row < this.rows; row++) {
            for (let col = 0; col < this.cols; col++) {
                if (!this.board[row][col].isMine) {
                    let count = 0;
                    for (let r = Math.max(row - 1, 0); r <= Math.min(row + 1, this.rows - 1); r++) {
                        for (let c = Math.max(col - 1, 0); c <= Math.min(col + 1, this.cols - 1); c++) {
                            if (this.board[r][c].isMine) count++;
                        }
                    }
                    this.board[row][col].number = count;
                }
            }
        }
    }


    reveal(row, col) {
        if (this.board[row][col].revealed || this.board[row][col].flagged) return;
        this.board[row][col].revealed = true;
        if (this.board[row][col].number === 0) {
            for (let r = Math.max(row - 1, 0); r <= Math.min(row + 1, this.rows - 1); r++) {
                for (let c = Math.max(col - 1, 0); c <= Math.min(col + 1, this.cols - 1); c++) {
                    if (!this.board[r][c].revealed) {
                        this.reveal(r, c);
                    }
                }
            }
        }
    }

    toggleFlag(row, col) {
        if (this.board[row][col].revealed) return;
        this.board[row][col].flagged = !this.board[row][col].flagged;
    }
}


const easyBtn = document.getElementById('easy');
const mediumBtn = document.getElementById('medium');
const hardBtn = document.getElementById('hard');
const restartBtn = document.getElementById('restart');
let newRows, newCols, newMines;

easyBtn.addEventListener('click', () => setDifficulty('easy'));
mediumBtn.addEventListener('click', () => setDifficulty('medium'));
hardBtn.addEventListener('click', () => setDifficulty('hard'));

restartBtn.addEventListener('click', restartGame);

function setDifficulty(difficulty) {
    if (difficulty === 'easy') {
        newRows = 10;
        newCols = 10;
        newMines = 10;
    } else if (difficulty === 'medium') {
        newRows = 15;
        newCols = 16;
        newMines = 40;
    } else {
        newRows = 15;
        newCols = 16;
        newMines = 99;
    }
    game = new Minesweeper(newRows, newCols, newMines);
    createTableStructure();
    renderBoard();
}

let rows = 20;
let cols = 10;
let mines = 13;
let game = new Minesweeper(rows, cols, mines);
let board = document.getElementById('game-board');

function restartGame() {
    location.reload();
}

function renderBoard() {
    board.innerHTML = '';
    for (let row = 0; row < game.rows; row++) {
        for (let col = 0; col < game.cols; col++) {
            const cell = document.createElement('div');
            cell.classList.add('cell');
            if (game.board[row][col].revealed) {
                cell.classList.add('revealed');
                if (game.board[row][col].isMine) {
                    cell.classList.add('mine');
                } else {
                    cell.textContent = game.board[row][col].number || '';
                }
            } else if (game.board[row][col].flagged) {
                cell.classList.add('flag');
                cell.textContent = 'Q';
            }
            cell.addEventListener('click', () => handleClick(row, col));
            cell.addEventListener('contextmenu', (e) => handleRightClick(e, row, col));
            board.appendChild(cell);
        }
    }
}
function handleClick(row, col) {
    if (game.board[row][col].flagged) return;
    game.reveal(row, col);
    if (game.board[row][col].isMine) {
        alert('You Lose!, Play again?');
        location.reload();
    } else if (checkWin()) {
        alert('You Win!');
    }
    renderBoard();
}

function handleRightClick(e, row, col) {
    e.preventDefault();
    game.toggleFlag(row, col);
    renderBoard();
}

function createTableStructure() {
    board.innerHTML = '';
    for (let row = 0; row < game.rows; row++) {
        const tableRow = document.createElement('div');
        tableRow.classList.add('table-row');
        board.appendChild(tableRow);
        for (let col = 0; col < game.cols; col++) {
            const cell = document.createElement('div');
            cell.classList.add('cell');
            tableRow.appendChild(cell);
        }
    }
}

function checkWin() {
    let revealedCount = 0;
    for (let row = 0; row < game.rows; row++) {
        for (let col = 0; col < game.cols; col++) {
            if (game.board[row][col].revealed) revealedCount++;
        }
    }
    return revealedCount === game.rows * game.cols - game.mines;
}

createTableStructure();
renderBoard();

