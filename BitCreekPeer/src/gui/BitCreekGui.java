package gui;

import condivisi.*;
import java.awt.event.ActionEvent;
import peer.*;

import java.awt.BorderLayout;
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

/**
 * Gui del protocollo client BitCreek
 * @author Bandettini Alberto
 * @author Lottarini Andrea
 * @version BitCreePeer 1.0
 */
public class BitCreekGui extends javax.swing.JFrame {

    /** 
     * Costruttore dell'interfaccia
     */
    public BitCreekGui() {
        /* per emulare l'aspetto di vari sistemi operativi */
        try {
            javax.swing.UIManager.setLookAndFeel(javax.swing.UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            PrintInformation("Impossibile settare look and feel", ERRORE);
            System.exit(ERRORE);
        }
        /* inizializzazione componenti grafici*/
        initComponents();
        /* inizializzazione parte client del protocollo (logica del programma) */
        initProtocol();
        /* inizializzazione del timer per aggiornamento tabelle */
        javax.swing.Timer timer = new javax.swing.Timer(AGGIORNAMENTO, new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                tabellelistenerActionPerformed(evt);
            }
        });
        /* inizializzazione timer per grafico connessioni */
        javax.swing.Timer timerg = new javax.swing.Timer(AGGIORNAMENTOGRAFICO, new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent evt) {
                graficolistenerActionPerformed(evt);
            }
        });
        timer.start();
        timerg.start();
    }

    /**
     * Event-loop della GUI
     * @param args non  utilizzato
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                /* creo il frame */
                BitCreekGui frame = new BitCreekGui();
                /* setto l'icona al frame*/
                frame.setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(this.getClass().getResource("/icone/icon-16x16-computers.png")));
                /* visualizzo il frame */
                frame.setVisible(true);
            }
        });
    }

    /**
     * Stampa un messaggio su un dialogo
     * @param message messaggio da visualizzare
     * @param t tipo del dialogo
     */
    public void PrintInformation(String message, char t) {
        final String msg = message;
        final char tipo = t;
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                if (tipo == ERRORE) {
                    javax.swing.JOptionPane.showMessageDialog(getRootPane(), msg, "Errore", JOptionPane.ERROR_MESSAGE);
                } else {
                    String titolo = null;
                    javax.swing.ImageIcon icona = null;
                    if (tipo == INFORMATION) {
                        titolo = "Attenzione";
                        javax.swing.JOptionPane.showMessageDialog(getRootPane(), msg, titolo, JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        /* carico l'icona per il pannello */
                        icona = new javax.swing.ImageIcon(getClass().getResource("/icone/icon-16x16-computers.png"));
                        titolo = "Aiuto";
                        javax.swing.JOptionPane.showMessageDialog(getRootPane(), msg, titolo, JOptionPane.INFORMATION_MESSAGE, icona);
                    }

                }
            }
        });
    }

    /**
     * Esegue l'aggiornamento grafico in seguito alla connetti
     */
    public void connettiDone() {
        /* aggiorno interfaccia grafica */
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                doubleclick();
                /* se non ho effettuato il test NAT mioip non è inizializzato*/
                InetAddress mioip = peer.getMioIp();
                if (mioip != null) {
                    labelmioip.setText("Mio IP : " + mioip.getHostAddress());
                }
                InetAddress ipserver = peer.getIpServer();
                if (ipserver != null) {
                    labelipserver.setText("IP Server : " + ipserver.getHostAddress());
                    labelconnessione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-smile.png")));
                    bottonetest.setEnabled(true);
                    menutest.setEnabled(true);
                    labelnatfaccina.setEnabled(true);
                    if (peer.getBloccato()) {
                        labelnatfaccina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-sad.png")));
                    } else {
                        labelnatfaccina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-smile.png")));
                    }
                    tabellamieicreek.clearSelection();
                    tabellapubblicati.clearSelection();
                    tabellaricerca.clearSelection();
                    bottoneconnetti.setEnabled(false);
                    connetti.setEnabled(false);
                    bottonecrea.setEnabled(true);
                    crea.setEnabled(true);
                    bottoneapri.setEnabled(true);
                    apri.setEnabled(true);
                    elimina.setEnabled(true);
                    eliminaselezionato.setEnabled(false);
                    apri.setEnabled(true);
                    avvia.setEnabled(true);
                    avviaselezionato.setEnabled(false);
                    bottonedisconnetti.setEnabled(true);
                    disconnetti.setEnabled(true);
                    bottonecerca.setEnabled(true);
                    bottonefortuna.setEnabled(true);
                    cerca.setEnabled(true);
                    bottonesettaporta.setEnabled(false);
                    menusettaporta.setEnabled(false);
                    /* per ogni elemento della tabella dei download tento di riavviarlo */
                    tabellamieicreek.selectAll();
                    avvia();
                }
            }
        });
    }

    /**
     * Esegue gli aggiornamenti in caso di test firewall - NAT
     * positivo
     * @param bloc dice se il peer è bloccato da NAT o firewall
     * @param pr portarichieste del peer
     */
    public void testDone(boolean bloc, int pr) {
        final boolean bloccato = bloc;
        final int portarichieste = pr;
        /* aggiorna interfaccia grafica */
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                doubleclick();
                if (portarichieste != NULL) {
                    labelnatfaccina.setEnabled(true);
                    labelmioip.setText("Mio IP : " + peer.getMioIp().getHostAddress());
                    labelporta.setText("Porta Utilizzata : " + portarichieste);
                }
                if (bloccato) {
                    /* controllo se ci sono errori di connessione */
                    if (portarichieste != NULL) {
                        labelnatfaccina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-sad.png")));
                        bottonetest.setEnabled(true);
                        menutest.setEnabled(true);
                        PrintInformation("La porta su cui sei in ascolto e' dietro nat o firewall.\n" +
                                "Questo compromette il corretto funzionamento dell'applicazione.\n" +
                                "Si consiglia di aprire la porta " + portarichieste + " sul proprio router" +
                                " e/o firewall NON chiudendo l'applicazione oppure cambiare la porta.\n" +
                                "Appena fatto si provi ad eseguire il test NAT/Firewall sotto il menu Aiuto.", INFORMATION);
                    }
                } else {
                    labelnatfaccina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-smile.png")));
                    bottonetest.setEnabled(false);
                    menutest.setEnabled(false);
                }
            }
        });
    }

    /** 
     * Metodo che inizializza i componenti grafici
     * @see Questo codice è autogenerato dal tool per le interfacce grafiche di Netbeans
     */

    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        pannellomieicreek = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        modellomieicreek = new ModelloTabellaMieiCreek();
        tabellamieicreek = new javax.swing.JTable(modellomieicreek);
        barrastrumenti = new javax.swing.JToolBar();
        bottoneapri = new javax.swing.JButton();
        bottonecrea = new javax.swing.JButton();
        bottonesalva = new javax.swing.JButton();
        jSeparator1 = new javax.swing.JToolBar.Separator();
        bottoneprecedente = new javax.swing.JButton();
        bottonesuccessivo = new javax.swing.JButton();
        bottoneelimina = new javax.swing.JButton();
        jSeparator2 = new javax.swing.JToolBar.Separator();
        bottoneconnetti = new javax.swing.JButton();
        bottonedisconnetti = new javax.swing.JButton();
        bottonetest = new javax.swing.JButton();
        jDesktopPane1 = new javax.swing.JDesktopPane();
        bottonesettaporta = new javax.swing.JButton();
        jSeparator3 = new javax.swing.JToolBar.Separator();
        bottoneexit = new javax.swing.JButton();
        pannellopubblicati = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        modellopubblicati = new ModelloTabellaPubblicati();
        tabellapubblicati = new javax.swing.JTable(modellopubblicati);
        areacerca = new javax.swing.JTextField();
        bottonecerca = new javax.swing.JButton();
        labelmioip = new javax.swing.JLabel();
        labelipserver = new javax.swing.JLabel();
        jLabel1 = new javax.swing.JLabel();
        labelconnessione = new javax.swing.JLabel();
        jPanel1 = new javax.swing.JPanel();
        jScrollPane3 = new javax.swing.JScrollPane();
        modellocerca = new ModelloTabellaCerca();
        tabellaricerca = new javax.swing.JTable(modellocerca);
        labelnat = new javax.swing.JLabel();
        labelnatfaccina = new javax.swing.JLabel();
        labelporta = new javax.swing.JLabel();
        bottonefortuna = new javax.swing.JButton();
        grafico = new FunctionPanel();
        tabellacercaclicked = false; // per emulare 2-click
        jMenuBar1 = new javax.swing.JMenuBar();
        file = new javax.swing.JMenu();
        crea = new javax.swing.JMenuItem();
        apri = new javax.swing.JMenuItem();
        menusalva = new javax.swing.JMenuItem();
        cerca = new javax.swing.JMenuItem();
        connetti = new javax.swing.JMenuItem();
        disconnetti = new javax.swing.JMenuItem();
        esci = new javax.swing.JMenuItem();
        trasferimenti = new javax.swing.JMenu();
        avvia = new javax.swing.JMenu();
        avviaselezionato = new javax.swing.JMenuItem();
        avviatutticerca = new javax.swing.JMenuItem();
        elimina = new javax.swing.JMenu();
        eliminaselezionato = new javax.swing.JMenuItem();
        bottoneeliminatutticreek = new javax.swing.JMenuItem();
        bottoneeliminapubblicati = new javax.swing.JMenuItem();
        eliminacercati = new javax.swing.JMenuItem();
        visualizza = new javax.swing.JMenu();
        barra = new javax.swing.JCheckBoxMenuItem();
        menuprecedente = new javax.swing.JMenuItem();
        menusuccessivo = new javax.swing.JMenuItem();
        aiuto = new javax.swing.JMenu();
        menutest = new javax.swing.JMenuItem();
        menusettaporta = new javax.swing.JMenuItem();
        menudownload = new javax.swing.JMenuItem();
        info = new javax.swing.JMenuItem();

        setDefaultCloseOperation(javax.swing.WindowConstants.DO_NOTHING_ON_CLOSE);
        setTitle("BitCreekPeer");
        setCursor(new java.awt.Cursor(java.awt.Cursor.DEFAULT_CURSOR));
        setForeground(java.awt.Color.white);
        addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                formMouseClicked(evt);
            }
        });
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
        });

        pannellomieicreek.setBorder(javax.swing.BorderFactory.createTitledBorder("I miei creek"));

        tabellamieicreek.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabellamieicreekMouseClicked(evt);
            }
        });
        jScrollPane1.setViewportView(tabellamieicreek);

        javax.swing.GroupLayout pannellomieicreekLayout = new javax.swing.GroupLayout(pannellomieicreek);
        pannellomieicreek.setLayout(pannellomieicreekLayout);
        pannellomieicreekLayout.setHorizontalGroup(
            pannellomieicreekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pannellomieicreekLayout.createSequentialGroup()
                .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
                .addContainerGap())
        );
        pannellomieicreekLayout.setVerticalGroup(
            pannellomieicreekLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane1, javax.swing.GroupLayout.DEFAULT_SIZE, 131, Short.MAX_VALUE)
        );

        barrastrumenti.setBorder(new javax.swing.border.SoftBevelBorder(javax.swing.border.BevelBorder.RAISED));
        barrastrumenti.setFloatable(false);
        barrastrumenti.setRollover(true);
        barrastrumenti.setPreferredSize(new java.awt.Dimension(100, 32));

        bottoneapri.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/document-open.png"))); // NOI18N
        bottoneapri.setToolTipText("Apre un .creek");
        bottoneapri.setEnabled(false);
        bottoneapri.setFocusable(false);
        bottoneapri.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottoneapri.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottoneapri.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottoneapriMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottoneapri);

        bottonecrea.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/stock_script.png"))); // NOI18N
        bottonecrea.setToolTipText("Crea un .creek");
        bottonecrea.setEnabled(false);
        bottonecrea.setFocusable(false);
        bottonecrea.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottonecrea.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottonecrea.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonecreaMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottonecrea);

        bottonesalva.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/document-save.png"))); // NOI18N
        bottonesalva.setToolTipText("Salva il .creek");
        bottonesalva.setEnabled(false);
        bottonesalva.setFocusable(false);
        bottonesalva.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottonesalva.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottonesalva.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonesalvaMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottonesalva);
        barrastrumenti.add(jSeparator1);

        bottoneprecedente.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/gtk-go-back-ltr.png"))); // NOI18N
        bottoneprecedente.setToolTipText("Precedente");
        bottoneprecedente.setEnabled(false);
        bottoneprecedente.setFocusable(false);
        bottoneprecedente.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottoneprecedente.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottoneprecedente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottoneprecedenteActionPerformed(evt);
            }
        });
        barrastrumenti.add(bottoneprecedente);

        bottonesuccessivo.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/gtk-go-forward-ltr.png"))); // NOI18N
        bottonesuccessivo.setToolTipText("Successivo");
        bottonesuccessivo.setEnabled(false);
        bottonesuccessivo.setFocusable(false);
        bottonesuccessivo.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottonesuccessivo.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottonesuccessivo.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonesuccessivoMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottonesuccessivo);

        bottoneelimina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/edit-delete.png"))); // NOI18N
        bottoneelimina.setToolTipText("Elimina");
        bottoneelimina.setEnabled(false);
        bottoneelimina.setFocusable(false);
        bottoneelimina.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottoneelimina.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottoneelimina.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottoneeliminaMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottoneelimina);
        barrastrumenti.add(jSeparator2);

        bottoneconnetti.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/network-wired.png"))); // NOI18N
        bottoneconnetti.setToolTipText("Connette al server");
        bottoneconnetti.setFocusable(false);
        bottoneconnetti.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottoneconnetti.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottoneconnetti.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottoneconnettiMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottoneconnetti);

        bottonedisconnetti.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/gtk-cancel.png"))); // NOI18N
        bottonedisconnetti.setToolTipText("Disconnetti");
        bottonedisconnetti.setEnabled(false);
        bottonedisconnetti.setFocusable(false);
        bottonedisconnetti.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottonedisconnetti.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottonedisconnetti.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonedisconnettiMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottonedisconnetti);

        bottonetest.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/gnome-session-reboot.png"))); // NOI18N
        bottonetest.setToolTipText("Test NAT-Firewall");
        bottonetest.setEnabled(false);
        bottonetest.setFocusable(false);
        bottonetest.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottonetest.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottonetest.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonetestMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottonetest);
        barrastrumenti.add(jDesktopPane1);

        bottonesettaporta.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/system-run.png"))); // NOI18N
        bottonesettaporta.setToolTipText("Setta la porta di acsolto");
        bottonesettaporta.setFocusable(false);
        bottonesettaporta.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottonesettaporta.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottonesettaporta.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonesettaportaMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottonesettaporta);
        barrastrumenti.add(jSeparator3);

        bottoneexit.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/application-exit.png"))); // NOI18N
        bottoneexit.setToolTipText("Esci");
        bottoneexit.setFocusable(false);
        bottoneexit.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bottoneexit.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bottoneexit.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottoneexitMouseClicked(evt);
            }
        });
        barrastrumenti.add(bottoneexit);

        pannellopubblicati.setBorder(javax.swing.BorderFactory.createTitledBorder("Creek pubblicati"));

        tabellapubblicati.setGridColor(new java.awt.Color(252, 245, 245));
        tabellapubblicati.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabellapubblicatiMouseClicked(evt);
            }
        });
        jScrollPane2.setViewportView(tabellapubblicati);

        javax.swing.GroupLayout pannellopubblicatiLayout = new javax.swing.GroupLayout(pannellopubblicati);
        pannellopubblicati.setLayout(pannellopubblicatiLayout);
        pannellopubblicatiLayout.setHorizontalGroup(
            pannellopubblicatiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(pannellopubblicatiLayout.createSequentialGroup()
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 790, Short.MAX_VALUE)
                .addContainerGap())
        );
        pannellopubblicatiLayout.setVerticalGroup(
            pannellopubblicatiLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 159, Short.MAX_VALUE)
        );

        bottonecerca.setText("Cerca");
        bottonecerca.setEnabled(false);
        bottonecerca.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonecercaMouseClicked(evt);
            }
        });

        labelmioip.setText("Mio IP : ");

        labelipserver.setText("IP Server : ");

        jLabel1.setText("Stato Connessione :");

        labelconnessione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-sad.png"))); // NOI18N

        jPanel1.setBorder(javax.swing.BorderFactory.createTitledBorder("Risultati Ricerca"));

        tabellaricerca.setGridColor(new java.awt.Color(252, 245, 245));
        tabellaricerca.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                tabellaricercaMouseClicked(evt);
            }
        });
        jScrollPane3.setViewportView(tabellaricerca);

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(jPanel1Layout.createSequentialGroup()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 525, Short.MAX_VALUE)
                .addContainerGap())
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(jScrollPane3, javax.swing.GroupLayout.PREFERRED_SIZE, 132, javax.swing.GroupLayout.PREFERRED_SIZE)
        );

        labelnat.setText("Firewall/NAT :");

        labelnatfaccina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-sad.png"))); // NOI18N
        labelnatfaccina.setEnabled(false);

        labelporta.setText("Porta Utilizzata :       ");

        bottonefortuna.setText("Mi sento fortunato");
        bottonefortuna.setEnabled(false);
        bottonefortuna.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                bottonefortunaMouseClicked(evt);
            }
        });

        grafico.setBorder(javax.swing.BorderFactory.createTitledBorder("Numero Connessioni"));
        grafico.setPreferredSize(new java.awt.Dimension(250, 150));

        javax.swing.GroupLayout graficoLayout = new javax.swing.GroupLayout(grafico);
        grafico.setLayout(graficoLayout);
        graficoLayout.setHorizontalGroup(
            graficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 241, Short.MAX_VALUE)
        );
        graficoLayout.setVerticalGroup(
            graficoLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 132, Short.MAX_VALUE)
        );

        jMenuBar1.setFont(new java.awt.Font("Bitstream Vera Serif", 2, 13));

        file.setText("File");

        crea.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_N, java.awt.event.InputEvent.CTRL_MASK));
        crea.setText("Crea Creek...");
        crea.setToolTipText("Crea un .creek");
        crea.setEnabled(false);
        crea.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                creaActionPerformed(evt);
            }
        });
        file.add(crea);

        apri.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_O, java.awt.event.InputEvent.CTRL_MASK));
        apri.setText("Apri Creek...");
        apri.setToolTipText("Apre un .creek");
        apri.setEnabled(false);
        apri.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                apriActionPerformed(evt);
            }
        });
        file.add(apri);

        menusalva.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_B, java.awt.event.InputEvent.CTRL_MASK));
        menusalva.setText("Salva Creek");
        menusalva.setEnabled(false);
        menusalva.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menusalvaActionPerformed(evt);
            }
        });
        file.add(menusalva);

        cerca.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Q, java.awt.event.InputEvent.CTRL_MASK));
        cerca.setText("Cerca");
        cerca.setEnabled(false);
        cerca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                cercaActionPerformed(evt);
            }
        });
        file.add(cerca);

        connetti.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_U, java.awt.event.InputEvent.CTRL_MASK));
        connetti.setText("Connetti...");
        connetti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                connettiActionPerformed(evt);
            }
        });
        file.add(connetti);

        disconnetti.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_Y, java.awt.event.InputEvent.CTRL_MASK));
        disconnetti.setText("Disconnetti");
        disconnetti.setEnabled(false);
        disconnetti.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                disconnettiActionPerformed(evt);
            }
        });
        file.add(disconnetti);

        esci.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F4, java.awt.event.InputEvent.ALT_MASK));
        esci.setText("Esci");
        esci.setToolTipText("Chiude BitCreekPeer");
        esci.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                esciActionPerformed(evt);
            }
        });
        file.add(esci);

        jMenuBar1.add(file);

        trasferimenti.setText("Trasferimenti");

        avvia.setText("Avvia");
        avvia.setEnabled(false);

        avviaselezionato.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_A, java.awt.event.InputEvent.CTRL_MASK));
        avviaselezionato.setText("Creek selezionato");
        avviaselezionato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avviaselezionatoActionPerformed(evt);
            }
        });
        avvia.add(avviaselezionato);

        avviatutticerca.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_J, java.awt.event.InputEvent.CTRL_MASK));
        avviatutticerca.setText("Tutti i file cercati");
        avviatutticerca.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                avviatutticercaActionPerformed(evt);
            }
        });
        avvia.add(avviatutticerca);

        trasferimenti.add(avvia);

        elimina.setText("Elimina");
        elimina.setEnabled(false);

        eliminaselezionato.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_E, java.awt.event.InputEvent.CTRL_MASK));
        eliminaselezionato.setText("Creek selezionato");
        eliminaselezionato.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminaselezionatoActionPerformed(evt);
            }
        });
        elimina.add(eliminaselezionato);

        bottoneeliminatutticreek.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_R, java.awt.event.InputEvent.CTRL_MASK));
        bottoneeliminatutticreek.setText("Tutti i miei creek");
        bottoneeliminatutticreek.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottoneeliminatutticreekActionPerformed(evt);
            }
        });
        elimina.add(bottoneeliminatutticreek);

        bottoneeliminapubblicati.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_T, java.awt.event.InputEvent.CTRL_MASK));
        bottoneeliminapubblicati.setText("Tutti i file pubblicati");
        bottoneeliminapubblicati.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bottoneeliminapubblicatiActionPerformed(evt);
            }
        });
        elimina.add(bottoneeliminapubblicati);

        eliminacercati.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_H, java.awt.event.InputEvent.CTRL_MASK));
        eliminacercati.setText("Tutti i file cercati");
        eliminacercati.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                eliminacercatiActionPerformed(evt);
            }
        });
        elimina.add(eliminacercati);

        trasferimenti.add(elimina);

        jMenuBar1.add(trasferimenti);

        visualizza.setText("Visualizza");

        barra.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_W, java.awt.event.InputEvent.CTRL_MASK));
        barra.setSelected(true);
        barra.setText("Barra degli strumenti");
        barra.setToolTipText("Abilita/Disabilita la barra degli strumenti");
        barra.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                barraActionPerformed(evt);
            }
        });
        visualizza.add(barra);

        menuprecedente.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_P, java.awt.event.InputEvent.CTRL_MASK));
        menuprecedente.setText("Precedente");
        menuprecedente.setEnabled(false);
        menuprecedente.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menuprecedenteActionPerformed(evt);
            }
        });
        visualizza.add(menuprecedente);

        menusuccessivo.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_L, java.awt.event.InputEvent.CTRL_MASK));
        menusuccessivo.setText("Successivo");
        menusuccessivo.setEnabled(false);
        menusuccessivo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menusuccessivoActionPerformed(evt);
            }
        });
        visualizza.add(menusuccessivo);

        jMenuBar1.add(visualizza);

        aiuto.setText("Aiuto");

        menutest.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_M, java.awt.event.InputEvent.CTRL_MASK));
        menutest.setText("Test NAT/Firewall");
        menutest.setToolTipText("Effettua il test sulla porta di ascolto");
        menutest.setEnabled(false);
        menutest.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menutestActionPerformed(evt);
            }
        });
        aiuto.add(menutest);

        menusettaporta.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_K, java.awt.event.InputEvent.CTRL_MASK));
        menusettaporta.setText("Setta Porta");
        menusettaporta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menusettaportaActionPerformed(evt);
            }
        });
        aiuto.add(menusettaporta);

        menudownload.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_X, java.awt.event.InputEvent.CTRL_MASK));
        menudownload.setText("Download");
        menudownload.setToolTipText("Mostra dove sono i file");
        menudownload.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                menudownloadActionPerformed(evt);
            }
        });
        aiuto.add(menudownload);

        info.setAccelerator(javax.swing.KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_I, java.awt.event.InputEvent.CTRL_MASK));
        info.setText("Info");
        info.setToolTipText("Visualizza le informazioni");
        info.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                infoActionPerformed(evt);
            }
        });
        aiuto.add(info);

        jMenuBar1.add(aiuto);

        setJMenuBar(jMenuBar1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(pannellopubblicati, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addComponent(pannellomieicreek, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addComponent(barrastrumenti, javax.swing.GroupLayout.PREFERRED_SIZE, 451, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, 128, Short.MAX_VALUE)
                        .addComponent(areacerca, javax.swing.GroupLayout.PREFERRED_SIZE, 156, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(bottonecerca, javax.swing.GroupLayout.PREFERRED_SIZE, 73, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelmioip, javax.swing.GroupLayout.PREFERRED_SIZE, 163, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(24, 24, 24)
                        .addComponent(labelipserver, javax.swing.GroupLayout.PREFERRED_SIZE, 186, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addComponent(jLabel1)
                        .addGap(18, 18, 18)
                        .addComponent(labelconnessione)
                        .addGap(18, 18, 18)
                        .addComponent(labelnat)
                        .addGap(18, 18, 18)
                        .addComponent(labelnatfaccina))
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                        .addComponent(grafico, javax.swing.GroupLayout.DEFAULT_SIZE, 253, Short.MAX_VALUE))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(labelporta, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(bottonefortuna)))
                .addContainerGap())
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                        .addComponent(bottonecerca)
                        .addComponent(areacerca, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addComponent(barrastrumenti, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                        .addComponent(labelconnessione)
                        .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                            .addGap(22, 22, 22)
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                                .addComponent(labelmioip)
                                .addComponent(jLabel1)
                                .addComponent(labelipserver))))
                    .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                        .addComponent(labelnat, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(labelnatfaccina, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pannellomieicreek, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(pannellopubblicati, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING, false)
                    .addComponent(grafico, javax.swing.GroupLayout.DEFAULT_SIZE, 161, Short.MAX_VALUE)
                    .addComponent(jPanel1, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(18, 18, 18)
                        .addComponent(labelporta))
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                        .addGap(23, 23, 23)
                        .addComponent(bottonefortuna, javax.swing.GroupLayout.PREFERRED_SIZE, 41, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(28, 28, 28))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    /**
     * Aggiorna il grafico presente nell'interfaccia
     * @param evt evento non utilizzato
     */
    private void graficolistenerActionPerformed(ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                FunctionPanel.settaConnessioni(peer.getConnessioni());
                grafico.repaint();
            }
        });
    }

    /** Esegue l' aggiornamento delle tabelle dell' interfaccia grafica
     * @param evt evento non utilizzato
     */
    private void tabellelistenerActionPerformed(ActionEvent evt) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {

                boolean problema = false;

                ArrayList<Creek> array = null;

                //QUI C"ERA UN TRY
                array = peer.getDescr();

                if (!problema) {

                    /* rimuovo vecchi modelli */
                    modellomieicreek.removeTutti();

                    //RigaTabellaMieiCreek riga = null;
                    RigaTabellaPubblicati rigap = null;

                    for (Creek c : array) {
                        if (c.getStato()) {

                            /* file in download */
                            RigaTabellaMieiCreek r = new RigaTabellaMieiCreek(c.getName(), c.getDimensione(), c.getPercentuale(), c.getPeer());
                            try {
                                modellomieicreek.addRiga(r);
                            } catch (ErrorException e) {
                                System.err.println("Listener : modellomieicreek.addRiga(r), " + e.getMessage());
                            }
                        } else {

                            /* file in upload */

                            if ((rigap = modellopubblicati.presenza(c.getName())) != null) {

                                /* modifico riga */
                                if (c.getPeer() > 0) {
                                    rigap.setSituazione("Attivo");
                                } else {
                                    rigap.setSituazione("Non Attivo");
                                }
                                rigap.setPeer(c.getPeer());
                                rigap.setPeerCerca(c.getPeerCerca());
                                rigap.setIdentita(c.getIdentita());

                            } else {

                                /* creek non presente : lo aggiungo */

                                RigaTabellaPubblicati r = new RigaTabellaPubblicati(c.getName(), c.getDimensione(), c.getPubblicato());
                                try {
                                    modellopubblicati.addRiga(r);
                                } catch (ErrorException e) {
                                    System.err.println("Listener : modellopubblicati.addRiga(r), " + e.getMessage());
                                }
                            }
                        }
                    }
                    if (tabellamieicreek.getSelectedRowCount() == 0) {
                        modellomieicreek.fireTableDataChanged();
                    }
                    if (tabellapubblicati.getSelectedRowCount() == 0) {
                        modellopubblicati.fireTableDataChanged();
                    }
                }
            }
        });
    }

    /**
     * Chiude l'applicazione chiamando l'handler pulizia
     * @param evt evento non utilizzato
     */
