package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Task che si occupa di ascoltare le richieste
 * di connessione dei peer.
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ServerListener implements Runnable {

    /* Costanti */
    /** Definisce NULL */
    private static final int NULL = -1;

    /* Variabili d' istanza */
    /** MetaInfo */
    private MetaInfo tabella;
    /** Socket di benvenuto */
    private ServerSocket welcome;

    /**
     * Costruttore
     * @param tabella
     * @param welcome
     */
    public ServerListener(MetaInfo tabella, ServerSocket welcome) {
        this.tabella = tabella;
        this.welcome = welcome;
    }

    /**
     * Corpo del task
     */
    public void run() {
        Socket connessione = null;
        Socket prova = null;
        InetAddress peer = null;
        DataInputStream in = null;
        int porta = NULL;

        /****************** il ciclo ******************/
        
        while (true) {
            try {
                connessione = welcome.accept();
                in = new DataInputStream(connessione.getInputStream());
                porta = in.readInt();
                peer = connessione.getInetAddress();
                connessione.close();
                prova = new Socket(peer, porta);
                prova.close();
            } catch (IOException ex) {
                System.err.println("Impossibile stabilire una connessione con : " + peer.getHostAddress());
            }
        }
    }
}
