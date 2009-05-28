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
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Implementazione dell'interfaccia RMI
 * @author bande
 */
public class ImplementazioneRMI implements InterfacciaRMI {

    /* Variabili d'istanza */
    private MetaInfo tabella;

    /**
     * Costruttore
     * @param tabella
     * @throws condivisi.ErrorException
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
     * @param nomefile
     * @param ind
     * @return lista di descrittori
     */
    public ArrayList<Descrittore> ricerca(String nomefile, InetAddress ind) {
        System.out.println("RICERCA");
        ArrayList<Descrittore> ris = tabella.cerca(nomefile);
        Iterator<Descrittore> i = ris.iterator();
        Descrittore temp = null;
        InterfacciaCallback cb = null;
        // eseguo le callback
        while (i.hasNext()) {
            temp = i.next();
            cb = temp.getCallback();
            try {
                cb.notifyMe(ind, temp.getName());
            } catch (RemoteException ex) {
            }
        }
        // restituisco il risultato
        return ris;
    }

    /**
     * Aggiunge il descrittore d alle metainfo
     * @param d
     * @return numero porta dei tracker TCP e UDP
     * @throws java.rmi.RemoteException
     */
    public Porte inviaDescr(Descrittore d, InetAddress ip, int porta) {
        System.out.println("inviaDescrs");
        SSLServerSocket welcome = null;
        DatagramSocket alive = null;
        Descrittore temp = null;
        // controllo che il file non sia già pubblicato
        if ((temp = tabella.presenza(d)) != null) {
            try {
                // il file esiste già --> aggiungo il nuovo peer allo swarm
                // SI NOTA COME AL PEER LA COSA SIA DEL TUTTO TRASPARENTE
                // L'UNICA DIFFERENZA E` CHE NON SARA` REGISTRATO
                //PER LA CALLBACK
                return new Porte(temp.getTCP(), temp.getUDP(), temp.getId());
            } catch (ErrorException ex) {
                // --------> da gestire
            }
        }
        // il file non esiste
        try {
            // creazione lista IP
            ListaPeer lista = new ListaPeer();
            try {
                /**creo una nuova listaPeer associata al nuovo descrittore*/
                lista.add(new NetRecord(ip, porta, false));
            } catch (Exception e) {
                System.err.println("Errore");
                Logger.getLogger(ImplementazioneRMI.class.getName()).log(Level.SEVERE, null, e);
                System.exit(-1);
            }
            // creo welcom socket per Tcp
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            welcome = (SSLServerSocket) sslserversocketfactory.createServerSocket(0);
            // creo Datagram Socket per Udp
            alive = new DatagramSocket();
            // creazione thread
            Thread t1 = new Thread(new TrackerTCP(welcome, lista, d));
            Thread t2 = new Thread(new TrackerUDP(alive, lista));
            t1.start();
            t2.start();
            // aggiornamento descrittore
            d.setPortaTCP(welcome.getLocalPort());
            d.setPortaUDP(alive.getLocalPort());
            d.setId(BitCreekServer.idcount++);
            // salviamo id su file
            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File("./id.info")));
            out.writeInt(BitCreekServer.idcount);
            out.close();
            // aggiornamento tabella
            tabella.add(d);
            /**avvio anche il thread trimmer sulla lista usando il timer comune del server*/
            BitCreekServer.timer.schedule(new Trimmer(lista,d), 1000, 1000);

        } catch (IOException ex) {
            Logger.getLogger(ImplementazioneRMI.class.getName()).log(Level.SEVERE, null, ex);
        }
        Porte ris = null;
        try {
            ris = new Porte(welcome.getLocalPort(), alive.getLocalPort(), d.getId());
        } catch (ErrorException ex) {
            // --------> da gestire
        }
        ris.setPubblicato(); // comunico che ha pubblicato il file
        return ris;
    }
}
