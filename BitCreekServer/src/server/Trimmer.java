package server;

import condivisi.Descrittore;
import java.util.TimerTask;

/**
 * Trimma la lista dei peer da quelli deceduti
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 */
public class Trimmer extends TimerTask {

    /* Variabili d'istanza */
    /** lista peer */
    ListaPeer lista;
    /** Descrittore associato */
    Descrittore d;

    /**
     * Costruttore
     * @param lista
     * @param d
     */
    public Trimmer(ListaPeer lista, Descrittore d) {
        this.lista = lista;
        this.d = d;
    }

    /**
     * Corpo del task
     */
    @Override
    public void run() {
        /* trimmo la lista dai peer deceduti*/
        NumPeer np = lista.trimPeer();
        /* aggiorno il descrittore */
        d.setNumSeeders(np.getSeeders());
        d.setNumLeechers(np.getLeechers());
    }
}
