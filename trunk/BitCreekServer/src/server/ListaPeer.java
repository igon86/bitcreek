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
 * @author bande
 */
public class ListaPeer extends ArrayList<NetRecord> implements Serializable {

    public static final long serialVersionUID = 16;

    public ListaPeer() {
        super();
    }
    @Override
    public synchronized boolean add(NetRecord record) {
        return super.add(record);
    }

    @Override
    public synchronized boolean remove(Object record) {
        return super.remove(record);
    }

    /**l'ho messo synchronized perché il Tracker UDP potrebbe modificarlo
     * nel mentre lo esamino.
     * @param ip
     * @return
     */
    public synchronized void touchPeer(InetAddress ip, int porta, boolean stato) {
        boolean trovato = false;
        NetRecord r;
        Iterator h = this.iterator();
        while (h.hasNext() && !trovato) {
            /** se il netrecord e` gia presente lo toucho*/
            r = (NetRecord) h.next();
            if (r.getIp().equals(ip) && r.getPorta() == porta && r.getStato() == stato) {
                //System.out.println("Touchato peer con ip " + ip.getHostAddress() + ", porta : " + porta);
                r.touch();
                trovato = true;
            }
        }
        // caso peer che riappare
        if (!trovato) {
            NetRecord nuovo = null;
            try {
                nuovo = new NetRecord(ip, porta, stato);
            } catch (ErrorException ex) {
                // --------> da gestire
            }
            this.add(nuovo);
        }
    }

    /**elimina tutti i Peer che non hanno mandato messaggi di keepalive
     * ritorna il numero di seeder e leecher attualmente presenti
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
                System.out.println("Rimosso net record con porta : " + a.getPorta());
            } else {
                if (a.getStato() == false) seeders++;
                else leechers ++;
            }
        }
        return new NumPeer(seeders,leechers);
    }
}
