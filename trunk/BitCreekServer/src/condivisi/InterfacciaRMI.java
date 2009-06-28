package condivisi;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interfaccia RMI
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public interface InterfacciaRMI extends Remote {

    /**
     * Metodo esportato dal server al peer per l'invio di un descrittore
     * @param d descrittore da pubblicare
     * @param ip ip client che pubblica
     * @param porta porta in ascolto del peer che pubblica
     * @return porte TCP e UDP dei tracker
     * @throws java.rmi.RemoteException
     */
    public Porte inviaDescr(Descrittore d, InetAddress ip, int porta) throws RemoteException;

    /**
     * Metodo esportato dal server al peer per effettuare una ricerca
     * @param nomefile nome del file da cercare
     * @param ind ip del peer che sta cercando
     * @return lista di descrittori
     * @throws java.rmi.RemoteException
     */
    public ArrayList<Descrittore> ricerca(String nomefile, InetAddress ind) throws RemoteException;
}
