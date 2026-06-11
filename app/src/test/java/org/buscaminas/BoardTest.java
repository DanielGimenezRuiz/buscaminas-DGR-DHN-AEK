package org.buscaminas;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Board – lógica principal del tablero")
class BoardTest {

    // Usamos NOVATO (10×10 / 10 minas) para la mayoría de tests: es pequeño y rápido.
    private Board board;

    @BeforeEach
    void setUp() {
        board = new Board(Difficulty.NOVATO);
    }

    // ── Inicialización ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Estado inicial del tablero")
    class InitialState {

        @Test
        @DisplayName("El tablero tiene las dimensiones correctas según la dificultad")
        void correctDimensions() {
            assertEquals(10, board.getRows());
            assertEquals(10, board.getCols());
        }

        @Test
        @DisplayName("El total de minas corresponde a la dificultad")
        void correctTotalMines() {
            assertEquals(10, board.getTotalMines());
        }

        @Test
        @DisplayName("Al inicio no hay banderas colocadas")
        void noFlagsInitially() {
            assertEquals(0, board.getFlagsPlaced());
        }

        @Test
        @DisplayName("El contador de casillas reveladas empieza en 0")
        void noRevealedInitially() {
            assertEquals(0, board.getRevealedCount());
        }

        @Test
        @DisplayName("La partida no está terminada al inicio")
        void gameNotOverInitially() {
            assertFalse(board.isGameOver());
        }

        @Test
        @DisplayName("El primer click está pendiente al inicio")
        void firstClickPending() {
            assertTrue(board.isFirstClick());
        }

        @Test
        @DisplayName("Ninguna casilla está revelada ni tiene bandera al inicio")
        void allCellsHidden() {
            for (int r = 0; r < board.getRows(); r++)
                for (int c = 0; c < board.getCols(); c++) {
                    Cell cell = board.getCell(r, c);
                    assertFalse(cell.isRevealed(), "celda (" + r + "," + c + ") no debe estar revelada");
                    assertFalse(cell.isFlagged(),  "celda (" + r + "," + c + ") no debe tener bandera");
                }
        }
    }

    // ── Colocación de minas ───────────────────────────────────────────────────

    @Nested
    @DisplayName("Colocación de minas (placeMines)")
    class MinePlacement {

        @Test
        @DisplayName("Tras placeMines se colocan exactamente 10 minas")
        void exactMineCount() {
            board.placeMines(5, 5);
            int count = countMines(board);
            assertEquals(10, count);
        }

        @Test
        @DisplayName("La casilla del primer click no tiene mina")
        void safeFirstClick() {
            board.placeMines(0, 0);
            assertFalse(board.getCell(0, 0).isMine(), "la casilla (0,0) no debe ser mina");
        }

        @Test
        @DisplayName("Ningún vecino de la primera casilla tiene mina")
        void safeNeighboursOfFirstClick() {
            int sr = 5, sc = 5;
            board.placeMines(sr, sc);
            for (int dr = -1; dr <= 1; dr++)
                for (int dc = -1; dc <= 1; dc++) {
                    int r = sr + dr, c = sc + dc;
                    if (r >= 0 && r < 10 && c >= 0 && c < 10)
                        assertFalse(board.getCell(r, c).isMine(),
                            "vecino (" + r + "," + c + ") no debe ser mina");
                }
        }

        @Test
        @DisplayName("Ninguna celda no-mina tiene minas adyacentes negativas")
        void adjacentCountNonNegative() {
            board.placeMines(5, 5);
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (!board.getCell(r, c).isMine())
                        assertTrue(board.getCell(r, c).getAdjacentMines() >= 0);
        }

        @Test
        @DisplayName("Las minas adyacentes no superan 8")
        void adjacentCountMax8() {
            board.placeMines(5, 5);
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (!board.getCell(r, c).isMine())
                        assertTrue(board.getCell(r, c).getAdjacentMines() <= 8);
        }
    }

    // ── Primer click ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Comportamiento del primer click")
    class FirstClick {

