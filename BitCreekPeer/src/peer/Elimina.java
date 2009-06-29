package peer;

import condivisi.ErrorException;
import java.io.File;

/**
 * Task che si occupa di eliminare un creek
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Elimina implements Runnable {

    /* Variabili d' istanza */
    /** Nome del creek da eliminare */
    private String nome;
    /** Peer */
    private BitCreekPeer peer;

    /**
     * Costruttore
     * @param nome nome del creek da eliminare
     * @param peer logica
     * @throws condivisi.ErrorException se almeno un parametro è null
     */
    public Elimina(String nome, BitCreekPeer peer) throws ErrorException {
        if (nome == null || peer == null) {
            throw new ErrorException("Param null");
        }
        this.nome = nome;
        this.peer = peer;
    }

    /**
     * Corpo del task
     */
    public void run() {
        /* rimozione del creek */
        File f = null;
        boolean problema = false;
        boolean rimosso = false;
        try {
            rimosso = peer.deleteCreek(nome);
        } catch (ErrorException ex) {
            problema = true;
        }
        if (!problema && rimosso) {
            f = new File("./MetaInfo/" + nome + ".creek");
            f.delete();
        }
    }
}
