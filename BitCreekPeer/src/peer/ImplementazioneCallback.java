package peer;

import condivisi.ErrorException;
import condivisi.InterfacciaCallback;
import java.net.InetAddress;

/**
 * Classe che implementa l' interfaccia delle callback
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class ImplementazioneCallback implements InterfacciaCallback {

    /* Variabili d' istanza */
    /** Peer */
    private BitCreekPeer peer;

    /**
     * Costruttore
     * @param peer
     * @throws condivisi.ErrorException se peer è null
     */
    public ImplementazioneCallback(BitCreekPeer peer) throws ErrorException {
        super();
        if (peer == null) {
            throw new ErrorException("Param null");
        }
        this.peer = peer;
    }

    /**
     * Implementazione del metodo dell' interfaccia
     * @param ind
     * @param nome
     */
    public void notifyMe(InetAddress ind, String nome) {
        peer.notifica(ind, nome);
    }
}
