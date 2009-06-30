package server;

import condivisi.ErrorException;
import condivisi.NetRecord;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;

/**
 * Classe che implementa una lista di IP
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ListaPeer extends ArrayList<NetRecord> implements Serializable {

    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 16;

    /**
     * Costruttore
     */
    public ListaPeer() {
        super();
    }

    /**
     * Aggiunge un netrecord alla lista
     * @param record netrecord da aggiungere
     * @return esito operazione
     */
    @Override
    public synchronized boolean add(NetRecord record) {
        return super.add(record);
    }

    /**
     * Rimuove il netrecord record salla lista
     * @param record netrecord da eliminare
     * @return esito operazione
     */
    @Override
    public synchronized boolean remove(Object record) {
        return super.remove(record);
    }

    /**
     * Effettua l' aggiornamneto della lista di
     * peer
     * @param ip IP del peer che ha fatto keep-alive
     * @param porta porta del peer che ha fatto keep-alive
     * @param stato
     */
    public synchronized void touchPeer(InetAddress ip, int porta, boolean stato) {
        boolean trovato = false;
        NetRecord r;
        Iterator h = this.iterator();
        while (h.hasNext() && !trovato) {
            /** se il netrecord e` gia presente lo toucho*/
            r = (NetRecord) h.next();
            if (r.getIp().getHostAddress().compareTo(ip.getHostAddress()) == 0 && r.getPorta() == porta && r.getStato() == stato) {
                r.touch();
                trovato = true;
            }
        }
        /* caso peer che riappare */
        if (!trovato) {
            NetRecord nuovo = null;
            try {
                nuovo = new NetRecord(ip, porta, stato);
            } catch (ErrorException ex) {
            }
            this.add(nuovo);
        }
    }

    /**
     * Elimina tutti i Peer che non hanno mandato messaggi di keepalive
     * ritorna il numero di seeder e leecher attualmente presenti
     * @return new NumPeer(seeders,leechers)
     */
    public synchronized NumPeer trimPeer() {
        int seeders = 0;
        int leechers = 0;
        Iterator h = this.iterator();
        while (h.hasNext()) {
            NetRecord a = (NetRecord) h.next();
            long test = Calendar.getInstance().getTimeInMillis() - a.getTouch().getTimeInMillis();
            if (test > TrackerUDP.TIMEOUT) {
                h.remove();
            } else {
                if (a.getStato() == false) {
                    seeders++;
                } else {
                    leechers++;
                }
            }
        }
        return new NumPeer(seeders, leechers);
    }
}
