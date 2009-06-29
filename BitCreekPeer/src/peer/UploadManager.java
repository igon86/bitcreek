package peer;

import condivisi.ErrorException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        FileOutputStream file = null;
        PrintStream output = null;
        try {
            file = new FileOutputStream(Thread.currentThread().getName() + ".log");
            output = new PrintStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        Creek.stampaDebug(output, "UploadManager del creek " + c.getName() + " avviato");
        try {
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Creek.stampaDebug(output, "UploadManager : sono stato ainterrotto");
        }

        int countOrdina = 0;
        int random = -1;
        /* ciclo infinito */
        while (true) {
            if (Thread.interrupted()) {
                break;
            }
            Creek.stampaDebug(output, "\n\nARRIVA l'UPLOAD MANAGER :D\n\n");
            try {
                /*sort delle connessioni*/
                random = this.c.ordinaConnessioni(countOrdina, random);
            } catch (ErrorException ex) {
                Creek.stampaDebug(output, "Esco perchè mi hanno chiuso : " + ex.getMessage());
                break;
            }
            Creek.stampaDebug(output, "HO FINITO :D \n\n");
            /* dormo */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                Creek.stampaDebug(output, "UploadManager : sono stato interrotto");
                break;
            }
        }
        System.out.println(Thread.currentThread().getName() + " MUOIO!!!!!!!");
    }
}
