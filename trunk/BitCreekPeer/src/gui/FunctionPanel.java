package gui;

import java.awt.Color;
import java.awt.Graphics;
import java.util.ArrayList;
import javax.swing.JPanel;

class FunctionPanel extends JPanel {

    public static final long serialVersionUID = 100;
    private final int ALTEZZA = 130;
    private final int INIZIOX = 30;
    private final int INIZIOY = 25;
    private final int SPAZIO = 20;

    /* Variabili d' istanza */
    private int larghezza;
    /* Variabili di classe */
    private static ArrayList<Integer> array = new ArrayList<Integer>();

    public FunctionPanel() {
        super();
        this.larghezza = 250;
    }

    public static void settaConnessioni(int c) {
        if (c < 0) {
            c = 0;
        }
        if (c > 100) {
            c = 100;
        }
        array.add(0, new Integer(c));
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);

        larghezza = super.getWidth();
        setBackground(super.getBackground());
        /* disegno il grafico comprensivo di linee orizzontali e legenda */
        g.setColor(Color.black);
        g.drawRect(INIZIOX, INIZIOY, larghezza - INIZIOX - 15, ALTEZZA - INIZIOY - 5);
        g.setColor(Color.black);
        int i = 0;
        for (; i <= (ALTEZZA - INIZIOY - 5) / SPAZIO; i++) {
            g.drawLine(INIZIOX, INIZIOY + i * SPAZIO, larghezza - 15, INIZIOY + i * SPAZIO);
        }
        int n = 0, j = 0;
        for (--i; i >= 0; i--) {
            n = (i * SPAZIO);
            g.drawString("" + n, INIZIOX - 25, INIZIOY + j++ * SPAZIO + 5);
        }
        g.drawString("Tempo", INIZIOX + (larghezza / 2) - 40, ALTEZZA + 16);
        /* disegno la funzione */
        disegnaconnessioni(g);
    }

    /**
     * Disegna la funzione che rappresenta il numero di connessioni nel tempo
     * @param g grafico su cui disegnare
     */
    private void disegnaconnessioni(Graphics g) {
        int connessioni = 0;
        g.setColor(Color.blue);
        if (array.size() > 0) {
            connessioni = array.get(0).intValue();
            g.drawLine(INIZIOX, ALTEZZA - 5 - connessioni, INIZIOX, ALTEZZA - 5 - connessioni);
            int ultimox = INIZIOX;
            int ultimoy = ALTEZZA - 5 - connessioni;
            int nuovox = ultimox + 10;
            for (int i = 1; i < array.size(); i++) {
                connessioni = array.get(i).intValue();
                g.drawLine(ultimox, ultimoy, nuovox, ALTEZZA - 5 - connessioni);
                ultimox = nuovox;
                ultimoy = ALTEZZA - 5 - connessioni;
                nuovox += 10;
                if (nuovox > larghezza - 13) {
                    break;
                }
            }
        }
    }
}

