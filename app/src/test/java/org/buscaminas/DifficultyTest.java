package org.buscaminas;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
 
import static org.junit.jupiter.api.Assertions.*;
 
@DisplayName("Difficulty – configuración de cada modo de juego")
class DifficultyTest {
 
    // ── Valores concretos de cada dificultad ─────────────────────────────────
 
    @Test
    @DisplayName("NOVATO: 10×10, 10 minas, multiplicador 1.0")
    void novato_values() {
        assertEquals("Novato",  Difficulty.NOVATO.getDisplayName());
        assertEquals(10,        Difficulty.NOVATO.getRows());
        assertEquals(10,        Difficulty.NOVATO.getCols());
        assertEquals(10,        Difficulty.NOVATO.getMines());
        assertEquals(1.0,       Difficulty.NOVATO.getScoreMultiplier(), 0.001);
    }
 
    @Test
    @DisplayName("INTERMEDIO: 16×16, 40 minas, multiplicador 1.5")
    void intermedio_values() {
        assertEquals("Intermedio", Difficulty.INTERMEDIO.getDisplayName());
        assertEquals(16,           Difficulty.INTERMEDIO.getRows());
        assertEquals(16,           Difficulty.INTERMEDIO.getCols());
        assertEquals(40,           Difficulty.INTERMEDIO.getMines());
        assertEquals(1.5,          Difficulty.INTERMEDIO.getScoreMultiplier(), 0.001);
    }
 
    @Test
    @DisplayName("DIFICIL: 30×30, 99 minas, multiplicador 2.5")
    void dificil_values() {
        assertEquals("Difícil", Difficulty.DIFICIL.getDisplayName());
        assertEquals(30,        Difficulty.DIFICIL.getRows());
        assertEquals(30,        Difficulty.DIFICIL.getCols());
        assertEquals(99,        Difficulty.DIFICIL.getMines());
        assertEquals(2.5,       Difficulty.DIFICIL.getScoreMultiplier(), 0.001);
    }
 
    // ── Invariantes para todas las dificultades ───────────────────────────────
 
    @ParameterizedTest(name = "{0}: las minas deben caber en el tablero")
    @EnumSource(Difficulty.class)
    @DisplayName("Las minas nunca superan el total de casillas")
    void minesDoNotExceedTotalCells(Difficulty d) {
        int totalCells = d.getRows() * d.getCols();
        assertTrue(d.getMines() < totalCells,
            "Las minas (" + d.getMines() + ") deben ser menores que las casillas totales (" + totalCells + ")");
    }
 
    @ParameterizedTest(name = "{0}: multiplicador > 0")
    @EnumSource(Difficulty.class)
    @DisplayName("El multiplicador de puntuación siempre es positivo")
    void scoreMultiplierPositive(Difficulty d) {
        assertTrue(d.getScoreMultiplier() > 0);
    }
 
    @ParameterizedTest(name = "{0}: filas y columnas > 0")
    @EnumSource(Difficulty.class)
    @DisplayName("Filas y columnas son siempre positivas")
    void rowsAndColsPositive(Difficulty d) {
        assertTrue(d.getRows() > 0);
        assertTrue(d.getCols() > 0);
    }
 
    @Test
    @DisplayName("El multiplicador crece con la dificultad")
    void scoreMultiplierIncreases() {
        assertTrue(Difficulty.NOVATO.getScoreMultiplier()
                 < Difficulty.INTERMEDIO.getScoreMultiplier());
        assertTrue(Difficulty.INTERMEDIO.getScoreMultiplier()
                 < Difficulty.DIFICIL.getScoreMultiplier());
    }
 
    @Test
    @DisplayName("El número de minas crece con la dificultad")
    void minesIncrease() {
        assertTrue(Difficulty.NOVATO.getMines()     < Difficulty.INTERMEDIO.getMines());
        assertTrue(Difficulty.INTERMEDIO.getMines() < Difficulty.DIFICIL.getMines());
    }
 
    @Test
    @DisplayName("Existen exactamente 3 dificultades")
    void exactlyThreeDifficulties() {
        assertEquals(3, Difficulty.values().length);
    }
}