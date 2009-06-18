package peer;

import condivisi.ErrorException;
import gui.BitCreekGui;
import java.awt.Cursor;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;

/**
 * Task che si occupa di aprire un .creek dal disco
 * @author Bandettini
 */
public class Apri implements Runnable {

    /* Variabili d'istanza */
    private File creek;
    private BitCreekPeer peer;
    private BitCreekGui gui;

    /**
     * Costruttore
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
        Creek c = null;

        boolean problema = false;

        /* controllo che sia un .creek */

        if (nome.length() > 6 && nome.substring(nome.length() - 6, nome.length()).compareTo(".creek") == 0) {
            try {
                in = new ObjectInputStream(new FileInputStream(creek));
                c = (Creek) in.readObject();
                in.close();
            } catch (IOException ex) {
                problema = true;
            } catch (ClassNotFoundException ex) {
                problema = true;
            }

            /* se il file non è già presente ed è da downlodare lo aggiungo */

            if (!problema && c.getStato()) {
                try {
                    if ( ! peer.addCreek(c) ) {
                        problema = true;
                    }
                } catch (ErrorException ex) {
                    problema = true;
                }
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
