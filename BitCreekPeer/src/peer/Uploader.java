
package peer;

/**
 *
 * @author andrea
 */
public class Uploader implements Runnable{
    
    Connessione conn;
    
    public Uploader(Connessione conn){
        this.conn = conn;
    }

    public void run() {
        System.out.println("UPLOADER AVVIATO");
        while(true){
            Messaggio m = this.conn.receiveUp();
            int tipo = m.getTipo();
            switch (tipo){
                case Messaggio.REQUEST:{
                    Integer idPezzo = (Integer) m.getObj();
                    int pezzo = idPezzo.intValue();
                    System.out.println("THREAD "+Thread.currentThread().getName()+" Mando chunk con id"+pezzo);
                    this.conn.sendUp(new Messaggio(Messaggio.CHUNK,new Chunk(null,idPezzo,BitCreekPeer.DIMBLOCCO)));
                    break;
                }
                case Messaggio.INTERESTED:{
                    System.out.println(Thread.currentThread().getName()+" L'altro peer e` interessato");
                    this.conn.setInteresseUp(true);
                    //FAKE A BESTIA
                    this.conn.sendUp(new Messaggio(Messaggio.UNCHOKE,null));
                    break;
                }
                case Messaggio.NOT_INTERESTED:{
                    System.out.println(Thread.currentThread().getName()+" L'altro peer e` interessato");
                    this.conn.setInteresseUp(false);
                    break;
                }
            }
        }
        
    }

}
