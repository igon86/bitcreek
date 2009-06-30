package server;

import condivisi.Porte;
import condivisi.Descrittore;
import condivisi.ErrorException;
import condivisi.NetRecord;
import condivisi.InterfacciaRMI;
import condivisi.InterfacciaCallback;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Implementazione dell'interfaccia RMI
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ImplementazioneRMI implements InterfacciaRMI {

    /* Costanti */
    /** Definizione costante ERRORE */
    private final int ERRORE = -1;
    /* Variabili d'istanza */
    /** Metainfo del server*/
    private MetaInfo tabella;

    /**
     * Costruttore
     * @param tabella tabella
     * @throws condivisi.ErrorException se tabella è null
     */
    public ImplementazioneRMI(MetaInfo tabella) throws ErrorException {
        super();
        if (tabella == null) {
            throw new ErrorException("Param null");
        }
        this.tabella = tabella;
    }

    /**
     * Implementazione della ricerca di un file
     * @param nomefile nome del file da cercare
     * @param ind IP di chi ha cercato
     * @return lista di descrittori
     */
    public ArrayList<Descrittore> ricerca(String nomefile, InetAddress ind) {
        ArrayList<Descrittore> ris = tabella.cerca(nomefile);
        Iterator<Descrittore> i = ris.iterator();
        Descrittore temp = null;
        InterfacciaCallback cb = null;
        /* eseguo le callback */
        while (i.hasNext()) {
            temp = i.next();
            cb = temp.getCallback();
            try {
                cb.notifyMe(ind, temp.getName());
            } catch (RemoteException ex) {
            }
        }
        /* restituisco il risultato della ricerca */
        return ris;
    }

    /**
     * Aggiunge il descrittore d alle metainfo del server
     * @param d descrittore da pubblicare
     * @param ip IP peer
     * @param porta porta di scolto del peer
     * @return porte TCP e UDP dei tracker creati
     */
    public Porte inviaDescr(Descrittore d, InetAddress ip, int porta) {
        SSLServerSocket welcome = null;
        DatagramSocket alive = null;
        Descrittore temp = null;
        /* controllo che il file non sia già pubblicato */
        if ((temp = tabella.presenza(d)) != null) {
            try {
                return new Porte(temp.getTCP(), temp.getUDP(), temp.getId());
            } catch (ErrorException ex) {
                System.err.println("Implemantazione RMI : problema con new Porte");
                System.exit(ERRORE);
            }
        }
        /* il file non esiste */
        try {
            /* creazione lista IP */
            ListaPeer lista = new ListaPeer();
            try {
                /* creo una nuova listaPeer associata al nuovo descrittore */
                lista.add(new NetRecord(ip, porta, false));
            } catch (Exception e) {
                System.err.println("Implementazione RMI : Errore");
                System.exit(ERRORE);
            }
            /* creo welcom socket per Tcp */
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            welcome = (SSLServerSocket) sslserversocketfactory.createServerSocket(0);
            /* creo Datagram Socket per Udp */
            alive = new DatagramSocket();
            /* creazione thread */
            Thread t1 = new Thread(new TrackerTCP(welcome, lista, d));
            Thread t2 = new Thread(new TrackerUDP(alive, lista));
            t1.start();
            t2.start();
            /* aggiornamento descrittore */
            d.setPortaTCP(welcome.getLocalPort());
            d.setPortaUDP(alive.getLocalPort());
            d.setId(BitCreekServer.idcount++);
            // salviamo id su file
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("./id.info")));
            out.writeInt(BitCreekServer.idcount);
            out.close();
            /* aggiornamento tabella */
            tabella.add(d);
            /* avvio anche il thread trimmer sulla lista usando il timer comune del server */
            BitCreekServer.timer.schedule(new Trimmer(lista, d), 1000, 1000);
        } catch (IOException ex) {
            System.err.println("Implementazione RMI : IOException");
            System.exit(ERRORE);
        }
        Porte ris = null;
        try {
            ris = new Porte(welcome.getLocalPort(), alive.getLocalPort(), d.getId());
        } catch (ErrorException ex) {
        }
        ris.setPubblicato();
        return ris;
    }
}
