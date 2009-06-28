package peer;

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
 * @author bande
 */
public class Riavvia implements Runnable {

    private BitCreekPeer peer;

    //array e` l'array di indici dei descrittori da avviare
    /**
     *
     * @param peer
     */
    public Riavvia(BitCreekPeer peer) {
        this.peer = peer;
    }

    public void run() {

        ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
        ArrayList<Creek> array = null;
        try {
            array = this.peer.getDescr();
        } catch (ErrorException ex) {
            System.out.println("ErrorException in riavvia");
        }
        if (array != null) {
            for (Creek c : array) {
                /* inizializzo il creek */
                c.init();
                /* se il file è in download */
                if (c.getStato()) {
                    SSLSocket s = null;
                    ObjectInputStream oin = null;
                    //recupero della lista Peer dal tracker
                    int portatracker = c.getTCP();

                    //CONTATTO SSL
                    try {
                        s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peer.getIpServer(), portatracker);
                        oin = new ObjectInputStream(s.getInputStream());

                        // leggo la dimensione della lista
                        int dimlista = oin.readInt();
                        // faccio un for per leggere i netrecord
                        for (int j = 0; j < dimlista; j++) {
                            lista.add((NetRecord) oin.readObject());
                        }
                        s.close();
                    } catch (ClassNotFoundException ex) {
                        System.out.println(Thread.currentThread().getName() + " Avvia : Classnotfound");
                        Logger.getLogger(BitCreekPeer.class.getName()).log(Level.SEVERE, null, ex);
                    } catch (IOException ex) {
                        System.out.println(Thread.currentThread().getName() + " Avvia : IOIO");
                        Logger.getLogger(BitCreekPeer.class.getName()).log(Level.SEVERE, null, ex);
                    }
                    //devo contattare i peer nella lista
                    for (NetRecord n : lista) {

                        try {
                            if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                                //INSERIRE MONITOR
                                break;
                            }
                            if (n.getPorta() == peer.getPortaRichieste() && n.getIp().getHostAddress().compareTo(peer.getMioIp().getHostAddress()) == 0) {
                                System.out.println("MI STAVO AUTOCONTATTANDO PERCHE SONO IMBECILLE");
                                continue;
                            }

                            if (c.presenzaConnessione(n.getIp(), n.getPorta()) != null) {
                                System.out.println("STAVO RICONTATTANDO UNO STESSO PEER PERCHE LA LISTAPEER E` BUGGATA");
                                continue;
                            }

                            //contatto il peer n
                            SocketAddress sa = new InetSocketAddress(n.getIp(), n.getPorta());
                            System.out.println(Thread.currentThread().getName() + "fatto inetsocketaddress");

                            Socket sock = new Socket();
                            System.out.println(Thread.currentThread().getName() + "aftta socket");

                            sock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                            System.out.println(Thread.currentThread().getName() + "fatto connect");
                            Bitfield b = new Bitfield(null);
                            ObjectOutputStream contactOUT = new ObjectOutputStream(sock.getOutputStream());
                            System.out.println(Thread.currentThread().getName() + "fatto OUT");
                            ObjectInputStream contactIN = new ObjectInputStream(sock.getInputStream());
                            System.out.println(Thread.currentThread().getName() + "fatto IN");

                            //QUI LA CREO COSI SONO SICURO CHE QUANDO VERRO RICONTATTATO LA CONNESSIONE C'E GIA
                            Connessione conn = new Connessione();
                            /* Prova nuovo metodo */
                            System.out.println("Prova metodo");
                            conn.set(true, sock, contactIN, contactOUT, b.getBitfield(), n.getPorta());
                            c.addConnessione(conn);
                            //lo contatto dandogli le informazioni per contattarmi in seguito (la mia server socket)
                            //System.out.print("\n\n Avvia : " + c.getId());
                            contactOUT.writeObject(new Contact(peer.getMioIp(), peer.getPortaRichieste(), c.getId()));
                            System.out.println(Thread.currentThread().getName() + "fatto write delle info verso " + sock.getInetAddress().getHostAddress());
                            try {
                                //lui mi risponde con il suo bitfield
                                b = (Bitfield) contactIN.readObject();
                                //lo scrivo nella connessione
                                conn.setBitfield(b.getBitfield());
                                //AGGIORNA RARITA!! l'altra parte e` gestita dall'upload manager _>se avremo voglia
                                c.addRarita(b.getBitfield());
                                System.out.println(Thread.currentThread().getName() + " Ricevuto Bitfield");
                            //aggiungo l'oggetto connessione
                            } catch (ClassNotFoundException ex) {
                                System.out.println(Thread.currentThread().getName() + " Avvia : Classnotfound");
                                Logger.getLogger(Avvia.class.getName()).log(Level.SEVERE, null, ex);
                            }
                            peer.addTask(new Downloader(c, conn, peer));
                            /* incremento  il numero di connessioni */
                            peer.incrConnessioni();
                            /* incremento numero peer */
                            c.incrPeer();
                        } catch (IOException ex) {
                            /* passo al prossimo netrecord perchè nessuno mi ha risposto */
                            System.out.println(Thread.currentThread().getName() + " Avvia : Passo al prossimo netrecord");
                            continue;
                        }
                    }
                    System.out.println(Thread.currentThread().getName() + " Avvia : CREO UPLOADER MANAGER !!!!!");
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

