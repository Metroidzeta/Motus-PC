/*
 * @author Alain Barbier alias "Metroidzeta"
 *
 * Pour compiler avec Windows, GNU/Linux et MacOS :
 *     > javac *.java
 *
 * Pour exécuter :
 *     > java Motus
 *
 * Pour créer un jar de l'application :
 *     > jar cvmf MANIFEST.MF Motus.jar *.class bruitages/* listesMots/*
 */

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Insets;
import java.awt.Rectangle;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;

public class Fenetre extends JFrame {

	private static final long serialVersionUID = 1L;

	private final int realWidth;
	private final int realHeight;

	private final JTextField champTexte = new JTextField();
	private final JButton boutonValider = new JButton("Valider");

	private boolean boutonAppuye = false;
	private String msgErr = "";

	public Fenetre(String titre, JPanel renderer) {
		super(titre);
		setDefaultCloseOperation(EXIT_ON_CLOSE); // ferme le programme avec la croix
		setContentPane(renderer);
		getContentPane().setLayout(null); // placement libre

		int largeur = Motus.WINDOW_WIDTH;
		int hauteur = Motus.WINDOW_HEIGHT;

		// Initialisation des composants
		initUI(largeur, hauteur);

		setSize(largeur, hauteur); // taille "brute" de la fenêtre sans les décorations
		setLocationRelativeTo(null); // centrer la fenêtre
		setVisible(true); // rendre la fenêtre visible pour initialiser correctement les insets

		// Ajuster la taille réelle pour inclure les bords (insets)
		Insets insets = getInsets();
		realWidth = largeur + insets.left + insets.right;
		realHeight = hauteur + insets.top + insets.bottom;
		setSize(realWidth, realHeight);
	}

	/** Initialisation de l'UI **/
	private void initUI(int largeur, int hauteur) {
		champTexte.setBounds((int)(largeur * 0.15), hauteur - 70, (int)(largeur * 0.20), 30);
		boutonValider.setBounds((int)(largeur * 0.65), hauteur - 70, (int)(largeur * 0.20), 30);

		boutonValider.addActionListener(e -> {
			if (!boutonAppuye) {
				rendreSaisieImpossible();
				boutonAppuye = true;
			}
		});

		getContentPane().add(champTexte);
		getContentPane().add(boutonValider);
	}

	/*** Getters ***/
	public int getRealWidth() { return realWidth; }
	public int getRealHeight() { return realHeight; }
	public String getMotSaisi() { return champTexte.getText(); }
	public boolean getBoutonAppuye() { return boutonAppuye; }
	public String getMsgErr() { return msgErr; }

	/*** Setters ***/
	public void setMsgErr(String s) { msgErr = s; }

	/*** Saisie ***/
	public void rendreSaisiePossible() {
		boutonValider.setEnabled(true);
		champTexte.setEditable(true);
		boutonAppuye = false;
	}

	public void rendreSaisieImpossible() {
		boutonValider.setEnabled(false);
		champTexte.setEditable(false);
	}

	/*** Méthodes de dessin ***/
	public void dessinerRectangle(Graphics g, Color couleur, Rectangle rect) {
		g.setColor(couleur);
		g.fillRect(rect.x, rect.y, rect.width, rect.height);
	}

	public void dessinerFondNoir(Graphics g) {
		Rectangle fond = new Rectangle(0, 0, Motus.WINDOW_WIDTH, Motus.WINDOW_HEIGHT);
		dessinerRectangle(g, Color.BLACK, fond);
	}

	public void dessinerOvale(Graphics g, Color couleur, int x, int y, int largeur, int hauteur) {
		g.setColor(couleur);
		g.fillOval(x, y, largeur, hauteur);
	}

	public void dessinerTexte(Graphics g, Color couleur, Font font, String texte, int x, int y) {
		g.setColor(couleur);
		g.setFont(font);
		g.drawString(texte, x, y);
	}
}