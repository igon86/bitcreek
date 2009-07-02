package peer;

import condivisi.ErrorException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task che si occupa di scaricare un file
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Downloader implements Runnable {

    /* Costanti */
    /** Definisce la costante ENDGAME */
    protected static final int ENDGAME = -1;
    /** Definisce la costante PREQ */
    private static final int PREQ = -1;
    /** Definisce la costante MAXFAILURE */
    protected static final int MAXFAILURE = 10;
    /* Variabili d' istanza */
    /** Creek del file da scaricare */
    private Creek c;
    /** Connessione da scaricare */
    private Connessione conn;
    /** Peer */
    private BitCreekPeer peer;
    /** flag che indica se ci sono richieste pendenti */
    private int pendingRequest;
    /** Flag che indica che siamo nello stato ENDGAME */
    private boolean endgame;
    /** Contatore */
    private int failed;

    /**
     * Costruttore
     * @param c creek del file
     * @param conn connessione dove fare download
     * @param peer logica
     */
    public Downloader(Creek c, Connessione conn, BitCreekPeer peer) {
        this.c = c;
        this.conn = conn;
        this.peer = peer;
        this.pendingRequest = PREQ;
        this.endgame = false;
        this.failed = 0;
    }

    /**
     * Corpo del task
     */
    public void run() {

        System.out.println(Thread.currentThread().getName() + " Downloader Avviato da " + this.conn.getIPVicino() + ", " + this.conn.getPortaVicino());

        /* setto l' interesse per la connessione */
        if (c.interested(conn.getBitfield())) {
            conn.setInteresseDown(true);
            conn.sendDown(new Messaggio(Messaggio.INTERESTED, null));
        } else {
            conn.setInteresseDown(false);
            conn.sendDown(new Messaggio(Messaggio.NOT_INTERESTED, null));
        }

        int count = 0; // utilizzato per lo svuotamento dello buffer degli stream
        PIO p = null;

        /*********************** il ciclo ********************/
        while (true) {
            /* controllo se devo terminare */
            if (!this.c.getStato() || this.conn.getTermina()) {
                if (pendingRequest > PREQ) {
                    this.c.liberaPio(pendingRequest); //rilascio PIO
                }
                /* invio CLOSE ed esco */
                conn.sendDown(new Messaggio(Messaggio.CLOSE, null));
                break;
            }

            /* ricezione messaggio */
            Messaggio m = this.conn.receiveDown();
            if (m == null) {
                this.failed++;
                continue;
            } else {
                this.failed = 0;
            }
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.HAVE: {
                    /* aggiorno i bitfield e di conseguenza l' interesse per la connessione */
                    this.conn.setArrayBitfield((int[]) m.getObj());
                    if (this.conn.getInteresseDown() == false) {
                        this.conn.setInteresseDown(this.c.interested(this.conn.getBitfield()));
                    }
                    break;
                }
                case Messaggio.CHOKE: {
                    /* sono stato soffocato */
                    this.conn.setStatoDown(Connessione.CHOKED);
                    break;
                }
                case Messaggio.UNCHOKE: {
                    /* sono in grado di scaricare se voglio */
                    this.conn.setStatoDown(Connessione.UNCHOKED);
                    break;
                }
                case Messaggio.CLOSE: {
                    /* devo uscire */
                    break;
                }
                case Messaggio.CHUNK: {
                    /* ho ricevuto un pezzo : lo scrivo su file se sha è ok */
                    count++;
                    Chunk chunk = (Chunk) m.getObj();
                    int offset = chunk.getOffset();
                    try {
                        /* controllo SHA */
                        byte[] stringa = c.getHash();
                        byte[] sha = new byte[Creek.DIMSHA];
                        int i = Creek.DIMSHA * offset;
                        for (int j = 0; j < Creek.DIMSHA; j++) {
                            sha[j] = stringa[i + j];
                        }
                        MessageDigest md = null;
                        try {
                            md = MessageDigest.getInstance("SHA-1");
                        } catch (NoSuchAlgorithmException ex) {
                            throw new ErrorException("No such Algorithm");
                        }
                        md.update(chunk.getData());
                        byte[] ris = new byte[Creek.DIMSHA];
                        ris = md.digest();
                        for (i = 0; i < ris.length; i++) {
                            if (ris[i] != sha[i]) {
                                float temp = (float) c.getDimensione() / (float) BitCreekPeer.DIMBLOCCO;
                                int dim = (int) Math.ceil(temp);
                                if (chunk.getOffset() != dim - 1) {
                                    throw new ErrorException("SHA non corretto"); // lancio eccezione se lo sha non torna
                                }
                            }
                        }
                    }
                    catch (ErrorException ex) {
                        /* lo sha non torna : richiedo il pezzo */
                        int[] richiesta = new int[1];
                        richiesta[0] = chunk.getOffset();
                        conn.sendDown(new Messaggio(Messaggio.REQUEST, richiesta));
                        break;
                    }
                    try {
                        //il pezzo potrebbe gia essere presente
                        if (c.scriviChunk(chunk)) {
                            conn.incrDown();
                        }  
                    }
                    catch (NullPointerException ex) {
                        /* devo chiudere */
                        tipo = Messaggio.CLOSE;
                        break;
                    }
                    /* aggiorno la percentuale */
                    c.settaPerc();
                    /* resetto il canale per evitare di impallare tutto */
                    if (count % 100 == 0) {
                        conn.ResetDown();
                    }
                    this.pendingRequest = PREQ;
                }
            }
            if (tipo == Messaggio.CLOSE) {
                if (conn != null) {
                    conn.sendDown(new Messaggio(Messaggio.CLOSE, null));
                }
                break;
            }
            /*se siamo in endgame niente piu` richieste */
            if (pendingRequest == PREQ && !endgame) {
                p = c.getNext(this.conn.getBitfield());
                if (p != null) {
                    int id = p.getId();
                    if (id == Downloader.ENDGAME) {
                        /* sono passato in endgame : chiedo tutti i PIO rimasti */
                        int[] ultimi = c.getLast();
                        this.endgame = true;
                        if (ultimi.length > 0) {
                            conn.sendDown(new Messaggio(Messaggio.REQUEST, ultimi));
                            this.pendingRequest = ultimi[0];
                        }
                    } else {
                        /* invio normale */
                        int[] toSend = new int[1];
                        toSend[0] = id;
                        conn.sendDown(new Messaggio(Messaggio.REQUEST, toSend));
                        this.pendingRequest = toSend[0];
                    }
                } else {
                    try {
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        System.err.println("Sono stato interrotto");
                    }
                }
            }
        }
        /* decremento il numero di connessioni e il numero dei peer */
        peer.decrConnessioni();
        c.decrPeer();
        /* rilascio il PIO se sono stato chiuso */
        if (p != null) {
            p.setFree();
        }
        /* metto socket a null */
        this.conn.setSocketDown(null);
        System.out.println(Thread.currentThread().getName() + " Downloader Terminato, ho scaricato: " + this.conn.getDownloaded() + " Chunk");
    }
}
