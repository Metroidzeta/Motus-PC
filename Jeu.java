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
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.regex.Pattern;
import java.text.Normalizer;
import javax.swing.JPanel;

public class Jeu {

	private ArrayList<Dictionnaire> dictionnaires = new ArrayList<>(5); // dictionnaires de 6 à 10 lettres
	private Partie partie;
	private final float tempsInitial = Motus.TEMPS * 10; // temps converti en dixièmes de seconde
	private float tempsRestant;
	private Font[] lesFonts = new Font[2];
	private Bruitage[] lesBruitages = new Bruitage[6];
	private JPanel panel;
	private Fenetre fenetre;

	public Jeu() {
		verifierArguments();

		for (int i = 0; i < 5; i++) {
			String fichier = "listesMots/listeMots" + (i + 6) + ".json";
			Dictionnaire dictionnaire = new Dictionnaire(fichier);
			dictionnaires.add(dictionnaire);
		}

		int index = Motus.NB_LETTRES - 6;
		partie = new Partie(dictionnaires.get(index).getMots(), Motus.NB_GRILLES);
		tempsRestant = tempsInitial;

		int firstfontSize = (int) (Math.min(Motus.WINDOW_WIDTH, Motus.WINDOW_HEIGHT) * 0.03);
		int secondfontSize = (int) (Math.min(Motus.WINDOW_WIDTH, Motus.WINDOW_HEIGHT) * 0.1);
		lesFonts[0] = new Font("Arial", Font.PLAIN, firstfontSize);
		lesFonts[1] = new Font("Arial", Font.PLAIN, secondfontSize);
		chargerBruitages();
		panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				fenetre.dessinerFondNoir(g);
				partie.getGrilleActuelle().dessinerCouleursCases(g, fenetre);
				partie.getGrilleActuelle().dessinerLettres(g, lesFonts[1], fenetre);
				String msgErr = fenetre.getMsgErr();
				if(msgErr != null) {
					fenetre.dessinerTexte(g, Color.RED, lesFonts[0], msgErr, // dessiner message d'erreur (string)
						(int) (Motus.WINDOW_WIDTH * 0.15),
						(int) (Motus.WINDOW_HEIGHT * 0.98)
					);
				}
				fenetre.dessinerTexte(g, Color.WHITE , lesFonts[0] , String.format("%.1f", tempsRestant / 10), // dessiner temps restant (string)
					(int) (Motus.WINDOW_WIDTH * 0.80),
					(int) (Motus.WINDOW_HEIGHT * 0.98)
				);
			}
		};
		fenetre = new Fenetre("Motus", panel);
	}

	private static void verifierArguments() {
		if (Motus.WINDOW_WIDTH < 400) throw new IllegalArgumentException("WINDOW_WIDTH (largeur de la fenetre trop petite) < 400");
		if (Motus.WINDOW_HEIGHT < 400) throw new IllegalArgumentException("WINDOW_HEIGHT (hauteur de la fenetre trop petite) < 400");
		if (Motus.NB_LETTRES < 6 || Motus.NB_LETTRES > 10) throw new IllegalArgumentException("NB_LETTRES doit être entre 6 et 10");
		if (Motus.TEMPS < 5) throw new IllegalArgumentException("Temps trop court (< 5 sec)");
	}

	private void chargerBruitages() {
		String[] noms = {"mauvaiselettre", "lettrejaune", "bonnelettre", "mauvaismot", "reussi", "gameover"};
		for (int i = 0; i < noms.length; i++) {
			lesBruitages[i] = new Bruitage("bruitages/" + noms[i] + ".wav");
		}
	}

	private boolean contientAccents(String mot) {
		String norm = Normalizer.normalize(mot, Normalizer.Form.NFD);
		return Pattern.compile("\\p{M}").matcher(norm).find();
	}

	private boolean dansDictionnaire(String mot) {
		return dictionnaires.get(mot.length() - 6).contains(mot);
	}

	private void attendre(long ms) {
		try { Thread.sleep(ms); } catch (InterruptedException ie) {}
	}

	private void jouerBruitageEtAttendre(int num, long ms) {
		lesBruitages[num].play();
		attendre(ms);
		lesBruitages[num].stop();
	}

	private void grilleReussie() {
		System.out.println("Bravo, le mot a été trouvé");
		jouerBruitageEtAttendre(4, 5000); // attendre 5 secondes
	}

	private void grilleEchec(Grille grille) {
		System.out.println("Echec, le mot n'a pas été trouvé");
		char[] mot = grille.getMot().toCharArray();
		int tour = grille.getTour();
		jouerBruitageEtAttendre(3, 3000); // attendre 3 secondes

		grille.viderLigne(tour); // efface la dernière ligne

		for (int j = 0; j < mot.length; j++) { // donner la réponse attendue
			grille.setCouleursCases(tour, j , 2);
			grille.setLettresCases(tour, j , mot[j]);
			panel.repaint();
			jouerBruitageEtAttendre(2, 250); // attendre 0.25 seconde
		}
		jouerBruitageEtAttendre(5, 3000); // attendre 3 secondes
	}

	public void jouer() {
		for (Grille grille : partie.getGrilles()) {
			int tour = 0;
			int nbEssais = grille.getHauteur();
			int tailleMot = grille.getLargeur();
			System.out.println("Mot mystère = " + grille.getMot());

			while (!grille.resolue() && tour < nbEssais) {
				grille.setTour(tour);
				grille.preRemplirLigne();
				tempsRestant = tempsInitial;
				panel.repaint();

				String motSaisi = "";
				fenetre.rendreSaisiePossible();

				while (tempsRestant > 0) {
					if (fenetre.getBoutonAppuye()) {
						motSaisi = fenetre.getMotSaisi().toUpperCase(); // mot transformé en majuscules

						String msg = null;
						if (motSaisi.length() != tailleMot) msg = "Le mot doit avoir une taille de " + tailleMot + " lettres";
						else if (contientAccents(motSaisi)) msg = "Les accents sont interdits";
						else if (!dansDictionnaire(motSaisi)) msg = "Mot inconnu du dictionnaire";
						else if (grille.getLesMotsSaisis().contains(motSaisi)) msg = "Mot déjà utilisé";

						if (msg != null) {
							fenetre.setMsgErr(msg);
							fenetre.rendreSaisiePossible();
						} else {
							break;
						}
					}
					attendre(100); // attendre 0.1 seconde
					tempsRestant--; // - 0.1 sec (tempsRestant = dixièmes de seconde)
					panel.repaint();
				}

				fenetre.setMsgErr(null);

				if (tempsRestant == 0) {
					System.out.println("Temps écoulé");
					fenetre.rendreSaisieImpossible();
					break;
				}

				grille.insererMot(motSaisi);
				System.out.print("mot saisi : " + motSaisi + " | ");
				panel.repaint();

				for (int i = 0; i < tailleMot; i++) {
					int numCouleur = grille.colorierLettre(i);
					System.out.print(numCouleur == 2 ? "R" : (numCouleur == 1 ? "J" : "B"));
					panel.repaint();
					jouerBruitageEtAttendre(numCouleur, 250); // attendre 0.25 seconde
				}
				System.out.println();
				tour++;
			}

			if (grille.resolue()) grilleReussie(); // le mot a été trouvé
			else grilleEchec(grille); // le mot n'a pas été trouvé

			partie.setIndiceGrilleActuelle(partie.getIndiceGrilleActuelle() + 1);
		}
		System.exit(0);
	}
}