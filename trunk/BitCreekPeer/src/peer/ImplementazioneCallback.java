package peer;

import condivisi.ErrorException;
import condivisi.InterfacciaCallback;
import java.net.InetAddress;

/**
 *
 * @author Bandettini
 */

public class ImplementazioneCallback implements InterfacciaCallback{

    /* Variabili d' istanza */
    private BitCreekPeer peer;
    
    
    public ImplementazioneCallback(BitCreekPeer peer) throws ErrorException{
        super();
        if(peer == null)throw new ErrorException("Param null");
        this.peer = peer;
    }
    
    public void notifyMe(InetAddress ind,String nome) {
        peer.notifica(ind,nome);
    }

}