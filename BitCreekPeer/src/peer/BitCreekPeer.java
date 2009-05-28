package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import condivisi.InterfacciaCallback;
import condivisi.InterfacciaRMI;
import gui.BitCreekGui;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.BindException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.UnknownHostException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Parte client del protocollo BitCreek
 * @author Bandettini
 */
public class BitCreekPeer {

    /* Costanti */
    private final int NULL = -1;
    private final int ATTESARISPSERVER = 3000;
    private final int PORTASERVER = 9999;
    private final int PORTARMI = 10000;
    private final int FINITO = 100;
    private final int NUMTHREAD = 100;
    /*dimensione del blocco*/
    public static final int DIMBLOCCO = 4096;
    protected static final int MAXCONNESSIONI = 100;
    protected static final int TIMEOUTCONNESSIONE = 500;
    /* Variabili d'istanza */
    /** Mio ip */
    private InetAddress mioip;
    /** Ip del server, null se peer disconnesso */
    private InetAddress ipServer;
    /** Socket di benvenuto */
    private ServerSocket welcome;
    /** Porta della socket di benvenuto */
    private int portarichieste;
    /** Array di creek */
    private ArrayList<Creek> arraydescr;
    /** Array di descrittori risultanti dall'ultima ricerca */
    private ArrayList<Descrittore> cercati;
    /** booleano per sapere se il thread per i keepalive è avviato */
    private boolean keepalive;
    /** booleano per sapere se il thread per in ascolto su welcome e è avviato */
    private boolean ascolto;
    /** booleano per sapere se il peer è dietro NAT o firewall */
    private boolean bloccato;
    /* dichiarazione delle interfacce del client */
    /** Interfaccia RMI */
    private InterfacciaRMI stub;
    /** Interfaccia per le callback */
    private InterfacciaCallback stubcb;
    /** Implementazione delle callback */
    private ImplementazioneCallback callback;
    /** ThreadPool per il p2p*/
    private ExecutorService TP;
    /** Numero di connessioni aperte */
    private int connessioni;
    

    /**
     * Costruttore vuoto
     * @exception ErrorException
     */
    public BitCreekPeer() throws ErrorException {

        /* inizializzazione variabili istanza */

        mioip = null;
        portarichieste = NULL;
        ipServer = null;
        welcome = null;
        arraydescr = new ArrayList<Creek>();
        cercati = null;
        keepalive = false;
        ascolto = false;
        bloccato = true;
        stub = null;
        stubcb = null;
        callback = null;
        connessioni = 0;
        //Avvio del thread Pool
        TP = Executors.newFixedThreadPool(NUMTHREAD);

        /* Creazione cartelle se non esistenti*/

        File dir = new File("./FileCondivisi");
        dir.mkdir();
        dir = new File("./MetaInfo");
        dir.mkdir();

        /* scandisco le metainfo e aggiorno arraydescr */

        File[] array = dir.listFiles();
        ObjectInputStream in = null;
        Creek c = null;
        for (File f : array) {
            try {
                in = new ObjectInputStream(new FileInputStream(f));
                c = (Creek) in.readObject();
                /* inserisco i descrittori nel vettore : non importa mutua esclusione
                perchè non ci sono ancora altri thread che accedono a questa struttura */
                arraydescr.add(c);
                in.close();
            } catch (FileNotFoundException ex) {
                throw new ErrorException("File not found");
            } catch (IOException ex) {
                throw new ErrorException("IO Problem");
            } catch (ClassNotFoundException ex) {
                throw new ErrorException("Class not found");
            }
        }
    }

    //METODI GETTER
    /**
     * Restituisce il numero di connessioni
     * @return
     */
    public synchronized int getConnessioni(){
        return this.connessioni;
    }

    /**
     * Incrementa il numero di connessioni
     */
    public synchronized void incrConnessioni(){
        this.connessioni++;
    }
    /**
     * Incrementa il numero di connessioni
     */
    public synchronized void decrConnessioni(){
        this.connessioni--;
    }
    /**
     * Restituisce l' ip del client
     * @return mioip
     */
    public InetAddress getMioIp() {
        return this.mioip;
    }

