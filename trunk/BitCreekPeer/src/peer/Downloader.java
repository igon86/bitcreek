
package peer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrea
 */
public class Downloader implements Runnable{

    
    private Creek c;
    private Socket s;
    private Connessione conn;
    private boolean pendingRequest;
    
    public Downloader(Creek c,Connessione conn){
        this.c = c;
        this.conn = conn;
        this.pendingRequest = false;
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
        
        //l'ho SCRITTO DAVVEROOO??
        Creek.stampaSbrodolina(output,"\n\n SONO UN NUOVO THREAD DOWNLOADER DA  "+this.conn.getIPVicino().getHostAddress()+" , " +this.conn.getPortaVicino() +"\n");
        //come prima cosa il thread verifica l'effettivo stato di interesse alla connessione
        
        if(c.interested(conn.getBitfied())){
            conn.setInteresseDown(true);
            Creek.stampaSbrodolina(output," Downloader : connessione interessante ");
            conn.sendDown(new Messaggio(Messaggio.INTERESTED, null));
            Creek.stampaSbrodolina(output," Downloader : connessione interessante COMUNICATO ALL'UPLOADER ");
        } else {
            conn.setInteresseDown(false);
            conn.sendDown(new Messaggio(Messaggio.NOT_INTERESTED, null));
            Creek.stampaSbrodolina(output, " Downloader : ! connessione interessante");
        }

        int count=0;
        while(true){
            output.print("STO PER FARE LA RECEIVE......");
            Messaggio m = this.conn.receiveDown();
            output.println(".....FATTA");
            
            if ( m == null){
                Creek.stampaSbrodolina(output,"Continuo perchè il 'canale' è null");
                continue;
            }
            //System.out.print(count+" ");
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.HAVE:{
                    Creek.stampaSbrodolina(output," Downloader : HAVE ricevuto");
                    boolean[] bitfield = (boolean[]) m.getObj();
                    this.conn.setBitfield(bitfield);
                    
                    /* ma questo controllo serve ????..non va eseguito in ogni caso il corpo ??*/
                    if (this.conn.getInteresseDown() == false){
                        this.conn.setInteresseDown(this.c.interested(bitfield));
                    }
                    break;
                }
                case Messaggio.CHOKE:{
                    Creek.stampaSbrodolina(output," Downloader : CHOCKE ricevuto");
                    this.conn.setStatoDown(Connessione.CHOKED);
                    break;
                }
                case Messaggio.UNCHOKE:{
                    Creek.stampaSbrodolina(output," Downloader : UNCHOKE ricevuto");
                    this.conn.setStatoDown(Connessione.UNCHOKED);
                    break;
                }
                case Messaggio.CHUNK:{
                    count++;
                    Creek.stampaSbrodolina(output," Ricevuto Messaggio CHUNK: "+((Chunk) m.getObj()).getOffset());
                    Chunk chunk = (Chunk) m.getObj();
                    c.scriviChunk(chunk);
                    /* incremento il numero dei pezzi ricevuti settando la percentuale nel creek */
                    conn.incrDown();
                    c.settaPerc();
                    /* resetto il canale per evitare di impallare tutto -> nel downloader e` probabilmente inutile
                     in ogni caso questo pezzo di codice non viene eseguito sempre per qualche motivo */
                    if( count % 100 == 0){
                        Creek.stampaSbrodolina(output,"\n\n SVUOTO LO STREAM DEL DOWNLOADER\n");
                        conn.ResetDown();
                    }
                    /*  controllare lo SHA del pezzo ------> da fare !!!!  */

                    this.pendingRequest = false;
                }
            }
            if(! pendingRequest){
                PIO p = c.getNext(this.conn.getBitfied());
                if(p != null){
                    //System.out.println("Downloader : Sto per fare sendDown per chè p != null");
                    output.print("STO PER MANDARE UNA REQUEST.......");
                    conn.sendDown(new Messaggio(Messaggio.REQUEST,new Integer(p.getId())));
                    Creek.stampaSbrodolina(output,"Downloader : REQUEST inviato for chunk : "+p.getId());
                }else{
                    Creek.stampaSbrodolina(output,"vediamo se esco dal while");
                    break;
                }
            }
            try {
                //}
                //TEMPORANEO!!!
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        /**
        try {
            this.c.raf.close();
        } catch (IOException ex) {
            Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
        }
        */
        Creek.stampaSbrodolina(output," Downloader terminato");
    }
    
}
