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

public class Motus {
	public static final int WINDOW_WIDTH = 920;
	public static final int WINDOW_HEIGHT = 720;

	public static final int NB_ESSAIS = 6;
	public static final int NB_LETTRES = 7;
	public static final int NB_GRILLES = 10;
	public static final int TEMPS = 30;

	// Couleurs RVB
	public static final Color BLEU = new Color(10,129,209);
	public static final Color ROUGE = new Color(210,44,44);
	public static final Color JAUNE = new Color(254,197,4);

	public static void main(String[] args) {
		Jeu jeu = new Jeu();
		jeu.jouer();
	}
}