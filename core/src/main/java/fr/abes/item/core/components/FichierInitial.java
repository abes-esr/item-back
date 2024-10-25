package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.utilitaire.Utilitaires;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;

@Component
public class FichierInitial extends AbstractFichier implements Fichier {
    protected int ligneCourante;

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
    public void checkFileContent(Demande demande) throws FileCheckingException, IOException {
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
     * @param ligne : ligne à vérifier
     * @throws FileCheckingException : erreur dans la format de la ligne
     */
    protected void checkBodyLine(String ligne) throws FileCheckingException {
        if (ligne.length() != 9) {
            throw new FileCheckingException(Constant.ERR_FILE_LINE + ligneCourante + " : " + Constant.ERR_FILE_ONLYONEPPN);
        }
        ligneCourante++;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_INITIAL_NAME + demande.getId() + Constant.EXTENSIONTXT;
    }

    public void supprimerRetourChariot() throws IOException {
        String line;
        StringBuilder sb = new StringBuilder();
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader reader = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            while ((line = reader.readLine()) != null) if (!line.trim().isEmpty()) {
                sb.append(line).append(System.getProperty("line.separator"));
            }
            try (BufferedWriter out = new BufferedWriter(new FileWriter(path.resolve(this.filename).toString()))) {
                out.write(sb.toString());
            }

        }
    }
}
