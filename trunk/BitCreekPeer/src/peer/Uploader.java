package peer;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintStream;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrea
 */
public class Uploader implements Runnable {

    private Connessione conn;
    private Creek c;
    private BitCreekPeer peer;
    private int puntatoreHave;
    private int failed;

    public Uploader(Connessione conn, Creek c, int numPieces, BitCreekPeer peer) {
        this.conn = conn;
        this.c = c;
        this.puntatoreHave = numPieces;
        this.peer = peer;
        this.failed = 0;
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

        Creek.stampaDebug(output, " SONO UN NUOVO THREAD UPLOADER \n verso " + this.conn.getIPVicino());
        int count = 0;
        while (true) {

            // GESTIONE TERMINAZIONE
            if (this.conn.getTermina() || this.failed > Downloader.MAXFAILURE) {
                // invio msg CLOSE al downloader associato
                Messaggio nuovo = new Messaggio(Messaggio.CLOSE, null);
                this.conn.sendUp(nuovo);
                break;
            }

            //LETTURA MESSAGGIO
            Messaggio m = this.conn.receiveUp();
            if (m == null) {
                Creek.stampaDebug(output, "Uploader: Timeout da connessione");
                this.failed++;
                continue;
            } else {
                this.failed = 0;
            }


            int tipo = m.getTipo();
            switch (tipo) {
                case Messaggio.REQUEST: {
                    Creek.stampaDebug(output, "TESTO IL SEMAFORO....");
                    this.conn.possoUploadare();
                    Creek.stampaDebug(output, "...TESTATO");
                    int pezzo;
                    int[] idPezzo = (int[]) m.getObj();
                    if (idPezzo.length == 1) {
                        pezzo = idPezzo[0];
                        Creek.stampaDebug(output, " Mando chunk con id " + pezzo);
                        //creo il chunk corretto da mandare
                        Chunk pezzoRichiesto = c.getChunk(pezzo);
                        if (pezzoRichiesto != null) {
                            Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                            //riempio il buffer
                            this.conn.sendUp(nuovo);
                        } else {
                            Creek.stampaDebug(output, "CAZZO LA GETCHUNK RESTITUISCE DAVVERO NULL: "+pezzo);
                            //io non gli mando nulla e lui prima o poi mi mandera in culo
                        }

                        break;
                    } else {
                        //gestione endgame -> glieli mando tutti (se li possiedo)
                        for (int i = 0; i < idPezzo.length; i++) {
                            pezzo = idPezzo[i];

                            //creo il chunk corretto da mandare
                            Chunk pezzoRichiesto = c.getChunk(pezzo);

                            if (pezzoRichiesto != null) {
                                Creek.stampaDebug(output, " Mando chunk con id " + pezzo);
                                Messaggio nuovo = new Messaggio(Messaggio.CHUNK, pezzoRichiesto);
                                this.conn.sendUp(nuovo);
                            } else {
                                Creek.stampaDebug(output, "NON CE L'HOOOOO il " + pezzo);
                            }

                        //riempio il buffer

                        }
                        break;
                    }
                }
                case Messaggio.INTERESTED: {
                    Creek.stampaDebug(output, " L'altro peer e` interessato");
                    this.conn.setInteresseUp(true);
                    this.conn.sendUp(new Messaggio(Messaggio.UNCHOKE, null));
                    break;
                }
                case Messaggio.NOT_INTERESTED: {
                    Creek.stampaDebug(output, " L'altro peer NON e` interessato");
                    this.conn.setInteresseUp(false);
                    break;
                }
                case Messaggio.CLOSE: {
                    Creek.stampaDebug(output, " Mi e` arrivata una close");
                    break;
                }
            }
            if (tipo == Messaggio.CLOSE) {
                break;
            }
            //CONTROLLO RESET STREAM
            if (++count % 100 == 0) {
                Creek.stampaDebug(output, "\n\n SVUOTO LO STEAM DELL'UPLOADER \n");
                this.conn.ResetUp();
            }
            //CONTROLLO/INVIO MESSAGGI DI HAVE
            while (this.c.getScaricati() > this.puntatoreHave) {
                int daNotificare = this.c.getScaricatiIndex(this.puntatoreHave);
                //ennino il wrapper automatico
                Messaggio have = new Messaggio(Messaggio.HAVE, daNotificare);
                this.puntatoreHave++;
                this.conn.sendUp(have);
                Creek.stampaDebug(output, " Invio la notifica del pezzo:  " + daNotificare);
            }
        }

        Creek.stampaDebug(output, "Uploader: sto morendo perche` me l'ha detto l'altro");
        // decremento il numero di connessioni
        peer.decrConnessioni();
        // setto il flag di chiusura
        conn.setTermina(false);
    }
}
