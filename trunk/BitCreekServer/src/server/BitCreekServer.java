package server;

import condivisi.Descrittore;
import condivisi.ErrorException;
import condivisi.InterfacciaRMI;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.SocketException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Timer;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Server del protocollo BitCreek
 * @author Bandettini
 */
public class BitCreekServer {

    /* Costanti */
    private static final int ERRORE = 1;
    private static final int PORTA = 9999;
    private static final int PORTARMI = 10000;
    private static final int INIZIO = 3000;
    private static final int DURATA = 4000;

    /* Variabili */
    private MetaInfo tabella;
    /* socket per il test nat/firewall*/
    private int porta;
    private ServerSocket welcome;
    /* timer per i task ricorrenti */
    protected static Timer timer = new Timer();
    /* id univoco */
    protected static int idcount;

    /**
     * Costruttore
     */
    public BitCreekServer() {
        try {
            tabella = new MetaInfo();
            porta = PORTA;
            welcome = new ServerSocket(porta);
        } catch (IOException ex) {
            System.err.println("Problema creazione server socket sulla porta " + porta+": l'applicazione verrà chiusa");
            System.exit(ERRORE);
        }
        /* carico l'id univoco */
        ObjectInputStream in = null;
        try {
                in = new ObjectInputStream(new FileInputStream(new File("./id.info")));
                idcount = in.readInt();
                in.close();
            } catch (IOException ex) {
                idcount = 0;
            }

        /* creo la cartella per salvare le metainfo se non esiste */
        File dir = new File("./MetaInfo");
        dir.mkdir();
        /* per ogni file al suo interno ricostruisco le metainfo e avvio i tracker */
        SSLServerSocket ssl = null;
        DatagramSocket alive = null;
        Descrittore temp = null;
        for (File f : dir.listFiles()) {
            try {
                in = new ObjectInputStream(new FileInputStream(f));
            } catch (IOException ex) {
                System.err.println("Impossibile leggere dalle MetaInfo : l'applicazione verrà chiusa");
                System.exit(ERRORE);
            }
            try {
                temp = (Descrittore) in.readObject();
            } catch (IOException ex) {
                System.err.println("Impossibile leggere dalle MetaInfo : l'applicazione verrà chiusa");
                System.exit(ERRORE);
            } catch (ClassNotFoundException ex) {
                System.err.println("Impossibile trovare la classe Descrittore : l'applicazione verrà chiusa");
                System.exit(ERRORE);
            }
            tabella.add(temp);
            /* creo lista peer vuota */
            ListaPeer lista = new ListaPeer();
            /* creo sslserversocket sulla porta in cui era */
            SSLServerSocketFactory sslserversocketfactory =
                    (SSLServerSocketFactory) SSLServerSocketFactory.getDefault();
            try {
                ssl = (SSLServerSocket) sslserversocketfactory.createServerSocket(temp.getTCP());
            } catch (IOException ex) {
                System.err.println("Impossibile creare SSL server socket sulla porta " + temp.getTCP() + " : l'applicazione verrà chiusa");
                System.exit(ERRORE);
            }
            /* creo Datagram Socket per Udp sulla porta in cui era */
            try {
                alive = new DatagramSocket(temp.getUDP());
            } catch (SocketException ex) {
                System.err.println("Impossibile creare datagram socket sulla porta " + temp.getUDP() + " : l'applicazione verrà chiusa");
                System.exit(ERRORE);
            }
            /* creazione tracker */
            if (temp == null) {
                System.out.println("temp e` null");
            }
            Thread t1 = new Thread(new TrackerTCP(ssl, lista, temp));
            Thread t2 = new Thread(new TrackerUDP(alive, lista));
            t1.start();
            t2.start();
            /* avvio anche il thread trimmer sulla lista usando il timer comune del server */
            timer.schedule(new Trimmer(lista, temp), INIZIO, DURATA);
        }
    }

    public static void main(String[] args) {

        /* inizializzo il server*/
        BitCreekServer server = new BitCreekServer();

        /* creo thread di ascolto */
        Thread t1 = new Thread(new ServerListener(server.tabella, server.welcome));
        t1.start();

        /* creo thread di salvataggio */
        Thread t2 = new Thread(new ThreadSaver(server.tabella));
        t2.start();

        /* Attivazione RMI */
        InterfacciaRMI impl = null;
        try {
            try {
                impl = new ImplementazioneRMI(server.tabella);
            } catch (ErrorException ex) {
                System.err.println("Impossibilire avviare implementazione RMI : l' applicazione verrà chiusa");
                System.exit(ERRORE);
            }
            InterfacciaRMI stub = (InterfacciaRMI) UnicastRemoteObject.exportObject(impl, 0);
            Registry reg = LocateRegistry.createRegistry(PORTARMI);
            reg.rebind("MetodiRMI", stub);
        } catch (RemoteException e) {
            System.err.println("Impossibilire avviare implementazione RMI : l' applicazione verrà chiusa");
            System.exit(ERRORE);
        }
        System.out.println("\n   --------   SERVER   READY   !!!   --------   \n");
    }
}
