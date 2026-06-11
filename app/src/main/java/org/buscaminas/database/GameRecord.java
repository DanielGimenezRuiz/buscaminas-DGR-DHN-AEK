package org.buscaminas.database;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Representa una fila de la tabla <b>partidas</b>.
 * Se usa tanto para insertar nuevas partidas como para leer el historial.
 */
public class GameRecord {

    
    private long          id;               
    private Long          jugadorId;        
    private String        alias;            
    private String        dificultad;       
    private String        resultado;        
    private int           tiempoSegundos;
    private int           casillasReveladas;
    private int           banderasColocadas;
    private int           puntuacion;
    private int           filas;
    private int           columnas;
    private int           totalMinas;
    private LocalDateTime jugadaEn;

    private static final DateTimeFormatter FMT =
        DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public GameRecord(Long jugadorId,
                      String dificultad, String resultado,
                      int tiempoSegundos, int casillasReveladas,
                      int banderasColocadas, int puntuacion,
                      int filas, int columnas, int totalMinas) {
        this.jugadorId         = jugadorId;
        this.dificultad        = dificultad;
        this.resultado         = resultado;
        this.tiempoSegundos    = tiempoSegundos;
        this.casillasReveladas = casillasReveladas;
        this.banderasColocadas = banderasColocadas;
        this.puntuacion        = puntuacion;
        this.filas             = filas;
        this.columnas          = columnas;
        this.totalMinas        = totalMinas;
    }

    public GameRecord(long id, Long jugadorId, String alias,
                      String dificultad, String resultado,
                      int tiempoSegundos, int casillasReveladas,
                      int banderasColocadas, int puntuacion,
                      int filas, int columnas, int totalMinas,
                      LocalDateTime jugadaEn) {
        this(jugadorId, dificultad, resultado,
             tiempoSegundos, casillasReveladas, banderasColocadas,
             puntuacion, filas, columnas, totalMinas);
        this.id       = id;
        this.alias    = alias;
        this.jugadaEn = jugadaEn;
    }

    public long          getId()                  { return id; }
    public void          setId(long id)           { this.id = id; }

    public Long          getJugadorId()           { return jugadorId; }

    public String        getAlias()               { return alias != null ? alias : "Anónimo"; }
    public void          setAlias(String alias)   { this.alias = alias; }

    public String        getDificultad()          { return dificultad; }
    public String        getResultado()           { return resultado; }

    public int           getTiempoSegundos()      { return tiempoSegundos; }
    public int           getCasillasReveladas()   { return casillasReveladas; }
    public int           getBanderasColocadas()   { return banderasColocadas; }
    public int           getPuntuacion()          { return puntuacion; }

    public int           getFilas()               { return filas; }
    public int           getColumnas()            { return columnas; }
    public int           getTotalMinas()          { return totalMinas; }

    public LocalDateTime getJugadaEn()            { return jugadaEn; }


    public String getTiempoFormateado() {
        return String.format("%02d:%02d", tiempoSegundos / 60, tiempoSegundos % 60);
    }

    public String getFechaFormateada() {
        return jugadaEn != null ? jugadaEn.format(FMT) : "–";
    }

    public boolean esVictoria() { return "VICTORIA".equals(resultado); }

    @Override
    public String toString() {
        return String.format(
            "GameRecord[id=%d alias=%s diff=%s result=%s score=%d time=%s fecha=%s]",
            id, getAlias(), dificultad, resultado,
            puntuacion, getTiempoFormateado(), getFechaFormateada());
    }
}