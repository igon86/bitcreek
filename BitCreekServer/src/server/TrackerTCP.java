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
 * @author bande
 */
public class TrackerTCP implements Runnable {

    private SSLServerSocket ss;
    private ListaPeer lista;
    private Descrittore d;

    /** la serverSocket viene passata dall'implementazione RMI, 
     * il descrittore viene passato per permettere le modifiche sul numero di peer aderenti allo swarm
     */
    public TrackerTCP(SSLServerSocket ss, ListaPeer lista, Descrittore d) {
        this.ss = ss;
        this.lista = lista;
        this.d = d;
    }

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
                        System.out.println("Sto scrivendo");
                        out.writeObject(i.next());
                    }
                    out.flush();
                    s.close();
                }
            } catch (IOException ex) {
                System.err.print("Errore ssl\n");
                ex.printStackTrace();
            }
        }
    }
}
