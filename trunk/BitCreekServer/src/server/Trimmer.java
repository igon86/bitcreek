/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package server;

import condivisi.Descrittore;
import java.util.TimerTask;

/**
 *
 * @author lottarin
 */
public class Trimmer extends TimerTask {

    ListaPeer lista;
    Descrittore d;

    public Trimmer(ListaPeer lista, Descrittore d) {
        this.lista = lista;
        this.d = d;
    }

    @Override
    public void run() {
        /* trimmo la lista dai peer deceduti*/
        NumPeer np = lista.trimPeer();
        /* aggiorno il descrittore */
        d.setNumSeeders(np.getSeeders());
        d.setNumLeechers(np.getLeechers());
    }
}
