package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import condivisi.NetRecord;
import gui.BitCreekGui;
import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.ArrayList;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 * Task che si occupa di aprire un .creek dal disco
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Apri implements Runnable {

    /* Variabili d'istanza */
    /** File da aprire */
    private File creek;
    /** Peer */
    private BitCreekPeer peer;
    /** Gui */
    private BitCreekGui gui;

    /**
     * Costruttore
     * @param creek 
     * @param peer 
     * @param gui
     * @throws ErrorException se almeno un parametro è null
     */
    public Apri(File creek, BitCreekPeer peer, BitCreekGui gui) throws ErrorException {
        if (creek == null || peer == null || gui == null) {
            throw new ErrorException("Param null");
        }
        this.creek = creek;
        this.peer = peer;
        this.gui = gui;
    }

    /**
     * Corpo del task
     */
    public void run() {

        /* cambio il cursore */
        gui.getRootPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        String nome = creek.getName();
        ObjectInputStream in = null;
        Creek c = null, cr = null;

        boolean problema = false;

        /* controllo che sia un .creek */

        if (nome.length() > 6 && nome.substring(nome.length() - 6, nome.length()).compareTo(".creek") == 0) {
            try {
                in = new ObjectInputStream(new FileInputStream(creek));
                cr = (Creek) in.readObject();
                try {
                    c = new Creek((Descrittore) cr, cr.getStato(), cr.getPubblicato());
                } catch (ErrorException ex) {
                    problema = true;
                }
                in.close();
            } catch (IOException ex) {
                problema = true;
            } catch (ClassNotFoundException ex) {
                problema = true;
            }
            /* se il file non è già presente ed è da downlodare lo inizializzo e poi  lo aggiungo */
            if (!problema && c.getStato()) {
                try {
                    if (!peer.addCreek(c)) {
                        problema = true;
                    }
                } catch (ErrorException ex) {
                    problema = true;
                }
                /* inizializzo il creek */
                c.setToDo();
                /* vedo di scaricarlo */
                ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
                SSLSocket s = null;
                ObjectInputStream oin = null;
                /* recupero della lista Peer dal tracker */
                int portatracker = c.getTCP();
                /* contatto il tracker via SSL */
                try {
                    s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peer.getIpServer(), portatracker);
                    oin = new ObjectInputStream(s.getInputStream());
                    int dimlista = oin.readInt();
                    for (int j = 0; j < dimlista; j++) {
                        lista.add((NetRecord) oin.readObject());
                    }
                    s.close();
                } catch (ClassNotFoundException ex) {
                    System.err.println("Avvia : Classnotfound");
                } catch (IOException ex) {
                    System.err.println("Avvia : IOException");
                }
                /* contatto i peer nella lista */
                for (NetRecord n : lista) {

                    try {
                        if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                            /* esco perchè non posso aggiungere altre connessini */
                            break;
                        }
                        /* controlli */
                        if (n.getPorta() == peer.getPortaRichieste() && n.getIp().getHostAddress().compareTo(peer.getMioIp().getHostAddress()) == 0) {
                            continue;
                        }
                        if (c.presenzaConnessione(n.getIp(), n.getPorta()) != null) {
                            continue;
                        }
                        /* tutto ok : contatto il peer */
                        SocketAddress sa = new InetSocketAddress(n.getIp(), n.getPorta());
                        Socket sock = new Socket();
                        sock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                        Bitfield b = new Bitfield(null);
                        ObjectOutputStream contactOUT = new ObjectOutputStream(sock.getOutputStream());
                        ObjectInputStream contactIN = new ObjectInputStream(sock.getInputStream());
                        /* creo la connessione in modo da essere anche ricontattato */
                        Connessione conn = new Connessione();
                        conn.set(true, sock, contactIN, contactOUT, b.getBitfield(), n.getPorta());
                        c.addConnessione(conn);
                        /* invio le informazioni per contattarmi in seguito */
                        contactOUT.writeObject(new Contact(peer.getMioIp(), peer.getPortaRichieste(), c.getId()));
                        try {
                            /* lui mi risponde con il suo bitfield come da protocollo */
                            b = (Bitfield) contactIN.readObject();
                            /* aggiorno la connessione */
                            conn.setBitfield(b.getBitfield());
                            c.addRarita(b.getBitfield());
                        } catch (ClassNotFoundException ex) {
                            System.err.println("Avvia : Classnotfound");
                        }
                        /* creo nuovo thread downloader */
                        peer.addTask(new Downloader(c, conn, peer));
                        /* incremento  il numero di connessioni */
                        peer.incrConnessioni();
                        /* incremento numero peer */
                        c.incrPeer();
                    } catch (IOException ex) {
                        /* passo al prossimo netrecord perchè nessuno mi ha risposto */
                        continue;
                    }
                }
                /* creo uploaderManager */
                peer.addTask(new UploadManager(peer, c));
            } else {
                problema = true;
            }
        } else {
            problema = true;
        }
        if (problema) {
            gui.PrintInformation("Non si può aprire creek", gui.ERRORE);
        }
        /* cambio il cursore */
        gui.getRootPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
