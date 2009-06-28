/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
 *
 * @author lottarin
 */
public class ThreadSaver implements Runnable {
    
    private static final int ATTESA = 15000;
    
    private MetaInfo tabella;

    /**
     *
     * @param tabella
     */
    public ThreadSaver(MetaInfo tabella) {
        this.tabella = tabella;
    }

    public void run() {
        File dir = new File("./MetaInfo");
        File temp = null;
        Descrittore d = null;
        Iterator<Descrittore> i = null;
        ObjectOutputStream o = null;

        while (true) {

            /* rimuovo tutto dalla cartella MetaInfo */
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
                    Logger.getLogger(BitCreekServer.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IOException ex) {
                    Logger.getLogger(BitCreekServer.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            /* dormo per 15 secondi */
            try {
                Thread.sleep(ATTESA);
            } catch (InterruptedException ex) {
                System.err.println("Thread salvataggio interrotto");
            }
        }
    }
    }

