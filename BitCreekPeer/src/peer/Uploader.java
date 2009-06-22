package peer;

/**
 *
 * @author andrea
 */
public class Uploader implements Runnable {

    private Connessione conn;
    private Creek c;
    private BitCreekPeer peer;
    private int puntatoreHave;

    public Uploader(Connessione conn, Creek c, int numPieces, BitCreekPeer peer) {
        this.conn = conn;
        this.c = c;
        this.puntatoreHave = numPieces;
        this.peer = peer;
    }

    public void run() {
        System.out.println("\n\n" + Thread.currentThread().getName() + " SONO UN NUOVO THREAD UPLOADER \n");
        int count = 0;
        while (true) {

            // controllo se sono stato chiuso
            if (this.conn.getTermina()) {
                // invio msg CLOSE al downloader associato
                Messaggio nuovo = new Messaggio(Messaggio.CLOSE, null);
                this.conn.sendUp(nuovo);
                break;
            }

            Messaggio m = this.conn.receiveUp();
            if (m == null) {
                System.out.println(Thread.currentThread().getName() + "Uploader: Timeout da connessione -> dormo un pochetto");
                continue;
            }
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.REQUEST: {
                    int pezzo;
                    int[] idPezzo = (int[]) m.getObj();
                    if (idPezzo.length == 1) {
                        pezzo = idPezzo[0];
                        System.out.println("THREAD " + Thread.currentThread().getName() + " Mando chunk con id " + pezzo);
                        //creo il chunk corretto da mandare
                        Chunk pezzoRichiesto = c.getChunk(pezzo);
                        Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                        //riempio il buffer
                        this.conn.sendUp(nuovo);
                        break;
                    } else {
                        //gestione endgame -> glieli mando tutti (se li possiedo)
                        for (int i = 0; i < idPezzo.length; i++) {
                            pezzo = idPezzo[i];
                            System.out.println("THREAD " + Thread.currentThread().getName() + " Mando chunk con id " + pezzo);
                            //creo il chunk corretto da mandare
                            Chunk pezzoRichiesto = c.getChunk(pezzo);
                            
                            if(pezzoRichiesto!=null){
                                Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                                this.conn.sendUp(nuovo);
                            }
                            
                            //riempio il buffer
                            
                        }
                        break;
                    }
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
                case Messaggio.CLOSE: {
                    System.out.println(Thread.currentThread().getName() + " Mi e` arrivata una close");
                    break;
                }
            }
            if (tipo == Messaggio.CLOSE) {
                break;
            }
            //CONTROLLO RESET STREAM
            if (++count % 100 == 0) {
                System.out.println("\n\n SVUOTO LO STEAM DELL'UPLOADER \n");
                this.conn.ResetUp();
            }
            //CONTROLLO/INVIO MESSAGGI DI HAVE
            while (this.c.getScaricati() > this.puntatoreHave) {
                int daNotificare = this.c.getScaricatiIndex(this.puntatoreHave);
                //ennino il wrapper automatico
                Messaggio have = new Messaggio(Messaggio.HAVE, daNotificare);
                this.puntatoreHave++;
                this.conn.sendUp(have);
                System.out.println(Thread.currentThread().getName() + " Invio la notifica del pezzo:  " + daNotificare);
            }
        }

        System.out.println(Thread.currentThread().getName() + "Uploader: sto morendo perche` me l'ha detto l'altro");
        // decremento il numero di connessioni
        peer.decrConnessioni();
    }
}
