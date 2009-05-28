package server;

import condivisi.Descrittore;
import java.util.regex.Pattern;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

/**
 * 
 * @author Bandettini 
 */
public class MetaInfo extends HashSet<Descrittore> {

    /* Costanti */
    private static final int NUMMAXDESCR = 15;
    public static final long serialVersionUID = 15;

    public MetaInfo() {
        super();
    }

    @Override
    public synchronized boolean add(Descrittore d) {
        return super.add(d);
    }

    @Override
    public Iterator<Descrittore> iterator() {
        return super.iterator();
    }

    public synchronized Descrittore presenza(Descrittore d) {
        Iterator<Descrittore> i = super.iterator();
        Descrittore temp = null;
        while (i.hasNext()) {
            temp = i.next();
            // controllo hash da rivedere
            if (/*temp.getHash().equals(d.getHash()) && */temp.getDimensione() == d.getDimensione() && temp.getName().compareTo(d.getName()) == 0) {
                return temp;
            }
        }
        return null;
    }

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
            if (nummatch == cerca.length) {
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
