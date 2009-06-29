package peer;

import java.io.Serializable;
import java.net.InetAddress;

/**
 * Classe che rappresenta il primo messaggio
 * di Handshake inviato tra peer.
 * Comunica le informazioni del peer.
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Contact implements Serializable {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 45;
    /* Varibili d' istanza */
    /** Porta in ascolto del peer*/
    private int ss;
    /** IP del peer*/
    private InetAddress ip;
    /** Id dello swarm a cui il peer è interessato */
    private int id;

    /**
     * Costruttore
     * @param ip ip peer
     * @param ss porta peer
     * @param id id swarm
     */
    public Contact(InetAddress ip, int ss, int id) {
        this.id = id;
        this.ip = ip;
        this.ss = ss;
    }

    /**
     * Restituisce l 'IP del contact
     * @return ip
     */
    public InetAddress getIp() {
        return this.ip;
    }

    /**
     * Restituisce l 'id dello swarm del contact
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Restituisce la porta del peer del contact
     * @return ss
     */
    public int getSS() {
        return this.ss;
    }
}
