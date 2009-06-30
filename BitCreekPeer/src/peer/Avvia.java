package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
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
 * Task che si occupa di avviare il download
 * dei file selezionati nella tabella della ricerca
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Avvia implements Runnable {

    /* Variabili d'istanza*/
    /** Peer */
    private BitCreekPeer peer;
    /** Array con gli indici dei file selezionati */
    private int[] array;

    /**
     * Costruttore
     * @param peer
     * @param array
     */
    public Avvia(BitCreekPeer peer, int[] array) {
        this.peer = peer;
        this.array = array;
    }

    /**
     * Corpo del task
     */
    public void run() {
        ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
        Creek c = null;
        Descrittore d = null;
        boolean presenza = false;

        for (int index : this.array) {

            SSLSocket s = null;
            ObjectInputStream oin = null;

            /* questa provoca l'aggiornamento dell' interfaccia grafica aggiungendo il creek a arraydescr */
            try {
                d = peer.getCercati().get(index);
                c = new Creek(d, true, false);
                c.setToDo();
                presenza = peer.addCreek(c);
            } catch (ErrorException ex) {
                System.err.println("Avvia : ErrorException");
            }

            /* contatto gli altri e creo i thread solo se non ho già in download quel file */
            if (presenza) {
                int portatracker = d.getTCP();
                /* effettuo il contatto via SSL */
                try {
                    s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peer.getIpServer(), portatracker);
                    oin = new ObjectInputStream(s.getInputStream());
                    int dimlista = oin.readInt();
                    for (int j = 0; j < dimlista; j++) {
                        lista.add((NetRecord) oin.readObject());
                        NetRecord toPrint = lista.get(j);
                    }
                    s.close();
                } catch (ClassNotFoundException ex) {
                    System.err.println("Avvia : Classnotfound");
                } catch (IOException ex) {
                    System.err.println("Avvia : IOexception");
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
                            System.err.println("Avvia : Classnotfound");
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
