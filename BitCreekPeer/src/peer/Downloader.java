package peer;

import condivisi.ErrorException;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrea
 */
public class Downloader implements Runnable {

    //Messaggio utilizzato per comunicare il passaggio in endgame
    protected static final int ENDGAME = -1;
    protected static final int MAXFAILURE=10;
    private Creek c;
    private Connessione conn;
    private BitCreekPeer peer;
    private boolean pendingRequest;
    private boolean endgame;
    private int failed;

    public Downloader(Creek c, Connessione conn, BitCreekPeer peer) {
        this.c = c;
        this.conn = conn;
        this.peer = peer;
        this.pendingRequest = false;
        this.endgame = false;
        this.failed =0;      
    }

    public void run() {

        //INIZIALIZZAZIONE STAMPA DI DEBUG
        FileOutputStream file = null;
        PrintStream output = null;
        try {
            file = new FileOutputStream(Thread.currentThread().getName() + ".log");
            output = new PrintStream(file);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Uploader.class.getName()).log(Level.SEVERE, null, ex);
        }

        Creek.stampaDebug(output, "\n\n");
        Creek.stampaDebug(output, "SONO UN NUOVO THREAD DOWNLOADER \n");
        //come prima cosa il thread verifica l'effettivo stato di interesse alla connessione



        if (c.interested(conn.getBitfield())) {
            conn.setInteresseDown(true);
            conn.sendDown(new Messaggio(Messaggio.INTERESTED, null));
            Creek.stampaDebug(output, "connessione interessante ");
        } else {
            conn.setInteresseDown(false);
            conn.sendDown(new Messaggio(Messaggio.NOT_INTERESTED, null));
            Creek.stampaDebug(output, "connessione NON interessante");
        }

        //utilizzato per lo svuotamento dello buffer degli stream
        int count = 0;
        PIO p = null;

        //********************** il ciclo *******************
        while (true) {
            //come prima cosa controllo se e` terminato il download oppure
            // se devo uscire
            if (!this.c.getStato() || this.conn.getTermina() || this.failed > MAXFAILURE ) {
                if(this.failed > MAXFAILURE){
                    Creek.stampaDebug(output, "MUOIO PERCHE E MORTO L'ALTRO");
                }
                Creek.stampaDebug(output, "Ho terminato");
                conn.sendDown(new Messaggio(Messaggio.CLOSE, null));
                break;
            }

            //RICEZIONE MESSAGGIO
            Messaggio m = this.conn.receiveDown();

            // non cancellare : importante
            if (m == null) {
                Creek.stampaDebug(output, "TIMEOUT sulla receiveDOwn ho ricevuto null come messaggio");
                this.failed++;
                continue;
            }
            else{
                this.failed=0;
            }
            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.HAVE: {
                    int piece = (Integer) m.getObj();
                    //this.conn.bitfield[piece] = true;
                    this.conn.setIndexBitfield(piece);
                    Creek.stampaDebug(output, "HAVE ricevuto di " + piece);
                    /* ma questo controllo serve ????..non va eseguito in ogni caso il corpo ??*/
                    if (this.conn.getInteresseDown() == false) {
                        this.conn.setInteresseDown(this.c.interested(this.conn.getBitfield()));
                    }
                    break;
                }
                case Messaggio.CHOKE: {
                    Creek.stampaDebug(output, "CHOKE ricevuto");
                    this.conn.setStatoDown(Connessione.CHOKED);
                    break;
                }
                case Messaggio.UNCHOKE: {
                    Creek.stampaDebug(output, "UNCHOKE ricevuto");
                    this.conn.setStatoDown(Connessione.UNCHOKED);
                    break;
                }
                case Messaggio.CLOSE: {
                    break;
                }
                case Messaggio.CHUNK: {
                    count++;
                    Creek.stampaDebug(output, "Ricevuto Messaggio CHUNK: " + ((Chunk) m.getObj()).getOffset());
                    Chunk chunk = (Chunk) m.getObj();
                    try {
                        if (c.scriviChunk(chunk)) {
                            conn.incrDown();
                        }
                    } catch (ErrorException ex) {
                        System.out.println("Lo sha non torna : " + ex.getMessage());
                        // lo sha non torna : richiedo il pezzo
                        int[] richiesta = new int[1];
                        richiesta[0]=chunk.getOffset();
                        conn.sendDown(new Messaggio(Messaggio.REQUEST, richiesta));
                        continue;
                    }

                    c.settaPerc();
                    /* resetto il canale per evitare di impallare tutto -> nel downloader e` probabilmente inutile
                     */
                    if (count % 100 == 0) {
                        Creek.stampaDebug(output, "\n\n SVUOTO LO STREAM DEL DOWNLOADER\n");
                        conn.ResetDown();
                    }
                    /*  controllare lo SHA del pezzo ------> da fare !!!!  */

                    this.pendingRequest = false;
                }
            }
            if (tipo == Messaggio.CLOSE) {
                Creek.stampaDebug(output, "Ho terminato");
                break;
            }
            //debug perverso
            if (pendingRequest) {
                output.println("Ho una pending Request");
            } else {
                output.println("Non ho una pending Request");
            }
            
            //se siamo in endgame niente piu` richieste
            if (!pendingRequest && !endgame) {
                p = c.getNext(this.conn.getBitfield());
                if (p != null) {
                    int id = p.getId();
                    if (id == Downloader.ENDGAME) {
                        //sono passato in endgame, chiedo tutti i PIO
                        int[] ultimi = c.getLast();
                        if(ultimi.length>0){
                            Creek.stampaDebug(output, "\n\nTEST ENDGAME: \n\n");
                            conn.sendDown(new Messaggio(Messaggio.REQUEST, ultimi));
                            Creek.stampaDebug(output, " Downloader : REQUEST inviato per endgame : ");
                        }
                        this.endgame = true;
                        this.pendingRequest = true;
                    } else {
                        //invio normale
                        int[] toSend = new int[1];
                        toSend[0] = id;
                        //System.out.println("Downloader : Sto per fare sendDown perchè p != null");
                        conn.sendDown(new Messaggio(Messaggio.REQUEST, toSend));
                        this.pendingRequest = true;
                        Creek.stampaDebug(output, " Downloader : REQUEST inviato for chunk : " + p.getId());
                    }
                } else {
                    output.println();
                    try {
                        Creek.stampaDebug(output, "getNext mi da null dormo un pochetto...");
                        Thread.sleep(100);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(Downloader.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        // decremento il numero di connessioni
        peer.decrConnessioni();
        // rilascio il PIO se sono stato chiuso
        if (p != null) {
            p.setFree();
        }
        conn.setTermina(false);
        Creek.stampaDebug(output, " Downloader terminato");
    }
}

