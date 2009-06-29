package peer;

import condivisi.Descrittore;
import condivisi.ErrorException;
import gui.BitCreekGui;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Task che si occupa di cercare un file contattando il server
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class Cerca implements Runnable {

    /* Costanti */
    /** Definisce la costante ERRORE */
    private final char ERRORE = 1;
    /** Definisce la costante INFORMATION */
    private final char INFORMATION = 2;

    /* Variabili d'istanza */
    /** Nome da cercare */
    private String nome;
    /** Peer */
    private BitCreekPeer peer;
    /** Gui */
    private BitCreekGui gui;

    /**
     * Costruttore
     * @param nome nome da cercare
     * @param peer logica del peer
     * @param gui interfaccia grafica da aggiornare
     * @throws ErrorException se almeno un parametro è null
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
