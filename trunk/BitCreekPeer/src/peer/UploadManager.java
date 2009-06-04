
package peer;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Thread per l'implementazione delle politiche di CHOKE/UNCHOKE delle
 * connessioni in upload di uno swarm
 * @author andrea
 */
public class UploadManager implements Runnable{
    
    /* Costanti */
    private final int ATTESA = 10000;

    /* Variabili d'istanza */
    private BitCreekPeer peer;
    private Creek c;
    
    public UploadManager(BitCreekPeer peer, Creek c){
        this.peer = peer;
        this.c= c;
    }

    public void run() {
        System.out.println("UploadManager del creek "+c.getName()+" avviato");
        /* ciclo infinito */
        while ( true ){
            /* dormo */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                System.out.println("UploadManager : sono stato interrotto");
            }
            /* invio i msg di HAVE*/
            System.out.println("UploadManager : Invio msg di HAVE su tutte le connessioni");
            c.inviaHave();
        }
    }

}