private void esciActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_esciActionPerformed
    pulizia();
}//GEN-LAST:event_esciActionPerformed

    /**
     * Chiude l'applicazione chiamando l'handler pulizia
     * @param evt evento non utilizzato
     */
private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
    pulizia();
}//GEN-LAST:event_formWindowClosing

    /**
     * Visualizza o non visualizza la barra degli strumenti
     * @param evt evento non utilizzato
     */
private void barraActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_barraActionPerformed
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            doubleclick();
            if (barra.isSelected()) {
                barrastrumenti.setVisible(true);
            } else {
                barrastrumenti.setVisible(false);
            }
        }
    });
}//GEN-LAST:event_barraActionPerformed

    /**
     * Crea, se possibile, un .creek chiamando l'handler crea
     * @param evt evento non utilizzato
     */
private void creaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_creaActionPerformed
    if (crea.isEnabled()) {
        crea();
    }
}//GEN-LAST:event_creaActionPerformed

    /**
     * Crea,se possibile,un .creek chiamando l'handler crea
     * @param evt evento non utilizzato
     */
private void bottonecreaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonecreaMouseClicked
    if (bottonecrea.isEnabled()) {
        crea();
    }
}//GEN-LAST:event_bottonecreaMouseClicked

    /**
     * Apre,se possibile, un .creek chiamando l'handler apri
     * @param evt evento non utilizzato
     */
