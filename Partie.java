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

import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.Random;

public class Partie {

	private List<Grille> grilles;
	private int indiceGrilleActuelle = 0;

	public Partie(Set<String> mots, int nbGrilles) {
		validerArguments(mots, nbGrilles);
		grilles = new ArrayList<>(nbGrilles);

		List<String> list = new ArrayList<>(mots); // convertit le set en liste pour l'accès par index
		int tailleListe = list.size();
		Random random = new Random();

		for (int i = 0; i < nbGrilles; i++) {
			int index = random.nextInt(tailleListe - i);
			String mot = list.remove(index); // récupérer et supprimer l'élément sélectionné
			grilles.add(new Grille(mot, Motus.NB_ESSAI));
		}
	}

	private static void validerArguments(Set<String> mots, int nbGrilles) {
		if (mots == null) throw new NullPointerException("Le set de mots est null");
		if (mots.isEmpty()) throw new IllegalArgumentException("Le set de mots est vide");
		if (nbGrilles < 1) throw new IllegalArgumentException("Le nombre de grilles de la partie doit être >= 1");
		if (nbGrilles > mots.size()) throw new IllegalArgumentException("Pas assez de mots pour générer " + nbGrilles + " grilles uniques");
	}

	/*** Getters ***/
	public List<Grille> getGrilles() { return grilles; }
	public Grille getGrilleActuelle() { return grilles.get(indiceGrilleActuelle); }
	public int getIndiceGrilleActuelle() { return indiceGrilleActuelle; }

	/*** Setter ***/
	public void setIndiceGrilleActuelle(int i) { indiceGrilleActuelle = i; }
}