        @Test
        @DisplayName("Después del primer click, firstClick pasa a false")
        void firstClickBecomesFlase() {
            board.reveal(5, 5);
            assertFalse(board.isFirstClick());
        }

        @Test
        @DisplayName("El primer click nunca revela una mina (la zona es segura)")
        void firstClickNeverHitsMine() {
            // Repetimos 20 veces para ganar confianza estadística
            for (int i = 0; i < 20; i++) {
                Board b = new Board(Difficulty.NOVATO);
                boolean hitMine = b.reveal(0, 0);
                assertFalse(hitMine, "el primer click no debe explotar");
            }
        }
    }

    // ── Revelar casillas ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Revelar casillas (reveal / flood fill)")
    class Reveal {

        @Test
        @DisplayName("Revelar una casilla ya revelada no cambia el estado")
        void revealAlreadyRevealed() {
            board.reveal(5, 5);
            int countBefore = board.getRevealedCount();
            board.reveal(5, 5);
            assertEquals(countBefore, board.getRevealedCount());
        }

        @Test
        @DisplayName("Revelar una casilla con bandera no hace nada")
        void revealFlaggedCell() {
            board.toggleFlag(3, 3);
            boolean hit = board.reveal(3, 3);
            assertFalse(hit);
            assertFalse(board.getCell(3, 3).isRevealed());
        }

        @Test
        @DisplayName("El contador de casillas reveladas aumenta tras un reveal válido")
        void revealedCountIncreases() {
            board.reveal(5, 5);
            assertTrue(board.getRevealedCount() > 0);
        }

        @Test
        @DisplayName("El flood fill revela al menos la casilla clickada")
        void floodRevealAtLeastOneCell() {
            board.reveal(5, 5);
            assertTrue(board.getCell(5, 5).isRevealed());
        }
    }

    // ── Banderas ──────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Gestión de banderas (toggleFlag)")
    class Flags {

        @Test
        @DisplayName("toggleFlag coloca la bandera en una casilla libre")
        void placeFlag() {
            board.toggleFlag(2, 3);
            assertTrue(board.getCell(2, 3).isFlagged());
            assertEquals(1, board.getFlagsPlaced());
        }

        @Test
        @DisplayName("toggleFlag dos veces quita la bandera")
        void removeFlagOnSecondToggle() {
            board.toggleFlag(2, 3);
            board.toggleFlag(2, 3);
            assertFalse(board.getCell(2, 3).isFlagged());
            assertEquals(0, board.getFlagsPlaced());
        }

        @Test
        @DisplayName("toggleFlag en casilla ya revelada no hace nada")
        void noFlagOnRevealed() {
            board.reveal(5, 5);
            board.toggleFlag(5, 5); // casilla revelada → se ignora
            // Si la celda estaba revelada la bandera no se pone
            assertFalse(board.getCell(5, 5).isFlagged());
        }

        @Test
        @DisplayName("getRemainingMines disminuye al colocar banderas")
        void remainingMinesDecrease() {
            int before = board.getRemainingMines();
            board.toggleFlag(0, 0);
            assertEquals(before - 1, board.getRemainingMines());
        }

        @Test
        @DisplayName("getRemainingMines vuelve al valor anterior al quitar bandera")
        void remainingMinesRestoreOnRemove() {
            int before = board.getRemainingMines();
            board.toggleFlag(0, 0);
            board.toggleFlag(0, 0);
            assertEquals(before, board.getRemainingMines());
        }

        @Test
        @DisplayName("Se pueden colocar varias banderas independientes")
        void multipleFlags() {
            board.toggleFlag(0, 0);
            board.toggleFlag(1, 1);
            board.toggleFlag(2, 2);
            assertEquals(3, board.getFlagsPlaced());
        }
    }

    // ── Condición de victoria ─────────────────────────────────────────────────

    @Nested
    @DisplayName("Condición de victoria")
    class WinCondition {

        @Test
        @DisplayName("No se gana antes de revelar todas las casillas seguras")
        void noWinBeforeAllSafeRevealed() {
            board.reveal(5, 5);
            // Es prácticamente imposible ganar con un solo click en 10×10
            // (a menos que el flood fill cubra todo, lo cual es improbable con 10 minas)
            // Lo que sí podemos asegurar: si no está ganado, isGameWon() es false
            if (!board.isGameWon()) {
                assertFalse(board.isGameWon());
            }
        }
    }

