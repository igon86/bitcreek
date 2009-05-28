package condivisi;


import java.net.InetAddress;
import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * Interfaccia per le callback
 * @author Bandettini
 */

public interface InterfacciaCallback extends Remote {

    /**
     * Metodo esportato dal peer al server
     * @param ind
     * @param nome
     * @throws java.rmi.RemoteException
     */
    public void notifyMe(InetAddress ind,String nome) throws RemoteException;
}
