package server;

import condivisi.Descrittore;
import condivisi.NetRecord;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;

/**
 * Task che implementa il tracker TCP
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class TrackerTCP implements Runnable {

    /* Variabili d'istanza */
    /** Server Socket con SSL */
    private SSLServerSocket ss;
    /** Lista dei peer */
    private ListaPeer lista;
    /** Descrittore */
    private Descrittore d;

    /**Costruttore
     * la serverSocket viene passata dall'implementazione RMI,
     * il descrittore viene passato per permettere le modifiche sul numero di peer aderenti allo swarm
     * @param ss 
     * @param lista
     * @param d
     */
    public TrackerTCP(SSLServerSocket ss, ListaPeer lista, Descrittore d) {
        this.ss = ss;
        this.lista = lista;
        this.d = d;
    }

    /**
     * corpo del task
     */
    public void run() {
        while (true) {
            /* accetto connessioni TCP/SSL */
            SSLSocket s = null;
            ObjectOutputStream out = null;
            try {
                s = (SSLSocket) ss.accept();
                /**aggiungere al descrittore ormai deprecato d.addseeder()*/
                out = new ObjectOutputStream(s.getOutputStream());
                /* invio al client la dimensione */
                out.writeInt(lista.size());
                /* invio al client tutti i netrecord della lista */
                synchronized (this.lista) {
                    Iterator<NetRecord> i = lista.iterator();
                    while (i.hasNext()) {
                        out.writeObject(i.next());
                    }
                    out.flush();
                    s.close();
                }
            } catch (IOException ex) {
                System.err.print("Errore ssl\n");
            }
        }
    }
}
