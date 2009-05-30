package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.Serializable;
import java.net.InetAddress;
import java.util.ArrayList;
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
    private static final int STARTED = 0;
    /* Variabili d'istanza */
    private boolean stato; // true leecher,false seeder

    private boolean situazione; // true se attivo, false altrimenti

    private int situazioneDownload;
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
        this.stato = stato;
        this.situazione = false;
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
        int dimArray = (int) Math.ceil(d.getDimensione() / BitCreekPeer.DIMBLOCCO);
        have = new boolean[dimArray];
        if (this.getStato() == LEECHER) {
            //System.out.println(Thread.currentThread().getName()+" SONO LEECHER");
            //SONO LEECHER
            for (int i = 0; i < dimArray; i++) {
                have[i] = false;
            }
            this.situazioneDownload = STARTED;
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

    //METODI PER IL P2P
    /**
     * Ci vuole questo metodo in quanto in alcune piattaforme puo` esistere un solo
     * FileOutputStream per file
     * @param c
     */
    public synchronized void scriviChunk(Chunk c) {
        int offset = c.getOffset();
        //la lunghezza serve perché il buffer passato ha sempre la dimensione
        //di 1K ma l'ultimo è zero-padded quindi non lo devo scrivere
        int length = c.getDim();
        System.out.println("Sto per scrivere un chunk di dimensione: "+length);
        try {
            raf.seek(offset*BitCreekPeer.DIMBLOCCO);
            raf.write(c.getData(), 0, length);
        } catch (IOException ex) {
            Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    /**
     * ritorna un chunk bello caldo per l'offset specificato
     * @param id
     */
    public synchronized Chunk getChunk(int offset){
        int ridden = 0;
        byte [] buffer = new byte[BitCreekPeer.DIMBLOCCO];
        for (int i = 0; i < buffer.length; i++) {
            buffer[i] = 0;
        }
        try {
            if(raf == null) System.out.println("E` successa una tragedia al RAF");
            long indice = offset * BitCreekPeer.DIMBLOCCO;
            raf.seek(indice);
            System.out.println("Mi sono spostato al byte : "+indice);
            ridden = raf.read(buffer, 0, buffer.length);
            System.out.println("HO LETTO "+ridden+" BYTE");
        } catch (IOException ex) {
            System.out.println(Thread.currentThread().getName()+" ERRORE IN LETTURA");
            Logger.getLogger(Creek.class.getName()).log(Level.SEVERE, null, ex);
        }
        return new Chunk(buffer,offset,ridden);
    }
    
    /**
     * Ripulisce il creek dalla roba non serializzabile;
     */
    public synchronized void setClean(){
        this.raf = null;
    }
    
    public synchronized void testFile(){
        if (this.raf == null){
            System.out.println("E` PURGATO ERRORE");
        }
        else{
            System.out.println("E` L'ORIGINALE!! DI LUSSO");
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

    public synchronized PIO next(boolean[] bitfield) {
        boolean b = false;
        int index = 0;
        Iterator h = this.toDo.iterator();
        while (h.hasNext()) {
            PIO temp = (PIO) h.next();
            if (!temp.getBusy() && bitfield[temp.getId()]) {
                return temp;
            }
        }
        return null;
    }

    public synchronized PIO getNext(boolean[] bitfield) {
        if (this.situazioneDownload == STARTED) {
            PIO temp = this.next(bitfield);
            if (temp == null) {
                return null;
            }
            temp.setBusy();
            return temp;
        }
        return null;
    }

    public void addConnessione(Connessione conn) {
        this.connessioni.add(conn);
    }

    public Connessione presenzaConnessione(Connessione conn) {
        for (Connessione c : this.connessioni) {
            if (c.confronta(conn.getIPVicino(), conn.getPortaVicino())) {
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
    public synchronized void setPIO() {
        int count = 0;
        if (stato == LEECHER) {
            for (boolean b : this.have) {
                if (!b) {
                    this.toDo.add(new PIO(count));
                }
                count++;
            }
        }
    }

    public synchronized void removePIO(PIO p) {
        throw new UnsupportedOperationException();
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
        c.situazione = false;
        /* controllare se va bene */
        c.peercercano = this.peercercano;
        c.ind = this.ind;
        return c;
    }
}
