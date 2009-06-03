package gui;

import condivisi.ErrorException;

/**
 *
 * @author bande
 */
public class RigaTabellaCerca {

    /* Costanti */
    private final int K = 1024;

    /* Variabili d'istanza */
    private String nome;
    private String dimensione;
    private int numeroSeeders;
    private int numeroLeechers;
    
    
    public RigaTabellaCerca(String nome, long dimensione, int numseeders, int numleechers) throws ErrorException{
        if( nome == null || dimensione <= 0 || numseeders < 0 || numleechers < 0 ) throw new ErrorException("Param null");
        this.nome = nome;
        this.dimensione = this.Dim(dimensione);
        this.numeroSeeders = numseeders;
        this.numeroLeechers = numleechers;
        
    }
    
    public String getNome(){
        return this.nome;
    }

    public String getDimensione(){
        return this.dimensione;
    }
    
    public int getSeeders(){
        return this.numeroSeeders;
    }
    
    public int getLeechers(){
        return this.numeroLeechers;
    }

    private String Dim(long dimensione){
        int i = 0;
        for (i = 0; ; i++){
            if( (dimensione / K) < 1)
                break;
            else
                dimensione = dimensione / K;
        }
        if(i == 1)return dimensione + " Kbyte";
        if(i == 2)return dimensione + " Mbyte";
        if(i == 3)return dimensione + " Gbyte";
        if(i == 4)return dimensione + " Tbyte";
        return dimensione + " byte";
    }
    
}
