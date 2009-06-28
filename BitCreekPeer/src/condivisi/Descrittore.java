package condivisi;

import java.io.Serializable;

/**
 * Classe che definisce il descrittore di un file
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Descrittore implements Serializable {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 13;
    /** Costante che definisce NULL */
    private final int NULL = -1;
    /* Variabili d'istanza */
    /** Identificatore dello swarm */
    private int id;
    /* campi riguardanti il file descritto */
    /** Nome del file */
    private String nomefile;
    /** Dimensione del file */
    private long dimensione;
    /** Stringa hash del file */
    private byte[] hash;
    /* campi utili a chi usa il descrittore */
    /** Porta del tracker TCP */
    private int portaTCP;
    /** Porta del tracker UDP */
    private int portaUDP;
    /** Interfaccia per la callback */
    private InterfacciaCallback stubcb;
    /** Numero di seeder del file */
    private int numSeeders;
    /** Numero di leecher del file */
    private int numLeechers;

    /**
     * Costruttore vuoto di Descrittore
     */
    public Descrittore() {
    }

    /**
     * Costruttore di Descrittore
     * @param nomefile nome del file
     * @param dimensione dimensione del file
     * @param hash stringa hash del file
     * @param stubcb interfaccia per callback
     * @throws condivisi.ErrorException se almeno un parametro non è valido
     */
    public Descrittore(String nomefile, long dimensione, byte[] hash, InterfacciaCallback stubcb) throws ErrorException {
        if (nomefile == null || dimensione <= 0 || hash == null || stubcb == null) {
            throw new ErrorException("Param null");
        }
        this.nomefile = nomefile;
        this.dimensione = dimensione;
        this.hash = hash;
        this.portaTCP = NULL;
        this.portaUDP = NULL;
        this.stubcb = stubcb;
        /* il descrittore si crea solo a partire da un seeder */
        this.numSeeders = 1;
        this.numLeechers = 0;
        this.id = NULL;
    }

    /**
     * Metodo che ritorna il numero di Seeders attualmente presente
     * sul descrittore
     * @return numSeeders
     */
    public synchronized int getNumSeeders() {
        return this.numSeeders;
    }

    /**
     * Metodo che ritorna il numero di leechers attualmente presente
     * sul descrittore
     * @return numLeechers
     */
    public synchronized int getNumLeechers() {
        return this.numLeechers;
    }

    /**
     * Setta il numero di seeder al numero passato come parametro.
     * Se num minore di 0 il numero di seeder prende 0
     * @param num numero seeder
     */
    public synchronized void setNumSeeders(int num) {
        if (num < 0) {
            num = 0;
        }
        this.numSeeders = num;
    }

    /**
     * Setta il numero di leecher al numero passato come parametro.
     * Se num minore di 0 il numero di leecher prende 0
     * @param num numero leecher
     */
    public synchronized void setNumLeechers(int num) {
        if (num < 0) {
            num = 0;
        }
        this.numLeechers = num;
    }

    /**
     * Restituisce il nome del file
     * @return nomefile
     */
    public String getName() {
        return this.nomefile;
    }

    /**
     * Restituisce l'id dello swarm
     * @return id
     */
    public int getId() {
        return this.id;
    }

    /**
     * Setta l' id dello swarm al parametro id passato
     * come argomento
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce la dimensione del file
     * @return dimensione
     */
    public long getDimensione() {
        return this.dimensione;
    }

    /**
     * restituisce la stringa hash del file
     * @return hash
     */
    public byte[] getHash() {
        return this.hash;
    }

    /**
     * Restituisce il numero della porta del tracker TCP
     * @return portaTCP, vale NULL se non impostata
     */
    public int getTCP() {
        return this.portaTCP;
    }

    /**
     * Restituisce il numero della porta del tracker UDP
     * @return portaUDP, vale NULL se non impostata
     */
    public int getUDP() {
        return this.portaUDP;
    }

    /**
     * Restituisce l'interfaccia per la callback
     * @return stubcb
     */
    public InterfacciaCallback getCallback() {
        return this.stubcb;
    }

    /**
     * Setta la porta TCP a porta
     * @param porta
     */
    public void setPortaTCP(int porta) {
        if (porta <= 0) {
            porta = NULL;
        }
        this.portaTCP = porta;
    }

    /**
     * Effettua una copia del descrittore
     * @return d Descrittore copiato
     * @throws condivisi.ErrorException se non è opssibile copiare
     */
    public Descrittore copia() throws ErrorException {
        Descrittore d = null;
        try {
            d = new Descrittore(nomefile, dimensione, hash, stubcb);
        } catch (ErrorException e) {
            throw new ErrorException(e.getMessage());
        }
        d.portaTCP = portaTCP;
        d.portaUDP = portaUDP;
        d.numLeechers = numLeechers;
        d.numSeeders = numSeeders;
        d.id = id;
        return d;
    }

    /**
     * Setta la porta UDP a porta
     * @param porta
     */
    public void setPortaUDP(int porta) {
        if (porta <= 0) {
            porta = NULL;
        }
        this.portaUDP = porta;
    }
}