private void apriActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_apriActionPerformed
    if (apri.isEnabled()) {
        apri();
    }
}//GEN-LAST:event_apriActionPerformed

    /**
     * Apre,se possibile, un .creek chiamando l'handler apri
     * @param evt evento non utilizzato
     */
private void bottoneapriMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottoneapriMouseClicked
    if (bottoneapri.isEnabled()) {
        apri();
    }
}//GEN-LAST:event_bottoneapriMouseClicked

    /**
     * Apre le informazioni
     * @param evt evento non utilizzato
     */
private void infoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_infoActionPerformed
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            doubleclick();
            /* visualizzo le info */
            PrintInformation("A cura di Bandettini Alberto e " +
                    "Andrea Lottarini\nalberto.bandettini@gmail.com\nandre.lotta86@gmail.com\n" +
                    "In particolare : per problemi e lamentele contattare Lottarini,\n" +
                    "per elogi vari ed eventuali contattare Bandettini", AIUTO);
        }
    });
}//GEN-LAST:event_infoActionPerformed

    /**
     * Elimina le informazioni selezionate
     * @param evt evento non utilizzato
     */
private void bottoneeliminaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottoneeliminaMouseClicked
    if (bottoneelimina.isEnabled()) {
        elimina();
    }
}//GEN-LAST:event_bottoneeliminaMouseClicked
    /**
     * Elimina l' informazione selezionata
     * @param evt evento non utilizzato
     */
