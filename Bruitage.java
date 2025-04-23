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

import java.io.IOException;
import java.net.URL;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Bruitage {

	private Clip son;

	public Bruitage(String cheminFichier) {
		try {
			URL audioFileURL = getClass().getResource(cheminFichier);
			if (audioFileURL != null) {
				try(AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audioFileURL)) { // try-with-resources
					son = AudioSystem.getClip();
					son.open(audioInputStream);
				}
			} else {
				System.err.println("Le fichier audio spécifié est introuvable : " + cheminFichier);
			}
		} catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
			e.printStackTrace();
		}
	}

	public void play() {
		if (son != null) {
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
		if (son != null) son.close();
	}
}