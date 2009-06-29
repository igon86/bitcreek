package server;

/**
 * Classe che contiene il numero di seeder e leecher
 * di uno swarm
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version bitCreekPeer 1.0
 */
public class NumPeer {

    /* Variabili d' istanza */
    /** Numero seeders */
    private int seeders;
    /** Numero leechers */
    private int leechers;

    /**
     * Costruttore
     * @param seeders
     * @param leechers
     */
    public NumPeer(int seeders, int leechers) {
        this.seeders = seeders;
        this.leechers = leechers;
    }

    /**
     * Restituisce il numero di seeders dello swarm
     * @return seeders
     */
    public int getSeeders() {
        return this.seeders;
    }

    /**
     * Restituisce il numero di leechers dello swarm
     * @return leechers
     */
    public int getLeechers() {
        return this.leechers;
    }
}
