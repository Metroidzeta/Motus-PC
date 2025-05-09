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

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Dictionnaire {

	private static final String DOSSIER = "listesMots";

	private final Set<String> mots = new HashSet<>();

	public Dictionnaire(String nomFichier) {
		validerArguments(nomFichier);
		extraireListeMot(nomFichier);
	}

	private static void validerArguments(String nomFichier) {
		if (nomFichier == null || nomFichier.isBlank()) throw new IllegalArgumentException("nomFichier null ou vide");
	}

	private void extraireListeMot(String nomFichier) {
		Path chemin = Paths.get(DOSSIER, nomFichier);
		try (BufferedReader reader = Files.newBufferedReader(chemin)) { // try-with-resources
			String ligne = reader.readLine(); // lire la première ligne
			if (ligne != null) {
				String[] motsExtraits = ligne
					.replaceAll("[ \"\\[\\]]", "") // enlever tous les espaces, les "\"", "[" et "]"
					.split(","); // séparer chaque mot par une virgule
				for (String mot : motsExtraits) {
					mots.add(mot.trim().toUpperCase()); // ajouter chaque mot en majuscules
				}
			}
		} catch (IOException e) {
			System.err.println("Erreur lors de la lecture du fichier : " + chemin);
			e.printStackTrace();
		}
	}

	/*** Getters ****/
	public Set<String> getMots() { return mots; }

	/*** Autres méthodes ***/
	public boolean contains(String mot) {
		return mots.contains(mot);
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (!(o instanceof Dictionnaire dictionnaire)) return false;

		return mots.equals(dictionnaire.mots);
	}

	@Override
	public int hashCode() { return mots.hashCode(); }
}