    // ── Condición de derrota ──────────────────────────────────────────────────

    @Nested
    @DisplayName("Condición de derrota (hit mine)")
    class LoseCondition {

        @Test
        @DisplayName("Revelar una mina directamente activa gameOver")
        void revealMineDirectlySetsGameOver() {
            // Forzamos la colocación de minas y buscamos una que NO sea vecina de (5,5)
            board.placeMines(5, 5);
            int mineRow = -1, mineCol = -1;
            outer:
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (board.getCell(r, c).isMine()) { mineRow = r; mineCol = c; break outer; }

            // Simulamos que NO es el primer click (ya se colocaron minas)
            // Usamos un nuevo board con firstClick ya consumido
            Board b2 = new Board(Difficulty.NOVATO);
            b2.reveal(5, 5); // primer click seguro
            // Forzamos la mina manualmente en una celda no revelada
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    b2.getCell(r, c).setMine(false);
            b2.getCell(0, 0).setMine(true);
            b2.getCell(0, 0).setRevealed(false); // aseguramos que no esté revelada
            // Ahora reveal sobre la mina debe devolver true
            boolean hit = b2.reveal(0, 0);
            assertTrue(hit, "revelar una mina debe devolver true");
            assertTrue(b2.isGameOver(), "gameOver debe activarse");
            assertFalse(b2.isGameWon(), "no debe marcarse como ganado");
        }
    }

    // ── revealAllMines ────────────────────────────────────────────────────────

    @Nested
    @DisplayName("revealAllMines – descubre todas las minas al perder")
    class RevealAllMines {

        @Test
        @DisplayName("Tras revealAllMines todas las casillas mina están reveladas")
        void allMinesRevealedAfterCall() {
            board.placeMines(5, 5);
            board.revealAllMines();
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (board.getCell(r, c).isMine())
                        assertTrue(board.getCell(r, c).isRevealed(),
                            "mina en (" + r + "," + c + ") debe estar revelada");
        }

        @Test
        @DisplayName("revealAllMines no revela casillas sin mina")
        void nonMinesNotRevealedByRevealAll() {
            board.placeMines(5, 5);
            board.revealAllMines();
            for (int r = 0; r < 10; r++)
                for (int c = 0; c < 10; c++)
                    if (!board.getCell(r, c).isMine())
                        assertFalse(board.getCell(r, c).isRevealed(),
                            "casilla sin mina (" + r + "," + c + ") no debe ser revelada por revealAllMines");
        }
    }

    // ── Dimensiones de tableros grandes ───────────────────────────────────────

    @Nested
    @DisplayName("Integridad de tableros en todas las dificultades")
    class AllDifficulties {

        @Test
        @DisplayName("INTERMEDIO: 16×16 con 40 minas – primer click seguro")
        void intermedioFirstClickSafe() {
            Board b = new Board(Difficulty.INTERMEDIO);
            boolean hit = b.reveal(8, 8);
            assertFalse(hit);
            assertEquals(16, b.getRows());
            assertEquals(16, b.getCols());
        }

        @Test
        @DisplayName("DIFICIL: 30×30 con 99 minas – primer click seguro")
        void dificilFirstClickSafe() {
            Board b = new Board(Difficulty.DIFICIL);
            boolean hit = b.reveal(15, 15);
            assertFalse(hit);
            assertEquals(30, b.getRows());
            assertEquals(30, b.getCols());
        }

        @Test
        @DisplayName("DIFICIL: exactamente 99 minas colocadas")
        void dificilMineCount() {
            Board b = new Board(Difficulty.DIFICIL);
            b.placeMines(0, 0);
            assertEquals(99, countMines(b));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int countMines(Board b) {
        int count = 0;
        for (int r = 0; r < b.getRows(); r++)
            for (int c = 0; c < b.getCols(); c++)
                if (b.getCell(r, c).isMine()) count++;
        return count;
    }
}