private void eliminaselezionatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminaselezionatoActionPerformed
    if (elimina.isEnabled()) {
        elimina();
    }
}//GEN-LAST:event_eliminaselezionatoActionPerformed

    /**
     * Seleziona la riga precedente chiamando l'handler precedente
     * @param evt evento non utilizzato
     */
private void bottoneprecedenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottoneprecedenteActionPerformed
    if (bottoneprecedente.isEnabled()) {
        precedente();
    }
}//GEN-LAST:event_bottoneprecedenteActionPerformed

    /**
     * Elimina tutti i creek nella prima tabella
     * @param evt evento non utilizzato
     */
private void bottoneeliminatutticreekActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottoneeliminatutticreekActionPerformed
    if (elimina.isEnabled()) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                tabellamieicreek.selectAll();
                elimina();
            }
        });
    }
}//GEN-LAST:event_bottoneeliminatutticreekActionPerformed
    /**
     * Elimina tutti i creek nella seconda tabella
     * @param evt evento non utilizzato
     */
private void bottoneeliminapubblicatiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bottoneeliminapubblicatiActionPerformed
    if (elimina.isEnabled()) {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                tabellapubblicati.selectAll();
                elimina();
            }
        });
    }
}//GEN-LAST:event_bottoneeliminapubblicatiActionPerformed

    /**
     * Seleziona la riga precedente chiamando l'handler precedente
     * @param evt evento non utilizzato
     */
private void menuprecedenteActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menuprecedenteActionPerformed
    if (menuprecedente.isEnabled()) {
        precedente();
    }
}//GEN-LAST:event_menuprecedenteActionPerformed

    /**
     * Seleziona la riga successiva chiamando l'handler successivo
     * @param evt evento non utilizzato
     */
private void menusuccessivoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menusuccessivoActionPerformed
    if (menusuccessivo.isEnabled()) {
        successivo();
    }
}//GEN-LAST:event_menusuccessivoActionPerformed

    /**
     * Seleziona la riga successiva chiamando l'handler successivo
     * @param evt evento non utilizzato
     */
private void bottonesuccessivoMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonesuccessivoMouseClicked
    if (bottonesuccessivo.isEnabled()) {
        successivo();
    }
}//GEN-LAST:event_bottonesuccessivoMouseClicked

    /**
     * Tenta di connettersi chiamando l' handler connetti
     * @param evt evento non utilizzato
     */
