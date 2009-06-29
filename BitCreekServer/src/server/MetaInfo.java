package server;

import condivisi.Descrittore;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Classe che rappresenta la struttura fondamentale
 * utilizzata dal server
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreekPeer 1.0
 */
public class MetaInfo extends HashSet<Descrittore> {

    /* Costanti */
    /** Costante che definisce il numero max di descrittori restituiti per ogni ricerca */
    private static final int NUMMAXDESCR = 15;
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 15;

    /**
     * Costruttore
     */
    public MetaInfo() {
        super();
    }

    /**
     * Aggiunge il descrittore d alle metainfo
     * @param d descittore da aggiungere
     * @return sito operazione
     */
    @Override
    public synchronized boolean add(Descrittore d) {
        return super.add(d);
    }

    /**
     * Restituisce i descrittori delle metainfo
     * @return super.iterator()
     */
    @Override
    public Iterator<Descrittore> iterator() {
        return super.iterator();
    }

    /**
     * verifica la presenza nelle metainfo del descrittore
     * passato come parametro
     * @param d descrittore da trovare
     * @return descrittore trovato ; altrimenti null
     */
    public synchronized Descrittore presenza(Descrittore d) {
        Iterator<Descrittore> i = super.iterator();
        Descrittore temp = null;
        while (i.hasNext()) {
            temp = i.next();
            if (temp.getDimensione() == d.getDimensione() && temp.getName().compareTo(d.getName()) == 0) {
                return temp;
            }
        }
        return null;
    }

    /**
     * Cerca descrittori nelle metainfo con nome
     * uguale a quello passato come parametro
     * @param nomefile nome file da cercare
     * @return lista di descrittori trovati ; altrimenti null
     */
    public synchronized ArrayList<Descrittore> cerca(String nomefile) {

        ArrayList<Descrittore> array = new ArrayList<Descrittore>();
        Iterator<Descrittore> i = super.iterator();
        Descrittore d = null;

        String regex = null;
        String testo = null;
        String[] cerca = nomefile.split(" ");
        boolean b = false;
        int nummatch = 0;

        while (i.hasNext()) {
            d = i.next();
            testo = d.getName().toLowerCase();
            nummatch = 0;

            for (int j = 0; j < cerca.length; j++) {
                regex = "";
                regex += ".*" + cerca[j].toLowerCase() + ".*";
                b = Pattern.matches(regex, testo);
                if (!b) {
                    break; //  non matcha
                }
                nummatch++;
            }
            if (nummatch == cerca.length && !(d.getNumLeechers() == 0 && d.getNumSeeders() == 0)) {
                array.add(d);
            }
            /* se ho trovato 15 descrittori esco */
            if (array.size() == NUMMAXDESCR) {
                break;
            }
        }
        // stampa di prova
        for (int j = 0; j < array.size(); j++) {
            System.out.println(array.get(j).getName());
        }
        return array;
    }
}
