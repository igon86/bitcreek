package peer;

import condivisi.ErrorException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task che si occupa di ascoltare richieste sulla porta di ricezione
 * @author Bandettini
 */
public class Ascolto implements Runnable {

    /* Costanti */
    private final int ATTESA = 3000;

    /* Variabili d'istanza */
    private BitCreekPeer peer;

    /**
     * Costruttore
     */
    public Ascolto(BitCreekPeer peer) throws ErrorException {
        if (peer == null) {
            throw new ErrorException("Param null");
        }
        this.peer = peer;
    }

    /**
     * Corpo del task
     */
    public void run() {
        while (true) {
            try {

                /* GESTIONE ERRORE + RISCRIVERE STE MILLE ECCEZZIONI */
                
                ObjectInputStream in = null;
                ObjectOutputStream out = null;
                Socket scambio = null;
                peer.getIpServer().getHostAddress();

                /* se non posso accettare connessioni vado in sleep come se fossi disconnesso */
                if (peer.getConnessioni() >= BitCreekPeer.MAXCONNESSIONI) {
                    throw new NullPointerException();
                }
                try {
                    scambio = peer.getSS().accept();
                } catch (SocketTimeoutException e) {
                    // timeout scaduto : continuo a ciclare
                    continue;
                } catch (IOException ex) {
                    System.err.println(Thread.currentThread().getName() + " Sono stato disconnesso : la socket di benvenuto è stata chiusa");
                }
                try {
                    in = new ObjectInputStream(scambio.getInputStream());
                    out = new ObjectOutputStream(scambio.getOutputStream());
                } catch (IOException ex) {
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    //RICEVO UN CONTATTO
                    Contact con = (Contact) in.readObject();
                    System.out.println(Thread.currentThread().getName() + " : Ascolto : contatto ricevuto : " + con.getIp() +","+ con.getSS() +"," +con.getId());
                    int swarmId = con.getId();
                    Creek contacted = peer.getCreek(swarmId);
                    //INVIO IL BITFILED RELATIVO
                    out.writeObject(new Bitfield(contacted.getHave()));
                    /*
                     * EFFETTUO IL CONTROLLO CHE L'OGGETTO NON ESISTA GIA
                     */
                    System.out.println(Thread.currentThread().getName() + " : Ascolto : con.getSS :: " + con.getSS());
                    Connessione conn = new Connessione(null, scambio, null, con.getSS());
                    Connessione temp = null;
                    if ((temp = contacted.presenzaConnessione(conn)) == null) {
                        System.out.println(Thread.currentThread().getName() + " UPLOADER - SONO STATO CONTATTATO, connessione aggiunta");
                        contacted.addConnessione(conn);
                    } else {
                        System.out.println(Thread.currentThread().getName() + " UPLOADER - SONO STATO CONTATTATO, connessione gia presente");
                        /* in teoria se esiste già una connessione in upload non dovrei fare niente
                        Non c'è il controllo perchè per come è fatto sotto non ricontatto l'altro
                        peer se ho già una connessione in down quindi vado avanti creando thread
                        di upload ------------> controllare che sia così anche (e se) quando
                        ci occuperemo dei download stoppati e ripartiti !!!*/
                        temp.setSocketUp(scambio);
                        conn = temp;
                    }
                    //CREO IL THREAD RELATIVO IN UPLOAD
                    peer.addTask(new Uploader(conn,contacted));
                    /* chiudo i file : NO */
                    //in.close();
                    //out.close();
                    /* incremento numero di connessioni */
                    peer.incrConnessioni();
                    /* operazioni ulteriori se sono leecher : creo connessione in down

                    Lo devo fare solo se non ho già una connessione in down, non sono
                    seeder e posso creare connessioni !!!! */
                    if (contacted.getStato() == true/*&& !conn.downAttiva()*/ && peer.getConnessioni() < BitCreekPeer.MAXCONNESSIONI) {
                        System.out.println("\n\nSONO ENTRATO\n\n");
                            Contact mycon = new Contact(peer.getMioIp(), peer.getPortaRichieste(), swarmId);
                            SocketAddress sa = new InetSocketAddress(con.getIp(), con.getSS());
                            Socket mysock = new Socket();
                            mysock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                            ObjectInputStream input = new ObjectInputStream(mysock.getInputStream());
                            ObjectOutputStream output = new ObjectOutputStream(mysock.getOutputStream());
                            output.writeObject(mycon);
                            Bitfield b = (Bitfield) input.readObject();
                            conn.setSocketDown(mysock);
                            conn.setBitfield(b.getBitfield());
                            System.out.println("Creo thread downloader perchè ho inviato mie credenzioali");
                            /* aggiungo thread per download */
                            peer.addTask(new Downloader(contacted, conn));
                            /* incremento numero connessioni */
                            peer.incrConnessioni();
                    }
                } catch (IOException ex) {
                    System.out.println("IOException in Ascolto");
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                } catch (ClassNotFoundException ex) {
                    System.out.println("ClassNotFoundException in Ascolto");
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                }


            } catch (NullPointerException e) {
                /* ipServer è null --> sono disconnesso quindi aspetto */
                try {
                    Thread.sleep(ATTESA);
                } catch (InterruptedException ex) {
                    System.err.println(Thread.currentThread().getName() + " Interrotto thread Ascolto");
                }
            }
        }
    }
}
