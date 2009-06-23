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
    protected static final int UPLOADLIMIT = 4;

    /* Variabili d'istanza */
    private BitCreekPeer peer;
    private Creek c;

    public UploadManager(BitCreekPeer peer, Creek c){
        this.peer = peer;
        this.c= c;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName()+"UploadManager del creek "+c.getName()+" avviato");
        try {
            
            //giusto per dare il tempo di riprendersi
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Logger.getLogger(UploadManager.class.getName()).log(Level.SEVERE, null, ex);
        }

        /* ciclo infinito */
        while ( true ){
            System.out.println("\n\nARRIVA l'UPLOAD MANAGER :D\n\n");
            /*sort delle connessioni*/
            this.c.ordinaConnessioni();
            System.out.println("HO FINITO :D \n\n");
            /* dormo */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                System.out.println("UploadManager : sono stato interrotto");
            }
        }
    }

}
