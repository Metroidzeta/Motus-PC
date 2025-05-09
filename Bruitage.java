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

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

public class Bruitage {

	private static final String DOSSIER = "bruitages";

	private final Clip son;

	public Bruitage(String nomFichier) {
		validerArguments(nomFichier);
		son = initClip(nomFichier);
		if (son == null) throw new IllegalStateException("Impossible de charger le fichier audio : " + nomFichier);
	}

	private static void validerArguments(String nomFichier) {
		if (nomFichier == null || nomFichier.isBlank()) throw new IllegalArgumentException("nomFichier null ou vide");
	}

	private Clip initClip(String nomFichier) {
		Path chemin = Paths.get(DOSSIER, nomFichier);
		try (AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(chemin.toFile())) { // try-with-resources
			Clip clip = AudioSystem.getClip();
			clip.open(audioInputStream);
			return clip;
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			System.err.println("Erreur lors du chargement du bruitage : " + chemin);
			e.printStackTrace();
			return null;
		}
	}

	/*** Autre méthodes ***/
	public void play() {
		if (son != null && son.isOpen()) {
			son.setFramePosition(0); // positionner le lecteur au début
			son.start(); // jouer le bruitage
		}
	}

	public void stop() {
		if (son != null && son.isRunning()) son.stop();
	}

	public boolean isRunning() {
        return son != null && son.isRunning();
    }

	public void close() {
		if (son != null && son.isOpen()) son.close();
	}
}