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
 * @author bande
 */

public class TrackerUDP implements Runnable{

    /* Costanti */
    public static final int TIMEOUT = 10000;
    private final int NULL = -1;

    /* Variabili d'istanza */
    private DatagramSocket alive;
    private ListaPeer lista;
    
    public TrackerUDP(DatagramSocket d,ListaPeer lista){
        this.alive = d;
        this.lista = lista;
    }
    
    public void run() {
        while(true){
            byte buffer[] = new byte[256];
            DatagramPacket dpin = new DatagramPacket(buffer,buffer.length);
            int porta = NULL;
            InetAddress rcv = null;
            boolean stato = true;
            try {
                alive.receive(dpin);
                ByteArrayInputStream bin = new ByteArrayInputStream(dpin.getData(),0,dpin.getLength());
                DataInputStream din = new DataInputStream(bin);
                porta = din.readInt();
                stato = din.readBoolean();
                rcv = dpin.getAddress();
            } catch (IOException ex) {
                Logger.getLogger(TrackerUDP.class.getName()).log(Level.SEVERE, null, ex);
            }
            //System.out.println("Ricevuto pk da "+ rcv.getHostAddress()+", porta : "+porta+", stato : "+stato);
            lista.touchPeer(rcv,porta,stato);
        }
    }

}