private void bottoneconnettiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottoneconnettiMouseClicked
    if (bottoneconnetti.isEnabled()) {
        connetti();
    }
}//GEN-LAST:event_bottoneconnettiMouseClicked

    /**
     * Tenta di connettersi chiamando l' handler connetti
     * @param evt evento non utilizzato
     */
private void connettiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_connettiActionPerformed
    if (connetti.isEnabled()) {
        connetti();
    }
}//GEN-LAST:event_connettiActionPerformed

    /**
     * Pulisce lo stato; chiamato quando si clicca sulla finestra
     * ma non su un particolare bottone o menu
     * @param evt evento non utilizzato
     */
private void formMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_formMouseClicked
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            doubleclick();
            tabellamieicreek.clearSelection();
            tabellapubblicati.clearSelection();
            tabellaricerca.clearSelection();
            bottoneprecedente.setEnabled(false);
            bottonesuccessivo.setEnabled(false);
            menuprecedente.setEnabled(false);
            menusuccessivo.setEnabled(false);
            bottoneelimina.setEnabled(false);
            eliminaselezionato.setEnabled(false);
            avviaselezionato.setEnabled(false);
            bottonesalva.setEnabled(false);
            menusalva.setEnabled(false);
        }
    });
}//GEN-LAST:event_formMouseClicked

    /**
     * Tenta di disconnettersi chiamando l' handler disconnetti
     * @param evt evento non utilizzato
     */
private void bottonedisconnettiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonedisconnettiMouseClicked
    if (bottonedisconnetti.isEnabled()) {
        disconnetti();
    }
}//GEN-LAST:event_bottonedisconnettiMouseClicked

    /**
     * Tenta di disconnettersi chiamando l' handler disconnetti
     * @param evt evento non utilizzato
     */
private void disconnettiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_disconnettiActionPerformed
    if (disconnetti.isEnabled()) {
        disconnetti();
    }
}//GEN-LAST:event_disconnettiActionPerformed

    /**
     * Gestisce il click sulla seconda tabella
     * @param evt evento non utilizzato
     */
private void tabellapubblicatiMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabellapubblicatiMouseClicked
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            doubleclick();
            int[] array = tabellapubblicati.getSelectedRows();
            if (array.length > 0) {
                tabellamieicreek.clearSelection();
                tabellaricerca.clearSelection();
                bottonesuccessivo.setEnabled(true);
                bottoneprecedente.setEnabled(true);
                menuprecedente.setEnabled(true);
                menusuccessivo.setEnabled(true);
                menusalva.setEnabled(false);
                bottonesalva.setEnabled(false);
                // se sono connesso attivo anche altre funzioni
                if (peer.getIpServer() != null) {
                    eliminaselezionato.setEnabled(true);
                    bottoneelimina.setEnabled(true);
                    avviaselezionato.setEnabled(false);
                }
            }
        }
    });
}//GEN-LAST:event_tabellapubblicatiMouseClicked

    /**
     * Gestisce il click sulla prima tabella
     * @param evt evento non utilizzato
     */
private void tabellamieicreekMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabellamieicreekMouseClicked
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            doubleclick();
            int[] array = tabellamieicreek.getSelectedRows();
            if (array.length > 0) {
                tabellapubblicati.clearSelection();
                tabellaricerca.clearSelection();
                bottonesuccessivo.setEnabled(true);
                bottoneprecedente.setEnabled(true);
                menuprecedente.setEnabled(true);
                menusuccessivo.setEnabled(true);
                bottonesalva.setEnabled(true);
                menusalva.setEnabled(true);
                /* se sono connesso attivo anche altre funzionalità */
                if (peer.getIpServer() != null) {
                    eliminaselezionato.setEnabled(true);
                    avviaselezionato.setEnabled(true);
                    bottoneelimina.setEnabled(true);
                }
            }
        }
    });
}//GEN-LAST:event_tabellamieicreekMouseClicked

    /**
     * Tenta di effettuare una ricerca chiamando l' handler cerca
     * @param evt evento non utilizzato
     */
private void bottonecercaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonecercaMouseClicked
    if (bottonecerca.isEnabled()) {
        cerca();
    }
}//GEN-LAST:event_bottonecercaMouseClicked

    /**
     * Tenta di effettuare una ricerca chiamando l' handler cerca
     * @param evt evento non utilizzato
     */
private void cercaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_cercaActionPerformed
    if (cerca.isEnabled()) {
        cerca();
    }
}//GEN-LAST:event_cercaActionPerformed

    /**
     *  Gestisce il click sulla tabella dei file cercati
     * @param evt evento non utilizzato
     */
private void tabellaricercaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_tabellaricercaMouseClicked
    SwingUtilities.invokeLater(new Runnable() {

        public void run() {
            if (!tabellacercaclicked) {
                tabellacercaclicked = true;
                int[] array = tabellaricerca.getSelectedRows();
                if (array.length > 0) {
                    tabellamieicreek.clearSelection();
                    tabellapubblicati.clearSelection();
                    bottonesuccessivo.setEnabled(true);
                    bottoneprecedente.setEnabled(true);
                    menuprecedente.setEnabled(true);
                    menusuccessivo.setEnabled(true);
                    menusalva.setEnabled(true);
                    bottonesalva.setEnabled(true);
                    if (peer.getIpServer() != null) {
                        bottoneelimina.setEnabled(false);
                        eliminaselezionato.setEnabled(false);
                        eliminacercati.setEnabled(true);
                        avviaselezionato.setEnabled(true);
                    }
                }
            } else {
                if (avviaselezionato.isEnabled()) {
                    avvia();
                }
            }
        }
    });
}//GEN-LAST:event_tabellaricercaMouseClicked

    /**
     * Elimina tutti i creek tabella dei file cercati
     * @param evt evento non utilizzato
     */
private void eliminacercatiActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_eliminacercatiActionPerformed
    if (elimina.isEnabled()) {
        modellocerca.removeall();
        modellocerca.fireTableDataChanged();
    }
}//GEN-LAST:event_eliminacercatiActionPerformed

    /**
     * Tenta di avviare il download sul creek selezionato chiamando
     * l' handler avvia
     * @param evt evento non utilizzato
     */
private void avviaselezionatoActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avviaselezionatoActionPerformed
    if (avviaselezionato.isEnabled()) {
        avvia();
    }
}//GEN-LAST:event_avviaselezionatoActionPerformed

    /**
     * Tenta di avviare il download su tutti i creek chiamando
     * l' handler avvia
     * @param evt evento non utilizzato
     */
private void avviatutticercaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_avviatutticercaActionPerformed
    if (avviatutticerca.isEnabled()) {
        tabellaricerca.selectAll();
        avvia();
    }
}//GEN-LAST:event_avviatutticercaActionPerformed

    /**
     * Tenta di avviare il test NAT-firewall chiamando
     * l' handler test
     * @param evt evento non utilizzato
     */
private void menutestActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menutestActionPerformed
    if (menutest.isEnabled()) {
        test();
    }
}//GEN-LAST:event_menutestActionPerformed

    /**
     * Tenta di avviare il test NAT-firewall chiamando
     * l' handler test
     * @param evt evento non utilizzato
     */
private void bottonetestMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonetestMouseClicked
    if (bottonetest.isEnabled()) {
        test();
    }
}//GEN-LAST:event_bottonetestMouseClicked

    /**
     * Tenta di settare la porta chiamando
     * l' handler settaporta
     * @param evt evento non utilizzato
     */
private void bottonesettaportaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonesettaportaMouseClicked
    if (bottonesettaporta.isEnabled()) {
        try {
            settaporta(false);
        } catch (ErrorException e) {
            PrintInformation("Impossibile settare la porta : " + e.getMessage(), ERRORE);
        }
    }
}//GEN-LAST:event_bottonesettaportaMouseClicked

    /**
     * Tenta di settare la porta chiamando
     * l' handler settaporta
     * @param evt evento non utilizzato
     */
private void menusettaportaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menusettaportaActionPerformed
    if (menusettaporta.isEnabled()) {
        try {
            settaporta(false);
        } catch (ErrorException e) {
            PrintInformation("Impossibile settare la porta : " + e.getMessage(), ERRORE);
        }
    }
}//GEN-LAST:event_menusettaportaActionPerformed

    /**
     * Tenta di chiudere l'applicazione chiamando
     * l' handler pulizia
     * @param evt evento non utilizzato
     */
