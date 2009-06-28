package gui;

import condivisi.ErrorException;

/**
 * Classe che definisce una riga della tabella dei file
 * cercati
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitcreekPeer 1.0
 */
public class RigaTabellaCerca {

    /* Costanti */
    /** Definisce il valore di un K*/
    private final int K = 1024;

    /* Variabili d'istanza */
    /** Nome del file */
    private String nome;
    /** Dimensione del file */
    private String dimensione;
    /** Numero Seeder del file */
    private int numeroSeeders;
    /** Numero Leecher del file */
    private int numeroLeechers;

    /**
     * Costruttore
     * @param nome nome del file
     * @param dimensione dimensione del file
     * @param numseeders numero seeder
     * @param numleechers numero leecher
     * @throws condivisi.ErrorException se almeno un parametro non è valido
     */
    public RigaTabellaCerca(String nome, long dimensione, int numseeders, int numleechers) throws ErrorException {
        if (nome == null || dimensione <= 0 || numseeders < 0 || numleechers < 0) {
            throw new ErrorException("Param null");
        }
        this.nome = nome;
        this.dimensione = this.Dim(dimensione);
        this.numeroSeeders = numseeders;
        this.numeroLeechers = numleechers;

    }

    /**
     * Restituisce il nome del file
     * @return nome
     */
    public String getNome() {
        return this.nome;
    }

    /**
     * Restituisce la dimensione del file
     * @return dimensione
     */
    public String getDimensione() {
        return this.dimensione;
    }

    /**
     * Restituisce il numero di seeder del file
     * @return numeroSeeders
     */
    public int getSeeders() {
        return this.numeroSeeders;
    }

    /**
     * Restituisce il numero di leecher del file
     * @return numeroLeechers
     */
    public int getLeechers() {
        return this.numeroLeechers;
    }

    /**
     * Restituisce la dimensione in byte, Kbyte, Mbyte,...
     * a seconda della rappresentazione migliore
     * @param dimensione
     * @return stringa rappresentate la dimensione
     */
    private String Dim(long dimensione) {
        int i = 0;
        for (i = 0;; i++) {
            if ((dimensione / K) < 1) {
                break;
            } else {
                dimensione = dimensione / K;
            }
        }
        if (i == 1) {
            return dimensione + " Kbyte";
        }
        if (i == 2) {
            return dimensione + " Mbyte";
        }
        if (i == 3) {
            return dimensione + " Gbyte";
        }
        if (i == 4) {
            return dimensione + " Tbyte";
        }
        return dimensione + " byte";
    }
}
