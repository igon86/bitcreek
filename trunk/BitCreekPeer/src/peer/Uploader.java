package peer;

/**
 *
 * @author andrea
 */
public class Uploader implements Runnable {

    Connessione conn;
    Creek c;
    int puntatoreHave;

    public Uploader(Connessione conn, Creek c, int numPieces) {
        this.conn = conn;
        this.c = c;
        this.puntatoreHave = numPieces;
    }

    
    public void run() {
        System.out.println("\n\n"+Thread.currentThread().getName() +" SONO UN NUOVO THREAD UPLOADER \n");
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
                    System.out.println(Thread.currentThread().getName() + " L'altro peer NON e` interessato");
                    this.conn.setInteresseUp(false);
                    break;
                }
            }
            //CONTROLLO RESET STREAM
            if (++count % 100 == 0) {
                System.out.println("\n\n SVUOTO LO STEAM DELL'UPLOADER \n");
                this.conn.ResetUp();
            }
            //CONTROLLO/INVIO MESSAGGI DI HAVE
            System.out.println(Thread.currentThread().getName()+"this.c.getScaricati: "+this.c.getScaricati() +" VS this.puntatoreHave: "+ this.puntatoreHave);
            while (this.c.getScaricati() > this.puntatoreHave){
                int daNotificare = this.c.getScaricatiIndex(this.puntatoreHave);
                //ennino il wrapper automatico
                Messaggio have = new Messaggio(Messaggio.HAVE,daNotificare);
                this.puntatoreHave++;
                this.conn.sendUp(have);
                System.out.println(Thread.currentThread().getName()+ " Invio la notifica del pezzo:  "+daNotificare);
            }
        }

    }
}
