package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
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
    //l'oggetto connessione e` l'unico abilitato ad inviare messaggi sulle socket
    Socket down;
    Socket up;
    //identificativo unico dell'altro peer sulla connessione
    int portaVicino;
    InetAddress ipVicino;
    ObjectInputStream inDown;
    ObjectOutputStream outDown;
    ObjectInputStream inUp;
    ObjectOutputStream outUp;
    //CHOKED o UNCHOKED
    boolean statoDown;
    boolean statoUp;
    /**interesseDown viene inizializzato all'avvio del thread Downloader
     * interesseUp invece viene inizializzato a NOT_INTERESTED
     */
    //INTERESTED o NOT_INTERESTED
    boolean interesseDown;
    boolean interesseUp;
    boolean[] bitfield;
    int downloaded; /* numero pezzi scaricati su questa connessione */


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
     * Metodo utilizzato per controllare se una connessione e` gia presente
     * @param ip
     * @param porta
     * @return
     */
    public synchronized boolean confronta(InetAddress ip, int porta) {
        if (this.ipVicino.getHostAddress().compareTo(ip.getHostAddress()) == 0  && this.portaVicino == porta) {
            return true;
        } else {
            return false;
        }
    }

    //Virtualizzazione
    public synchronized void sendDown(Messaggio m) {
        if (outDown != null) {
            try {
                outDown.writeObject(m);
            } catch (IOException ex) {
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized void sendUp(Messaggio m) {
        if (outUp != null) {
            try {
                this.outUp.writeObject(m);
            } catch (IOException ex) {
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public synchronized Messaggio receiveDown() {
        try {
            if (inDown == null) {
                System.out.println(Thread.currentThread().getName() + " inDown non inizializzata, sei un programmatore BUSTA");
            }else{
                return (Messaggio) inDown.readObject();
            }
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized Messaggio receiveUp() {
        try {
            if (inUp == null) {
                System.out.println(Thread.currentThread().getName() + " inUp non inizializzata, sei un programmatore BUSTA");
            }else{
                return (Messaggio) inUp.readObject();
            }
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

    //GETTER
    public synchronized boolean[] getBitfied() {
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
