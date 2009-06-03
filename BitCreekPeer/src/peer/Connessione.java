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
public class Connessione implements Serializable{
    
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
    int downloaded; /* niìumero pezzi scaricati su questa connessione */
    
    public Connessione(Socket down, Socket up, boolean[] bitfield, int portaVicino){
        this.down = down;
        this.up = up;
        this.bitfield = bitfield;
        if (down != null){
            this.ipVicino = down.getInetAddress();
            try {
                this.outDown = new ObjectOutputStream(down.getOutputStream());
                this.inDown = new ObjectInputStream(down.getInputStream());
            } catch (IOException ex) {
                System.out.println(Thread.currentThread().getName()+" NON MI SI WRAPPANO LE SOCKET IN DOWNLOAD");
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
            
        }
        else{
            this.ipVicino = up.getInetAddress();
            try {
                this.outUp = new ObjectOutputStream(up.getOutputStream());
                this.inUp = new ObjectInputStream(up.getInputStream());
            } catch (IOException ex) {
                System.out.println(Thread.currentThread().getName()+" NON MI SI WRAPPANO LE SOCKET IN UPLOAD");
                Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        this.portaVicino = portaVicino;
        this.downloaded = 0;
        //Il binding degli stream alle socket viene effettuato solo al momento opportuno
        //dai thread
    }

    /**
     * Restituisce true se la socket in download è null
     * @return
     */
    public boolean DownNull(){
        return down == null;
    }

    /**
     * Metodo utilizzato per controllare se una connessione e` gia presente
     * @param ip
     * @param porta
     * @return
     */
    public boolean confronta(InetAddress ip, int porta){
        if (this.ipVicino == ip && this.portaVicino == porta)
            return true;
        else
            return false;
    }
    
    //Virtualizzazione
    public synchronized void sendDown(Messaggio m){
        try {
            outDown.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void sendUp(Messaggio m){
        try {
            this.outUp.writeObject(m);
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized Messaggio receiveDown(){
        try {
            if(inDown == null) System.out.println(Thread.currentThread().getName()+" inDown non inizializzata, sei un programmatore BUSTA");
            return (Messaggio) inDown.readObject();
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
    
    public synchronized Messaggio receiveUp(){
        try {
            if(inUp == null) System.out.println(Thread.currentThread().getName()+" inUp non inizializzata, sei un programmatore BUSTA");
            return (Messaggio) inUp.readObject();
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public synchronized void ResetDown(){
        try {
            outDown.reset();
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public synchronized void ResetUp(){
        try {
            outUp.reset();
        } catch (IOException ex) {
            Logger.getLogger(Connessione.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    //chiamato in seguito ad un messaggio di have
    public synchronized void setBitfield(boolean[] b){
        this.bitfield = b;
    }
    
    //GETTER
    
    public boolean[] getBitfied(){
        return this.bitfield;
    }
    
    public int getPortaVicino(){
        return this.portaVicino;
    }
    
    public InetAddress getIPVicino(){
        return this.ipVicino;
    }
    
    public boolean getStatoDown(){
        return this.statoDown;
    }
    
    public boolean getStatoUp(){
        return this.statoUp;
    }
    
    public boolean getInteresseDown(){
        return this.interesseDown;
    }
    
    public boolean getInteresseUp(){
        return this.interesseUp;
    }
    
    //SETTER
    public synchronized void setStatoDown(boolean stato){
        this.statoDown = stato;
    } 
    
    public synchronized void setStatoUp(boolean stato){
        this.statoUp = stato;
    }
    public synchronized void setSocketUp(Socket up){
        this.up = up;
    }
    
    public synchronized void setSocketDown(Socket down){
        this.down = down;
    }
    
    public synchronized void setInteresseDown(boolean b){
        this.interesseDown = b;
    }
    
    public synchronized void setInteresseUp(boolean b){
        this.interesseUp = b;
    }
    
    public synchronized int incrDown(){
        return ++this.downloaded;
    }
}
