
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
        
        //come prima cosa il thread verifica l'effettivo stato di interesse alla connessione
        
        if(c.interested(conn.getBitfied())){
            conn.setInteresseDown(true);
            conn.sendDown(new Messaggio(Messaggio.INTERESTED,null));
            System.out.println(Thread.currentThread().getName() + " Downloader : connessione interessante ");
        }
        else{
            conn.setInteresseDown(false);
            conn.sendDown(new Messaggio(Messaggio.NOT_INTERESTED,null));
            System.out.println(Thread.currentThread().getName() + " Downloader : ! connessione interessante");
        }
        int count=0;
        while(true){
            Messaggio m = this.conn.receiveDown();
            System.out.print(count+" ");
            
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.HAVE:{
                    System.out.println(Thread.currentThread().getName() + " Downloader : HAVE ricevuto");
                    boolean[] bitfield = (boolean[]) m.getObj();
                    this.conn.setBitfield(bitfield);
                    if (this.conn.getInteresseDown()==false){
                        this.conn.setInteresseDown(this.c.interested(bitfield));
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
                    System.out.println(Thread.currentThread().getName()+" Ricevuto Messaggio CHUNK: "+count);
                    Chunk chunk = (Chunk) m.getObj();
                    //c.scriviChunk(chunk);
                    this.pendingRequest = false;
                }
            }
            //if(! pendingRequest){
                PIO p = c.getNext(this.conn.getBitfied());
                conn.sendDown(new Messaggio(Messaggio.REQUEST,new Integer(p.getId())));
                System.out.println(Thread.currentThread().getName() + " Downloader : REQUEST inviato");
            try {
                //}
                //TEMPORANEO!!!
                Thread.sleep(10);
            } catch (InterruptedException ex) {
                Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
