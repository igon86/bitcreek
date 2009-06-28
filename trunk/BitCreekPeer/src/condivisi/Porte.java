package condivisi;

import java.io.Serializable;

/**
 * Struttura dati restituita dal server al peer per informarlo
 * delle porta dei tracker
 * @author bande
 */

public class Porte implements Serializable{

    /* Costanti */
    /**
     *
     */
    public static final long serialVersionUID = 17;

    /* Variabili d'istanza */
    private int portaTCP;
    private int portaUDP;
    private boolean pubblicato; /* info che ci dice se il file era già stato pubblicato */
    private int id;


    /**
     *
     * @param portatcp
     * @param portaudp
     * @param id
     * @throws condivisi.ErrorException
     */
    public Porte(int portatcp,int portaudp, int id) throws ErrorException{
        if( portatcp <= 0 || portaudp <= 0){
            throw new ErrorException("param null");
        }
        this.portaTCP = portatcp;
        this.portaUDP = portaudp;
        this.pubblicato = false;
        this.id = id;
    }

    /**
     * Restituisce la porta TCP
     * @return portaTCP
     */
    public int getPortaTCP(){
        return this.portaTCP;
    }

    /**
     * Restituisce la porta UDP
     * @return portaUDP
     */
    public int getPortaUDP(){
        return this.portaUDP;
    }

    /**
     * Restituisce il booleano pubblicato che ci dice
     * se un file era già stato pubblicato o no
     * @return pubblicato
     */
    public boolean getPubblicato(){
        return this.pubblicato;
    }

    /**
     *
     * @return
     */
    public int getId(){
        return this.id;
    }

    /**
     * Setta il booleano pubblicato a true
     */
    public void setPubblicato(){
        this.pubblicato = true;
    }
    
}
