import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class Tropcomp {
    public static void main(String[] args) throws IOException {
        if (args.length < 3) {
            System.out.println("Utilisation : java Tropcomp <chemin-du-dossier> <seuil> [-o <chemin-de-sortie.csv>]");
            System.exit(1);
        }

        String folderPath = args[0];
        double seuil = Double.parseDouble(args[1]);

        // Liste pour stocker les données CSV
        List<String[]> csvData = new ArrayList<>();
        // Ajoute l'en-tête CSV
        csvData.add(new String[] { "Chemin du fichier", "Nom du paquet", "Nom de la classe", "TLOC", "TASSERT", "TCMP" });

        // Parcourir le dossier de manière récursive pour trouver les fichiers Java de test
        Files.walkFileTree(Paths.get(folderPath), new TestFileVisitor(csvData));

        // Trier les données CSV par TCMP (ordre décroissant)
        csvData.sort(Comparator.comparingDouble(row -> -Double.parseDouble(row[5])));

        // Calculer le seuil (nombre de classes à inclure)
        int seuilIndex = (int) (seuil * csvData.size());

        // Récupérer les classes suspectes
        List<String[]> suspectClasses = csvData.subList(0, seuilIndex);

        // Spécifier le chemin du fichier de sortie (optionnel)
        String outputPath = "output.csv";

        // Écrire les classes suspectes dans un fichier CSV ou les afficher à la console
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath), CSVFormat.DEFAULT)) {
            printer.printRecords(suspectClasses);
        }

        System.out.println("Classes suspectes générées avec succès.");
    }

    // Classe interne pour visiter les fichiers
    private static class TestFileVisitor extends SimpleFileVisitor<Path> {
        private List<String[]> csvData;

        public TestFileVisitor(List<String[]> csvData) {
            this.csvData = csvData;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
            if (file.toString().endsWith(".java")) {
                String filePath = file.toString();
                String packageName = extractPackageName(filePath);
                String className = extractClassName(filePath);
                int tloc = calculateTLOC(filePath);
                int tassert = calculateTASSERT(filePath);
                double tcmp = tloc / (double) tassert;

                // Ajoute les données du fichier au CSV
                csvData.add(new String[] { filePath, packageName, className, String.valueOf(tloc),
                        String.valueOf(tassert), String.format("%.2f", tcmp) });
            }

            return FileVisitResult.CONTINUE;
        }

        // Méthodes d'extraction et de calcul (déjà existantes dans le programme TLS)
    }
}
