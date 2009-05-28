package peer;

import condivisi.ErrorException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task che si occupa di ascoltare richieste sulla porta di ricezione
 * @author Bandettini
 */
public class Ascolto implements Runnable {

    /* Costanti */
    private final int ATTESA = 3000;

    /* Variabili d'istanza */
    private BitCreekPeer peer;

    /**
     * Costruttore
     */
    public Ascolto(BitCreekPeer peer) throws ErrorException {
        if (peer == null) {
            throw new ErrorException("Param null");
        }
        this.peer = peer;
    }

    /**
     * Corpo del task
     */
    public void run() {
        while (true) {
            try {
                /**MANCA GESTIONE MONITOR + GESTIONE ERRORE + RISCRIVERE STE MILLE ECCEZZIONI 
                 * + CONTATTARE L"ALTRO SE SONO LEECHER
                 */
                ObjectInputStream in = null;
                ObjectOutputStream out = null;
                Socket scambio = null;
                peer.getIpServer().getHostAddress();

                try {
                    scambio = peer.getSS().accept();
                } catch (SocketTimeoutException e) {
                    // timeout scaduto : continuo a ciclare
                    } catch (IOException ex) {
                    System.err.println(Thread.currentThread().getName()+" Sono stato disconnesso : la socket di benvenuto è stata chiusa");
                }
                try {
                    in = new ObjectInputStream(scambio.getInputStream());
                    out = new ObjectOutputStream(scambio.getOutputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    //RICEVO UN CONTATTO
                    Contact con = (Contact) in.readObject();
                    int swarmId = con.getId();
                    Creek contacted = peer.getCreek(swarmId);
                    //INVIO IL BITFILED RELATIVO
                    out.writeObject(new Bitfield(contacted.getHave()));
                    /**
                     * EFFETTUO IL CONTROLLO CHE L'OGGETTO NON ESISTA GIA
                     */
                    Connessione conn = new Connessione(null, scambio, null, con.getSS());
                    Connessione temp;
                    if ((temp = contacted.presenzaConnessione(conn)) == null) {
                        System.out.println(Thread.currentThread().getName()+" UPLOADER - SONO STATO CONTATTATO, connessione aggiunta");
                        contacted.addConnessione(conn);
                    }
                    else{
                        System.out.println(Thread.currentThread().getName()+" UPLOADER - SONO STATO CONTATTATO, connessione gia presente");
                        temp.setSocketUp(scambio);
                        conn = temp;
                    }
                    //CREO IL THREAD RELATIVO IN UPLOAD
                    peer.getTP().execute(new Uploader(conn));
                    //in.close();
                    //out.close();
                    //OPERAZIONI ULTERIORI SE SONO LEECHER nello SWARM
                    //RICHIEDO ANCHE IO DI SCARICARE
                    if (contacted.getStato() == true) {
                        System.out.println(Thread.currentThread().getName()+" EFFETTUO OPERAZIONI AGGIUNTIVE PERCHE SONO LEECHER");
                        Contact mycon = new Contact(peer.getMioIp(), peer.getPortaRichieste(), swarmId);
                        Socket mysock = new Socket(con.getIp(), con.getSS());
                        in = new ObjectInputStream(mysock.getInputStream());
                        out = new ObjectOutputStream(mysock.getOutputStream());
                        out.writeObject(mycon);
                        Bitfield b = (Bitfield) in.readObject();
                        conn.setSocketDown(mysock);
                        conn.setBitfield(b.getBitfield());
                        peer.getTP().execute(new Downloader(contacted, conn));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                }


            } catch (NullPointerException e) {
                /* ipServer è null --> sono disconnesso quindi aspetto */
                try {
                    Thread.sleep(ATTESA);
                } catch (InterruptedException ex) {
                    System.err.println(Thread.currentThread().getName()+" Interrotto thread Ascolto");
                }
            }
        }
    }
}
