package peer;

import condivisi.ErrorException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;

/**
 * Task che si occupa di ascoltare richieste sulla porta di ricezione
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Ascolto implements Runnable {

    /* Costanti */
    /**
     * Definisce i millisecondi di attesa del task
     * nel caso il peer sia disconnesso
     */
    private final int ATTESA = 3000;

    /* Variabili d'istanza */
    /** Peer */
    private BitCreekPeer peer;

    /**
     * Costruttore
     * @param peer 
     * @throws ErrorException se peer è null
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
                ObjectInputStream in = null;
                ObjectOutputStream out = null;
                Socket scambio = null;
                /* istruzione che lancia NullPointerException se ipserver è null */
                peer.getIpServer().getHostAddress();
                /* se non posso accettare connessioni vado in sleep come se fossi disconnesso */
                if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                    throw new NullPointerException();
                }
                try {
                    scambio = peer.getSS().accept();
                } catch (SocketTimeoutException e) {
                    /* timeout scaduto : continuo a ciclare */
                    continue;
                } catch (IOException ex) {
                    System.err.println("Ascolto : la socket di benvenuto è stata chiusa --> sono disconnesso");
                    continue;
                }
                try {
                    in = new ObjectInputStream(scambio.getInputStream());
                    out = new ObjectOutputStream(scambio.getOutputStream());
                } catch (IOException ex) {
                    System.err.println("Ascolto : IOexception");
                }
                try {
                    /* ricevo un contatto */
                    Contact con = (Contact) in.readObject();
                    int swarmId = con.getId();
                    Creek contacted = peer.getCreek(swarmId);
                    /* controllo  esito : se non appartengo allo swarm chiudo la socket */
                    if (contacted == null) {
                        scambio.close();
                        continue;
                    }
                    /* invio bitfield come da protocollo */
                    int alreadyDownloaded = contacted.getScaricati();
                    out.writeObject(new Bitfield(contacted.getHave()));
                    /* effettuo il controllo che la connessione non esista già */
                    Connessione conn = null;
                    Connessione toModify = contacted.presenzaConnessione(scambio.getInetAddress(), con.getSS());
                    if (toModify == null) {
                        conn = new Connessione();
                        conn.set(false, scambio, in, out, null, con.getSS());
                        contacted.addConnessione(conn);
                    } else {
                        /* connessione già presente */
                        conn = toModify;
                        conn.set(false, scambio, in, out, null, con.getSS());
                    }
                    /* creo il thread in upload */
                    peer.addTask(new Uploader(conn, contacted, alreadyDownloaded, peer));
                    /* incremento numero di connessioni */
                    peer.incrConnessioni();
                    /* incremento nuemro peer in upload se sono seeder */
                    if (!contacted.getStato()) {
                        contacted.incrPeer();
                    }
                    /* operazioni ulteriori se sono leecher : creo connessione in down
                    Lo devo fare solo se non ho già una connessione in down, non sono
                    seeder e posso creare connessioni */
                    if (contacted.getStato() && conn.DownNull() && peer.getConnessioni() < BitCreekPeer.MAXCONNESSIONI) {
                        Contact mycon = new Contact(peer.getMioIp(), peer.getPortaRichieste(), swarmId);
                        SocketAddress sa = new InetSocketAddress(con.getIp(), con.getSS());
                        Socket mysock = new Socket();
                        mysock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                        ObjectOutputStream output = new ObjectOutputStream(mysock.getOutputStream());
                        ObjectInputStream input = new ObjectInputStream(mysock.getInputStream());
                        Bitfield b = null;
                        conn.set(true, mysock, input, output, null, con.getSS());
                        output.writeObject(mycon);
                        b = (Bitfield) input.readObject();
                        conn.setBitfield(b.getBitfield());
                        /* aggiungo thread per download */
                        peer.addTask(new Downloader(contacted, conn, peer));
                        /* incremento numero connessioni */
                        peer.incrConnessioni();
                        /* incremento numero peer in download */
                        contacted.incrPeer();
                    }
                } catch (IOException ex) {
                    System.err.println("Ascolto : IOException");
                } catch (ClassNotFoundException ex) {
                    System.err.println("Ascolto : ClassNotFoundException");
                }
            } catch (NullPointerException e) {
                /* ipServer è null --> sono disconnesso quindi aspetto */
                try {
                    Thread.sleep(ATTESA);
                } catch (InterruptedException ex) {
                    System.err.println("Ascolta : sono stato interrotto");
                }
            }
        }
    }
}
