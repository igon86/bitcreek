package server;

import condivisi.Descrittore;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Task che si occupa di salvare le metainfo
 * ad intervalli di tempo regolari
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ThreadSaver implements Runnable {

    /* Costanti */
    /**
     * Costante che definisce il tempo di attesa tra un salvataggio
     * e il successivo
     */
    private static final int ATTESA = 15000;
    /* Variabili d' istanza */
    /** Metainfo */
    private MetaInfo tabella;

    /**
     * Costruttore
     * @param tabella
     */
    public ThreadSaver(MetaInfo tabella) {
        this.tabella = tabella;
    }

    /**
     * Corpo del task
     */
    public void run() {
        File dir = new File("./MetaInfo");
        File temp = null;
        Descrittore d = null;
        Iterator<Descrittore> i = null;
        ObjectOutputStream o = null;

        while (true) {
            for (File f : dir.listFiles()) {
                f.delete();
            }
            /* salvo tutto su file nella cartella MetaInfo */
            i = tabella.iterator();
            while (i.hasNext()) {
                d = i.next();
                temp = new File("./MetaInfo/" + d.getName() + ".metainfo");
                try {
                    o = new ObjectOutputStream(new FileOutputStream(temp));
                    o.writeObject(d);
                    o.close();
                } catch (FileNotFoundException ex) {
                } catch (IOException ex) {
                }
            }
            /* dormo per 15 secondi */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                System.err.println("ThreadSaver : sono stato interrotto");
            }
        }
    }
}
