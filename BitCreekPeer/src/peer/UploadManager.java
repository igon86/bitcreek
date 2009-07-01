package peer;

import condivisi.ErrorException;

/**
 * Thread per l'implementazione delle politiche di CHOKE/UNCHOKE delle
 * connessioni in upload di uno swarm
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class UploadManager implements Runnable {

    /* Costanti */
    /**
     * Costante che definisce il tempo di attesa tra
     * un' esecuzione e l' altra
     */
    private final int ATTESA = 10000;
    /** Numero max di upload */
    protected static final int UPLOADLIMIT = 4;

    /* Variabili d'istanza */
    /** Peer */
    private BitCreekPeer peer;
    /** Creek associato */
    private Creek c;

    /**
     * Costruttore
     * @param peer logica
     * @param c creek associato
     */
    public UploadManager(BitCreekPeer peer, Creek c) {
        this.peer = peer;
        this.c = c;
    }

    /**
     * Corpo del task
     */
    public void run() {
        
        System.out.println(Thread.currentThread().getName()+" UploadManager Avviato");
        
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            System.err.println("uploaderManager : sono stato interrotto");
        }
        int countOrdina = 0, random = -1;

        /**************************** il ciclo ********************/

        while (true) {
            if (Thread.interrupted()) {
                /* esco perchè mi hanno chiuso */
                break;
            }
            try {
                /* sort delle connessioni */
                random = this.c.ordinaConnessioni(countOrdina, random);
            } catch (ErrorException ex) {
                /* esco perchè mi hanno chiuso */
                break;
            }
            //contatto il tracker per avere una nuova lista
            
            /* dormo */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                /* esco perchè mi hanno chiuso */
                System.err.println("uploaderManager : sono stato interrotto");
                break;
            }
        }
        
        System.out.println(Thread.currentThread().getName()+" UploadManager Terminato ");
    }
}
