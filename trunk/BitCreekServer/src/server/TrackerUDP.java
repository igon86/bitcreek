package server;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task che implenta il tracker UDP
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class TrackerUDP implements Runnable {

    /* Costanti */
    /**
     * Timeout dopo il quale un peer che non si è ancora
     * fatto sentire viene considerato disconnesso
     */
    public static final int TIMEOUT = 10000;
    /** Definisce NULL */
    private final int NULL = -1;

    /* Variabili d'istanza */
    /** Socket UDP */
    private DatagramSocket alive;
    /** Lista peer */
    private ListaPeer lista;

    /**
     * Costruttore
     * @param d
     * @param lista
     */
    public TrackerUDP(DatagramSocket d, ListaPeer lista) {
        this.alive = d;
        this.lista = lista;
    }

    /**
     * Corpo del task
     */
    public void run() {
        while (true) {
            byte buffer[] = new byte[256];
            DatagramPacket dpin = new DatagramPacket(buffer, buffer.length);
            int porta = NULL;
            InetAddress rcv = null;
            boolean stato = true;
            try {
                alive.receive(dpin);
                ByteArrayInputStream bin = new ByteArrayInputStream(dpin.getData(), 0, dpin.getLength());
                DataInputStream din = new DataInputStream(bin);
                porta = din.readInt();
                stato = din.readBoolean();
                rcv = dpin.getAddress();
            } catch (IOException ex) {
                Logger.getLogger(TrackerUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            lista.touchPeer(rcv, porta, stato);
        }
    }
}
