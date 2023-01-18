package fr.abes.item.components;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.utilitaire.Utilitaires;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class FichierInitial extends AbstractFichier implements Fichier {
    private int ligneCourante;

    @Autowired
    public FichierInitial(@Value("") final String filename) {
        this.filename = filename;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_PREPARATION;
    }


    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.MODIF;
    }

    @Override
    public void checkFileContent(Demande demandeModif) throws FileCheckingException, IOException {
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            String ligne;
            this.ligneCourante = 1;

            while ((ligne = Utilitaires.checkBom(bufLecteur.readLine())) != null) {
                checkBodyLine(ligne);
            }

            //cas ou il y a trop de lignes dans le fichier
            if ((ligneCourante - 1) > Constant.MAX_LIGNE_FICHIER_INIT_MODIF) {
                throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_TOOMUCH_MODIF);
            }

        }

    }


    /**
     * Méthode de vérification d'une ligne du corps du fichier initial
     *
     * @param ligne
     * @throws FileCheckingException
     */
    private void checkBodyLine(String ligne) throws FileCheckingException {
        if (ligne.length() != 9) {
            throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_ONLYONEPPN);
        }
        ligneCourante++;
    }

    @Override
    public void generateFileName(Integer numDemande) {
        this.filename = Constant.FIC_INITIAL_NAME + numDemande + Constant.EXTENSIONTXT;
    }

    public void supprimerRetourChariot() throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) if (!line.trim().isEmpty()) {
                sb.append(line + System.getProperty("line.separator"));
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(path.resolve(this.filename).toString()))) {
                out.write(sb.toString());
            }

        }
    }
}
