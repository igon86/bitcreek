package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import condivisi.NetRecord;
import gui.BitCreekGui;
import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.util.ArrayList;

/**
 * Task che si occupa di aprire un .creek dal disco
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Apri implements Runnable {

    /* Variabili d'istanza */
    /** File da aprire */
    private File creek;
    /** Peer */
    private BitCreekPeer peer;
    /** Gui */
    private BitCreekGui gui;

    /**
     * Costruttore
     * @param creek 
     * @param peer 
     * @param gui
     * @throws ErrorException se almeno un parametro è null
     */
    public Apri(File creek, BitCreekPeer peer, BitCreekGui gui) throws ErrorException {
        if (creek == null || peer == null || gui == null) {
            throw new ErrorException("Param null");
        }
        this.creek = creek;
        this.peer = peer;
        this.gui = gui;
    }

    /**
     * Corpo del task
     */
    public void run() {

        /* cambio il cursore */
        gui.getRootPane().setCursor(new Cursor(Cursor.WAIT_CURSOR));

        String nome = creek.getName();
        ObjectInputStream in = null;
        Creek c = null, cr = null;

        boolean problema = false;

        /* controllo che sia un .creek */
        if (nome.length() > 6 && nome.substring(nome.length() - 6, nome.length()).compareTo(".creek") == 0) {
            try {
                in = new ObjectInputStream(new FileInputStream(creek));
                cr = (Creek) in.readObject();
                try {
                    c = new Creek((Descrittore) cr, cr.getStato(), cr.getPubblicato());
                } catch (ErrorException ex) {
                }
                in.close();
            } catch (IOException ex) {
                problema = true;
            } catch (ClassNotFoundException ex) {
                problema = true;
            }
            /* se il file non è già presente ed è da downlodare lo inizializzo e poi  lo aggiungo */
            if (!problema && c.getStato()) {
                try {
                    if (!peer.addCreek(c)) {
                        problema = true;
                    }
                } catch (ErrorException ex) {
                    problema = true;
                }
                /* inizializzo il creek */
                c.setToDo();
                /* vedo di scaricarlo */
                ArrayList<NetRecord> lista = peer.contattaTracker(c);
                /* contatto i peer nella lista */
                peer.aggiungiLista(c, lista);
                /* creo uploaderManager */
                peer.addTask(new UploadManager(peer, c));
            } else {
                problema = true;
            }
        } else {
            problema = true;
        }
        if (problema) {
            gui.PrintInformation("Non si può aprire creek", gui.ERRORE);
        }
        /* cambio il cursore */
        gui.getRootPane().setCursor(new Cursor(Cursor.DEFAULT_CURSOR));
    }
}
