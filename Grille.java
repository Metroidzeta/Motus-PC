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
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Rectangle;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Objects;

public class Grille {

	private final String mot;
	private final int hauteur;
	private final int largeur;
	private int tour = 0;
	private final Rectangle[][] cases;
	private int[][] couleursCases;
	private char[][] lettresCases;
	private boolean[] lettresTrouvees;
	private HashSet<String> motsSaisis = new HashSet<>();
	private boolean resolue = false;

	public Grille(String mot, int nbEssais) {
		validerArguments(mot, nbEssais);

		this.mot = mot;
		hauteur = nbEssais;
		largeur = mot.length();
		cases = new Rectangle[hauteur][largeur];
		couleursCases = new int[hauteur][largeur];
		lettresCases = new char[hauteur][largeur];

		int largeurCases = (Motus.WINDOW_WIDTH - largeur - 1) / largeur; // ajuster la largeur de la case par rapport à la largeur de la fenêtre
		int hauteurCases = (Motus.WINDOW_HEIGHT - largeur - 100) / largeur; // ajuster la hauteur de la case par rapport à la hauteur de la fenêtre
		int offSetL = (Motus.WINDOW_WIDTH - (largeurCases * largeur) - largeur + 1) / 2;
		for (int i = 0; i < hauteur; i++) {
			for (int j = 0; j < largeur; j++) {
				cases[i][j] = new Rectangle(offSetL + j * largeurCases + j, i * hauteurCases + i, largeurCases, hauteurCases);
				couleursCases[i][j] = 0;
				lettresCases[i][j] = '\0';
			}
		}
		lettresTrouvees = new boolean[largeur]; // tableau booléen (false par défaut) pour savoir quelles lettres ont déjà été trouvées
	}

	private static void validerArguments(String mot, int nbEssais) {
		if (mot == null) throw new NullPointerException("Mot null");
		if (mot.isEmpty()) throw new IllegalArgumentException("Mot vide");
		if (nbEssais < 1 || nbEssais > 10) throw new IllegalArgumentException("NbEssais doit être compris entre 1 et 10");
	}

	/*** Getters ***/
	public String getMot() { return mot; }
	public int getHauteur() { return hauteur; }
	public int getLargeur() { return largeur; }
	public int getTour() { return tour; }
	public boolean resolue() { return resolue; }
	public HashSet<String> getLesMotsSaisis() { return motsSaisis; }

	/*** Setters ***/
	public void setTour(int i) { tour = i; }
	public void setCouleursCases(int i, int j, int num) { couleursCases[i][j] = num; }
	public void setLettresCases(int i, int j, char c) { lettresCases[i][j] = c; }

	public void preRemplirLigne() {
		char firstChar = mot.charAt(0);
		for (int j = 0; j < largeur; j++) {
			lettresCases[tour][j] = (j == 0 || mot.charAt(j) == firstChar || lettresTrouvees[j]) ? mot.charAt(j) : '.';
		}
	}

	public void insererMot(String motSaisi) {
		if (motSaisi.length() != largeur) throw new IllegalArgumentException("Le mot saisi doit avoir une longueur de " + largeur);
		System.arraycopy(motSaisi.toCharArray(), 0, lettresCases[tour], 0, motSaisi.length());
		motsSaisis.add(motSaisi);
		if (motSaisi.equals(mot)) resolue = true;
	}

	public int colorierLettre(int i) {
		char c = lettresCases[tour][i];
		if (c == mot.charAt(i)) { // la lettre est bien positionnée
			couleursCases[tour][i] = 2; // la case devient rouge
			lettresTrouvees[i] = true; // ajoute la lettre trouvée au résultat
			return 2;
		}
		int occ = 0;
		for (int j = 0; j < largeur; j++) { // nb d'occurence de cette lettre qui n'est pas à la bonne place
			if (c == mot.charAt(j) && lettresCases[tour][j] != mot.charAt(j)) occ++;
		}
		for (int j = 0; j < i; j++) { // si une lettre précédente est déjà jaune on décrémente le compteur d'occurence
			if (c == lettresCases[tour][j] && lettresCases[tour][j] != mot.charAt(j)) occ--;
		}
		if (occ > 0) {
			couleursCases[tour][i] = 1; // la case devient jaune
			return 1;
		}
		return 0; // la case reste bleue
	}

	private static void dessinerCaseBleue(Graphics g, Rectangle rect, Fenetre f) {
		f.dessinerRectangle(g, Motus.BLEU, rect); // dessine case bleue
	}

	private static void dessinerCercleJaune(Graphics g, Rectangle rect, Fenetre f) {
		int diam = Math.min(rect.width, rect.height) - 12;
		int x = rect.x + (rect.width - diam) / 2;
		int y = rect.y + (rect.height - diam) / 2;
		f.dessinerOvale(g, Motus.JAUNE, x , y, diam, diam); // dessine cercle jaune
	}

	private static void dessinerCaseRouge(Graphics g, Rectangle rect, Fenetre f) {
		f.dessinerRectangle(g, Motus.ROUGE, rect); // dessine case rouge
	}

	public void dessinerCouleursCases(Graphics g, Fenetre f) {
		for (int i = 0; i < hauteur; i++) {
			for (int j = 0; j < largeur; j++) {
				Rectangle rect = cases[i][j];
				switch (couleursCases[i][j]) {
					case 0 -> dessinerCaseBleue(g, rect, f); // mauvaise lettre
					case 1 -> { // lettre à la mauvaise place
						dessinerCaseBleue(g, rect, f);
						dessinerCercleJaune(g, rect, f);
					}
					case 2 -> dessinerCaseRouge(g, rect, f); // lettre à la bonne place
				}
			}
		}
	}

	public void viderLigne(int i) {
		Arrays.fill(couleursCases[i], 0);
		Arrays.fill(lettresCases[i], '.');
	}

	public void dessinerLettres(Graphics g, Font font) {
		g.setColor(Color.WHITE);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();

		for (int i = 0; i <= tour; i++) {
			for (int j = 0; j < largeur; j++) {
				char c = lettresCases[i][j];
				if (c != '\0') {
					String s = String.valueOf(c);
					int largeurLettre = fm.charWidth(c);
					int hauteurLettre = fm.getHeight();
					Rectangle rect = cases[i][j];

					int x = rect.x + (rect.width - largeurLettre) / 2;
					int y = rect.y + (rect.height - hauteurLettre) / 2 + fm.getAscent();
					g.drawString(s, x, y);
				}
			}
		}
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Grille g)) return false;

		return mot.equals(g.mot)
			&& hauteur == g.hauteur
			&& largeur == g.largeur
			&& tour == g.tour
			&& Arrays.deepEquals(cases, g.cases)
			&& Arrays.deepEquals(couleursCases, g.couleursCases)
			&& Arrays.deepEquals(lettresCases, g.lettresCases)
			&& Arrays.equals(lettresTrouvees, g.lettresTrouvees)
			&& motsSaisis.equals(g.motsSaisis)
			&& resolue == g.resolue;
	}

	@Override
	public int hashCode() {
		return Objects.hash(
			mot, hauteur, largeur, tour, motsSaisis, resolue,
			Arrays.deepHashCode(cases),
			Arrays.deepHashCode(couleursCases),
			Arrays.deepHashCode(lettresCases),
			Arrays.hashCode(lettresTrouvees)
		);
	}
}