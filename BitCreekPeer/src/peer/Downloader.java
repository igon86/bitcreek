
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
        System.out.println("DOWNLOADER DEL FILE "+c.getName()+" AVVIATO");
        
        //come prima cosa il thread verifica l'effettivo stato di interesse alla connessione
        
        if(c.interested(conn.getBitfied())){
            conn.setInteresseDown(true);
            conn.sendDown(new Messaggio(Messaggio.INTERESTED,null));
            System.out.println(Thread.currentThread().getName()+" DOWNLOADER:La connessione e` interessante");
        }
        else{
            conn.setInteresseDown(false);
            conn.sendDown(new Messaggio(Messaggio.NOT_INTERESTED,null));
            System.out.println(Thread.currentThread().getName()+" DOWNLOADER:La connessione non e` interessante");
        }
        int count=0;
        while(true){
            Messaggio m = this.conn.receiveDown();
            System.out.print(count+" ");
            
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.HAVE:{
                    System.out.println("Ricevuto Messaggio HAVE");
                    boolean[] bitfield = (boolean[]) m.getObj();
                    this.conn.setBitfield(bitfield);
                    if (this.conn.getInteresseDown()==false){
                        this.conn.setInteresseDown(this.c.interested(bitfield));
                    }
                    break;
                }
                case Messaggio.CHOKE:{
                    System.out.println(Thread.currentThread().getName()+" Ricevuto Messaggio CHOKE");
                    this.conn.setStatoDown(Connessione.CHOKED);
                    break;
                }
                case Messaggio.UNCHOKE:{
                    System.out.println(Thread.currentThread().getName()+" Ricevuto Messaggio UNCHOKE");
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
                System.out.println("INVIATO Messaggio REQUEST: "+count);
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
