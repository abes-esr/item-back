package fr.abes.item.core.components;

import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
@Component
public class FichierSauvegardeSupp extends AbstractFichier implements Fichier{
    private List<Pair<String, List<Exemplaire>>> ppnWithExemplairesList = new ArrayList<>();

    private String filename;

    private Path path;

    public void addPpnWithExemplaires(String ppn, List<Exemplaire> exemplaires) {
        Pair<String, List<Exemplaire>> newPair = new Pair<>(ppn, exemplaires);
        this.ppnWithExemplairesList.add(newPair);
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public int getType() {
        return 3; // Supposons que 3 représente le type de fichier de sauvegarde de suppression
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = "sauvegarde_supp_" + demande.getId() + "_" +
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".txt";
    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {
        // Comme c'est un fichier de sauvegarde généré par le système,
        // nous pouvons supposer que le contenu est toujours valide.
        // Cependant, nous pourrions ajouter des vérifications si nécessaire.
        if (ppnWithExemplairesList.isEmpty()) {
            throw new FileCheckingException("La liste des PPN et exemplaires est vide.");
        }
    }

    public void genererFichier(String uploadPath) throws IOException {
        if (this.filename == null) {
            throw new IllegalStateException("Le nom du fichier n'a pas été généré. Appelez generateFileName() d'abord.");
        }

        Path fullPath = this.path.resolve(this.filename);

        try (FileWriter fw = new FileWriter(fullPath.toString(), false);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {

            out.println("Fichier de sauvegarde des suppressions");
            out.println("Date de génération : " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            out.println("Nombre de PPN : " + ppnWithExemplairesList.size());
            out.println("--------------------------------------------");

            for (Pair<String, List<Exemplaire>> ppnWithExemplaires : ppnWithExemplairesList) {
                String ppn = ppnWithExemplaires.key();
                List<Exemplaire> exemplaires = ppnWithExemplaires.value();

                out.println("PPN : " + ppn);
                out.println("Nombre d'exemplaires : " + exemplaires.size());

                for (Exemplaire exemplaire : exemplaires) {
                    out.println("  RCR : " + exemplaire.getNumEx());
                    out.println("  EPN : " + exemplaire.getListeZones().toString());
                    // Ajoutez d'autres informations d'exemplaire si nécessaire
                    out.println("  ----");
                }
                out.println("--------------------------------------------");
            }
        }
    }

    @Override
    public String toString() {
        return "FichierSauvegardeSupp{" +
                "ppnWithExemplairesList=" + ppnWithExemplairesList.stream().toList() +
                ", filename='" + filename + '\'' +
                ", path=" + path +
                '}';
    }

    record Pair<K, V>(K key, V value) {}
}
