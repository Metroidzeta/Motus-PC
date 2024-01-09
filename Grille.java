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
	private int ligneActuelle;
	private final Rectangle[][] cases;
	private int[][] couleursCases;
	private char[][] lettresCases;
	private boolean[] resultat;
	private boolean resolue = false;

	public Grille(String mot, int nbEssais) {
		verificationsArgs(mot,nbEssais);
		this.mot = mot;
		int tailleMot = mot.length();
		this.hauteur = nbEssais;
		this.largeur = tailleMot;
		this.ligneActuelle = 0;
		this.cases = new Rectangle[nbEssais][tailleMot];
		this.couleursCases = new int[nbEssais][tailleMot];
		this.lettresCases = new char[nbEssais][tailleMot];
		int largeurCases = (Motus.WINDOW_WIDTH - tailleMot - 1) / tailleMot; // Ajuster la largeur de la case par rapport à la largeur de la fenêtre
		int hauteurCases = (Motus.WINDOW_HEIGHT - nbEssais - 100) / nbEssais; // Ajuster la hauteur de la case par rapport à la hauteur de la fenêtre
		int offSetL = (Motus.WINDOW_WIDTH - (largeurCases * tailleMot) - tailleMot + 1) / 2;
		for(int i = 0; i < hauteur; i++) {
			for(int j = 0; j < largeur; j++) {
				cases[i][j] = new Rectangle(offSetL + j * largeurCases + j, i * hauteurCases + i, largeurCases, hauteurCases);
				couleursCases[i][j] = lettresCases[i][j] = 0;
			}
		}
		this.resultat = new boolean[tailleMot]; // Tableau booléen pour savoir si une lettre est à la bonne case
		Arrays.fill(resultat,false);
	}

	private void verificationsArgs(String mot, int nbEssais) {
		if(mot == null) { throw new NullPointerException("Le mot de la grille est null"); }
		if(mot.isEmpty()) { throw new IllegalArgumentException("Le mot de la grille est vide"); }
		if(nbEssais < 1) { throw new IllegalArgumentException("Le nbEssais de la grille est < 1"); }
	}

	/*** Les getters ***/
	public String getMot() { return mot; }
	public int getHauteur() { return hauteur; }
	public int getLargeur() { return largeur; }
	public boolean resolue() { return resolue; }
	public HashSet<String> getLesMotsSaisis() {
		HashSet<String> lesMotsSaisis = new HashSet<>();
		for(int i = 0; i < ligneActuelle; i++) {
			lesMotsSaisis.add(new String(lettresCases[i]));
		}
		return lesMotsSaisis;
	}

	/*** Les setters ***/
	public void setCouleursCases(int i, int j, int num) { couleursCases[i][j] = num; }
	public void setLettresCases(int i, int j, char c) { lettresCases[i][j] = c; }
	public void setLigneActuelle(int ligneActuelle) { this.ligneActuelle = ligneActuelle; }

	public void preRemplirLigne() {
		char firstChar = mot.charAt(0);
		lettresCases[ligneActuelle][0] = firstChar; // On met la première lettre du mot dans la grille
		for(int i = 1; i < largeur; i++) {
			char currentChar = mot.charAt(i);
			if(currentChar == firstChar || resultat[i]) { // Si la lettreActuelle correspond à la première lettre du mot ou que cette lettre a déjà été trouvée
				lettresCases[ligneActuelle][i] = currentChar; // On met la lettreActuelle à la case correspondante
			} else {
				lettresCases[ligneActuelle][i] = '.';
			}
		}
	}

	public void insererMot(String motSaisi) {
		System.arraycopy(motSaisi.toCharArray(),0,lettresCases[ligneActuelle],0,motSaisi.length());
		if(motSaisi.equals(mot)) { resolue = true; }
	}

	public int colorierLettre(int i) {
		char lettreActuelle = lettresCases[ligneActuelle][i];
		if(lettreActuelle == mot.charAt(i)) { // Si la lettre est bien positionnée
			couleursCases[ligneActuelle][i] = 2; // La case devient rouge
			resultat[i] = true; // On ajoute la lettre trouvée au résultat
			return 2;
		}
		int compteurOcurr = 0;
		for(int j = 0; j < largeur; j++) { // On compte le nombre d'occurence de cette lettre qui n'est pas à la bonne place
			if(lettreActuelle == mot.charAt(j) && mot.charAt(j) != lettresCases[ligneActuelle][j]) {
				compteurOcurr++;
			}
		}
		for(int j = 0; j < i; j++) { // Si une lettre précédente est déjà jaune on décrémente le compteur
			if(lettreActuelle == lettresCases[ligneActuelle][j] && mot.charAt(j) != lettresCases[ligneActuelle][j]) {
				compteurOcurr--;
			}
		}
		if(compteurOcurr > 0) {
			couleursCases[ligneActuelle][i] = 1; // La case devient jaune
			return 1;
		}
		return 0; // La case reste bleue
	}

	public void afficherCouleursCases(Graphics g, Fenetre fenetre) {
		for(int i = 0; i < hauteur; i++) {
			for(int j = 0; j < largeur; j++) {
				switch(couleursCases[i][j]) {
					case 0: // Mauvaise lettre
						fenetre.dessinerRectangle(g,Motus.BLEU,cases[i][j]); // Case bleue
						break;
					case 1: // Lettre présente mais à la mauvaise place
						fenetre.dessinerRectangle(g,Motus.BLEU,cases[i][j]); // Case bleue
						// Calcul du diamètre et position du cercle
						int diametre = Math.min(cases[i][j].width,cases[i][j].height) - 12;
						int posX = cases[i][j].x + (cases[i][j].width - diametre) / 2;
						int posY = cases[i][j].y + (cases[i][j].height - diametre) / 2;
						// Dessin du cercle jaune
						fenetre.dessinerOvale(g,Motus.JAUNE,posX,posY,diametre,diametre);
						break;
					case 2: // Lettre présente à la bonne place
						fenetre.dessinerRectangle(g,Motus.ROUGE,cases[i][j]); // Case rouge
						break;
				}
			}
		}
	}

	public void viderLigne(int i) {
		Arrays.fill(couleursCases[i],0);
		Arrays.fill(lettresCases[i],'.');
	}

	public void afficherLettres(Graphics g, Font font) {
		g.setColor(Color.WHITE);
		g.setFont(font);
		FontMetrics fm = g.getFontMetrics();
		for(int i = 0; i < ligneActuelle + 1; i++) {
			for(int j = 0; j < largeur; j++) {
				char lettreC = lettresCases[i][j];
				if(lettreC != '\0') {
					String lettreS = String.valueOf(lettreC);
					int largeurLettre = fm.stringWidth(lettreS);
					int hauteurLettre = fm.getHeight();

					int posX = cases[i][j].x + (cases[i][j].width - largeurLettre) / 2;
					int posY = cases[i][j].y + (cases[i][j].height - hauteurLettre) / 2 + fm.getAscent();
					g.drawString(lettreS, posX, posY);
				}
			}
		}
	}

	@Override
	public boolean equals(Object obj) {
		if(this == obj) return true;
		if(obj == null || getClass() != obj.getClass()) return false;

		Grille grille = (Grille) obj;

		return this.mot.equals(grille.mot)
			&& this.hauteur == grille.hauteur
			&& this.largeur == grille.largeur
			&& this.ligneActuelle == grille.ligneActuelle
			&& Arrays.deepEquals(this.cases,grille.cases)
			&& Arrays.deepEquals(this.couleursCases,grille.couleursCases)
			&& Arrays.deepEquals(this.lettresCases,grille.lettresCases)
			&& Arrays.equals(this.resultat,grille.resultat)
			&& this.resolue == grille.resolue;
	}

	@Override
	public int hashCode() {
		int result = Objects.hash(mot,hauteur,largeur,ligneActuelle,resolue);
		result = 31 * result + Arrays.deepHashCode(cases);
		result = 31 * result + Arrays.deepHashCode(couleursCases);
		result = 31 * result + Arrays.deepHashCode(lettresCases);
		result = 31 * result + Arrays.hashCode(resultat);
		return result;
	}
}