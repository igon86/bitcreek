package peer;

import condivisi.ErrorException;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;

/**
 * Task che si occupa di mandare msg di keepalive al server
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class KeepAlive implements Runnable {

    /* Costanti */
    /**
     * Definizione del tempo di attesa tra un invio dei keep-alive
     * e il successivo
     */
    private final int ATTESA = 3000;

    /* Variabili d'istanza */
    /** Peer */
    private BitCreekPeer peer;

    /**
     * Costruttore
     * @param peer 
     * @throws ErrorException se peer è null
     */
    public KeepAlive(BitCreekPeer peer) throws ErrorException {
        if (peer == null) {
            throw new ErrorException("Param null");
        }
        this.peer = peer;
    }

    /**
     * Corpo del task
     */
    public void run() {
        int porta = -1;
        DatagramSocket s = null;
        byte[] mess = new byte[256];
        DatagramPacket pk = null;
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(bout);
        ArrayList<Creek> arraydescr = null;
        try {
            s = new DatagramSocket();
        } catch (SocketException ex) {
            System.err.println("SocketException");
        }
        while (true) {
            try {
                // se sono connesso
                if (peer.getIpServer() != null) {
                    arraydescr = peer.getDescr();
                    synchronized (arraydescr) {
                        for (Creek c : arraydescr) {
                            porta = c.getUDP();
                            /* invio keep alive x ogni descr */
                            try {
                                dout.writeInt(peer.getPortaRichieste());
                                dout.writeBoolean(c.getStato());
                                mess = bout.toByteArray();
                                pk = new DatagramPacket(mess, mess.length, peer.getIpServer(), porta);
                                pk.setData(mess, 0, mess.length);
                                pk.setLength(mess.length);
                                s.send(pk);
                                bout.reset();
                            } catch (IOException ex) {
                                System.err.println("IOException");
                            }
                        }
                    }
                }
            } catch (NullPointerException e) {
                /* ipServer è null --> sono disconnesso */
            }
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                System.err.println("Interrotto");
            }
        }
    }
}
