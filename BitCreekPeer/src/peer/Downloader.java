package peer;

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

        System.out.println("\n\n"+Thread.currentThread().getName() +" SONO UN NUOVO THREAD DOWNLOADER \n");
        //come prima cosa il thread verifica l'effettivo stato di interesse alla connessione

        if(c.interested(conn.getBitfied())){
            conn.setInteresseDown(true);
            conn.sendDown(new Messaggio(Messaggio.INTERESTED, null));
            System.out.println(Thread.currentThread().getName() + " Downloader : connessione interessante ");
        } else {
            conn.setInteresseDown(false);
            conn.sendDown(new Messaggio(Messaggio.NOT_INTERESTED, null));
            System.out.println(Thread.currentThread().getName() + " Downloader : ! connessione interessante");
        }

        int count=0;
        while(true){
            //come prima cosa controllo se e` terminato il download
            if(this.c.getStato() == false){
                        System.out.println("Ho terminato");
                        conn.sendDown(new Messaggio(Messaggio.CLOSE,null));
                        break;                  
            }
            Messaggio m = this.conn.receiveDown();

            // non cancellare : importante
            if ( m == null){
    
                    System.out.println(Thread.currentThread().getName() + " Downloader: TIMEOUT sulla receiveDOwn ho ricevuto null come messaggio -> dormo un po");
                    //dormo un pochetto e spero in BENE
                    //Thread.sleep(100);
                    continue;
                //} catch (InterruptedException ex) {
                  //  Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
                //}
            }
            //System.out.print(count+" ");
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.HAVE:{
                    
                    int piece = (Integer) m.getObj();
                    //this.conn.bitfield[piece] = true;
                    this.conn.getBitfied()[piece] = true;
                    System.out.println(Thread.currentThread().getName() + " Downloader : HAVE ricevuto di "+piece);
                    /* ma questo controllo serve ????..non va eseguito in ogni caso il corpo ??*/
                    if (this.conn.getInteresseDown() == false){
                        this.conn.setInteresseDown(this.c.interested(this.conn.getBitfied()));
                    }
                    break;
                }
                case Messaggio.CHOKE:{
                    System.out.println(Thread.currentThread().getName() + " Downloader : CHOCKE ricevuto");
                    this.conn.setStatoDown(Connessione.CHOKED);
                    break;
                }
                case Messaggio.UNCHOKE:{
                    System.out.println(Thread.currentThread().getName() + " Downloader : UNCHOKE ricevuto");
                    this.conn.setStatoDown(Connessione.UNCHOKED);
                    break;
                }
                case Messaggio.CHUNK:{
                    count++;
                    System.out.println(Thread.currentThread().getName()+" Ricevuto Messaggio CHUNK: "+((Chunk) m.getObj()).getOffset());
                    Chunk chunk = (Chunk) m.getObj();
                    c.scriviChunk(chunk);  
                    /* incremento il numero dei pezzi ricevuti settando la percentuale nel creek */
                    conn.incrDown();
                    c.settaPerc();
                    /* resetto il canale per evitare di impallare tutto -> nel downloader e` probabilmente inutile
                     */
                    if( count % 100 == 0){
                        System.out.println("\n\n SVUOTO LO STREAM DEL DOWNLOADER\n");
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
                    conn.sendDown(new Messaggio(Messaggio.REQUEST,new Integer(p.getId())));
                    System.out.println(Thread.currentThread().getName() + " Downloader : REQUEST inviato for chunk : "+p.getId());
                }else{
                    try {
                        System.out.println(Thread.currentThread().getName() + " getNext mi da null dormo un pochetto...");
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
                    }
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
        System.out.println(Thread.currentThread().getName() + " Downloader terminato");
    }

}

