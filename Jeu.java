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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Scanner;
import java.util.regex.Pattern;
import java.io.InputStream;
import java.text.Normalizer;
import javax.swing.JPanel;

public class Jeu {

	private ArrayList<HashSet<String>> listeMotsXlettres; // de 6 à 10 lettres possibles
	private Partie partie;
	private float tempsRestant;
	private Font[] lesFonts;
	private Bruitage[] lesBruitages;
	private JPanel panel;
	private Fenetre fenetre;

	public Jeu() {
		verificationsArgs();
		this.listeMotsXlettres = new ArrayList<>(5);
		for(int i = 0; i < 5; i++) {
			this.listeMotsXlettres.add(new HashSet<>());
			enregistrerListeMot(this.listeMotsXlettres.get(i),"listesMots/listeMots" + (i + 6) + ".json");
		}
		int indiceListeMots = Motus.NB_LETTRES - 6;
		this.partie = new Partie(listeMotsXlettres.get(indiceListeMots),Motus.NB_GRILLES);
		this.tempsRestant = Motus.TEMPS * 10;
		this.lesFonts = new Font[2];
		int firstfontSize = (int) (Math.min(Motus.WINDOW_WIDTH,Motus.WINDOW_HEIGHT) * 0.03);
		int secondfontSize = (int) (Math.min(Motus.WINDOW_WIDTH,Motus.WINDOW_HEIGHT) * 0.1);
		this.lesFonts[0] = new Font("Arial",Font.PLAIN,firstfontSize);
		this.lesFonts[1] = new Font("Arial",Font.PLAIN,secondfontSize);
		this.lesBruitages = new Bruitage[6];
		this.lesBruitages[0] = new Bruitage("bruitages/mauvaiselettre.wav");
		this.lesBruitages[1] = new Bruitage("bruitages/lettrejaune.wav");
		this.lesBruitages[2] = new Bruitage("bruitages/bonnelettre.wav");
		this.lesBruitages[3] = new Bruitage("bruitages/mauvaismot.wav");
		this.lesBruitages[4] = new Bruitage("bruitages/reussi.wav");
		this.lesBruitages[5] = new Bruitage("bruitages/gameover.wav");
		this.panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				fenetre.dessinerFondNoir(g);
				partie.getGrilleActuelle().afficherCouleursCases(g,fenetre);
				partie.getGrilleActuelle().afficherLettres(g,lesFonts[1]);
				String msgErr = fenetre.getMsgErr();
				if(msgErr != null) {
					fenetre.dessinerTexte(g,Color.RED,lesFonts[0],msgErr,(int) (Motus.WINDOW_WIDTH * 0.15),(int) (Motus.WINDOW_HEIGHT * 0.98));
				}
				fenetre.dessinerTexte(g,Color.WHITE,lesFonts[0],String.format("%.1f",tempsRestant / 10),(int) (Motus.WINDOW_WIDTH * 0.80),(int) (Motus.WINDOW_HEIGHT * 0.98));
			}
		};
		this.fenetre = new Fenetre("Motus",panel);
	}

	private void verificationsArgs() {
		if(Motus.WINDOW_WIDTH < 400) { throw new IllegalArgumentException("Le WINDOW_WIDTH (largeur fenetre) < 400"); }
		if(Motus.WINDOW_HEIGHT < 400) { throw new IllegalArgumentException("Le WINDOW_HEIGHT (hauteur fenetre) < 400"); }
		if(Motus.NB_LETTRES < 6 || Motus.NB_LETTRES > 10) { throw new IllegalArgumentException("Le NB_LETTRES est < 6 ou > 10"); }
		if(Motus.TEMPS < 5) { throw new IllegalArgumentException("Le TEMPS < 5 secondes"); }
	}

	private void enregistrerListeMot(HashSet<String> hs, String chemin) {
		try(InputStream inputStream = getClass().getResourceAsStream(chemin);
			Scanner myReader = new Scanner(inputStream)) { // try-with-resources
			String data = myReader.nextLine(); // On récupère la première ligne
			data = data.replaceAll("[ \"\\[\\]]", ""); // On enlève tous les espaces, les "\"", les "[" et les "]"
			String[] mots = data.split(","); // On split les mots à chaque virgule et on met tout dans un tableau

			for(String mot : mots) { // On met tous les mots en majuscules dans un Hashset
				hs.add(mot.toUpperCase());
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	private boolean contientAccents(String mot) {
		String decomposed = Normalizer.normalize(mot,Normalizer.Form.NFD);
		Pattern pattern = Pattern.compile("\\p{M}");
		return pattern.matcher(decomposed).find();
	}

	private boolean dansDictionnaire(String mot, int tailleMot) {
		return listeMotsXlettres.get(tailleMot - 6).contains(mot);
	}

	private void attendre(long millisSecondes) {
		try { Thread.sleep(millisSecondes); } catch (InterruptedException ie) {}
	}

	private void jouerBruitageEtAttendre(int numBruitage, long millisSecondes) {
		lesBruitages[numBruitage].play();
		attendre(millisSecondes);
		lesBruitages[numBruitage].stop();
	}

	private void grilleReussie() {
		System.out.println("Bravo, le mot a été trouvé");
		jouerBruitageEtAttendre(4,5000); // On attend 5 secondes
	}

	private void grilleEchec(Grille grille, int tour) {
		System.out.println("Echec, le mot n'a pas ete trouve");
		char[] motMystere = grille.getMot().toCharArray();
		jouerBruitageEtAttendre(3,3000); // On attend 3 secondes
		grille.viderLigne(tour - 1); // On efface entièrement la dernière ligne
		int tailleMot = motMystere.length;
		for(int i = 0; i < tailleMot; i++) { // On donne finalement la réponse qui était attendu
			grille.setCouleursCases(tour - 1,i,2);
			grille.setLettresCases(tour - 1,i,motMystere[i]);
			panel.repaint();
			jouerBruitageEtAttendre(2,250); // On attend 0.25 seconde entre chaque lettre affichée
		}
		jouerBruitageEtAttendre(5,3000); // On attend 3 secondes
	}

	public void jouer() {
		fenetre.rendreVisible();
		for(Grille grille : partie.getGrilles()) {
			int tour = 0;
			int nbEssais = grille.getHauteur();
			int tailleMot = grille.getLargeur();
			System.out.println("Mot mystere = " + grille.getMot());
			while(!grille.resolue() && tour < nbEssais) {
				grille.setLigneActuelle(tour);
				grille.preRemplirLigne();
				panel.repaint();
				tempsRestant = Motus.TEMPS * 10; // Temps converti en dixièmes de seconde
				String motSaisi = "";
				fenetre.actionPossible();
				while(tempsRestant > 0) {
					if(fenetre.getBoutonAppuye()) {
						motSaisi = fenetre.getMotSaisi().toUpperCase(); // Mot transformé en majuscules
						if(motSaisi.length() != tailleMot) {
							fenetre.setMsgErr("Le mot doit avoir une taille de " + tailleMot + " lettres");
							fenetre.actionPossible();
						} else if(contientAccents(motSaisi)) {
							fenetre.setMsgErr("Vous ne devez pas écrire les accents");
							fenetre.actionPossible();
						} else if(!dansDictionnaire(motSaisi,tailleMot)) {
							fenetre.setMsgErr("Ce mot n'existe pas dans le dictionnaire");
							fenetre.actionPossible();
						} else if(grille.getLesMotsSaisis().contains(motSaisi)) {
							fenetre.setMsgErr("Ce mot a déjà été utilisé");
							fenetre.actionPossible();
						} else {
							break;
						}
					}
					attendre(100); // On attend 0.1 seconde après chaque vérification
					tempsRestant--; // - 0.1 sec (tempsRestant = centièmes de seconde)
					panel.repaint();
				}
				fenetre.setMsgErr(null);
				tour++;
				if(tempsRestant == 0) {
					System.out.println("Temps ecoule");
					fenetre.actionImpossible();
					break;
				}
				grille.insererMot(motSaisi);
				System.out.print("motSaisi : " + motSaisi + " | ");
				panel.repaint();
				for(int i = 0; i < tailleMot; i++) {
					int numCouleur = grille.colorierLettre(i);
					System.out.print(numCouleur == 2 ? "R" : numCouleur == 1 ? "J" : "B");
					panel.repaint();
					jouerBruitageEtAttendre(numCouleur,250); // On attend 0.25 seconde entre chaque lettre affichée
				}
				System.out.println();
			}

			if(grille.resolue()) { // Le mot à été trouvé
				 grilleReussie();
			} else { // Le mot n'a pas été trouvé
				grilleEchec(grille,tour);
			}
			partie.setIndiceGrilleActuelle(partie.getIndiceGrilleActuelle() + 1);
		}
		System.exit(0);
	}
}