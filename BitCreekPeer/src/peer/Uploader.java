package peer;

/**
 * Task che si occupa di far scaricare un file
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Uploader implements Runnable {

    /* Variabili d' istanza */
    /** Connessione dove fare upload */
    private Connessione conn;
    /** Creek associato */
    private Creek c;
    /** Peer */
    private BitCreekPeer peer;
    /** Mah */
    private int puntatoreHave;
    /** Contatore */
    private int failed;

    /**
     * Costruttore
     * @param conn connessione dove fare upload
     * @param c creek associato
     * @param numPieces
     * @param peer
     */
    public Uploader(Connessione conn, Creek c, int numPieces, BitCreekPeer peer) {
        this.conn = conn;
        this.c = c;
        this.puntatoreHave = numPieces;
        this.peer = peer;
        this.failed = 0;
    }

    /**
     * Corpo del task
     */
    public void run() {
        
        System.out.println(Thread.currentThread().getName()+" Uploader Avviato");
        
        int count = 0;

        /************************* il ciclo ************************/
        while (true) {

            /* controllo se devo terminare */
            if (this.conn.getTermina()) {
                /* invio msg CLOSE al downloader associato */
                this.conn.sendUp(new Messaggio(Messaggio.CLOSE, null));
                break;
            }

            Messaggio m = this.conn.receiveUp();
            if (m == null) {
                this.failed++;
                continue;
            } else {
                this.failed = 0;
            }
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.REQUEST: {
                    /* se posso uploadare invio il chunk richiesto */
                    this.conn.possoUploadare();
                    int pezzo;
                    int[] idPezzo = (int[]) m.getObj();
                    if (idPezzo.length == 1) {
                        pezzo = idPezzo[0];
                        Chunk pezzoRichiesto = c.getChunk(pezzo);
                        if (pezzoRichiesto != null) {
                            Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                            this.conn.sendUp(nuovo);
                        }
                        break;
                    } else {
                        /* gestione endgame : glieli mando tutti (se li possiedo) */
                        for (int i = 0; i < idPezzo.length; i++) {
                            pezzo = idPezzo[i];
                            Chunk pezzoRichiesto = c.getChunk(pezzo);
                            if (pezzoRichiesto != null) {
                                Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                                this.conn.sendUp(nuovo);
                            }
                        }
                        break;
                    }
                }
                /* per questi msg : aggiorno stato ed invio risposte attinenti al protocollo */
                case Messaggio.INTERESTED: {
                    this.conn.setInteresseUp(true);
                    this.conn.sendUp(new Messaggio(Messaggio.UNCHOKE, null));
                    break;
                }
                case Messaggio.NOT_INTERESTED: {
                    this.conn.setInteresseUp(false);
                    break;
                }
                case Messaggio.CLOSE: {
                    /* devo chiudere */
                    break;
                }
            }
            if (tipo == Messaggio.CLOSE) {
                break;
            }
            /* resetto lo stream */
            if (++count % 100 == 0) {
                this.conn.ResetUp();
            }
            /* eseguo il controllo sui pezzi che ho ed, eventualmente, invio msh di HAVE*/
            int dimHave = this.c.getScaricati() - this.puntatoreHave;
            if (dimHave > 0) {
                int[] newHave = new int[dimHave];
                for (int i = 0; i < dimHave; i++) {
                    int daNotificare = this.c.getScaricatiIndex(this.puntatoreHave);
                    newHave[i] = daNotificare;
                    this.puntatoreHave++;
                }
                Messaggio have = new Messaggio(Messaggio.HAVE, newHave);
                this.conn.sendUp(have);
            }
        }
        /* uscita */
        /* decremento il numero di connessioni */
        peer.decrConnessioni();
        /* decremento il numero dei peer */
        c.decrPeer();
        
        System.out.println(Thread.currentThread().getName()+" Uploader Terminato");
    }
}
