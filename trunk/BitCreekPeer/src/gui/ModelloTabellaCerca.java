package gui;

import condivisi.ErrorException;
import java.util.ArrayList;
import javax.swing.table.AbstractTableModel;

/**
 * 
 * @author bande
 */

public class ModelloTabellaCerca extends AbstractTableModel{

    public static final long serialVersionUID = 12;
    
    private ArrayList<RigaTabellaCerca> vettore = new ArrayList<RigaTabellaCerca>();
    
    private String [] headers = {"File","Dimensione","Numero Seeders","Numero Leechers"};
    
    private Class [] tipi = {"".getClass(),"".getClass(),"".getClass(),"".getClass()};

    /**
     * Aggiunge una riga al modello
     * @param r
     * @throws condivisi.ErrorException
     */
    public void addRiga(RigaTabellaCerca r) throws ErrorException{
        if( r == null )throw new ErrorException("Param null");
        vettore.add(r);
    }
    
    public RigaTabellaCerca getRiga(int i) throws ErrorException{
        if(i < 0 || i >= vettore.size()) throw new ErrorException("Param invalid");
        return vettore.get(i);
    }
    
    public void removeall(){
        vettore.clear();
    }
    
    public int getRowCount() {
        return vettore.size();
    }

    public int getColumnCount() {
        return headers.length;
    }

    public Object getValueAt(int rowIndex, int columnIndex) {
        if(rowIndex < 0 || rowIndex >= vettore.size()) return null;
        if(columnIndex < 0 || columnIndex >= headers.length) return null;
        
        RigaTabellaCerca riga = vettore.get(rowIndex);
        if(columnIndex == 0)
            return riga.getNome();
        if(columnIndex == 1)
            return riga.getDimensione();
        if(columnIndex == 2)
            return riga.getSeeders();
        if(columnIndex == 3)
            return riga.getLeechers();
        return "";
    }
    
    @Override
    public String getColumnName(int c) {
        if( c < 0 || c >= headers.length) return null;
        return headers[c];
    }
    
    @Override
    public Class getColumnClass(int c) {
        if( c < 0 || c >= tipi.length) return null;
        return tipi[c];
    }
    
    @Override
    public boolean isCellEditable(int row, int col) {
        return false;
    }

}

