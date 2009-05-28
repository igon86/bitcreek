package condivisi;

import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.ArrayList;

/**
 * Interfaccia RMI
 * @author Bandettini
 */
public interface InterfacciaRMI extends Remote{

    /**
     * Metodo esportato dal server al peer per l'invio di un descrittore
     * @param d descrittore
     * @param ip ip client
     * @param porta porta client
     * @return porte TCP e UDP dei tracker
     * @throws java.rmi.RemoteException
     */
    public Porte inviaDescr (Descrittore d,InetAddress ip,int porta) throws RemoteException;

    /**
     * Metodo esportato dal server al peer per effettuare una ricerca
     * @param nomefile nome del file da cercare
     * @param ind ip del peer
     * @return lista di descrittori
     * @throws java.rmi.RemoteException
     */
    public ArrayList<Descrittore> ricerca(String nomefile,InetAddress ind) throws RemoteException;
    
}
