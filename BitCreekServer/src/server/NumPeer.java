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

    public NumPeer(int seeders, int leechers) {
        this.seeders = seeders;
        this.leechers = leechers;
    }
    
    public int getSeeders(){
       return this.seeders;
    }
    
    public int getLeechers(){
        return this.leechers;
    }
}
