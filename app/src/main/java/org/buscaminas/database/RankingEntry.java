package org.buscaminas.database;



public class RankingEntry {

    private final String  alias;
    private final String  dificultad;
    private final int     mejorPuntuacion;
    private final Integer mejorTiempo;      
    private final int     partidasTotales;
    private final int     victorias;

    public RankingEntry(String alias, String dificultad,
                        int mejorPuntuacion, Integer mejorTiempo,
                        int partidasTotales, int victorias) {
        this.alias           = alias;
        this.dificultad      = dificultad;
        this.mejorPuntuacion = mejorPuntuacion;
        this.mejorTiempo     = mejorTiempo;
        this.partidasTotales = partidasTotales;
        this.victorias       = victorias;
    }

    public String  getAlias()            { return alias; }
    public String  getDificultad()       { return dificultad; }
    public int     getMejorPuntuacion()  { return mejorPuntuacion; }
    public Integer getMejorTiempo()      { return mejorTiempo; }
    public int     getPartidasTotales()  { return partidasTotales; }
    public int     getVictorias()        { return victorias; }

    public int getWinRate() {
        return partidasTotales == 0
            ? 0
            : (int) Math.round(100.0 * victorias / partidasTotales);
    }

    public String getMejorTiempoFormateado() {
        if (mejorTiempo == null) return "–";
        return String.format("%02d:%02d", mejorTiempo / 60, mejorTiempo % 60);
    }

    public String getDificultadLabel() {
        return switch (dificultad) {
            case "NOVATO"      -> "Novato";
            case "INTERMEDIO"  -> "Intermedio";
            case "DIFICIL"     -> "Difícil";
            default            -> dificultad;
        };
    }

    @Override
    public String toString() {
        return String.format(
            "RankingEntry[alias=%s diff=%s score=%d winRate=%d%%]",
            alias, dificultad, mejorPuntuacion, getWinRate());
    }
}