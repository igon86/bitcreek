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
 * @author Bandettini
 */
public class KeepAlive implements Runnable {

    /* Costanti */
    private final int ATTESA = 3000;

    /* Variabili d'istanza */
    private BitCreekPeer peer;

    /**
     * Costruttore
     */
    public KeepAlive(BitCreekPeer peer) throws ErrorException {
        if (peer == null) {
            throw new ErrorException("Param null");
        }
        this.peer = peer;
    }

    /**
     * corpo del task
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
                    try {
                        arraydescr = peer.getDescr();
                    } catch (ErrorException ex) {
                        System.err.println("ErrorException");
                    }
                    synchronized (arraydescr) {
                        for (Creek c : arraydescr) {
                            porta = c.getUDP();

                            /* invio keep alive x ogni descr */

                            //System.out.println("invio alive per creek " + c.getName() + ",porta richieste : " + peer.getPortaRichieste());
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
                // ipServer è null --> sono disconnesso
            }
            // aspetto
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                System.err.println("Interrotto");
            }
        }
    }
}
