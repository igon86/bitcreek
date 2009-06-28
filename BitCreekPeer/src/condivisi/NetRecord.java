package condivisi;

import java.io.Serializable;
import java.net.InetAddress;
import java.util.Calendar;

/**
 * Classe che definisce Ip e porta in ascolto di un determinato peer
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class NetRecord implements Serializable {

    /* Costanti */
    /**
     * Costante che definisce la versione della classe
     */
    public static final long serialVersionUID = 18;
    /* Variabili d'istanza */
    /** IP del peer */
    private InetAddress ip;
    /** Porta del peer */
    private int porta;
    /** Istante in cui si è fatto l'ultimo keep-alive */
    private Calendar ultimoaccesso;
    /** Stato del netrecord */
    private boolean stato;

    /**
     * Costruttore
     * @param ip
     * @param porta
     * @param stato
     * @throws condivisi.ErrorException
     */
    public NetRecord(InetAddress ip, int porta, boolean stato) throws ErrorException {
        if (ip == null || porta <= 0) {
            throw new ErrorException("param null");
        }
        this.ip = ip;
        this.porta = porta;
        this.ultimoaccesso = Calendar.getInstance();
        /* stato viene sempre inizializzato dal tracker UDP tramite touchPeer di ListaPeer per evitare problemi*/
        this.stato = stato;
    }

    /**
     * Restituisce lo stato del peer associato al netrecord
     * @return stato
     */
    public boolean getStato() {
        return this.stato;
    }

    /**
     * Restituisce la porta
     * @return porta
     */
    public int getPorta() {
        return this.porta;
    }

    /**
     * Restituisce l'ip
     * @return ip
     */
    public InetAddress getIp() {
        return this.ip;
    }

    /**
     * Restituisce l'ultimo accesso
     * @return ultimoaccesso
     */
    public Calendar getTouch() {
        return this.ultimoaccesso;
    }

    /**
     * Setta l'ultimo accesso
     */
    public void touch() {
        this.ultimoaccesso = Calendar.getInstance();
    }

    /**
     * Metodo sovrascritto equals
     * @param a netrecord
     * @return true, false altrimenti
     */
    public boolean equals(NetRecord a) {
        if (this.ip.equals(a.ip)) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Metodo override
     * @return toString()
     */
    @Override
    public String toString() {
        return this.ip.getHostAddress() + " : " + this.ultimoaccesso.toString();
    }
}