private void bottoneexitMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottoneexitMouseClicked
    pulizia();
}//GEN-LAST:event_bottoneexitMouseClicked

    /**
     * Tenta di salvare un .creek chiamando
     * l' handler salva
     * @param evt evento non utilizzato
     */
private void bottonesalvaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonesalvaMouseClicked
    if (bottonesalva.isEnabled()) {
        salva();
    }
}//GEN-LAST:event_bottonesalvaMouseClicked

    /**
     * Tenta di salvare un .creek chiamando
     * l' handler salva
     * @param evt evento non utilizzato
     */
private void menusalvaActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menusalvaActionPerformed
    if (menusalva.isEnabled()) {
        salva();
    }
}//GEN-LAST:event_menusalvaActionPerformed

    /**
     * Visualizza le informazioni riguardanti
     * i file scaricati
     * @param evt evento non utilizzato
     */
private void menudownloadActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_menudownloadActionPerformed
    if (menudownload.isEnabled()) {
        PrintInformation("I file completi sono presenti in 'File Condivisi'\nnella cartella del programma", AIUTO);
    }
}//GEN-LAST:event_menudownloadActionPerformed

    /**
     * Esegue la ricerca 'Mi sento fortunato'
     * @param evt evento non utilizzato
     */
private void bottonefortunaMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_bottonefortunaMouseClicked
    if (bottonefortuna.isEnabled()) {
        /* rimuovo i vecchi risultati della ricerca*/
        modellocerca.removeall();
        modellocerca.fireTableDataChanged();
        try {
            /* effettuo la ricerca con un metacarattere speciale */
            peer.cerca("\\", this);
        } catch (ErrorException e) {
            PrintInformation(e.getMessage(), ERRORE);
        }
        doubleclick();
    }
}//GEN-LAST:event_bottonefortunaMouseClicked

    /**
     * Handler che si occupa di far partire uno o più download 
     */
    private void avvia() {
        int[] arraycerca = tabellaricerca.getSelectedRows();
        if (arraycerca.length != 0) {
            //QUI C'ERA UN TRY
            
                /* effettua l'aggiunta dei descrittori nell'arraydescr della logica del peer */
                peer.avviaDescr(arraycerca);
            
        }
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                doubleclick();
                tabellamieicreek.clearSelection();
                tabellaricerca.clearSelection();
                bottoneelimina.setEnabled(false);
                eliminaselezionato.setEnabled(false);
                avviaselezionato.setEnabled(false);
                bottonesuccessivo.setEnabled(false);
                menusuccessivo.setEnabled(false);
                bottoneprecedente.setEnabled(false);
                menuprecedente.setEnabled(false);
            }
        });
    }

    /**
     * Handler che si occupa di salvare i creek selezionati
     */
    private void salva() {
        final int cercare = tabellaricerca.getSelectedRow();
        final int down = tabellamieicreek.getSelectedRow();
        /* creazione della finestra di dialogo per connessione al server */
        final javax.swing.JDialog d = new javax.swing.JDialog(this, "Digita il Path", true);
        d.setSize(230, 115);
        javax.swing.JLabel l = new javax.swing.JLabel("Percorso :", javax.swing.JLabel.CENTER);
        final javax.swing.JTextField a = new javax.swing.JTextField("/home/bande/Scrivania/");
        d.getContentPane().setLayout(new BorderLayout());
        d.getContentPane().add(l, BorderLayout.NORTH);
        d.getContentPane().add(a, BorderLayout.CENTER);
        javax.swing.JButton b = new javax.swing.JButton("Salva");
        b.setMnemonic(java.awt.event.KeyEvent.VK_ENTER);
        b.addActionListener(new java.awt.event.ActionListener() {

            public void actionPerformed(java.awt.event.ActionEvent ev) {
                d.setVisible(false);
                d.dispose();
                String path = a.getText();
                boolean problema = false;
                if (path == null || path.compareTo("") == 0) {
                    problema = true;
                    PrintInformation("Percorso sbagliato", ERRORE);
                }
                if (path.charAt(path.length() - 1) != '/' && path.charAt(path.length() - 1) != '\\') {
                    problema = true;
                    PrintInformation("Aggiungi / oppure \\ in fondo al path\nin base al sistema operativo che usi", ERRORE);
                }
                if (!problema) {
                    try {
                        if (cercare != NULL) {
                            peer.salva(path, modellocerca.getRiga(cercare).getNome(), true);
                        } else {
                            peer.salva(path, modellomieicreek.getRiga(down).getFile(), false);
                        }
                    } catch (ErrorException e) {
                        PrintInformation("Impossibile salvare file : " + e.getMessage(), ERRORE);
                    }
                }
            }
        });
        javax.swing.JPanel p = new javax.swing.JPanel();
        p.add(b);
        d.getContentPane().add(p, BorderLayout.SOUTH);
        d.setLocationRelativeTo(this);
        d.setVisible(true);
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                tabellamieicreek.clearSelection();
                tabellaricerca.clearSelection();
                bottonesalva.setEnabled(false);
                menusalva.setEnabled(false);
            }
        });

    }

    /**
     * Handler che si occupa di cercare un file 
     */
    private void cerca() {

        String nome = areacerca.getText();
        if (nome.compareTo("") == 0 || nome == null) {
            PrintInformation("Non hai inserito la parola chiave", INFORMATION);
        } else if (nome.contains("[") || nome.contains("]") || nome.contains("{") || nome.contains("}") || nome.contains("(") || nome.contains(")") || nome.contains("?") || nome.contains("\\") || nome.contains("$") || nome.contains("^") || nome.contains("*") || nome.contains("|") || nome.contains(".") || nome.contains("+")) {
            PrintInformation("Non puoi inserire metacaratteri quali parentesi, *, ?,\n\\, $, ^, *, |, ., +", INFORMATION);
        } else {
            /* rimuovo i vecchi risultati della ricerca*/
            modellocerca.removeall();
            modellocerca.fireTableDataChanged();
            try {
                /* inizio la ricerca */
                peer.cerca(nome, this);
            } catch (ErrorException e) {
                PrintInformation(e.getMessage(), ERRORE);
            }
        }
        doubleclick();
    }

    /**
     * Aggiornamento dell' interfaccia in seguito all'
     * esecuzione di una ricerca
     * @param cerca array di descrittori risultato della ricerca
     */
    public void ricercaDone(ArrayList<Descrittore> cerca) {
        for (Descrittore d : cerca) {
            try {
                modellocerca.addRiga(new RigaTabellaCerca(d.getName(), d.getDimensione(), d.getNumSeeders(), d.getNumLeechers()));
            } catch (ErrorException e) {
                PrintInformation("Impossibile effettuare la ricerca : " + e.getMessage(), ERRORE);
            }
        }
        modellocerca.fireTableDataChanged();
    }

    /**
     * Handler che si occupa della disconnessione
     */
    private void disconnetti() {
        /* disconnetto il peer */
        peer.disconnetti();
        /* aggiornamento interfaccia grafica */
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                /* elimino eventuali risultati delle ricerche e aggiorno */
                modellocerca.removeall();
                doubleclick();
                tabellamieicreek.clearSelection();
                tabellapubblicati.clearSelection();
                tabellaricerca.clearSelection();
                labelipserver.setText("IP server : ");
                labelconnessione.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-sad.png")));
                labelnatfaccina.setIcon(new javax.swing.ImageIcon(getClass().getResource("/icone/face-sad.png")));
                labelnatfaccina.setEnabled(false);
                bottonetest.setEnabled(false);
                menutest.setEnabled(false);
                bottonecrea.setEnabled(false);
                crea.setEnabled(false);
                bottoneapri.setEnabled(false);
                apri.setEnabled(false);
                bottoneelimina.setEnabled(false);
                elimina.setEnabled(false);
                apri.setEnabled(false);
                avvia.setEnabled(false);
                bottonedisconnetti.setEnabled(false);
                disconnetti.setEnabled(false);
                bottonecerca.setEnabled(false);
                bottonefortuna.setEnabled(false);
                cerca.setEnabled(false);
                bottoneconnetti.setEnabled(true);
                connetti.setEnabled(true);
                bottonesettaporta.setEnabled(true);
                menusettaporta.setEnabled(true);
            }
        });
    }

    /**
     * Effettua il test NAT - Firewall
     */
    private void test() {
        try {
            peer.test(this);
        } catch (ErrorException e) {
            PrintInformation("Connessione fallita : " + e.getMessage(), ERRORE);
        }
    }

    /**
     * Handler per chiusura corretta del programma.
     */
    private void pulizia() {
        doubleclick();
        /* chiusura protocollo */
        peer.close();
    }

    /**
     * Crea un .creek
     */
    private void crea() {
        File sorgente = null; // file da pubblicare
        JFileChooser scelta = new JFileChooser(); // finestra di selezione

        scelta.setDialogTitle("Open file");
        if (scelta.showOpenDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
            sorgente = scelta.getSelectedFile();
        }
        doubleclick();
        /* creazione del creek*/
        try {
            peer.crea(sorgente, this);
        } catch (ErrorException e) {
            PrintInformation("Impossibile creare creek : " + e.getMessage(), ERRORE);
        }
    }

    /**
     * Apre un .creek
     */
    private void apri() {
        File f = null;
        JFileChooser scelta = new JFileChooser();
        scelta.setDialogTitle("Open .creek");
        javax.swing.filechooser.FileNameExtensionFilter filter =
                new javax.swing.filechooser.FileNameExtensionFilter("File creek", "creek");
        scelta.setFileFilter(filter);
        if (scelta.showOpenDialog(getRootPane()) == JFileChooser.APPROVE_OPTION) {
            f = scelta.getSelectedFile();
        }
        doubleclick();
        try {
            peer.apri(f, this);
        } catch (ErrorException e) {
            PrintInformation("Impossibile aprire .creek : " + e.getMessage(), ERRORE);
        }
    }

    /**
     * Tenta di stabilire una connessione con il server 
     */
    private void connetti() {
        boolean problema = false;
        try {
            /* configurazione porta in ascolto */
            settaporta(true);
        } catch (ErrorException e) {
            problema = true;
            PrintInformation("Impossibile settare la porta : " + e.getMessage(), ERRORE);
        }
        if (!problema) {
            /* creazione della finestra di dialogo per connessione al server */
            final javax.swing.JDialog d = new javax.swing.JDialog(this, "Connetti al server", true);
            d.setSize(230, 115);
            javax.swing.JLabel l = new javax.swing.JLabel("Digita IP del server :", javax.swing.JLabel.CENTER);
            final javax.swing.JTextField a = new javax.swing.JTextField("127.0.0.1");
            d.getContentPane().setLayout(new BorderLayout());
            d.getContentPane().add(l, BorderLayout.NORTH);
            d.getContentPane().add(a, BorderLayout.CENTER);
            javax.swing.JButton b = new javax.swing.JButton("Connetti");
            b.setMnemonic(java.awt.event.KeyEvent.VK_ENTER);
            b.addActionListener(new java.awt.event.ActionListener() {

                public void actionPerformed(java.awt.event.ActionEvent ev) {

                    try {
                        eseguiConnetti(InetAddress.getByName(a.getText()));
                    } catch (UnknownHostException ex) {
                        PrintInformation("Tentativo di connessione fallito", ERRORE);
                    } catch (ErrorException e) {
                        PrintInformation("Tentativo di connessione fallito : " + e.getMessage(), ERRORE);
                    }
                    d.setVisible(false);
                    d.dispose();
                }
            });
            javax.swing.JPanel p = new javax.swing.JPanel();
            p.add(b);
            d.getContentPane().add(p, BorderLayout.SOUTH);
            d.setLocationRelativeTo(this);
            d.setVisible(true);
        }
    }

    /**
     * Esegue la chiamata a connetti nel protocollo
     * @param server IP del server
     * @throws condivisi.ErrorException se il parametro è null
     */
    private void eseguiConnetti(InetAddress server) throws ErrorException {
        if (server == null) {
            throw new ErrorException("Param null");
        }
        try {
            peer.connetti(server, this);
        } catch (ErrorException e) {
            throw new ErrorException(e.getMessage());
        }
        /* eseguo il test NAT - Firewall*/
        test();
    }

    /**
     * Seleziona nelle tabelle il file successivo 
     */
    private void successivo() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                doubleclick();
                int[] array = tabellamieicreek.getSelectedRows();
                int[] arraypubblicati = tabellapubblicati.getSelectedRows();
                int[] arrayricerca = tabellaricerca.getSelectedRows();
                int numrighe = array.length + arraypubblicati.length + arrayricerca.length;
                if (numrighe == 0 || numrighe > 1); else {
                    if (array.length != 0) {
                        tabellamieicreek.clearSelection();
                        if (array[0] == modellomieicreek.getRowCount() - 1) {
                            tabellamieicreek.setRowSelectionInterval(0, 0);
                        } else {
                            tabellamieicreek.setRowSelectionInterval(array[0] + 1, array[0] + 1);
                        }
                    }
                    if (arraypubblicati.length != 0) {
                        tabellapubblicati.clearSelection();
                        if (arraypubblicati[0] == modellopubblicati.getRowCount() - 1) {
                            tabellapubblicati.setRowSelectionInterval(0, 0);
                        } else {
                            tabellapubblicati.setRowSelectionInterval(arraypubblicati[0] + 1, arraypubblicati[0] + 1);
                        }
                    }
                    if (arrayricerca.length != 0) {
                        tabellaricerca.clearSelection();
                        if (arrayricerca[0] == modellocerca.getRowCount() - 1) {
                            tabellaricerca.setRowSelectionInterval(0, 0);
                        } else {
                            tabellaricerca.setRowSelectionInterval(arrayricerca[0] + 1, arrayricerca[0] + 1);
                        }
                    }
                }

            }
        });
    }

    /**
     * Seleziona nelle tabelle il file precedente
     */
    private void precedente() {
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                doubleclick();
                int[] array = tabellamieicreek.getSelectedRows();
                int[] arraypubblicati = tabellapubblicati.getSelectedRows();
                int[] arraycerca = tabellaricerca.getSelectedRows();
                int numrighe = array.length + arraypubblicati.length + arraycerca.length;
                if (numrighe == 0 || numrighe > 1); else {
                    if (array.length != 0) {
                        tabellamieicreek.clearSelection();
                        if (array[0] == 0) {
                            tabellamieicreek.setRowSelectionInterval(modellomieicreek.getRowCount() - 1, modellomieicreek.getRowCount() - 1);
                        } else {
                            tabellamieicreek.setRowSelectionInterval(array[0] - 1, array[0] - 1);
                        }
                    }
                    if (arraypubblicati.length != 0) {
                        tabellapubblicati.clearSelection();
                        if (arraypubblicati[0] == 0) {
                            tabellapubblicati.setRowSelectionInterval(modellopubblicati.getRowCount() - 1, modellopubblicati.getRowCount() - 1);
                        } else {
                            tabellapubblicati.setRowSelectionInterval(arraypubblicati[0] - 1, arraypubblicati[0] - 1);
                        }
                    }
                    if (arraycerca.length != 0) {
                        tabellaricerca.clearSelection();
                        if (arraycerca[0] == 0) {
                            tabellaricerca.setRowSelectionInterval(modellocerca.getRowCount() - 1, modellocerca.getRowCount() - 1);
                        } else {
                            tabellaricerca.setRowSelectionInterval(arraycerca[0] - 1, arraycerca[0] - 1);
                        }
                    }

                }
            }
        });
    }

    /**
     * Metodo che inizializza il protocollo Client 
     */
    private void initProtocol() {
        /* controllo se l'applicazione è già avviata */
        File conf = new File("./avviato.on");
        try {
            if (!conf.createNewFile()) {
                /* esiste --> non posso avviare l'applicazione */
                javax.swing.JOptionPane.showMessageDialog(getRootPane(), "Applicazione già avviata", "Errore", JOptionPane.ERROR_MESSAGE);
                System.exit(0);
            }
        } catch (IOException ex) {
            javax.swing.JOptionPane.showMessageDialog(getRootPane(), "Problema nell' apertura del file ./avviato.on", "Errore", JOptionPane.ERROR_MESSAGE);
            System.exit(ERRORE);
        }
        /* avvio il protocollo client*/
        try {
            peer = new BitCreekPeer();
        } catch (ErrorException e) {
            javax.swing.JOptionPane.showMessageDialog(getRootPane(), "Problema durante il caricamento delle metainfo: " + e.getMessage(), "Errore", JOptionPane.ERROR_MESSAGE);
            System.exit(ERRORE);
        }
    }

    /**
     * Handler che si occupa di eliminare
     * i file selezionati nele tabelle
     */
    private void elimina() {
        /* pulisco le tabelle, le metainfo e aggiorno l'interfaccia grafica */
        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                int[] array = tabellapubblicati.getSelectedRows();
                int[] arraycreek = tabellamieicreek.getSelectedRows();
                String nome = null;
                /* gestisco i file in upload */
                for (int i = 0; i < array.length; i++) {
                    nome = (String) modellopubblicati.getValueAt(array[0], 1);
                    try {
                        peer.elimina(nome);
                        modellopubblicati.removeRiga(array[0]);
                    } catch (ErrorException e) {
                        PrintInformation("Impossibile cancellare il file : " + e.getMessage(), ERRORE);
                        break;
                    }
                }
                /* gestisco i file in download */
                for (int i = 0; i < arraycreek.length; i++) {
                    nome = (String) modellomieicreek.getValueAt(arraycreek[0], 1);
                    try {
                        peer.elimina(nome);
                        modellomieicreek.removeRiga(arraycreek[0]);
                    } catch (ErrorException e) {
                        PrintInformation("Impossibile cancellare il file : " + e.getMessage(), ERRORE);
                        break;
                    }
                }
                /* aggiornamento */
                doubleclick();
                tabellamieicreek.clearSelection();
                tabellapubblicati.clearSelection();
                tabellaricerca.clearSelection();
                menuprecedente.setEnabled(false);
                menusuccessivo.setEnabled(false);
                bottoneprecedente.setEnabled(false);
                bottonesuccessivo.setEnabled(false);
                bottoneelimina.setEnabled(false);
                eliminaselezionato.setEnabled(false);
                avviaselezionato.setEnabled(false);
            }
        });
    }

    /**
     * Si occupa di resettare il 2-click
     */
    private void doubleclick() {
        if (tabellacercaclicked) {
            tabellacercaclicked = false;
        }
    }

    /**
     * Setta la porta in ascolto se non già inizializzata precedentemente
     * @throws condivisi.ErrorException
     */
    private void settaporta(boolean cond) throws ErrorException {
        /* controllo se la porta è già stata settata */
        File conf = new File("./porta.conf");
        try {
            if ((cond & conf.createNewFile()) || !cond) {
                /* la porta va settata */
                final javax.swing.JDialog d = new javax.swing.JDialog(this, "Scegli la porta di ascolto", true);
                d.setSize(300, 115);
                javax.swing.JLabel l = new javax.swing.JLabel("Digita la porta :", javax.swing.JLabel.CENTER);
                final javax.swing.JTextField a = new javax.swing.JTextField("4900");
                d.getContentPane().setLayout(new BorderLayout());
                d.getContentPane().add(l, BorderLayout.NORTH);
                d.getContentPane().add(a, BorderLayout.CENTER);
                javax.swing.JButton b = new javax.swing.JButton("Verifica");
                b.setMnemonic(java.awt.event.KeyEvent.VK_ENTER);
                b.addActionListener(new java.awt.event.ActionListener() {

                    public void actionPerformed(java.awt.event.ActionEvent ev) {
                        boolean problema = false;
                        String p = a.getText();
                        if (p == null || p.compareTo("") == 0) {
                            problema = true;
                            PrintInformation("Porta non valida", INFORMATION);
                        }
                        if (!problema) {
                            int porta = NULL;
                            try {
                                porta = Integer.valueOf(p);
                                if (porta <= PORTAMIN || porta > PORTAMAX) {
                                    problema = true;
                                    PrintInformation("Porta non valida", INFORMATION);
                                }
                            } catch (NumberFormatException e) {
                                problema = true;
                                PrintInformation("Porta non valida", INFORMATION);
                            }
                            if (!problema) {
                                if (!peer.settaporta(porta)) {
                                    PrintInformation("Porta non disponibile\nSettare un'altra porta dal menu Aiuto", ERRORE);
                                } else {
                                    labelporta.setText("Porta Utilizzata : " + porta);
                                }
                            }
                        }
                        d.setVisible(false);
                        d.dispose();
                    }
                });
                javax.swing.JPanel p = new javax.swing.JPanel();
                p.add(b);
                d.getContentPane().add(p, BorderLayout.SOUTH);
                d.setLocationRelativeTo(this);
                d.setVisible(true);
            }
        } catch (IOException ex) {
            throw new ErrorException("Non si può aprire porta.conf");
        }
    }

    /* Variabili d'istanza*/

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JMenu aiuto;
    private javax.swing.JMenuItem apri;
    private javax.swing.JTextField areacerca;
    private javax.swing.JMenu avvia;
    private javax.swing.JMenuItem avviaselezionato;
    private javax.swing.JMenuItem avviatutticerca;
    private javax.swing.JCheckBoxMenuItem barra;
    private javax.swing.JToolBar barrastrumenti;
    private javax.swing.JButton bottoneapri;
    private javax.swing.JButton bottonecerca;
    private javax.swing.JButton bottoneconnetti;
    private javax.swing.JButton bottonecrea;
    private javax.swing.JButton bottonedisconnetti;
    private javax.swing.JButton bottoneelimina;
    private javax.swing.JMenuItem bottoneeliminapubblicati;
    private javax.swing.JMenuItem bottoneeliminatutticreek;
    private javax.swing.JButton bottoneexit;
    private javax.swing.JButton bottonefortuna;
    private javax.swing.JButton bottoneprecedente;
    private javax.swing.JButton bottonesalva;
    private javax.swing.JButton bottonesettaporta;
    private javax.swing.JButton bottonesuccessivo;
    private javax.swing.JButton bottonetest;
    private javax.swing.JMenuItem cerca;
    private javax.swing.JMenuItem connetti;
    private javax.swing.JMenuItem crea;
    private javax.swing.JMenuItem disconnetti;
    private javax.swing.JMenu elimina;
    private javax.swing.JMenuItem eliminacercati;
    private javax.swing.JMenuItem eliminaselezionato;
    private javax.swing.JMenuItem esci;
    private javax.swing.JMenu file;
    private javax.swing.JPanel grafico;
    private javax.swing.JMenuItem info;
    private javax.swing.JDesktopPane jDesktopPane1;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JMenuBar jMenuBar1;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JToolBar.Separator jSeparator1;
    private javax.swing.JToolBar.Separator jSeparator2;
    private javax.swing.JToolBar.Separator jSeparator3;
    private javax.swing.JLabel labelconnessione;
    private javax.swing.JLabel labelipserver;
    private javax.swing.JLabel labelmioip;
    private javax.swing.JLabel labelnat;
    private javax.swing.JLabel labelnatfaccina;
    private javax.swing.JLabel labelporta;
    private javax.swing.JMenuItem menudownload;
    private javax.swing.JMenuItem menuprecedente;
    private javax.swing.JMenuItem menusalva;
    private javax.swing.JMenuItem menusettaporta;
    private javax.swing.JMenuItem menusuccessivo;
    private javax.swing.JMenuItem menutest;
    private javax.swing.JPanel pannellomieicreek;
    private javax.swing.JPanel pannellopubblicati;
    private javax.swing.JTable tabellamieicreek;
    private javax.swing.JTable tabellapubblicati;
    private javax.swing.JTable tabellaricerca;
    private javax.swing.JMenu trasferimenti;
    private javax.swing.JMenu visualizza;
    // End of variables declaration//GEN-END:variables
    /* Variabili d'istanza */
    /** Modello della prima tabella */
    private ModelloTabellaMieiCreek modellomieicreek;
    /** Modello della seconda tabella */
    private ModelloTabellaPubblicati modellopubblicati;
    /** Modello della tabella dei file crecati */
    private ModelloTabellaCerca modellocerca;
    /** Per emulare 2-click sulla tabella dei file crecati */
    private boolean tabellacercaclicked;
    /** Logica del programma */
    private BitCreekPeer peer;

    /* Costanti */
    /**
     * Millisecondi che ci sono tra un aggiornamento e l'altro
     * dell' interfaccia grafica
     */
    private final int AGGIORNAMENTO = 500; /* 500 ms */

    /**
     * Millisecondi che ci sono tra un aggiornamento e l'altro
     * del grafico
     */
    private final int AGGIORNAMENTOGRAFICO = 1200; /* 1,2 secondi */

    /** Definisce la costante NULL */
    private final int NULL = -1;
    /** Definisce la costante porta + piccola */
    private final int PORTAMIN = 1;
    /** Definisce la costante porta più grande */
    private final int PORTAMAX = 65535;
    /** Definisce la costante ERRORE */
    public final char ERRORE = 1;
    /** Definisce la costante INFORMATION */
    private final char INFORMATION = 2;
    /** Definisce la costante AIUTO */
    private final char AIUTO = 3;
    /** Costante che definisce la versione della classe */
    public static final long serialVersionUID = 10;
}
