package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe che virtualizza la Connessione tra 2 peer facenti parte di uno swarm
 * @author andrea
 */
public class Connessione implements Serializable {

    public static final long serialVersionUID = 45;
    //codici mnemonici per lo stato
    protected static final boolean CHOKED = false;
    protected static final boolean UNCHOKED = true;
    private static final int TIMEOUT = 300;
    //l'oggetto connessione e` l'unico abilitato ad inviare messaggi sulle socket
    private Socket down;
    private Socket up;
    //identificativo unico dell'altro peer sulla connessione
    private int portaVicino;
    private InetAddress ipVicino;
    private ObjectInputStream inDown;
    private ObjectOutputStream outDown;
    private ObjectInputStream inUp;
    private ObjectOutputStream outUp;
    //CHOKED o UNCHOKED
    private boolean statoDown;
    private boolean statoUp;
    /**interesseDown viene inizializzato all'avvio del thread Downloader
     * interesseUp invece viene inizializzato a NOT_INTERESTED
     */
    //INTERESTED o NOT_INTERESTED
    private boolean interesseDown;
    private boolean interesseUp;
    private boolean[] bitfield;
    private int downloaded; /* numero pezzi scaricati su questa connessione */
    private boolean termina; /* flag che indica di terminare */

    /** Costruttore */
    public Connessione() {
        termina = false;
    }

