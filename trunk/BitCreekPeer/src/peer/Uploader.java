package peer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrea
 */
public class Uploader implements Runnable {

    private Connessione conn;
    private Creek c;

    public Uploader(Connessione conn, Creek c) {
        this.conn = conn;
        this.c = c;
    }

    public void run() {
        FileOutputStream file;
        PrintStream output = null;
        try {
            file = new FileOutputStream(Thread.currentThread().getName() + ".log");
            output = new PrintStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }
        Creek.stampaSbrodolina(output,"\n\n"+Thread.currentThread().getName() +" SONO UN NUOVO THREAD UPLOADER  VERSO  "+this.conn.getIPVicino().getHostAddress()+" , " +this.conn.getPortaVicino() +"\n");
        
        int count = 0;
        while (true) {
            output.print("FACCIO RECEIVE....");
            Messaggio m = this.conn.receiveUp();
            output.println("...FATTA");
            // fondamentale !!!! --> non cancellare
            if ( m == null){
                Creek.stampaSbrodolina(output,"Continuo perchè il 'canale' è null");
                continue;
            }
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.REQUEST: {
                    Integer idPezzo = (Integer) m.getObj();
                    int pezzo = idPezzo.intValue();
                    Creek.stampaSbrodolina(output,"RICEVUTA REQUEST  con id" + pezzo);
                    //creo il chunk corretto da mandare
                    Chunk pezzoRichiesto = c.getChunk(pezzo);
                    Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                    Creek.stampaSbrodolina(output,"Messaggio creato");
                    //riempio il buffer
                    this.conn.sendUp(nuovo);
                    Creek.stampaSbrodolina(output,"FATTA LA SENDUP");
                    break;
                }
                case Messaggio.INTERESTED: {
                    Creek.stampaSbrodolina(output, Thread.currentThread().getName() + " L'altro peer e` interessato");
                    this.conn.setInteresseUp(true);
                    //FAKE A BESTIA
                    this.conn.sendUp(new Messaggio(Messaggio.UNCHOKE, null));
                    break;
                }
                case Messaggio.NOT_INTERESTED: {
                    Creek.stampaSbrodolina(output,Thread.currentThread().getName() + " L'altro peer NON e` interessato");
                    this.conn.setInteresseUp(false);
                    break;
                }
            }
            if (++count % 100 == 0) {
                Creek.stampaSbrodolina(output,"\n\n SVUOTO LO STEAM DELL'UPLOADER \n");
                this.conn.ResetUp();
            }
        }

    }
}
