package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import gui.BitCreekGui;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Task che si occupa di cercare un file contattando il server
 * @author Bandettini
 */
public class Cerca implements Runnable {

    /* Costanti */
    private final char ERRORE = 1;
    private final char INFORMATION = 2;

    /* Variabili d'istanza */
    private String nome; /* nome da cercare */

    private BitCreekPeer peer;
    private BitCreekGui gui;

    /**
     * Costruttore
     * @param nome 
     * @param peer
     * @param gui
     * @throws ErrorException
     */
    public Cerca(String nome, BitCreekPeer peer, BitCreekGui gui) throws ErrorException {
        if (nome == null || peer == null || gui == null) {
            throw new ErrorException("Param null");
        }
        this.nome = nome;
        this.peer = peer;
        this.gui = gui;
    }

    /**
     * Corpo del task
     */
    public void run() {
        ArrayList<Descrittore> array = null;
        try {
            array = peer.getStub().ricerca(nome, peer.getMioIp());
        } catch (RemoteException ex) {
            gui.PrintInformation("Impossibile avviare metodo importato dal server per la ricerca", ERRORE);
        }
        peer.setCercati(array);
        if (array.size() == 0) {
            gui.PrintInformation("Nessun Elemento trovato", INFORMATION);
        } else {
            try {
                gui.ricercaDone(peer.getCercati());
            } catch (ErrorException e) {
                gui.PrintInformation("Problema incapibile al momento per l' eccessiva strutturazione del codice : " + e.getMessage(), ERRORE);
            }
        }

    }
}
