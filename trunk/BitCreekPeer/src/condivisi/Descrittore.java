package condivisi;

import java.io.Serializable;

/**
 * Classe che definisce il descrittore di un file
 * @author Bandettini Alberto, Lottarini Andrea
 * @version 1.0
 */
public class Descrittore implements Serializable {

    /* Costanti */
    /** Costante che definisce la versione */
    public static final long serialVersionUID = 13;
    /** Costante che definisce NULL */
    private final int NULL = -1;
    /* Variabili d'istanza */
    /** Id dello swarm */
    private int id;
    /* campi riguardanti il file descritto */
    /** Nome del file */
    private String nomefile;
    /** Dimensione del file */
    private long dimensione;
    /** Stringa hash */
    private byte[] hash;
    /* campi utili a chi usa il descrittore */
    /** Porta del tracker TCP */
    private int portaTCP;
    /** Porta del tracker UDP */
    private int portaUDP;
    /** Interfaccia per le callback */
    private InterfacciaCallback stubcb;
    /** Numero di seeder nello swarm */
    private int numSeeders;
    /** Numero di leecher nello swarm */
    private int numLeechers;

    /**
     * Costruttore vuoto di Descrittore
     */
    public Descrittore() {
    }

    /**
     * Costruttore
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
        /* il descrittore si crea solo a partire da un seeder*/
        this.numSeeders = 1;
        this.numLeechers = 0;
        this.id = -1;
    }

    /**metodo che ritorna il numero di Seeders attualmente sul descrittore
     * @return
     */
    public synchronized int getNumSeeders() {
        return this.numSeeders;
    }

    /**metodo che ritorna il numero di leechers attualmente sul descrittore
     * @return
     */
    public synchronized int getNumLeechers() {
        return this.numLeechers;
    }

    /**
     *
     * @param num
     */
    public synchronized void setNumSeeders(int num) {
        this.numSeeders = num;
    }

    /**
     *
     * @param num
     */
    public synchronized void setNumLeechers(int num) {
        this.numLeechers = num;
    }

    /**
     * Restituisce il nome del file presente nel descrittore
     * @return nomefile
     */
    public String getName() {
        return this.nomefile;
    }

    /**
     *
     * @return
     */
    public int getId() {
        return this.id;
    }

    /**
     *
     * @param id
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Restituisce la dimensione del file presente nel descrittore
     * @return dimensione
     */
    public long getDimensione() {
        return this.dimensione;
    }

    /**
     * restituisce la stringa hash del file presente nel descrittore
     * @return hash
     */
    public byte[] getHash() {
        return this.hash;
    }

    /**
     * Restituisce il numero della porta del tracker TCP
     * @return portaTCP, -1 se non impostata
     */
    public int getTCP() {
        return this.portaTCP;
    }

    /**
     * Restituisce il numero della porta del tracker UDP
     * @return portaUDP, -1 se non impostata
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
     *
     * @return
     * @throws condivisi.ErrorException
     */
    public Descrittore copia() throws ErrorException {
        Descrittore d = null;
        try {
            //System.out.println("COPIA - DESCRITTORE");
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

    /**
     * Metodo override
     * @return nomefile.hahsCode
     */
    @Override
    public int hashCode() {
        return this.nomefile.hashCode();
    }

    /**
     * Metodo override
     * @param obj
     * @return false
     */
    public boolean equals(Object obj) {
        return false;
    }
}
