package gui;

import condivisi.ErrorException;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Classe che definisce il modello della tabella dei file creek in download
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitcreekPeer 1.0
 */
public class ModelloTabellaMieiCreek extends AbstractTableModel {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 12;
    /** Vettore di headers */
    private final String[] headers = {"Id", "File", "Dimensione", "Stato", "Situazione", "Percentuale", "Peer"};
    /** Vettore di tipi */
    private final Class[] tipi = {"".getClass(), "".getClass(), "".getClass(), "".getClass(), "".getClass(), "".getClass(), "".getClass()};

    /* Variabili d'istanza */
    /** Vettore di righe */
    private ArrayList<RigaTabellaMieiCreek> vettore;

    /** 
     * Costruttore
     */
    public ModelloTabellaMieiCreek() {
        vettore = new ArrayList<RigaTabellaMieiCreek>();
    }

    /**
     * Aggiunge una riga al modello
     * @param r riga da aggiungere
     * @exception condivisi.ErrorException se riga è null
     */
    public void addRiga(RigaTabellaMieiCreek r) throws ErrorException {
        if (r == null) {
            throw new ErrorException("Param null");
        }
        vettore.add(r);
    }

    /**
     * Rimuove una riga dal modello in posizione i
     * se i è corretto
     * @param i posizione
     * @exception condivisi.ErrorException se i non valido
     */
    public void removeRiga(int i) throws ErrorException {
        if (i < 0 || i >= vettore.size()) {
            throw new ErrorException("Param invalid");
        }
        vettore.remove(i);
    }

    /**
     * Rimuove tutte le righe dal modello
     */
    public void removeTutti() {
        vettore.clear();
    }

    /**
     * Restituisce la riga in posizone i se i è corretto
     * @param i posizione
     * @return riga
     * @throws condivisi.ErrorException se i non valido
     */
    public RigaTabellaMieiCreek getRiga(int i) throws ErrorException {
        if (i < 0 || i >= vettore.size()) {
            throw new ErrorException("Param invalid");
        }
        return vettore.get(i);
    }

    /**
     * Controlla la presenza di un riga con nome uguale al nome
     * passato come parametro
     * @param nome da cercare
     * @return r se riga è stata trovata, null altrimenti
     */
    public RigaTabellaMieiCreek presenza(String nome) {
        if (nome == null) {
            return null;
        }
        for (RigaTabellaMieiCreek r : vettore) {
            if (r.getFile().compareTo(nome) == 0) {
                return r;
            }
        }
        return null;
    }

    /**
     * Restituisce il numero di righe del modello
     * @return numero righe
     */
    public int getRowCount() {
        return vettore.size();
    }

    /**
     * Restituisce il numero di colonne del modello
     * @return numero colonne
     */
    public int getColumnCount() {
        return headers.length;
    }

    /**
     * Restituisce l'oggetto in posizione [rowIndex][columnIndex]
     * se gli indici passati come argomento sono validi
     * @param rowIndex indice riga
     * @param columnIndex indice colonna
     * @return oggetto [rowIndex][columnIndex] se esiste, altrimenti null
     */
    public Object getValueAt(int rowIndex, int columnIndex) {

        if (rowIndex < 0 || rowIndex >= vettore.size()) {
            return null;
        }
        if (columnIndex < 0 || columnIndex >= headers.length) {
            return null;
        }

        RigaTabellaMieiCreek r = vettore.get(rowIndex);
        if (columnIndex == 0) {
            return rowIndex + 1;
        }
        if (columnIndex == 1) {
            return r.getFile();
        }
        if (columnIndex == 2) {
            return r.getDimensione();
        }
        if (columnIndex == 3) {
            return r.getStato();
        }
        if (columnIndex == 4) {
            return r.getSituazione();
        }
        if (columnIndex == 5) {
            return r.getPercentuale();
        }
        if (columnIndex == 6) {
            return r.getPeer();
        }
        return "";
    }

    /**
     * Restituisce il titolo della colonna in posizione c
     * se c è corretto
     * @param c
     * @return titolo se tutto va bene, null altrimenti
     */
    @Override
    public String getColumnName(int c) {
        if (c < 0 || c >= headers.length) {
            return null;
        }
        return headers[c];
    }

    /**
     * Restituisce il tipo della colonna in posizione c
     * se c è corretto
     * @param c
     * @return tipo della colonna; altrimenti null
     */
    @Override
    public Class getColumnClass(int c) {
        if (c < 0 || c >= tipi.length) {
            return null;
        }
        return tipi[c];
    }

    /**
     * Controlla se la cella[row][col] è editabile;
     * restituisce sempre false
     * @param row riga
     * @param col colonna
     * @return false
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
