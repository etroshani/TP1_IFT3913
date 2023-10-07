import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Tloc {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Utilisation : java Tloc <fichier_source>");
            System.exit(1);
        }

        String fichierSource = args[0];
        int tloc = calculerTloc(fichierSource);
        System.out.println("TLOC : " + tloc);
    }

    public static int calculerTloc(String fichierSource) {
        int tloc = 0;
        boolean enCommentaire = false;

        try (BufferedReader br = new BufferedReader(new FileReader(fichierSource))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                ligne = ligne.trim();

                if (ligne.isEmpty()) {
                    continue; // Ignorer les lignes vides
                }

                if (enCommentaire) {
                    if (ligne.endsWith("*/")) {
                        enCommentaire = false;
                    }
                } else {
                    if (ligne.startsWith("/*")) {
                        enCommentaire = true;
                        if (ligne.endsWith("*/")) {
                            enCommentaire = false;
                        }
                    } else if (!ligne.startsWith("//")) {
                        tloc++; // Compter la ligne comme une ligne de code
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier source : " + e.getMessage());
            System.exit(1);
        }

        return tloc;
    }
}