    /**
     * Metodo fico che setta la connessione
     * @param download
     * @param s
     * @param in
     * @param out
     * @param bitfield
     * @param portaVicino
     */
    public synchronized void set(boolean download, Socket s, ObjectInputStream in, ObjectOutputStream out, boolean[] bitfield, int portaVicino) {
        if (download) {
            //CHIAMATO DA AVVIA
            System.out.print("\n\nCHIAMATO DA AVVIA\n\n");
            this.bitfield = bitfield;
            this.down = s;
            System.out.println("Setto timeout su down");
            try {
                this.down.setSoTimeout(TIMEOUT);
            } catch (SocketException ex) {
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("socket DOWN porta remota : " + s.getPort() + " porta locale : " + s.getLocalPort());
            this.inDown = in;
            this.outDown = out;
            this.ipVicino = s.getInetAddress();
            this.portaVicino = portaVicino;
            System.out.println("Avvia :::: Porta vicino : " + portaVicino);
            this.downloaded = 0;
        } else {
            //CHIAMATO DA ASCOLTA
            System.out.print("\n\nCHIAMATO DA ASSCOLTA\n\n");
            this.up = s;
            System.out.println("Setto timeout su up");
            try {
                this.up.setSoTimeout(TIMEOUT);
            } catch (SocketException ex) {
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
            System.out.println("socket UP porta remota : " + s.getPort() + " porta locale : " + s.getLocalPort());
            this.inUp = in;
            this.outUp = out;
            this.ipVicino = s.getInetAddress();
            this.portaVicino = portaVicino;
            System.out.println("Ascolta :::: Porta vicino : " + portaVicino);
        }
    }

    @Deprecated
    public Connessione(Socket down, Socket up, boolean[] bitfield, int portaVicino) {
        System.out.println(Thread.currentThread().getName() + "COSTRUTTORE CONNESSIONE");
        this.down = down;
        this.up = up;
        this.bitfield = bitfield;
        if (down != null) {
            System.out.println(Thread.currentThread().getName() + "CONNESSIONE IN DOWNLOAD");
            this.ipVicino = down.getInetAddress();
            try {
                this.outDown = new ObjectOutputStream(down.getOutputStream());
                System.out.println(Thread.currentThread().getName() + "WRAPPATO L'OUTPUTSTREAM");
                this.inDown = new ObjectInputStream(down.getInputStream());
                System.out.println(Thread.currentThread().getName() + "WRAPPATO L'INPUTSTREAM");
            } catch (IOException ex) {
                System.out.println(Thread.currentThread().getName() + " NON MI SI WRAPPANO LE SOCKET IN DOWNLOAD");
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }

        } else {
            System.out.println(Thread.currentThread().getName() + "CONNESSIONE IN UPLOAD");
            this.ipVicino = up.getInetAddress();
            try {
                this.outUp = new ObjectOutputStream(up.getOutputStream());
                System.out.println(Thread.currentThread().getName() + "WRAPPATO L'OUTPUTSTREAM");
                this.inUp = new ObjectInputStream(up.getInputStream());
                System.out.println(Thread.currentThread().getName() + "WRAPPATO L'INPUTSTREAM");
            } catch (IOException ex) {
                System.out.println(Thread.currentThread().getName() + " NON MI SI WRAPPANO LE SOCKET IN UPLOAD");
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.portaVicino = portaVicino;
        this.downloaded = 0;
        //Il binding degli stream alle socket viene effettuato solo al momento opportuno
        //dai thread
        System.out.println(Thread.currentThread().getName() + "TERMINATO COSTRUTTORE CONNESSIONE");
    }

    /**
     * Restituisce true se la socket in download è null
     * @return
     */
    public synchronized boolean DownNull() {
        return down == null;
    }

    /**
     * Setta il flag di terminazione
     */
    public synchronized void setTermina(){
        this.termina = true;
    }

    /**
     * Restituisce il flag di terminazione
     * @return termina
     */
    public synchronized boolean getTermina(){
        return this.termina;
    }

    /**
     * Metodo utilizzato per controllare se una connessione e` gia presente
     * @param ip
     * @param porta
     * @return
     */
    public synchronized boolean confronta(InetAddress ip, int porta) {
        if (this.ipVicino.getHostAddress().compareTo(ip.getHostAddress()) == 0 && this.portaVicino == porta) {
            return true;
        } else {
            return false;
        }
    }

    //Virtualizzazione
    public synchronized void sendDown(Messaggio m) {
        try {
            outDown.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void sendUp(Messaggio m) {
        try {
            this.outUp.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized Messaggio receiveDown() {
        try {
            if (inDown == null) {
                System.out.println(Thread.currentThread().getName() + " inDown non inizializzata, sei un programmatore BUSTA");
            } else {
                return (Messaggio) inDown.readObject();
            }
        } catch (SocketTimeoutException ex) {
            System.out.println("TIMEOUT di merda che finalmente rilascia");
        } catch (IOException ex) {
            System.out.println(Thread.currentThread().getName() + " IO Exception nella receiveDown");
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            System.out.println("ClassNotFound Exception nella receiveDown");
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized Messaggio receiveUp() {
        try {
            if (inUp == null) {
                System.out.println(Thread.currentThread().getName() + " inUp non inizializzata, sei un programmatore BUSTA");
            } else {
                return (Messaggio) inUp.readObject();
            }
        } catch (SocketTimeoutException ex) {
            System.out.println("TIMEOUT di merda che finalmente rilascia");
            return null;
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized void ResetDown() {
        try {
            outDown.reset();
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public synchronized void ResetUp() {
        try {
            outUp.reset();
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //chiamato in seguito ad un messaggio di have
    public synchronized void setBitfield(boolean[] b) {
        this.bitfield = b;
    }
    
    public synchronized void setIndexBitfield(int id){
        this.bitfield[id] = true;
    }

    //GETTER

    public synchronized boolean[] getBitfield() {
        return this.bitfield;
    }

    public synchronized int getPortaVicino() {
        return this.portaVicino;
    }

    public synchronized InetAddress getIPVicino() {
        return this.ipVicino;
    }

    public synchronized boolean getStatoDown() {
        return this.statoDown;
    }

    public synchronized boolean getStatoUp() {
        return this.statoUp;
    }

    public synchronized boolean getInteresseDown() {
        return this.interesseDown;
    }

    public synchronized boolean getInteresseUp() {
        return this.interesseUp;
    }

    //SETTER
    public synchronized void setUp(Socket up, ObjectInputStream in, ObjectOutputStream out) {
        this.up = up;
        this.inUp = in;
        this.outUp = out;
    }

    public synchronized void setDown(Socket down, ObjectInputStream in, ObjectOutputStream out) {
        this.down = down;
        this.inDown = in;
        this.outDown = out;
    }

    public synchronized void setStatoDown(boolean stato) {
        this.statoDown = stato;
    }

    public synchronized void setStatoUp(boolean stato) {
        this.statoUp = stato;
    }

    public synchronized void setSocketUp(Socket up) {
        this.up = up;
    }

    public synchronized void setSocketDown(Socket down) {
        this.down = down;
    }

    public synchronized void setInteresseDown(boolean b) {
        this.interesseDown = b;
    }

    public synchronized void setInteresseUp(boolean b) {
        this.interesseUp = b;
    }

    public synchronized int incrDown() {
        return ++this.downloaded;
    }
}
