package peer;

import condivisi.NetRecord;
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
 * Task che si occupa di far ripartire tutti i download
 * in seguito alla riconnessione
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Riavvia implements Runnable {

    /* Variabili d' istanza */
    /** Peer */
    private BitCreekPeer peer;

    /**
     * Costruttore
     * @param peer
     */
    public Riavvia(BitCreekPeer peer) {
        this.peer = peer;
    }

    /**
     * Corpo del task
     */
    public void run() {

        ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
        ArrayList<Creek> array = null;
        array = this.peer.getDescr();
        if (array != null) {
            for (Creek c : array) {
                /* inizializzo il creek */
                c.init();
                /* se il file è in download */
                if (c.getStato()) {
                    SSLSocket s = null;
                    ObjectInputStream oin = null;
                    int portatracker = c.getTCP();
                    try {
                        s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peer.getIpServer(), portatracker);
                        oin = new ObjectInputStream(s.getInputStream());
                        int dimlista = oin.readInt();
                        for (int j = 0; j < dimlista; j++) {
                            lista.add((NetRecord) oin.readObject());
                        }
                        s.close();
                    } catch (ClassNotFoundException ex) {
                        System.err.println("Riavvia : ClassNotFoundException");
                    } catch (IOException ex) {
                        System.err.println("Riavvia : IOexception");
                    }
                    for (NetRecord n : lista) {
                        try {
                            if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                                break;
                            }
                            if (n.getPorta() == peer.getPortaRichieste() && n.getIp().getHostAddress().compareTo(peer.getMioIp().getHostAddress()) == 0) {
                                continue;
                            }
                            if (c.presenzaConnessione(n.getIp(), n.getPorta()) != null) {
                                continue;
                            }
                            SocketAddress sa = new InetSocketAddress(n.getIp(), n.getPorta());
                            Socket sock = new Socket();
                            sock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                            Bitfield b = new Bitfield(null);
                            ObjectOutputStream contactOUT = new ObjectOutputStream(sock.getOutputStream());
                            ObjectInputStream contactIN = new ObjectInputStream(sock.getInputStream());
                            Connessione conn = new Connessione();
                            conn.set(true, sock, contactIN, contactOUT, b.getBitfield(), n.getPorta());
                            c.addConnessione(conn);
                            contactOUT.writeObject(new Contact(peer.getMioIp(), peer.getPortaRichieste(), c.getId()));
                            try {
                                b = (Bitfield) contactIN.readObject();
                                conn.setBitfield(b.getBitfield());
                                c.addRarita(b.getBitfield());
                            } catch (ClassNotFoundException ex) {
                                System.err.println("Riavvia : Classnotfound");
                            }
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
                    peer.addTask(new UploadManager(peer, c));
                    /* inutile continuare a ciclare se non posso creare connessioni */
                    if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                        break;
                    }
                }
            }
        }
    }
}
