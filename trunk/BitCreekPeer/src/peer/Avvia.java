package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import condivisi.NetRecord;
import java.util.ArrayList;

/**
 * Task che si occupa di avviare il download
 * dei file selezionati nella tabella della ricerca
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Avvia implements Runnable {

    /* Variabili d'istanza*/
    /** Peer */
    private BitCreekPeer peer;
    /** Array con gli indici dei file selezionati */
    private int[] array;

    /**
     * Costruttore
     * @param peer
     * @param array
     */
    public Avvia(BitCreekPeer peer, int[] array) {
        this.peer = peer;
        this.array = array;
    }

    /**
     * Corpo del task
     */
    public void run() {

        ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
        Creek c = null;
        Descrittore d = null;
        boolean presenza = false;

        for (int index : this.array) {

            /* questa provoca l'aggiornamento dell' interfaccia grafica aggiungendo il creek a arraydescr */
            try {
                d = peer.getCercati().get(index);
                c = new Creek(d, true, false);
                c.setToDo();
                presenza = peer.addCreek(c);
            } catch (ErrorException ex) {
                System.err.println("Avvia : ErrorException");
            }

            /* contatto gli altri e creo i thread solo se non ho già in download quel file */
            if (presenza) {

                lista = peer.contattaTracker(d);

                peer.aggiungiLista(c, lista);
                
                peer.addTask(new UploadManager(peer, c));
            }
            
            /* inutile continuare a ciclare se non posso creare connessioni */
            if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                break;
            }
        }
    }
}
