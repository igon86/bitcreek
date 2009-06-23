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
 * Classe che definisce la struttura dati del client di supporto al
 * download/upload in uno swarm
 * @author Bandettini
 */
public class Creek extends Descrittore implements Serializable {

    /* Costanti */
    private final int NONATTIVO = -1;
    public static final long serialVersionUID = 45;
    private static final boolean LEECHER = true;
    private static final boolean SEEDER = false;
    private static final boolean NOTSTARTED = false;
    private static final boolean STARTED = true;
    private static final int DIMSHA = 20;
    /* Variabili d'istanza */
    private boolean stato; // true leecher,false seeder
    private boolean situazione; // true se attivo, false altrimenti
    private static final int ENDED = 0;
    private static final int INIT = 1;
    private static final int RAREST = 2;
    private static final int ENDGAME = 3;
    private static final int MINCHUNK = 20;
    //FONDAMENTALE determina la politica adottata per la scelta e scaricamento dei chunk
    private int statoDownload;
    private int percentuale;
    private boolean pubblicato;
    private int peer;
    private int peercercano;
    private InetAddress ind;
    private boolean[] have; //false se non posseduto true se posseduto
    private ArrayList<PIO> toDo;
    private ArrayList<Connessione> connessioni;
    //Strutture per la gestione del file
    protected File file;
    private RandomAccessFile raf;
    //ma che roba e`
    private int scaricati;
    private int[] scaricatiId;

