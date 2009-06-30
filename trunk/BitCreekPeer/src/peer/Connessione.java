package peer;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.net.SocketTimeoutException;

/**
 * Classe che virtualizza la connessione tra 2 peer
 * facenti parte di uno swarm
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Connessione implements Serializable, Comparable<Connessione> {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 45;
    /** Costante che definisce una connessione CHOCKED */
    protected static final boolean CHOKED = false;
    /** Costante che definisce una connessione UNCHOCKED */
    protected static final boolean UNCHOKED = true;
    /** Costante che definisce il timeout sulla receive */
    private static final int TIMEOUT = 100;

    /* Variabili d' istanza*/
    /** Socket in download */
    private Socket down;
    /** Socket in upload */
    private Socket up;
    /** Porta di ascolto del partner */
    private int portaVicino;
    /** IP del partner */
    private InetAddress ipVicino;
    /** Flusso in ingresso sulla socket in download */
    private ObjectInputStream inDown;
    /** Flusso in uscita sulla socket in download */
    private ObjectOutputStream outDown;
    /** Flusso in ingresso sulla socket in upload */
    private ObjectInputStream inUp;
    /** Flusso in uscita sulla socket in upload */
    private ObjectOutputStream outUp;
    /** Stato della connessione in download */
    private boolean statoDown;
    /** Stato della connessione in upload */
    private boolean statoUp;
    /** Interesse della connessione in download */
    private boolean interesseDown;
    /** Interesse della connessione in upload */
    private boolean interesseUp;
    /** Bitfield del partner */
    private boolean[] bitfield;
    /** Flag che indica se è possibile fare upload su questa connessione */
    private boolean uploadable;
    /** Numero chunk scaricati su questa connessione */
    private int downloaded;
    /** Flag che indica di terminare la connessione */
    private boolean termina;

    /**
     * Costruttore
     */
    public Connessione() {
        this.termina = false;
        this.uploadable = true;
    }

    /**
     * Metodo che testa se è possibile fare upload.
     * Se non è possibile ferma il chiamante
     */
    public synchronized void possoUploadare() {
        while (!this.uploadable) {
            try {
                wait();
            } catch (InterruptedException ex) {
                System.err.print("Connessione : sono stato interrotto");
            }
        }
    }

    /**
     * Metodo che dice che è possibile fare upload.
     * Sveglia chi era in attesa
     */
    public synchronized void puoiUploadare() {
        this.uploadable = true;
        notify();
    }

    /**
     * Metodo che dice che non è possibile fare upload su questa
     * connessione
     */
    public synchronized void nonPuoiUploadare() {
        this.uploadable = false;
    }

    /**
     * Metodo fico che setta la connessione
     * @param download flag che indica se le modifiche vanno
     * fatte in download o in upload
     * @param s socket instaurata
     * @param in flusso in ingresso associato ad s
     * @param out flusso in uscita associato ad s
     * @param bitfield chunk del partner
     * @param portaVicino porta del partner
     */
    public synchronized void set(boolean download, Socket s, ObjectInputStream in, ObjectOutputStream out, boolean[] bitfield, int portaVicino) {
        if (download) {
            this.bitfield = bitfield;
            this.down = s;
            try {
                this.down.setSoTimeout(TIMEOUT);
            } catch (SocketException ex) {
            } 
            this.inDown = in;
            this.outDown = out;
            this.ipVicino = s.getInetAddress();
            this.portaVicino = portaVicino;
            this.downloaded = 0;
        } else {
            this.up = s;
            try {
                this.up.setSoTimeout(TIMEOUT);
            } catch (SocketException ex) {
            }
            this.inUp = in;
            this.outUp = out;
            this.ipVicino = s.getInetAddress();
            this.portaVicino = portaVicino;
        }
    }

    /**
     * Connessione
     * @param down socket in download
     * @param up socket in upload
     * @param bitfield chunk del partner
     * @param portaVicino porta in ascolto del partner
     * @deprecated E' preferibile utilizzare set
     */
    @Deprecated
    public Connessione(Socket down, Socket up, boolean[] bitfield, int portaVicino) {
        this.down = down;
        this.up = up;
        this.bitfield = bitfield;
        if (down != null) {
            this.ipVicino = down.getInetAddress();
            try {
                this.outDown = new ObjectOutputStream(down.getOutputStream());
                this.inDown = new ObjectInputStream(down.getInputStream());
            } catch (IOException ex) {
            }

        } else {
            this.ipVicino = up.getInetAddress();
            try {
                this.outUp = new ObjectOutputStream(up.getOutputStream());
                this.inUp = new ObjectInputStream(up.getInputStream());
            } catch (IOException ex) {
            }
        }
        this.portaVicino = portaVicino;
        this.downloaded = 0;
    }

    /**
     * Restituisce true se la socket in download è null
     * @return down == null
     */
    public synchronized boolean DownNull() {
        return down == null;
    }

    /**
     * Setta il flag di terminazione
     */
    public synchronized void setTermina() {
        this.termina = true;
    }

    /**
     * Restituisce il flag di terminazione
     * @return termina
     */
    public synchronized boolean getTermina() {
        return this.termina;
    }

    /**
     * Metodo utilizzato per controllare se una
     * connessione e` gia presente
     * @param ip ip del parner
     * @param porta porta in ascolto del partner
     * @return true se presente ; false altrimenti
     */
    public synchronized boolean confronta(InetAddress ip, int porta) {
        if (this.ipVicino.getHostAddress().compareTo(ip.getHostAddress()) == 0 && this.portaVicino == porta) {
            return true;
        } else {
            return false;
        }
    }

    /**
     * Effettua una send sulla socket in download
     * del Messaggio m
     * @param m Messaggio da inviare
     */
    public synchronized void sendDown(Messaggio m) {
        try {
            outDown.writeObject(m);
        } catch (IOException ex) {
        }
    }

    /**
     * Effettua una send sulla socket in upload
     * del Messaggio m
     * @param m Messaggio da inviare
     */
    public synchronized void sendUp(Messaggio m) {
        try {
            this.outUp.writeObject(m);
        } catch (IOException ex) {
        }
    }

    /**
     * Effettua una receive sulla socket in download
     * @return m Messaggio ricevuto
     */
    public synchronized Messaggio receiveDown() {
        try {
            if (inDown != null) {
                return (Messaggio) inDown.readObject();
            }
        } catch (SocketTimeoutException ex) {
            /* scattato il timeout */
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    /**
     * Effettua una receive sulla socket in upload
     * @return m Messaggio ricevuto
     */
    public synchronized Messaggio receiveUp() {
        try {
            if (inUp != null) {
                return (Messaggio) inUp.readObject();
            }
        } catch (SocketTimeoutException ex) {
            return null;
        } catch (IOException ex) {
        } catch (ClassNotFoundException ex) {
        }
        return null;
    }

    /**
     * Resetta lo stream sulla socket in download
     */
    public synchronized void ResetDown() {
        try {
            outDown.reset();
        } catch (IOException ex) {
        }
    }

    /**
     * Resetta lo stream sulla socket in upload
     */
    public synchronized void ResetUp() {
        try {
            outUp.reset();
        } catch (IOException ex) {
        }
    }

    /**
     * Metodo che setta i bitfield all' array
     * ricevuto
     * @param b array di bitfield
     */
    public synchronized void setBitfield(boolean[] b) {
        this.bitfield = b;
    }

    /**
     * Setta i bitfield dell' array nelle posizioni presenti
     * nell' array passato come parametro .
     * @param toSet array di indici dei bitfield da settare
     */
    public synchronized void setArrayBitfield(int[] toSet) {
        for (int i = 0; i < toSet.length; i++) {
            this.bitfield[toSet[i]] = true;
        }
    }

    /**
     * Setta il bitfield nella posixione id
     * @param id indice del bitfield nell'array da settare
     * @deprecated E' preferibile utilizzare setArrayBitfield
     */
    @Deprecated
    public synchronized void setIndexBitfield(int id) {
        this.bitfield[id] = true;
    }

    /**
     * Restituisce il numero di pezzi scaricati su questa connessione
     * @return downloaded
     */
    public synchronized int getDownloaded() {
        return this.downloaded;
    }

    /**
     * Restituisce i bitfield del partner
     * @return bitfield
     */
    public synchronized boolean[] getBitfield() {
        return this.bitfield;
    }

    /**
     * Restituisce la porta in ascolto del partner
     * @return PottaVicino
     */
    public synchronized int getPortaVicino() {
        return this.portaVicino;
    }

    /**
     * Restituisce l' IP del parner
     * @return ipVicino
     */
    public synchronized InetAddress getIPVicino() {
        return this.ipVicino;
    }

    /**
     * Restituicse lo stato della connessione in download
     * @return statoDown
     */
    public synchronized boolean getStatoDown() {
        return this.statoDown;
    }

    /**
     * Restituicse lo stato della connessione in upload
     * @return statoUp
     */
    public synchronized boolean getStatoUp() {
        return this.statoUp;
    }

    /**
     * Restituisce l' interesse della connessione in download
     * @return interesseDown
     */
    public synchronized boolean getInteresseDown() {
        return this.interesseDown;
    }

    /**
     * Restituisce l' interesse della connessione in upload
     * @return interesseUp
     */
    public synchronized boolean getInteresseUp() {
        return this.interesseUp;
    }

    /**
     * Setta la connessione in upload
     * @param up socket in upload
     * @param in flusso in ingresso associato ad up
     * @param out flusso in uscita associato ad up
     */
    public synchronized void setUp(Socket up, ObjectInputStream in, ObjectOutputStream out) {
        this.up = up;
        this.inUp = in;
        this.outUp = out;
    }

    /**
     * Setta la connessione in download
     * @param down socket in download
     * @param in flusso in ingresso associato a down
     * @param out flusso in uscita associato a down+
     */
    public synchronized void setDown(Socket down, ObjectInputStream in, ObjectOutputStream out) {
        this.down = down;
        this.inDown = in;
        this.outDown = out;
    }

    /**
     * Setta la stato della connessione in download
     * al parametro stato
     * @param stato nuovo stato
     */
    public synchronized void setStatoDown(boolean stato) {
        this.statoDown = stato;
    }

    /**
     * Setta la stato della connessione in upload
     * al parametro stato
     * @param stato nuovo stato
     */
    public synchronized void setStatoUp(boolean stato) {
        this.statoUp = stato;
    }

    /**
     * Setta la socket in upload a up
     * @param up nuova socket
     */
    public synchronized void setSocketUp(Socket up) {
        this.up = up;
    }

    /**
     * Setta la socket in download a down
     * @param down nuova socket
     */
    public synchronized void setSocketDown(Socket down) {
        this.down = down;
    }

    /**
     * Setta l' interesse della connessione in download
     * al parametro b
     * @param b nuovo interesse
     */
    public synchronized void setInteresseDown(boolean b) {
        this.interesseDown = b;
    }

    /**
     * Setta l' interesse della connessione in upload
     * al parametro b
     * @param b nuovo interesse
     */
    public synchronized void setInteresseUp(boolean b) {
        this.interesseUp = b;
    }

    /**
     * Incrementa il numero di pezzi scaricati
     * e restiusce il nuovo valore
     * @return downloaded
     */
    public synchronized int incrDown() {
        return ++this.downloaded;
    }

    /**
     * Compara la connessione con la connessione
     * passata come parametro in base ao pizzi scaricati
     * @param arg0 connessione da comparare
     * @return differenza dei pezzi scaricati
     */
    public int compareTo(Connessione arg0) {
        if (this.getInteresseUp()) {
            if (arg0.getInteresseUp()) {
                return arg0.downloaded - this.downloaded;
            } else {
                return (arg0.downloaded - this.downloaded) - 3000;
            }

        } else {
            if (arg0.getInteresseUp()) {
                return (arg0.downloaded - this.downloaded) + 3000;
            } else {
                return arg0.downloaded - this.downloaded;
            }
        }
    }
}
