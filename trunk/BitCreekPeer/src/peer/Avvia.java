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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

/**
 *
 * @author andrea
 */
public class Avvia implements Runnable {

    private BitCreekPeer peer;
    private int[] array;

    //array e` l'array di indici dei descrittori da avviare
    public Avvia(BitCreekPeer peer, int[] array) {
        this.peer = peer;
        this.array = array;
    }

    public void run() {
        System.out.println(Thread.currentThread().getName() + " AVVIA");

        ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
        Creek c = null;
        Descrittore d = null;

        for (int index : this.array) {

            SSLSocket s = null;
            ObjectInputStream oin = null;

            //questa provoca l'aggiornamento dell;interfaccia grafica
            try {
                System.out.println(Thread.currentThread().getName() + " AVVIO IL DESCR " + index + " SU UNA LISTA DI DIMENSIONE " + peer.getCercati().size());
                d = peer.getCercati().get(0);
                if (d == null) {
                    System.out.println("STA SCAZZANDO");
                }
                d = peer.getCercati().get(index);
                if (d == null) {
                    System.out.println("STA SCAZZANDO 2");
                }
                c = new Creek(d, true, false);
                //introduce una serie di problemi tragici!
                c.setPIO();
                if (c == null) {
                    System.out.println("NON E POSSIBILE!!!");
                }
                peer.addCreek(c);
            } catch (ErrorException ex) {
                Logger.getLogger(Avvia.class.getName()).log(Level.SEVERE, null, ex);
            }

            //recupero della lista Peer dal tracker
            int portatracker = d.getTCP();
            System.out.println(Thread.currentThread().getName() + " porta tracker : " + portatracker);
            try {
                s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peer.getIpServer(), portatracker);
                oin = new ObjectInputStream(s.getInputStream());

                // leggo la dimensione della lista
                int dimlista = oin.readInt();
                System.out.println("dimlista : " + dimlista);
                // faccio un for per leggere i netrecord
                for (int j = 0; j < dimlista; j++) {
                    lista.add((NetRecord) oin.readObject());
                }
                s.close();
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(BitCreekPeer.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(BitCreekPeer.class.getName()).log(Level.SEVERE, null, ex);
            }

            //devo contattare i peer nella lista
            for (NetRecord n : lista) {
                try {
                    if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                        break;
                    }
                    //contatto il peer n
                    Socket sock = null;
                    SocketAddress sa = new InetSocketAddress(n.getIp(), n.getPorta());
                    sock = new Socket();
                    sock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                    Bitfield b = null;
                    ObjectOutputStream contactOUT = new ObjectOutputStream(sock.getOutputStream());
                    ObjectInputStream contactIN = new ObjectInputStream(sock.getInputStream());
                    //lo contatto dandogli le informazioni per contattarmi in seguito (la mia server socket)
                    contactOUT.writeObject(new Contact(peer.getMioIp(), peer.getPortaRichieste(), c.getId()));
                    try {
                        //lui mi risponde con il suo bitfield
                        b = (Bitfield) contactIN.readObject();
                        System.out.println(Thread.currentThread().getName() + " Ricevuto Bitfield");
                    //aggiungo l'oggetto connessione
                    } catch (ClassNotFoundException ex) {
                        Logger.getLogger(Avvia.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    //aggiungo l'oggetto connessione - manca il monitor
                    Connessione conn = new Connessione(sock, null, b.getBitfield(), n.getPorta());
                    c.addConnessione(conn);

                    //creo il thread per il download e lo aggiungo al ThreadPool
                    peer.addTask(new Downloader(c, conn));

                } catch (IOException ex) {
                    /* passo al prossimo netrecord perchè nessuno mi ha risposto */
                    continue;
                }


            }

            System.out.println("creo uploader manager");
            peer.addTask(new UploadManager(peer, c));
            /* inutile continuare a ciclare se non posso creare connessioni */
            if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                break;
            }


        }
    }
}