    /**
     * Costruttore
     * @param d descrittore file
     * @param stato del file
     * @param pubblicato : true se il peer ha pubblicato il file, false altrimenti
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
            //System.out.println(Thread.currentThread().getName()+" SONO LEECHER");
            //SONO LEECHER
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
        this.connessioni = new ArrayList<Connessione>();


        file = new File("./FileCondivisi/" + this.getName());
        try {
            raf = new RandomAccessFile(file, "rw");
        } catch (FileNotFoundException ex) {
            Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
        }
        System.out.println("CREEK COSTRUITO");
    }

    public static void stampaDebug(PrintStream output, String s) {
        System.out.println(Thread.currentThread().getName() + ": " + s);
        output.println(s);
    }

    //che furbata ragazzi
    public synchronized void ordinaConnessioni() {
        Collections.sort(this.connessioni);
        //TEST
        Iterator k = this.connessioni.iterator();
        while (k.hasNext()) {
            Connessione temp = (Connessione) k.next();
            if (temp.getInteresseUp()) {
                System.out.println("Connessione interessante verso "+temp.getIPVicino()+ " con: " + temp.getDownloaded());
            } else {
                System.out.println("Connessione stupida verso "+temp.getIPVicino()+" con: " + temp.getDownloaded());
            }
        }


        int count = 0;
        int index = 0;
        Iterator h = this.connessioni.iterator();
        while (count < UploadManager.UPLOADLIMIT && h.hasNext()) {
            index++;
            count++;
            //count++;  QUA non ci vuole count++ e non index++ ???????????????????
            Connessione temp = (Connessione) h.next();
            temp.puoiUploadare();
        }
        //se rimane spazio per il peer random
        if (count == UploadManager.UPLOADLIMIT) {
            //scelta peer random
            int random = (int) (UploadManager.UPLOADLIMIT + Math.floor(Math.random() * (this.connessioni.size() - UploadManager.UPLOADLIMIT + 1)));
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
    }

    //METODI PER IL P2P
    /**
     * Ci vuole questo metodo in quanto in alcune piattaforme puo` esistere un solo
     * FileOutputStream per file.... la file channel pero` pare essere thread safe...
     * @param c
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
                System.out.println("ris : " + ris[i] + " , sha : " + sha[i]);
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
            //come prima cosa cancello dalla lista toDO il PIO relativo al chunk scritto
            this.removePIO(offset);

            //poi modifico anche l'array have
            this.have[offset] = true;

            //in questo fortissimo ordine sequenziale mi arraccomando bande
            this.scaricatiId[this.scaricati] = offset;
            this.scaricati++;
            //la lunghezza serve perché il buffer passato ha sempre la dimensione
            //di 4K ma l'ultimo è zero-padded quindi non lo devo scrivere
            int length = c.getDim();
            System.out.println("Sto per scrivere un chunk di dimensione: " + length);
            try {
                raf.seek(offset * BitCreekPeer.DIMBLOCCO);
                raf.write(c.getData(), 0, length);
            } catch (IOException ex) {
                Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
            }
            return true;
        } else {
            return false;
        }
    }

    public synchronized void chiudi() {
        for (Connessione c : connessioni) {
            c.setTermina();
        }
    }

    /**
     * ritorna un chunk bello caldo per l'offset specificato --> da fare per bene !!!!
     * utilizzato dall'uploader
     * @param id
     */
    public synchronized Chunk getChunk(int offset) {
        if (this.have[offset]) {
            int ridden = 0;
            byte[] buffer = new byte[BitCreekPeer.DIMBLOCCO];
            for (int i = 0; i < buffer.length; i++) {
                buffer[i] = 0;
            }
            try {
                if (raf == null) {
                    System.out.println("E` successa una tragedia al RAF");
                }
                long indice = offset * BitCreekPeer.DIMBLOCCO;
                raf.seek(indice);
                //System.out.println(Thread.currentThread().getName() + " MI SONO SPOSTATO AL BYTE : " + indice);
                ridden = raf.read(buffer, 0, buffer.length);
            //System.out.println(Thread.currentThread().getName() + " HO LETTO " + ridden + " BYTE");
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
     * Ripulisce il creek dalla roba non serializzabile;
     */
    public synchronized void setClean() {
        this.raf = null;
    }

    public synchronized void testFile() {
        if (this.raf == null) {
            System.out.println("E` PURGATO ERRORE");
        } else {
            System.out.println("E` L'ORIGINALE!! DI LUSSO");
        }
    }

    /**
     * Metodo invocato dall'avvia per aggiungere un nuovo bitfield alla lista PIO
     * @param bitfield
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
     * metodo che controlla se ci sono chunk da scaricare tra quelli presenti
     * in bitfield
     * @param bitfield
     * @return
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
        System.out.print("NON sono entrato nel while di next");
        return null;
    }

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

    public synchronized PIO getNext(boolean[] bitfield) {
        System.out.print(Thread.currentThread().getName() + " getNext: La lista toDO contiene " + this.toDo.size() + " elementi ->");
        //questo controllo e` totalmente inutile
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
            //ordino per rarita
            Collections.sort(this.toDo);
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
     * metodo per ottenere gli id di tutti gli ultimi chunk da scaricare
     * @return
     */
    public synchronized int[] getLast() {
        //inizializzazione
        int[] ret = new int[this.toDo.size()];
        int count = 0;
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            PIO temp = (PIO) h.next();
            ret[count++] = temp.getId();
        }
        return ret;
    }

    public void addConnessione(Connessione conn) {
        this.connessioni.add(conn);
        //ma perche`???
        this.situazione = STARTED;
    }

    public Connessione presenzaConnessione(InetAddress ip, int porta) {
        for (Connessione c : this.connessioni) {
            if (c.confronta(ip, porta)) {
                return c;
            }
        }
        return null;
    }

    public synchronized void closeFile() {
    }

    public synchronized void closeAndDeleteFile() {
    }

    //metodo chiamato al momento della creazione del creek (in Download)
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
        //La ganzata glieli ordino a caso :D
        Collections.shuffle(this.toDo);
        //CONTROLLO SUL NUMERO DI PIO
        System.out.println(Thread.currentThread().getName() + " ToDo ha dimensione: " + this.toDo.size());
    }

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

    //GETTER
    public boolean getStato() {
        return this.stato;
    }

    public int getPercentuale() {
        return this.percentuale;
    }

    public boolean getPubblicato() {
        return this.pubblicato;
    }

    public boolean getSituazione() {
        return this.situazione;
    }

    public int getPeer() {
        return this.peer;
    }

    public int getPeerCerca() {
        return this.peercercano;
    }

    public boolean[] getHave() {
        return this.have;
    }

    public int getScaricati() {
        return this.scaricati;
    }

    public int getScaricatiIndex(int index) {
        return this.scaricatiId[index];
    }
    //SETTER

    public void settaPeerCerca() {
        if (this.peercercano != NONATTIVO) {
            this.peercercano++;
        }
    }

    public void settaIdentita(InetAddress ind) {
        if (this.peercercano != NONATTIVO && ind != null) {
            this.ind = ind;
        }
    }

    public InetAddress getIdentita() {
        return this.ind;
    }

    @Override
    public synchronized Creek copia() throws ErrorException {
        //System.out.println("COPIA CREEK");
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
     * @return
     * @throws condivisi.ErrorException
     */
    public synchronized Creek esporta() throws ErrorException {
        System.out.println("ESPORTA - CREEK");
        Descrittore temp = super.copia();
        Creek c = new Creek(temp, this.stato, this.pubblicato);
        c.peer = 0;
        c.percentuale = percentuale;
        c.situazione = NOTSTARTED;
        /* controllare se va bene */
        c.peercercano = this.peercercano;
        c.ind = this.ind;
        return c;
    }

    /**
     * Incrementa il numero di peer
     */
    public synchronized void incrPeer() {
        this.peer++;
    }

    /**
     * Setta la percentuale in base al parametro passato
     * @param np
     */
    public synchronized void settaPerc() {
        //QUI per motivi incapibili al genere umano si aumentava il contatore dei chunk scaricati
        this.percentuale = (this.scaricati * 100) / have.length;
        /* se percentuale = 100 ho finito di scaricare quindi il file può andare in upload */
        if (this.percentuale == 100) {
            this.stato = SEEDER;
            this.situazione = NOTSTARTED;
            this.peer = 0;
        }
    }
}
