
package server;

import java.io.DataInputStream;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

/**
 *
 * @author lottarin
 */
public class ServerListener implements Runnable {
    
    private static final int NULL = -1;
    
    private MetaInfo tabella;
    private ServerSocket welcome;
    
    public ServerListener(MetaInfo tabella, ServerSocket welcome){
        this.tabella = tabella;
        this.welcome = welcome;
    }
    
    public void run() {
        Socket connessione = null;
        Socket prova = null;
        InetAddress peer = null;
        DataInputStream in = null;
        int porta = NULL;
        /*int c = 0; ---> per prova nat-firewall*/

        while (true) {
            try {
                connessione = welcome.accept();
                in = new DataInputStream(connessione.getInputStream());
                porta = in.readInt();
                peer = connessione.getInetAddress();
                connessione.close();
                /*if(c != 0){ ---> per prova nat-firewall*/
                prova = new Socket(peer, porta);
                prova.close();
            /*}
            c++; ---> per prova nat-firewall*/
            } catch (IOException ex) {
                System.err.println("Impossibile stabilire una connessione con : " + peer.getHostAddress());
            }
        }
    }
}
