package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintStream;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.InetAddress;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Classe che definisce la struttura dati
 * del client di supporto al download/upload
 * in uno swarm
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Creek extends Descrittore implements Serializable {

    /* Costanti */
    /** Definisce NONATTIVO */
    private final int NONATTIVO = -1;
    /** Costante che defnisce la versione della classe */
    public static final long serialVersionUID = 45;
    /** Definisce LEECHER */
    private static final boolean LEECHER = true;
    /** Definisce SEEDER */
    private static final boolean SEEDER = false;
    /** Definisce NOTSTARTED */
    private static final boolean NOTSTARTED = false;
    /** Definisce STARTED */
    private static final boolean STARTED = true;
    /** Definisce la dimensione del risultato di un' applicazione di SHA-1 */
    private static final int DIMSHA = 20;
    /** Definisce INIT */
    private static final int INIT = 1;
    /** Definisce RAREST */
    private static final int RAREST = 2;
    /** Definisce ENDGAME */
    private static final int ENDGAME = 3;
    /** Definisce MINCHUNK */
    private static final int MINCHUNK = 20;
    /* Variabili di classe */
    /** Contatatore */
    private static int countNext = 0;

    /* Variabili d'istanza */
    /** Stato del creek : può essere LEECHER o SEEDER */
    private boolean stato;
    /** Situazione del creek : può essere NOTSTARTED o STARTED */
    private boolean situazione;
    /** Politica di download : può essere INIT, RAREST o ENDGAME */
    private int statoDownload;
    /** Percentuale di completezza del file */
    private int percentuale;
    /** Flag che indica se il file è stato pubblicato dal peer */
    private boolean pubblicato;
    /** Numero di peer connessi al peer per questo file */
    private int peer;
    /** Numero di peer che hanno crecato questo file */
    private int peercercano;
    /** Identità dell' ultimo peer che ha cercato questo file */
    private InetAddress ind;
    /** Array di falg che indicano i pezzi posseduti dal peer */
    private boolean[] have;
    /** Array di PIO che indicano i chunk liberi o occupati */
    private ArrayList<PIO> toDo;
    /** Array di connessioni per questo file */
    private ArrayList<Connessione> connessioni;
    /** File del creek */
    private File file;
    /** File Random associato al creek */
    private RandomAccessFile raf;
    /** Numero pezzi scaricati */
    private int scaricati;
    /** Array degli indici dei pezzi scaricati */
    private int[] scaricatiId;

    /**
     * Costruttore
     * @param d descrittore file
     * @param stato del file
     * @param pubblicato : true se il peer ha pubblicato il file, false altrimenti
     * @throws ErrorException
     */
    public Creek(Descrittore d, boolean stato, boolean pubblicato) throws ErrorException {
        super(d.getName(), d.getDimensione(), d.getHash(), d.getCallback());
        this.setPortaTCP(d.getTCP());
        this.setPortaUDP(d.getUDP());
        this.setId(d.getId());
        this.stato = stato;
        this.situazione = NOTSTARTED;
        this.percentuale = 0;
        this.pubblicato = pubblicato;
        this.peer = 0;
        this.ind = null;
        if (pubblicato) {
            this.peercercano = 0;
        } else {
            this.peercercano = NONATTIVO;
        }
        //aggiunte per il p2p
        float temp = (float) d.getDimensione() / (float) BitCreekPeer.DIMBLOCCO;
        System.out.println(Thread.currentThread().getName() + " NUMERO DI BLOCCHI: " + temp);
        int dimArray = (int) Math.ceil(temp);
        System.out.println("FILE HA DIMENSIONE: " + d.getDimensione() + "\nL'ARRAY HAVE HA DIMENSIONE: " + dimArray);

        //Array di supportos
        have = new boolean[dimArray];
        scaricatiId = new int[dimArray];

        if (this.getStato() == LEECHER) {
            for (int i = 0; i < dimArray; i++) {
                have[i] = false;
            }
            this.scaricati = 0;
        } else {
            for (int i = 0; i < dimArray; i++) {
                have[i] = true;
            }
        }
        this.toDo = new ArrayList<PIO>();
        this.init();
        System.out.println("CREEK COSTRUITO");
    }

    /**
     *
     * @param output
     * @param s
     */
    public static void stampaDebug(PrintStream output, String s) {
        System.out.println(Thread.currentThread().getName() + ": " + s);
        output.println(s);
    }

    /**
     * Ordina le connessioni in base alla rarità
     * @param id
     * @param random
     * @return random
     * @throws condivisi.ErrorException
     */
    public synchronized int ordinaConnessioni(int id, int random) throws ErrorException {
        try {
            Collections.sort(this.connessioni);
        } catch (NullPointerException ex) {
            throw new ErrorException("Conn null");
        }

        int count = 0;
        int index = 0;
        Iterator h = this.connessioni.iterator();
        while (count < UploadManager.UPLOADLIMIT && h.hasNext()) {
            index++;
            count++;
            Connessione temp = (Connessione) h.next();
            temp.puoiUploadare();
        }
        //se rimane spazio per il peer random
        if (count == UploadManager.UPLOADLIMIT) {
            //scelta peer random
            if (id % 3 == 0) {
                random = (int) (UploadManager.UPLOADLIMIT + Math.floor(Math.random() * (this.connessioni.size() - UploadManager.UPLOADLIMIT + 1)));
            }
            while (h.hasNext()) {
                index++;
                Connessione temp = (Connessione) h.next();
                if (index == random) {
                    temp.puoiUploadare();
                } else {
                    temp.nonPuoiUploadare();
                }
            }
        }
        return random;
    }

    /**
     * Scrive il chunk passato come parametro sul file
     * @param c chunk da scrivere
     * @return esito dell' operazione
     * @throws ErrorException se qualcosa non va
     */
    public synchronized boolean scriviChunk(Chunk c) throws ErrorException {
        int offset = c.getOffset();
        if (this.have[offset] == false) {
            // controllo SHA
            byte[] stringa = this.getHash();
            byte[] sha = new byte[DIMSHA];
            int i = DIMSHA * offset;
            for (int j = 0; j < DIMSHA; j++) {
                sha[j] = stringa[i + j];
            }
            MessageDigest md = null;
            try {
                md = MessageDigest.getInstance("SHA-1");
            } catch (NoSuchAlgorithmException ex) {
                throw new ErrorException("No such Algorithm");
            }
            md.update(c.getData());
            byte[] ris = new byte[DIMSHA];
            ris = md.digest();
            for (i = 0; i < ris.length; i++) {
                //System.out.println("ris : " + ris[i] + " , sha : " + sha[i]);
                if (ris[i] != sha[i]) {
                    float temp = (float) this.getDimensione() / (float) BitCreekPeer.DIMBLOCCO;
                    int dim = (int) Math.ceil(temp);
                    if (c.getOffset() != dim - 1) {
                        throw new ErrorException("SHA non corretto");
                    }
                }
            }
            //come prima cosa rendo consistente lo statoDownload
            if (this.statoDownload == INIT) {
                this.statoDownload = RAREST;
                System.out.println("Siamo passati in rarest");
            }
            if (this.toDo.size() < MINCHUNK && this.statoDownload != ENDGAME) {
                this.statoDownload = ENDGAME;
                System.out.println("Sono passato in endgame");
            }
            //la lunghezza serve perché il buffer passato ha sempre la dimensione
            //di 4K ma l'ultimo è zero-padded quindi non lo devo scrivere
            try {
                raf.seek(offset * BitCreekPeer.DIMBLOCCO);
                raf.write(c.getData(), 0, c.getDim());
                //come prima cosa cancello dalla lista toDO il PIO relativo al chunk scritto
                this.removePIO(offset);
                //poi modifico anche l'array have
                this.have[offset] = true;
                this.scaricatiId[this.scaricati] = offset;
                this.scaricati++;
            } catch (IOException ex) {
                Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
                return false;
            }
            return true;
        } else {
            return false;
        }
    }

    /**
     * Inizializza le connessioni e il file random del creek
     */
    public synchronized void init() {
        this.connessioni = new ArrayList<Connessione>();
        this.file = new File("./FileCondivisi/" + this.getName());
        try {
            this.raf = new RandomAccessFile(this.file, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * Chiude tutte le connessioni, riporta allo stato iniziale il creek e lo salva
     * su file
     */
    public synchronized void chiudi() {
        // stato iniziale
        this.situazione = NOTSTARTED;
        this.statoDownload = INIT;
        this.peer = 0;
        this.peercercano = 0;
        try {
            if (!connessioni.isEmpty()) {
                for (Connessione c : connessioni) {
                    c.setTermina();
                }
            }
        } catch (NullPointerException ex) {
            System.out.println("Connessioni e` gia null");
        }
        connessioni = null;
        try {
            // chiudo il random file
            this.raf.close();
            this.raf = null;
        } catch (IOException ex) {
            System.out.println("IOexception su random file");
        } catch (NullPointerException ex) {
            System.out.println("il raf era gia null");
        }
    }

    /**
     * Libera il PIO con id uguaule a quello passato
     * come parametro
     * @param id id PIO
     */
    public synchronized void liberaPio(int id) {
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            PIO temp = (PIO) h.next();
            if (temp.getId() == id) {
                temp.setFree();
                System.out.println("liberato il PIO");
                return;
            }
        }
    }

    /**
     * Ritorna un chunk bello caldo per l'offset specificato
     * @param offset offset specificato
     * @return chunk richiesto se tutto ok ; null altrimenti
     */
    public synchronized Chunk getChunk(int offset) {
        if (this.have[offset]) {
            int ridden = 0;
            byte[] buffer = new byte[BitCreekPeer.DIMBLOCCO];
            for (int i = 0; i <
                    buffer.length; i++) {
                buffer[i] = 0;
            }
            try {
                if (raf == null) {
                    System.out.println("E` successa una tragedia al RAF");
                    return null;
                }
                long indice = offset * BitCreekPeer.DIMBLOCCO;
                raf.seek(indice);
                ridden = raf.read(buffer, 0, buffer.length);
            } catch (IOException ex) {
                System.out.println(Thread.currentThread().getName() + " ERRORE IN LETTURA");
                Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
            }
            return new Chunk(buffer, offset, ridden);
        } else {
            return null;
        }
    }

    /**
     * Ripulisce il creek dalla roba non serializzabile
     */
    public synchronized void setClean() {
        this.raf = null;
    }

    /**
     * Metodo invocato dall'avvia per aggiungere un nuovo bitfield alla lista PIO.
     * Setta la rarità dei PIO.
     * @param bitfield da aggiungere
     */
    public synchronized void addRarita(boolean[] bitfield) {
        for (PIO p : toDo) {
            int id = p.getId();
            if (bitfield[id]) {
                int rarita = p.getRarita();
                p.setRarita(++rarita);
            }
        }
    }

    /**
     * Metodo che controlla se ci sono chunk da scaricare tra quelli presenti
     * in bitfield
     * @param bitfield
     * @return true se ci sono ; false altrimenti
     */
    public synchronized boolean interested(boolean[] bitfield) {
        for (PIO p : toDo) {
            if (bitfield[p.getId()] == true) {
                return true;
            }
        }
        return false;
    }

    /**
     * Metodo di supporto della getNext()
     * @param bitfield
     * @return il primo oggetto PIO libero nel bitfield
     */
    public synchronized PIO next(boolean[] bitfield) {
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            PIO temp = (PIO) h.next();
            if (!temp.getBusy() && bitfield[temp.getId()]) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Ordina i PIO
     * @param bitfield
     * @return il PIO più conveniente
     * @deprecated E' preferibile utilizzare la getNext
     */
    @Deprecated
    public synchronized PIO orderedNext(boolean[] bitfield) {
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            PIO temp = (PIO) h.next();
            if (temp.getBusy()) {
                return null;
            } else if (bitfield[temp.getId()]) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Restituisce il PIO più conveniente
     * @param bitfield
     * @return PIO più conveniente
     */
    public synchronized PIO getNext(boolean[] bitfield) {
        if (this.statoDownload == INIT) {
            PIO temp = this.next(bitfield);
            if (temp == null) {
                System.out.println("RITORNO NULL e sono in INIT o in ENDGAME");
                return null;
            } else {
                System.out.println(Thread.currentThread().getName() + " RITORNO PIO: " + temp.getId());
                temp.setBusy();
                return temp;
            }

        } else if (this.statoDownload == RAREST) {
            //ordino per rarita, lo faccio solo ogni dieci pezzi per ridurre l'overhead
            if (((countNext++) % 10) == 0) {
                Collections.sort(this.toDo);
            }
            PIO temp = this.next(bitfield);
            if (temp == null) {
                System.out.println(Thread.currentThread().getName() + " RITORNO NULL e sono in RAREST");
                return null;
            } else {
                System.out.println(Thread.currentThread().getName() + " RITORNO PIO: " + temp.getId());
                temp.setBusy();
                return temp;
            }
        } else if (this.statoDownload == ENDGAME) {
            //PIO fittizio per avvisare il downloader dell'endgame
            return new PIO(Downloader.ENDGAME);
        }
        return null;
    }

    /**
     * Metodo per ottenere gli id di tutti gli ultimi chunk da scaricare
     * @return array di id
     */
    public synchronized int[] getLast() {
        int[] ret = new int[this.toDo.size()];
        int count = 0;
        Collections.shuffle(this.toDo);
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            PIO temp = (PIO) h.next();
            ret[count++] = temp.getId();
        }
        return ret;
    }

    /**
     * Aggiunge una connessione all' array di connessioni
     * @param conn connessione da aggiungere
     */
    public synchronized void addConnessione(Connessione conn) {
        this.connessioni.add(conn);
        this.situazione = STARTED;
    }

    /**
     * Verifica la presenza di una connessione
     * @param ip ip partner
     * @param porta porta partner
     * @return la connesisone se è stata trovata ; altrimenti null
     */
    public synchronized Connessione presenzaConnessione(InetAddress ip, int porta) {
        if (this.connessioni == null) {
            return null;
        }
        for (Connessione c : this.connessioni) {
            if (c.confronta(ip, porta)) {
                return c;
            }
        }
        return null;
    }

    /**
     * Setta i PIO
     */
    public synchronized void setToDo() {
        int count = 0;
        if (stato == LEECHER) {
            for (boolean b : this.have) {
                if (!b) {
                    this.toDo.add(new PIO(count));
                }
                count++;
            }

        }
        if (this.toDo.size() < MINCHUNK) {
            this.statoDownload = ENDGAME;
        } else {
            this.statoDownload = INIT;
        }
        Collections.shuffle(this.toDo);
    }

    /**
     * Rimuove il PIO con indice p
     * @param p id PIO da eliminare
     */
    public synchronized void removePIO(int p) {
        PIO temp = null;
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            temp = (PIO) h.next();
            if (temp.getId() == p) {
                h.remove();
                break;
            }
        }
    }

    /**
     * Restitusce lo stato del creek
     * @return stato
     */
    public boolean getStato() {
        return this.stato;
    }

    /**
     * Restituisce la percentuale di completamento del creek
     * @return percentuale
     */
    public int getPercentuale() {
        return this.percentuale;
    }

    /**
     * Restituisce il flag pubblicato
     * @return pubblicato
     */
    public boolean getPubblicato() {
        return this.pubblicato;
    }

    /**
     * restituisce la situazioned el creek
     * @return situazione
     */
    public boolean getSituazione() {
        return this.situazione;
    }

    /**
     * restituisce il numero dei peer associati a questo creek
     * @return peer
     */
    public int getPeer() {
        return this.peer;
    }

    /**
     * Restituisce il nuemro di peer che hanno
     * cercato questo file
     * @return peercercano
     */
    public int getPeerCerca() {
        return this.peercercano;
    }

    /**
     * Restituisce l' array di pezzi che il peer ha
     * @return have
     */
    public boolean[] getHave() {
        return this.have;
    }

    /**
     * Restituisce il numero dei pezzi scaricati
     * @return scaricati
     */
    public int getScaricati() {
        return this.scaricati;
    }

    /**
     * Restituisce scaricati in posizione index
     * @param index
     * @return scaricati[index]
     */
    public int getScaricatiIndex(int index) {
        return this.scaricatiId[index];
    }

    /**
     * Setta il numero dei peer che hanno cercato
     * il file se attivo il servizio
     */
    public void settaPeerCerca() {
        if (this.peercercano != NONATTIVO) {
            this.peercercano++;
        }
    }

    /**
     * Setta l' identità dell' ultimo peer che ha cercato
     * il file se attivo il servizio
     * @param ind IP dell' ultimo peer
     */
    public void settaIdentita(InetAddress ind) {
        if (this.peercercano != NONATTIVO && ind != null) {
            this.ind = ind;
        }

    }

    /**
     * Restituisce l' IP di chi ha cercato il file
     * per ultimo. Se no attivo il servizio restituisce null.
     * @return ind
     */
    public InetAddress getIdentita() {
        return this.ind;
    }

    /**
     * Effettua la copia del creek
     * @return nuovo creek identico
     * @throws condivisi.ErrorException se qualcosa non va
     */
    @Override
    public synchronized Creek copia() throws ErrorException {
        Descrittore temp = super.copia();
        Creek c = new Creek(temp, this.stato, this.pubblicato);
        c.peer = peer;
        c.percentuale = 0;
        c.situazione = situazione;
        c.peercercano = peercercano;
        c.ind = ind;
        return c;
    }

    /**
     * Crea un creek da esportare
     * @return creek in stato iniziale
     * @throws condivisi.ErrorException se qualcosa non va
     */
    public synchronized Creek esporta() throws ErrorException {
        System.out.println("ESPORTA - CREEK");
        Descrittore temp = super.copia();
        Creek c = new Creek(temp, this.stato, this.pubblicato);
        c.stato = LEECHER;
        c.situazione = NOTSTARTED;
        c.statoDownload = INIT;
        c.peer = 0;
        c.percentuale = 0;
        c.scaricati = 0;
        c.peercercano = NONATTIVO;
        c.pubblicato = false;
        c.ind = null;
        c.raf = null;
        return c;
    }

    /**
     * Incrementa il numero di peer
     */
    public synchronized void incrPeer() {
        this.peer++;
    }

    /**
     * Setta la percentuale
     */
    public synchronized void settaPerc() {
        this.percentuale = (this.scaricati * 100) / have.length;
        /* se percentuale = 100 ho finito di scaricare quindi il file può andare in upload */
        if (this.percentuale == 100) {
            this.stato = SEEDER;
            this.situazione = NOTSTARTED;
            this.peer = 0;
        }
    }

    /**
     * decrementa il numero dei peer
     */
    public synchronized void decrPeer() {
        this.peer--;
    }
}
