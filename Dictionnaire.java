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

import java.io.InputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Scanner;
import java.util.Set;

public class Dictionnaire {

	private Set<String> mots;

	public Dictionnaire(String cheminFichier) {
		mots = new HashSet<>();
		extraireListeMot(cheminFichier);
	}
	
	/*** Getters ****/
	public Set<String> getMots() { return mots; }

	public boolean contains(String mot) {
		return mots.contains(mot);
	}

	private void extraireListeMot(String cheminFichier) {
		try (InputStream input = Dictionnaire.class.getResourceAsStream(cheminFichier);
			Scanner scanner = new Scanner(input)) { // try-with-resources
			if (scanner.hasNextLine()) {
				String[] motsExtraits = scanner.nextLine() // récupère la première ligne
						.replaceAll("[ \"\\[\\]]", "") // enlever tous les espaces, les "\"", "[" et "]"
						.split(","); // chaque mot est séparé d'une virgule
				for (String mot : motsExtraits) {
					mots.add(mot.trim().toUpperCase()); // chaque mot est inséré dans le set en majuscules
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}