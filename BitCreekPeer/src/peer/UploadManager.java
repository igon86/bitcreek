package peer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
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
        this.c = c;
    }

    public void run() {

        //INIZIALIZZAZIONE STAMPA DI DEBUG
        FileOutputStream file = null;
        PrintStream output = null;
        try {
            file = new FileOutputStream(Thread.currentThread().getName() + ".log");
            output = new PrintStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        Creek.stampaDebug(output,"UploadManager del creek "+c.getName()+" avviato");
        try {
            
            //giusto per dare il tempo di riprendersi
            Thread.sleep(100);
        } catch (InterruptedException ex) {
            Creek.stampaDebug(output,"UploadManager : sono stato ainterrotto");
        }

        /* ciclo infinito */
        while ( true ){
            Creek.stampaDebug(output,"\n\nARRIVA l'UPLOAD MANAGER :D\n\n");
            /*sort delle connessioni*/
            this.c.ordinaConnessioni();
            Creek.stampaDebug(output,"HO FINITO :D \n\n");
            /* dormo */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                Creek.stampaDebug(output,"UploadManager : sono stato interrotto");
            }
        }
    }

}
