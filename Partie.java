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

	private static final Random random = new Random();

	private final List<Grille> grilles;
	private int indiceGrilleActuelle = 0;

	public Partie(Set<String> mots, int nbGrilles) {
		validerArguments(mots, nbGrilles);
		grilles = new ArrayList<>(nbGrilles);

		List<String> list = new ArrayList<>(mots); // convertit le set en liste pour l'accès par index

		for (int i = 0; i < nbGrilles; i++) {
			int index = random.nextInt(list.size() - i);
			String mot = list.remove(index); // récupérer et supprimer l'élément sélectionné
			grilles.add(new Grille(mot, Motus.NB_ESSAIS));
		}
	}

	private static void validerArguments(Set<String> mots, int nbGrilles) {
		if (mots == null || mots.isEmpty()) throw new IllegalArgumentException("Set de mots null ou vide");
		if (nbGrilles < 1) throw new IllegalArgumentException("NbGrilles partie < 1");
		if (nbGrilles > mots.size()) throw new IllegalArgumentException("Pas assez de mots pour générer " + nbGrilles + " grilles uniques");
	}

	/*** Getters ***/
	public List<Grille> getGrilles() { return grilles; }
	public Grille getGrilleActuelle() { return grilles.get(indiceGrilleActuelle); }
	public int getIndiceGrilleActuelle() { return indiceGrilleActuelle; }

	/*** Setters ***/
	public void setIndiceGrilleActuelle(int i) { indiceGrilleActuelle = i; }
}