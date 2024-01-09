/* Auteur du projet : Metroidzeta
	Pour compiler avec Windows, GNU/Linux et MacOS :
		> javac *.java
	Pour exécuter :
		> java Motus
	Pour créer un jar de l'application :
		> jar cvmf MANIFEST.MF Motus.jar *.class bruitages/* listesMots/*
*/

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;

public class Partie {

	private ArrayList<Grille> grilles;
	private int indiceGrilleActuelle;

	public Partie(HashSet<String> hs, int nbGrilles) {
		verificationsArgs(hs,nbGrilles);
		this.grilles = new ArrayList<Grille>(nbGrilles);
		this.indiceGrilleActuelle = 0;
		ArrayList<String> list = new ArrayList<>(hs); // Création d'une ArrayList avec le contenu de l'HashSet
		int tailleListe = list.size();
		Random random = new Random();
		for(int i = 0; i < nbGrilles; i++) {
			int numAleatoire = random.nextInt(tailleListe - i);
			String motAleatoire = list.remove(numAleatoire); // Supprimer et récupérer l'élément sélectionné
			this.grilles.add(new Grille(motAleatoire,Motus.NB_ESSAI));
		}
	}

	private void verificationsArgs(HashSet<String> hs, int nbGrilles) {
		if(hs == null) { throw new NullPointerException("Le hashset est null"); }
		if(hs.isEmpty()) { throw new IllegalArgumentException("Le hashset est vide"); }
		if(nbGrilles < 1) { throw new IllegalArgumentException("Le nbGrilles de la partie est < 1"); }
	}

	/*** Les getters ***/
	public ArrayList<Grille> getGrilles() { return grilles; }
	public Grille getGrilleActuelle() { return grilles.get(indiceGrilleActuelle); }
	public int getIndiceGrilleActuelle() { return indiceGrilleActuelle; }

	/*** Les setters ***/
	public int setIndiceGrilleActuelle(int indiceGrilleActuelle) { return this.indiceGrilleActuelle = indiceGrilleActuelle; }
}