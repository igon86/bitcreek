package peer;

/**
 *
 * @author andrea
 */
public class Uploader implements Runnable {

    Connessione conn;
    Creek c;

    public Uploader(Connessione conn, Creek c) {
        this.conn = conn;
        this.c = c;
    }

    public void run() {
        System.out.println("UPLOADER AVVIATO");
        int count = 0;
        while (true) {
            Messaggio m = this.conn.receiveUp();
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.REQUEST: {
                    Integer idPezzo = (Integer) m.getObj();
                    int pezzo = idPezzo.intValue();
                    System.out.println("THREAD " + Thread.currentThread().getName() + " Mando chunk con id " + pezzo);
                    //creo il chunk corretto da mandare
                    Chunk pezzoRichiesto = c.getChunk(pezzo);
                    Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                    //riempio il buffer
                    this.conn.sendUp(nuovo);
                    break;
                }
                case Messaggio.INTERESTED: {
                    System.out.println(Thread.currentThread().getName() + " L'altro peer e` interessato");
                    this.conn.setInteresseUp(true);
                    //FAKE A BESTIA
                    this.conn.sendUp(new Messaggio(Messaggio.UNCHOKE, null));
                    break;
                }
                case Messaggio.NOT_INTERESTED: {
                    System.out.println(Thread.currentThread().getName() + " L'altro peer e` interessato");
                    this.conn.setInteresseUp(false);
                    break;
                }
            }
            if (++count % 100 == 0) {
                System.out.println("\n\n SVUOTO LO STEAM DELL'UPLOADER \n");
                this.conn.ResetUp();
            }
        }

    }
}
