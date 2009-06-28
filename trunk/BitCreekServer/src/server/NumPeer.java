/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

/**
 *
 * @author lottarin
 */
public class NumPeer {

    int seeders;
    int leechers;

    /**
     *
     * @param seeders
     * @param leechers
     */
    public NumPeer(int seeders, int leechers) {
        this.seeders = seeders;
        this.leechers = leechers;
    }
    
    /**
     * 
     * @return
     */
    public int getSeeders(){
       return this.seeders;
    }
    
    /**
     *
     * @return
     */
    public int getLeechers(){
        return this.leechers;
    }
}
