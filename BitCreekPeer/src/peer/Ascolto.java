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
                    continue;
                }
                try {
                    in = new ObjectInputStream(scambio.getInputStream());
                    out = new ObjectOutputStream(scambio.getOutputStream());
                } catch (IOException ex) {
                    System.out.println("Ascolto : IOExcpetion dopo out =  e in =");
                    Logger.getLogger(Ascolto.class.getName()).log(Level.SEVERE, null, ex);
                }
                try {
                    //RICEVO UN CONTATTO
                    Contact con = (Contact) in.readObject();
                    System.out.println("\n\n" + Thread.currentThread().getName() + " : ASCOLTO : contatto ricevuto : " + con.getIp() + "," + con.getSS() + "," + con.getId());
                    int swarmId = con.getId();
                    Creek contacted = peer.getCreek(swarmId);
                    /* controllo  esito */
                    if (contacted == null) {
                        System.out.println("Non appartengo a questo swarm");
                        scambio.close();
                        continue;
                    }
                    //INVIO IL BITFILED RELATIVO
                    int alreadyDownloaded = contacted.getScaricati();
                    out.writeObject(new Bitfield(contacted.getHave()));
                    System.out.println("INVIO BITFIELD\n");
                    /*
                     * EFFETTUO IL CONTROLLO CHE L'OGGETTO NON ESISTA GIA
                     */
                    //System.out.println(Thread.currentThread().getName() + " : Ascolto : con.getSS :: " + con.getSS());
                    Connessione conn = null;
                    Connessione toModify = contacted.presenzaConnessione(scambio.getInetAddress(), con.getSS());
                    if (toModify == null) {
                        conn = new Connessione();
                        conn.set(false, scambio, in, out, null, con.getSS());
                        contacted.addConnessione(conn);
                        System.out.println(Thread.currentThread().getName() + "CONNESSIONE AGGIUNTA");
                        //CREO IL THREAD RELATIVO IN UPLOAD
                        peer.addTask(new Uploader(conn, contacted, alreadyDownloaded));
                    } else {
                        System.out.println(Thread.currentThread().getName() + "CONNESSIONE GIA PRESENTE");
                        /* in teoria se esiste già una connessione in upload non dovrei fare niente
                        Non c'è il controllo perchè per come è fatto sotto non ricontatto l'altro
                        peer se ho già una connessione in down quindi vado avanti creando thread
                        di upload ------------> controllare che sia così anche (e se) quando
                        ci occuperemo dei download stoppati e ripartiti !!!*/
                        //DA RIFARE/!!
                        //toModify.setUp(scambio,in,out);
                        //conn = toModify;
                        toModify.set(false, scambio, in, out, null, con.getSS());
                        //CREO IL THREAD RELATIVO IN UPLOAD
                        peer.addTask(new Uploader(toModify, contacted,alreadyDownloaded));
                    }
                    //CREO IL THREAD RELATIVO IN UPLOAD
                    //peer.addTask(new Uploader(conn, contacted));
                    /* chiudo i file : NO */
                    //in.close();
                    //out.close();
                    /* incremento numero di connessioni */
                    System.out.println("ASCOLTO: AGGIUNGO CONNESSIONE NEL MONITOR");
                    peer.incrConnessioni();
                    /* incremento nuemro peer in upload se sono seeder */
                    if (!contacted.getStato()) {
                        contacted.incrPeer();
                    }
                /* operazioni ulteriori se sono leecher : creo connessione in down
                Lo devo fare solo se non ho già una connessione in down, non sono
                seeder e posso creare connessioni !!!! */
                if (contacted.getStato() && conn.DownNull() && peer.getConnessioni() < BitCreekPeer.MAXCONNESSIONI) {
                System.out.println("\n\n" + Thread.currentThread().getName() + "SONO ENTRATO PERCHE` SONO LEECHER\n\n");
                Contact mycon = new Contact(peer.getMioIp(), peer.getPortaRichieste(), swarmId);
                SocketAddress sa = new InetSocketAddress(con.getIp(), con.getSS());
                Socket mysock = new Socket();
                mysock.connect(sa, BitCreekPeer.TIMEOUTCONNESSIONE);
                System.out.println("HO FATTO LA CONNECT");
                ObjectOutputStream output = new ObjectOutputStream(mysock.getOutputStream());
                System.out.println("HO FATTO L'OUTPUT");
                ObjectInputStream input = new ObjectInputStream(mysock.getInputStream());
                System.out.println("HO FATTO L'INPUT");
                output.writeObject(mycon);
                Bitfield b = (Bitfield) input.readObject();
                System.out.println("BITFIELD: "+b.toString());
                // modifica
                //conn.setDown(mysock, input, output);
                //conn.setSocketDown(mysock);
                //conn.setBitfield(b.getBitfield());

                /* Prova nuovo metodo */
                conn.set(true, mysock, input, output, b.getBitfield(), con.getSS());
                System.out.println(Thread.currentThread().getName() + "Creo thread downloader perchè ho inviato mie credenzioali");
                // aggiungo thread per download
                peer.addTask(new Downloader(contacted, conn));
                // incremento numero connessioni
                System.out.println("ASCOLTO CALLBACK: AGGIUNGO CONNESSIONE");
                peer.incrConnessioni();
                // incremento numero peer in download
                contacted.incrPeer();
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
