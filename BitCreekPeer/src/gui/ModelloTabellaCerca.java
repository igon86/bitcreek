package gui;

import condivisi.ErrorException;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * Classe che definisce il modello della tabella dei file crecati
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ModelloTabellaCerca extends AbstractTableModel {

    /* Costanti */
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 12;
    /** Vettore di headers */
    private final String[] headers = {"File", "Dimensione", "Numero Seeders", "Numero Leechers"};
    /** Vettore dei tipi */
    private final Class[] tipi = {"".getClass(), "".getClass(), "".getClass(), "".getClass()};

    /* Variabili d' istanza */
    /** Vettore di righe */
    private ArrayList<RigaTabellaCerca> vettore;

    /**
     * Costruttore
     */
    public ModelloTabellaCerca(){
        vettore = new ArrayList<RigaTabellaCerca>();
    }

    /**
     * Aggiunge una riga al modello
     * @param r riga da aggiungere
     * @throws condivisi.ErrorException se la riga passata come parametro è null
     */
    public void addRiga(RigaTabellaCerca r) throws ErrorException {
        if (r == null) {
            throw new ErrorException("Param null");
        }
        vettore.add(r);
    }

    /**
     * Restituisce la riga con indice i passato come argomento
     * @param i indice della riga
     * @return riga in posizione i
     * @throws condivisi.ErrorException se l'indice non è valido
     */
    public RigaTabellaCerca getRiga(int i) throws ErrorException {
        if (i < 0 || i >= vettore.size()) {
            throw new ErrorException("Param invalid");
        }
        return vettore.get(i);
    }

    /**
     * Rimuove tutte le righe dalla tabella
     */
    public void removeall() {
        vettore.clear();
    }

    /**
     * Restitusce il numero di righe
     * @return vettore.size()
     */
    public int getRowCount() {
        return vettore.size();
    }

    /**
     * Restituisce il numero di colonne
     * @return headers.length
     */
    public int getColumnCount() {
        return headers.length;
    }

    /**
     * Restitisce l' oggetto in posizione [rowIndex][colunmIndex]
     * se gli indici sono corretti
     * @param rowIndex
     * @param columnIndex
     * @return vettore [rowIndex][colunmIndex]
     */
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (rowIndex < 0 || rowIndex >= vettore.size()) {
            return null;
        }
        if (columnIndex < 0 || columnIndex >= headers.length) {
            return null;
        }

        RigaTabellaCerca riga = vettore.get(rowIndex);
        if (columnIndex == 0) {
            return riga.getNome();
        }
        if (columnIndex == 1) {
            return riga.getDimensione();
        }
        if (columnIndex == 2) {
            return riga.getSeeders();
        }
        if (columnIndex == 3) {
            return riga.getLeechers();
        }
        return "";
    }

    /**
     * Restituisce l' intestazione della colonna c
     * se c è corretto ; altrimenti null
     * @param c indice di colonna
     * @return headers[c] ; altrimeenti null
     */
    @Override
    public String getColumnName(int c) {
        if (c < 0 || c >= headers.length) {
            return null;
        }
        return headers[c];
    }

    /**
     * Restituisce il tipo della colonna c
     * se c è corretto ; altrimenti null
     * @param c indice di colonna
     * @return tipi[c] ; altrimeenti null
     */
    @Override
    public Class getColumnClass(int c) {
        if (c < 0 || c >= tipi.length) {
            return null;
        }
        return tipi[c];
    }

    /**
     * Restiuisce sempre false come cella editabile
     * @param row
     * @param col
     * @return false in ogni caso
     */
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }
}