    /**
     * Aggiunge un task al pool
     * @param r
     */
    public synchronized void addTask(Runnable r){
        this.TP.execute(r);
    }
    /**
     * Restituisce il numero della porta in ascolto
     * @return portarichieste
     */
    public int getPortaRichieste() {
        return this.portarichieste;
    }

    /**
     * Restituisce l'ip del server
     * @return ipServer
     */
    public InetAddress getIpServer() {
        return this.ipServer;
    }

    /**
     * Restituisce true se il peer è sotto firewall o nat, false altrimenti
     * @return bloccato
     */
    public boolean getBloccato() {
        return this.bloccato;
    }
    
    /**
     * Restituisce lo stub per le callback
     * @return stubcb
     */
    public InterfacciaCallback getStubCb() {
        return this.stubcb;
    }

    /**
     * Restituisce la socket in ascolto
     * @return welcome
     */
    public ServerSocket getSS() {
        return this.welcome;
    }

    /**
     * Restituisce lo stub
     * @return stubcb
     */
    public InterfacciaRMI getStub() {
        return this.stub;
    }

    public synchronized ArrayList<Descrittore> getCercati() throws ErrorException {
        ArrayList<Descrittore> ris = new ArrayList<Descrittore>();
        for (Descrittore d : cercati) {
            Descrittore nuovo = d.copia();
            ris.add(nuovo);
        }
        return ris;
    }

    /**
     * Restituisce tutti i creek in un array di copia TOLTA LA COPIA
     * @return arraydescr
     */
    public synchronized ArrayList<Creek> getDescr() throws ErrorException {
        System.out.println("GETDESCR");
        ArrayList<Creek> ris = new ArrayList<Creek>();
        for (Creek c : arraydescr) {
            Creek nuovo = c.copia();
            ris.add(nuovo);
        }
        //return this.arraydescr;
        return ris;
    }

    //SETTER
    public synchronized void setCercati(ArrayList<Descrittore> results) {
        this.cercati = results;
    }

