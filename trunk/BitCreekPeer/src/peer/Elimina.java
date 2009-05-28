package peer;

import condivisi.ErrorException;
import java.io.File;

/**
 * Task che si occupa di eliminare un creek
 * @author Bandettini
 */
public class Elimina implements Runnable {

    /* Variabili d' istanza */

    String nome;
    BitCreekPeer peer;

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

        /* comunico al server che non ci sono più,dico ai peer di chiudere */

        // ---------> da fare !!!!

        /* rimozione del creek */
        File f = null;
        boolean problema = false;
        boolean rimosso = false;

        try {
            rimosso = peer.deleteCreek(nome);
        } catch (ErrorException ex) {
            problema = true;
        }
        if(!problema && rimosso){
            f = new File("./MetaInfo/" + nome + ".creek");
            f.delete();
        }
    }
}
