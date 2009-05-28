package gui;

import condivisi.ErrorException;

/**
 *
 * @author bande
 */
public class RigaTabellaCerca {

    private String nome;
    private long dimensione;
    private int numeroSeeders;
    private int numeroLeechers;
    
    
    public RigaTabellaCerca(String nome, long dimensione, int numseeders, int numleechers) throws ErrorException{
        if( nome == null || dimensione <= 0 || numseeders < 0 || numleechers < 0 ) throw new ErrorException("Param null");
        this.nome = nome;
        this.dimensione = dimensione;
        this.numeroSeeders = numseeders;
        this.numeroLeechers = numleechers;
        
    }
    
    public String getNome(){
        return this.nome;
    }

    public long getDimensione(){
        return this.dimensione;
    }
    
    public int getSeeders(){
        return this.numeroSeeders;
    }
    
    public int getLeechers(){
        return this.numeroLeechers;
    }
    
}