    /**
     * Controllas se la porta speceficata è libera
     * @param porta
     */
    public boolean settaporta(int porta) {
        boolean ris = true;
        try {
            DatagramSocket s = new DatagramSocket(porta);
        } catch (BindException e) {
            ris = false;
        } catch (SocketException e) {
            ris = false;
        }
        if (ris) {
            File f = new File("./porta.conf");
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(f);
                out.write(new byte[]{(byte) (porta >>> 24), (byte) (porta >>> 16), (byte) (porta >>> 8), (byte) porta});
                out.close();
            } catch (FileNotFoundException ex) {
                f.delete();
                ris = false;
            } catch (IOException ex) {
                f.delete();
                ris = false;
            }
        }
        return ris;
    }

    //METODI OPERANTI SU ARRAYDESCR
    /**
     * Controlla se esiste un creek con il nome nome
     * @param nome
     * @return true se esiste già, false altrimenti
     * @throws condivisi.ErrorException se nome è null
     */
    public synchronized boolean presenza(int id) throws ErrorException {
        boolean presenza = false;
        for (Creek c : arraydescr) {
            if (c.getId() == id) {
                presenza = true;
                break;
            }
        }
        return presenza;
    }
    
    /**
     * ricerca e ritorna un creek in base all'id univoco passato come parametro
     * se non lo trova ritorna null
     * @param id
     * @return il Creek cercato
     */
    public synchronized Creek getCreek(int id){
        Creek ret = null;
        for(Creek c : arraydescr){
            if(c.getId() == id){
                return c;
            }
        }
        //creek non trovato
        return ret;
    }
    /**
     * Avvia il download dei file selezionati nella ricerca per lo scaricamento
     * @param array indici dei file da scaricare
     */
    public synchronized void avviaDescr(int[] array) throws ErrorException {
        Thread t = new Thread(new Avvia(this,array));
        t.start();
    }

    /**
     * Aggiunge ad arraydescr un creek se non è già presente
     * @param c creek da aggiungere
     * @return true se tutto ok, false se è già presente
     * @throws condivisi.ErrorException se c è null
     */
    public synchronized boolean addCreek(Creek creek) throws ErrorException {
        if (creek == null) {
            throw new ErrorException("Param null");
        }
        int id = creek.getId();
        //boolean trovato = false;
        //for (Creek c : arraydescr) {
        //    if ((c.getName()).compareTo(nome) == 0) {
        //       trovato = true;
        //        break;
        //    }
        //}
        boolean trovato = this.presenza(id);
        if (!trovato) {
            System.out.println(Thread.currentThread().getName()+"CREEK NON PRESENTE IN ARRAYDESCR");
            FileOutputStream c = null;
            ObjectOutputStream o = null;
            try {
                System.out.println("INIZIO PEZZO TRAGICO");
                
                c = new FileOutputStream(new File("./MetaInfo/" + creek.getName() + ".creek"));
                o = new ObjectOutputStream(c);
                System.out.println("CREATO LO STREAM");
                o.writeObject(creek);
                c.close();
                System.out.println("FINE PEZZO TRAGICO");
            } catch (FileNotFoundException ex) {
                File f = new File("./MetaInfo/" + creek.getName() + ".creek");
                f.delete();
                throw new ErrorException("Impossibile aggiungere il file: FILE NOT FOUND");
            } catch (IOException e) {
                File f = new File("./MetaInfo/" + creek.getName() + ".creek");
                f.delete();
                throw new ErrorException("Impossibile aggiungere il file: IOEXCEPTION");
            }
            arraydescr.add(creek);
        }
        return !trovato;
    }

    /**
     * Aggiunge ad arraydescr un creek se non è già presente
     * @param c creek da aggiungere
     * @return true se tutto ok, false se è già presente
     * @throws condivisi.ErrorException se c è null
     */
    public synchronized boolean deleteCreek(String nome) throws ErrorException {
        if (nome == null) {
            throw new ErrorException("Param null");
        }
        int pos = 0;
        boolean rimosso = false;
        File f = null;

        for (Creek c : arraydescr) {
            if (c.getName().compareTo(nome) == 0) {
                arraydescr.remove(pos);
                if ((c.getStato() && c.getPercentuale() != FINITO) || !c.getStato()) {
                    f = new File("./FileCondivisi/" + nome);
                    f.delete();
                }
                rimosso = true;
                break;
            }
            pos++;
        }
        return rimosso;
    }

    /**
     * Metodo che viene invocato dall'implementazione callback per
     * aggiornare lo stato
     * 
     * @param ind
     * @param nome
     */
    public synchronized void notifica(InetAddress ind, String nome) {
        for (Creek c : arraydescr) {
            if (c.getName().compareTo(nome) == 0) {
                c.settaPeerCerca();
                c.settaIdentita(ind);
                break;
            }
        }
    }

    /**
     * Handler di chiusura del protocollo
     */
    public void close() {

        /* chiusura connessioni */

        disconnetti();

        /* chiusura threads */

        // vedere se necessario visto che appl termina

        /* eventuale salvataggio  */

        // già fatto durante la creazione ddel creek, vedere per file in download

        /* cancellazione del file di avvio del programma */

        File conf = new File("./avviato.on");
        conf.delete();

        /* termino il processo */

        System.exit(0);
    }

    /**
     * Disconnette il peer
     * @throws condivisi.ErrorException
     */
    public void disconnetti() {

        /* chiusura socket con altri peer */

        // --------> da fare ->basta uccidere il thread pool!!! se mai ci sara`

        /* chiusura "connessione" con il server */

        ipServer = null;
        portarichieste = NULL;
        stub = null;
        stubcb = null;
        callback = null;
        try {
            if (welcome != null) {
                /* chiudo la socket solo se è inizializzata */
                welcome.close();
            }
        } catch (IOException ex) {
        }
    }

    public void cerca(String nome, BitCreekGui gui) throws ErrorException {
        if (nome == null || gui == null) {
            throw new ErrorException("Param null");
        }
        Thread t = new Thread(new Cerca(nome, this, gui));
        t.start();
    }

    /**
     * Fa partire un task che si occupa di creare e pubblicare un creek
     * @param sorgente file da pubblicare
     * @exception condivisi.ErrorException se sorgente è null
     */
    public void crea(File sorgente, BitCreekGui gui) throws ErrorException {
        if (sorgente == null || gui == null) {
            throw new ErrorException("Param null");
        }
        Thread t = new Thread(new Crea(sorgente, this, gui));
        t.start();
    }

    /**
     * Fa partire un task che si occupa ci aprire un .creek su disco
     * @param creek file .creek
     * @throws condivisi.ErrorException se creek è null
     */
    public void apri(File creek, BitCreekGui gui) throws ErrorException {
        if (creek == null || gui == null) {
            throw new ErrorException("Param null");
        }
        Thread t = new Thread(new Apri(creek, this, gui));
        t.start();
    }

    /**
     * Fa partitìre un task che si occupa di eliminare il file con nome nome
     * @param nome
     * @throws condivisi.ErrorException
     */
    public void elimina(String nome) throws ErrorException {
        if (nome == null) {
            throw new ErrorException("Param null");
        }
        Thread t = new Thread(new Elimina(nome, this));
        t.start();
    }

    /**
     * Tenta di stabilire una connessione con il server
     * @param server ip del server
     * @throws condivisi.ErrorException se server è null
     */
    public void connetti(InetAddress server, BitCreekGui gui) throws ErrorException {
        if (server == null || gui == null) {
            throw new ErrorException("Param null");
        }
        File f = new File("./porta.conf");
        byte[] b = new byte[20];
        try {
            FileInputStream in = new FileInputStream(f);
            in.read(b);
        } catch (FileNotFoundException ex) {
            throw new ErrorException("File porta.conf non trovato");
        } catch (IOException e) {
            throw new ErrorException("IO Problem");
        }
        int porta = (b[0] << 24) + ((b[1] & 0xFF) << 16) + ((b[2] & 0xFF) << 8) + (b[3] & 0xFF);
        if (porta == 0) {
            throw new ErrorException("Porta non valida. Settarla dal menu Aiuto");
        }
        try {
            /* attivazione socket di ascolto per le query di altri peer */
            welcome = new ServerSocket(porta);
            welcome.setSoTimeout(ATTESARISPSERVER);
            portarichieste = welcome.getLocalPort();
            ipServer = server;
        } catch (UnknownHostException ex) {
            ipServer = null;
        } catch (IOException e) {
            ipServer = null;
        }

        if (ipServer != null) {

            try {

                /* attivazione rmi parte client e attivazione callback */

                Registry reg = LocateRegistry.getRegistry(ipServer.getHostAddress(), PORTARMI);
                stub = (InterfacciaRMI) reg.lookup("MetodiRMI");
                callback = new ImplementazioneCallback(this);
                stubcb = (InterfacciaCallback) UnicastRemoteObject.exportObject(callback, 0);
            } catch (NullPointerException e) {
                ipServer = null;
            } catch (RemoteException e) {
                ipServer = null;
            } catch (NotBoundException e) {
                ipServer = null;
            }
            /* Se non esiste faccio partire il thread per i keep alive */

            if (!keepalive) {
                Thread t = null;
                try {
                    t = new Thread(new KeepAlive(this));
                } catch (ErrorException ex) {
                    ipServer = null;
                }
                if (ipServer != null) {
                    t.start();
                    keepalive = true;
                }
            }
        }

        /* controllo l'esito dell'operazione */
        if (ipServer == null) {
            try {
                if (welcome != null) {
                    welcome.close();
                }
            } catch (IOException ex) {
            }
            portarichieste = NULL;
            stub = null;
            stubcb = null;
            callback = null;
            throw new ErrorException("Porta non valida. Settarla dal menu Aiuto");
        } else {
            /* avviso l'interfaccia che la connetti è stata effettuata con successo */
            gui.connettiDone();
        }

    }

    public void salva(String path, String file, boolean cerca) throws ErrorException {
        if (path == null || file == null) {
            throw new ErrorException("Param null");
        }
        File percorso = new File(path + file + ".creek");
        ObjectOutputStream output = null;
        try {
            output = new ObjectOutputStream(new FileOutputStream(percorso));
        } catch (FileNotFoundException ex) {
            percorso.delete();
            throw new ErrorException("Percorso non trovato");
        } catch (IOException e) {
            percorso.delete();
            throw new ErrorException("Impossibile creare creek");
        }
        if (cerca) {
            Descrittore d = getFileCerca(file);
            if (d == null) {
                percorso.delete();
                throw new ErrorException("File non trovato");
            } else {
                Creek c = new Creek(d, true, false);
                try {
                    output.writeObject(c);
                    output.close();
                } catch (IOException ex) {
                    percorso.delete();
                    throw new ErrorException("Impossibile leggere metainfo");
                }
            }
        } else {
            ObjectInputStream in = null;
            Creek copia = null;
            try {
                in = new ObjectInputStream(new FileInputStream(new File("./MetaInfo/" + file + ".creek")));
                Creek c = (Creek) in.readObject();
                copia = c.esporta();
                output.writeObject(copia);
                output.close();
                in.close();
            } catch (IOException e) {
                percorso.delete();
                throw new ErrorException("Impossibile leggere metainfo");
            } catch (ClassNotFoundException e) {
                percorso.delete();
                throw new ErrorException("Impossibile leggere metainfo");
            }
        }
    }

    /**
     * effettua una copia di un descrittore presente nell'array cercati
     * @param nome
     * @return la copia del descrittore
     * @throws condivisi.ErrorException
     */
    private synchronized Descrittore getFileCerca(String nome) throws ErrorException {
        if (nome == null) {
            throw new ErrorException("Param null");
        }
        Descrittore d = new Descrittore();
        for (Descrittore temp : cercati) {
            if (temp.getName().compareTo(nome) == 0) {
                d = temp.copia();
                break;
            }
        }
        return d;
    }

    /**
     * Effettua il test NAT - Firewall
     */
    public void test(BitCreekGui gui) throws ErrorException {

        Socket s = null;
        boolean problema = false;

        /* contatto il server specificando la porta su cui sono in ascolto */
        if (ipServer != null) {
            try {
                SocketAddress sa = new InetSocketAddress(ipServer, PORTASERVER);
                s = new Socket();
                s.connect(sa, ATTESARISPSERVER);
            } catch (IOException e) {
                /* il  server non risponde */
                disconnetti();
                problema = true;
            }
        }

        if (ipServer != null && !problema) {
            mioip = s.getLocalAddress();
            try {
                DataOutputStream out = new DataOutputStream(s.getOutputStream());
                out.writeInt(portarichieste);
                s.close();
            } catch (IOException e) {
                problema = true;
            }

            if (!problema) {
                bloccato = false;

                /* controllo se sono nattato o firewallato facendomi contattare dal server */
                try {
                    Socket prova = welcome.accept();
                    prova.close();
                } catch (SocketTimeoutException ex) {
                    /* sono nattato o firewallato */
                    bloccato = true;
                } catch (IOException ex) {
                    bloccato = true;
                    problema = true;
                }
            }
        }

        /* controllo l'esito delle operazioni*/
        if (ipServer != null && !problema) {
            if (!bloccato) {
                /* Se non esiste faccio partire il thread in ascolto sulla socket di benvenuto */
                if (!ascolto) {
                    Thread t = null;
                    try {
                        t = new Thread(new Ascolto(this));
                    } catch (ErrorException ex) {
                        bloccato = true;
                        problema = true;
                    }
                    t.start();
                    ascolto = true;
                }
            }
        }

        if (problema) {
            throw new ErrorException("Test fallito");
        } else {
            gui.testDone(bloccato, portarichieste);
        }

    }
}
