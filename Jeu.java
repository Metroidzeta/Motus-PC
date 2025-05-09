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
import java.util.regex.Pattern;
import java.text.Normalizer;
import javax.swing.JPanel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Jeu {

	private static final float TEMPS_INITIAL = Motus.TEMPS * 10;
	private static final String[] BRUITAGE_NOMS = {"mauvaiselettre", "lettrejaune", "bonnelettre", "mauvaismot", "reussi", "gameover"};
	private static final int MIN_WINDOW_SIZE = 400;

	private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private final ArrayList<Dictionnaire> dictionnaires = new ArrayList<>(5);
	private final ArrayList<Font> polices = new ArrayList<>(2);
	private final ArrayList<Bruitage> bruitages = new ArrayList<>(6);
	private Partie partie;
	private float tempsRestant;
	private JPanel panel;
	private Fenetre fenetre;

	public Jeu() {
		validerArguments();
		chargerDictionnaires();
		initPartie();
		initUI();
		chargerBruitages();
	}

	private static void validerArguments() {
		if (Motus.WINDOW_WIDTH < MIN_WINDOW_SIZE) throw new IllegalArgumentException("WINDOW_WIDTH trop petite (largeur fenetre)");
		if (Motus.WINDOW_HEIGHT < MIN_WINDOW_SIZE) throw new IllegalArgumentException("WINDOW_HEIGHT trop petite (hauteur fenetre)");
		if (Motus.NB_LETTRES < 6 || Motus.NB_LETTRES > 10) throw new IllegalArgumentException("NB_LETTRES doit être entre 6 et 10");
		if (Motus.TEMPS < 5) throw new IllegalArgumentException("Temps trop court (< 5 sec)");
	}

	private void chargerDictionnaires() {
		for (int i = 0; i < 5; i++) {
			dictionnaires.add(new Dictionnaire("listeMots" + (i + 6) + ".json"));
		}
	}

	private void initPartie() {
		int index = Motus.NB_LETTRES - 6;
		partie = new Partie(dictionnaires.get(index).getMots(), Motus.NB_GRILLES);
		tempsRestant = TEMPS_INITIAL;
	}

	private void initUI() {
		int minSize = Math.min(Motus.WINDOW_WIDTH, Motus.WINDOW_HEIGHT);
		polices.add(new Font("Arial", Font.PLAIN, (int) (minSize * 0.03)));
		polices.add(new Font("Arial", Font.PLAIN, (int) (minSize * 0.1)));
		panel = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				dessiner(g);
			}
		};
		fenetre = new Fenetre("Motus", panel);
	}

	private void dessiner(Graphics g) {
		fenetre.dessinerFondNoir(g);
		Grille grille = partie.getGrilleActuelle();
		grille.dessinerCouleursCases(g, fenetre);
		grille.dessinerLettres(g, polices.get(1), fenetre);

		String msgErr = fenetre.getMsgErr();
		if (msgErr != null) {
			fenetre.dessinerTexte(g, Color.RED, polices.get(0), msgErr,
					(int) (Motus.WINDOW_WIDTH * 0.15),
					(int) (Motus.WINDOW_HEIGHT * 0.98)
			);
		}

		fenetre.dessinerTexte(g, Color.WHITE, polices.get(0), String.format("%.1f", tempsRestant / 10),
				(int) (Motus.WINDOW_WIDTH * 0.80),
				(int) (Motus.WINDOW_HEIGHT * 0.98)
		);
	}

	private void chargerBruitages() {
		for (String nom : BRUITAGE_NOMS) {
			bruitages.add(new Bruitage(nom + ".wav"));
		}
	}

	private boolean contientAccents(String mot) {
		return Pattern.compile("\\p{M}").matcher(Normalizer.normalize(mot, Normalizer.Form.NFD)).find();
	}

	private boolean dansDictionnaire(String mot) {
		int index = mot.length() - 6;
		return dictionnaires.get(index).contains(mot);
	}

	private void attendre(long ms) {
		try { Thread.sleep(ms); } catch (InterruptedException ie) { Thread.currentThread().interrupt(); }
	}

	private void attendreEtJouerBruitage(int num, long ms) {
		try {
			CountDownLatch latch = new CountDownLatch(1); // synchroniser l'attente
			bruitages.get(num).play();

			// Planifie l'arrêt du bruitage après 'ms' millisecondes et signale la fin de l'attente
			scheduler.schedule(() -> {
				bruitages.get(num).stop(); // arrete le bruitage après la durée spécifiée
				latch.countDown(); // attente terminée
			}, ms, TimeUnit.MILLISECONDS);

			latch.await(); // bloque thread principal jusqu'à ce que countDown() soit appelé
		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
		}
	}

	private void afficherReussite() {
		System.out.println("Bravo, le mot a été trouvé");
		attendreEtJouerBruitage(4, 5000); // attendre 5 secondes
	}

	private void afficherEchec(Grille grille) {
		System.out.println("Echec, le mot n'a pas été trouvé");
		char[] mot = grille.getMot().toCharArray();
		int tour = grille.getTour();

		attendreEtJouerBruitage(3, 3000); // attendre 3 secondes
		grille.viderLigne(tour); // efface la dernière ligne

		for (int j = 0; j < mot.length; j++) { // donner la réponse attendue
			grille.setCouleursCases(tour, j , 2);
			grille.setLettresCases(tour, j , mot[j]);
			panel.repaint();
			attendreEtJouerBruitage(2, 250); // attendre 0.25 seconde
		}

		attendreEtJouerBruitage(5, 3000); // attendre 3 secondes
	}

	public void jouer() {
		for (Grille grille : partie.getGrilles()) {
			int tour = 0;
			int maxTours = grille.getHauteur();
			int tailleMot = grille.getLargeur();

			System.out.println("Mot mystère = " + grille.getMot());

			while (!grille.resolue() && tour < maxTours) {
				grille.setTour(tour);
				grille.initLigne();
				tempsRestant = TEMPS_INITIAL;
				fenetre.debloquerSaisie();
				panel.repaint();

				String motSaisi = attendreSaisie(tailleMot, grille);
                fenetre.setMsgErr(null);

				if (motSaisi == null) {
					fenetre.bloquerSaisie();
					System.out.println("Temps écoulé");
					break;
				}

				grille.insererMot(motSaisi);
				System.out.print("mot saisi : " + motSaisi + " | ");
				panel.repaint();

				for (int i = 0; i < tailleMot; i++) {
					int numCouleur = grille.colorierLettre(i);
					System.out.print(numCouleur == 2 ? "R" : (numCouleur == 1 ? "J" : "B"));
					panel.repaint();
					attendreEtJouerBruitage(numCouleur, 250); // attendre 0.25 seconde
				}

				System.out.println();
				tour++;
			}

			if (grille.resolue()) afficherReussite(); // le mot a été trouvé
			else afficherEchec(grille); // le mot n'a pas été trouvé

			partie.setIndiceGrilleActuelle(partie.getIndiceGrilleActuelle() + 1);
		}
		scheduler.shutdown(); // arrêter le scheduler
		fermerBruitages();
		System.exit(0);
	}

	private String attendreSaisie(int tailleMot, Grille grille) {
		while (tempsRestant > 0) {
			if (fenetre.getBtnValiderAppuye()) {
				String mot = fenetre.getMotSaisi().toUpperCase();
				String msgErr = validerMot(mot, tailleMot, grille);

				if (msgErr == null) return mot;
				fenetre.setMsgErr(msgErr);
				fenetre.debloquerSaisie();
			}
			attendre(100);
			tempsRestant--;
			panel.repaint();
		}
		return null;
	}

	private String validerMot(String mot, int tailleMot, Grille grille) {
		if (mot.length() != tailleMot) return "Le mot doit avoir une taille de " + tailleMot + " lettres";
		if (contientAccents(mot)) return "Les accents sont interdits";
		if (!dansDictionnaire(mot)) return "Mot inconnu du dictionnaire";
		if (grille.getLesMotsSaisis().contains(mot)) return "Mot déjà utilisé";
		return null;
	}

	private void fermerBruitages() {
		bruitages.forEach(Bruitage::close);
	}
}