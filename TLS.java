import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.FileVisitor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;

public class TLS {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.out.println("Utilisation : java TLS <chemin-du-dossier>");
            System.exit(1);
        }

        String folderPath = args[0];

        // Crée une liste pour stocker les données CSV
        List<String[]> csvData = new ArrayList<>();
        // Ajoute l'en-tête CSV
        csvData.add(new String[] { "Chemin du fichier", "Nom du paquet", "Nom de la classe", "TLOC", "TASSERT", "TCMP" });

        // Parcourir le dossier de manière récursive pour trouver les fichiers Java de test
        Files.walkFileTree(Paths.get(folderPath), new TestFileVisitor(csvData));

        // Spécifie le chemin du fichier de sortie (optionnel)
        String outputPath = "output.csv";

        // Écrit les données dans un fichier CSV ou les affiche à la console
        try (CSVPrinter printer = new CSVPrinter(new FileWriter(outputPath), CSVFormat.DEFAULT)) {
            printer.printRecords(csvData);
        }

        System.out.println("Données CSV générées avec succès.");
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

        // Extrait le nom du paquet à partir du fichier Java
        private String extractPackageName(String filePath) throws IOException {
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                Pattern packagePattern = Pattern.compile("package\\s+(.*?);");
                while ((line = reader.readLine()) != null) {
                    Matcher matcher = packagePattern.matcher(line);
                    if (matcher.find()) {
                        return matcher.group(1);
                    }
                }
            }
            return "";
        }

        // Extrait le nom de la classe à partir du nom du fichier Java
        private String extractClassName(String filePath) {
            String fileName = new File(filePath).getName();
            return fileName.substring(0, fileName.lastIndexOf("."));
        }

        // Calcule le TLOC (nombre de lignes de code) du fichier
        private int calculateTLOC(String filePath) throws IOException {
            int tloc = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (!line.trim().isEmpty()) {
                        tloc++;
                    }
                }
            }
            return tloc;
        }

        // Calcule le TASSERT (nombre d'assertions) du fichier
        private int calculateTASSERT(String filePath) throws IOException {
            int tassert = 0;
            try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.contains("assert")) {
                        tassert++;
                    }
                }
            }
            return tassert;
        }
    }
}
