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
        //System.out.println(Thread.currentThread().getName() + " AVVIA");

        ArrayList<NetRecord> lista = new ArrayList<NetRecord>();
        Creek c = null;
        Descrittore d = null;
        boolean presenza = false;

        for (int index : this.array) {

            SSLSocket s = null;
            ObjectInputStream oin = null;

            //questa provoca l'aggiornamento dell' interfaccia grafica aggiungendo il creek a arraydescr
            try {
                System.out.println(Thread.currentThread().getName() + " : Avvia : Avvio descr " + index + " su una lista lunga " + peer.getCercati().size());
                d = peer.getCercati().get(index);
                System.out.println("\n\n Avvia : descr in cercati ha id : " + d.getId());
                c = new Creek(d, true, false);
                System.out.println("\n\n Avvia : creek ha id : " + c.getId());

                //introduce una serie di problemi tragici! fortunosamente risolti....
                c.setToDo();

                if (c == null) {
                    System.out.println("NON E POSSIBILE!!!");
                }
                presenza = peer.addCreek(c);
            } catch (ErrorException ex) {
                Logger.getLogger(Avvia.class.getName()).log(Level.SEVERE, null, ex);
            }

            /* contatto gli altri e creo i thread solo se non ho già in download quel file */
            //NON DOVREMMO CONTROLLARLO PRIMA???
            if (presenza) {
                //recupero della lista Peer dal tracker
                int portatracker = d.getTCP();
                
                
                //CONTATTO SSL
                System.out.println(Thread.currentThread().getName() + " : Avvia : !presenza --> Contatto tracker sulla porta : " + portatracker);
                try {
                    s = (SSLSocket) SSLSocketFactory.getDefault().createSocket(peer.getIpServer(), portatracker);
                    oin = new ObjectInputStream(s.getInputStream());

                    // leggo la dimensione della lista
                    int dimlista = oin.readInt();
                    System.out.println(Thread.currentThread().getName() + " : Avvia : dimlista : " + dimlista);
                    // faccio un for per leggere i netrecord
                    for (int j = 0; j < dimlista; j++) {
                        lista.add((NetRecord) oin.readObject());
                        NetRecord toPrint = lista.get(j);
                        System.out.print("indirizzo : "+toPrint.getIp().getHostAddress());
                        System.out.println(" porta : "+toPrint.getPorta());
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
                        
                       
                        
                        if(n.getPorta() == peer.getPortaRichieste() && n.getIp().getHostAddress().compareTo(peer.getMioIp().getHostAddress()) == 0) {
                            System.out.println("MI STAVO AUTOCONTATTANDO PERCHE SONO IMBECILLE");
                            continue;
                        }
                        
                        if(c.presenzaConnessione(n.getIp(), n.getPorta()) != null){
                            System.out.println("STAVO RICONTATTANDO UNO STESSO PEER PERCHE LA LISTAPEER E` BUGGATA");
                            continue;
                        }
                        
                        System.out.println(Thread.currentThread().getName() + " Avvia : Contatto peer con porta " + n.getPorta());
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
                        System.out.println(Thread.currentThread().getName() + "fatto write delle info verso "+sock.getInetAddress().getHostAddress());
                        
                        
                        
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

                        //aggiungo l'oggetto connessione
                        System.out.println(Thread.currentThread().getName() + " Avvia : Aggiungo connessione in download");
                        //Connessione conn = new Connessione(sock, null, b.getBitfield(), n.getPorta());
                        
                       
                        System.out.println(Thread.currentThread().getName() + " Avvia : Creo Downloader");
                        //creo il thread per il download e lo aggiungo al ThreadPool
                        peer.addTask(new Downloader(c, conn));
                        System.out.println(Thread.currentThread().getName() + " Avvia : Incrconnessioni");
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
            }else
                System.out.println(Thread.currentThread().getName() + " Avvia : non aggiunto");

        }
    }
}
