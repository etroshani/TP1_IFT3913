import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class Tassert {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.out.println("Utilisation : java Tassert <fichier_source_test>");
            System.exit(1);
        }

        String fichierSourceTest = args[0];
        int tassert = calculerTassert(fichierSourceTest);
        System.out.println("TASSERT : " + tassert);
    }

    public static int calculerTassert(String fichierSourceTest) {
        int tassert = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(fichierSourceTest))) {
            String ligne;
            while ((ligne = br.readLine()) != null) {
                ligne = ligne.trim();

                // Rechercher des occurrences de méthodes d'assertion JUnit
                if (ligne.contains("assert") && ligne.contains("(") && ligne.endsWith(");")) {
                    tassert++;
                }
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de la lecture du fichier source de test : " + e.getMessage());
            System.exit(1);
        }

        return tassert;
    }
}
