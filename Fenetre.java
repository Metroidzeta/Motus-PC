/* Auteur du projet : Metroidzeta
	Pour compiler avec Windows, GNU/Linux et MacOS :
		> javac *.java
	Pour exécuter :
		> java Motus
	Pour créer un jar de l'application :
		> jar cvmf MANIFEST.MF Motus.jar *.class bruitages/* listesMots/*
*/

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class Fenetre extends JFrame implements ActionListener {

	private static final long serialVersionUID = 1L;
	private final int REAL_WIDTH;
	private final int REAL_HEIGHT;
	private JTextField zoneTexte;
	private JButton boutonValider;
	private boolean boutonAppuye = false;
	private String msgErr = null;

	public Fenetre(String nom, JPanel renderer) {
		super();
		setTitle(nom); // Titre de la fenêtre
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); // Ferme le programme lorsqu'on clique sur la croix rouge
		pack();
		int largeur = Motus.WINDOW_WIDTH;
		int hauteur = Motus.WINDOW_HEIGHT;
		this.REAL_WIDTH = largeur + getInsets().left + getInsets().right;
		this.REAL_HEIGHT = hauteur + getInsets().top + getInsets().bottom;
		setSize(REAL_WIDTH,REAL_HEIGHT); // Taile réelle de la fenêtre (largeur * hauteur) en pixels avec les bords

		zoneTexte = new JTextField();
		zoneTexte.setBounds((int)(largeur * 0.15),hauteur - 70,(int)(largeur * 0.20),30);

		boutonValider = new JButton("Valider");
		boutonValider.setBounds((int)(largeur * 0.65),hauteur - 70,(int)(largeur * 0.20),30);
		boutonValider.addActionListener(this);

		setLocationRelativeTo(null); // Fenêtre positionné au centre
		setContentPane(renderer);
		getContentPane().setLayout(null);
		getContentPane().add(zoneTexte);
		getContentPane().add(boutonValider);
	}

	/*** Les getters ***/
	public int getRealWidth() { return REAL_WIDTH; }
	public int getRealHeight() { return REAL_HEIGHT; }
	public String getMotSaisi() { return zoneTexte.getText(); }
	public boolean getBoutonAppuye() { return boutonAppuye; }
	public String getMsgErr() { return msgErr; }

	/*** Les setters ***/
	public void rendreVisible() { setVisible(true); }
	public void setMsgErr(String msgErr) { this.msgErr = msgErr; }

	public void dessinerRectangle(Graphics g, Color couleur, Rectangle rect) {
		g.setColor(couleur);
		g.fillRect(rect.x,rect.y,rect.width,rect.height);
	}

	public void dessinerFondNoir(Graphics g) {
		Rectangle fond = new Rectangle(0,0,Motus.WINDOW_WIDTH,Motus.WINDOW_HEIGHT);
		dessinerRectangle(g,Color.BLACK,fond); // Rectangle background noir
	}

	public void dessinerOvale(Graphics g, Color couleur, int x, int y, int largeur, int hauteur) {
		g.setColor(couleur);
		g.fillOval(x,y,largeur,hauteur);
	}

	public void dessinerTexte(Graphics g, Color couleur, Font font, String str, int x, int y) {
		g.setColor(couleur);
		g.setFont(font);
		g.drawString(str,x,y);
	}

	public void actionPossible() {
		boutonValider.setEnabled(true);
		zoneTexte.setEditable(true);
		boutonAppuye = false;
	}

	public void actionImpossible() {
		boutonValider.setEnabled(false);
		zoneTexte.setEditable(false);
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if(e.getSource() == boutonValider && !boutonAppuye) {
			actionImpossible();
			boutonAppuye = true;
		}
	}
}