package org.buscaminas;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
 
import static org.junit.jupiter.api.Assertions.*;
 
@DisplayName("Cell – estado de una casilla")
class CellTest {
 
    private Cell cell;
 
    @BeforeEach
    void setUp() {
        cell = new Cell();
    }
 
    // ── Estado inicial ────────────────────────────────────────────────────────
 
    @Test
    @DisplayName("Una celda nueva no es mina, no está revelada y no tiene bandera")
    void newCell_defaultState() {
        assertFalse(cell.isMine(),     "no debe ser mina por defecto");
        assertFalse(cell.isRevealed(), "no debe estar revelada por defecto");
        assertFalse(cell.isFlagged(),  "no debe tener bandera por defecto");
        assertEquals(0, cell.getAdjacentMines(), "adyacentes = 0 por defecto");
    }
 
    // ── Mina ──────────────────────────────────────────────────────────────────
 
    @Test
    @DisplayName("setMine(true) marca la celda como mina")
    void setMine_true() {
        cell.setMine(true);
        assertTrue(cell.isMine());
    }
 
    @Test
    @DisplayName("setMine(false) desmarca la celda como mina")
    void setMine_false() {
        cell.setMine(true);
        cell.setMine(false);
        assertFalse(cell.isMine());
    }
 
    // ── Revelado ──────────────────────────────────────────────────────────────
 
    @Test
    @DisplayName("setRevealed(true) marca la celda como revelada")
    void setRevealed_true() {
        cell.setRevealed(true);
        assertTrue(cell.isRevealed());
    }
 
    @Test
    @DisplayName("setRevealed(false) desmarca la celda como revelada")
    void setRevealed_false() {
        cell.setRevealed(true);
        cell.setRevealed(false);
        assertFalse(cell.isRevealed());
    }
 
    // ── Bandera ───────────────────────────────────────────────────────────────
 
    @Test
    @DisplayName("setFlagged(true) coloca la bandera")
    void setFlagged_true() {
        cell.setFlagged(true);
        assertTrue(cell.isFlagged());
    }
 
    @Test
    @DisplayName("setFlagged(false) quita la bandera")
    void setFlagged_false() {
        cell.setFlagged(true);
        cell.setFlagged(false);
        assertFalse(cell.isFlagged());
    }
 
    // ── Minas adyacentes ──────────────────────────────────────────────────────
 
    @Test
    @DisplayName("setAdjacentMines guarda el valor correctamente")
    void setAdjacentMines() {
        cell.setAdjacentMines(5);
        assertEquals(5, cell.getAdjacentMines());
    }
 
    @Test
    @DisplayName("setAdjacentMines acepta el valor máximo posible (8)")
    void setAdjacentMines_max() {
        cell.setAdjacentMines(8);
        assertEquals(8, cell.getAdjacentMines());
    }
 
    @Test
    @DisplayName("setAdjacentMines acepta cero")
    void setAdjacentMines_zero() {
        cell.setAdjacentMines(3);
        cell.setAdjacentMines(0);
        assertEquals(0, cell.getAdjacentMines());
    }
}