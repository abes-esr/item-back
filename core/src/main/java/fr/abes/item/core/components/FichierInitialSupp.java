package fr.abes.item.core.components;


import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.utilitaire.Utilitaires;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

@Component
public class FichierInitialSupp extends FichierInitial implements Fichier {
    private int ligneCourante;
    public FichierInitialSupp(@Value("") final String filename) {
        super(filename);
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void checkFileContent(Demande demande) throws FileCheckingException, IOException {
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            String ligne;
            this.ligneCourante = 0;

            while ((ligne = Utilitaires.checkBom(bufLecteur.readLine())) != null) {
                checkBodyLine(ligne);
            }

            //cas ou il y a trop de lignes dans le fichier
            if ((ligneCourante + 1) > Constant.MAX_LIGNE_FICHIER_INIT_SUPP) {
                throw new FileCheckingException(ligneCourante, Constant.ERR_FILE_TOOMUCH_SUPP);
            }
        }
    }

    /**
     * Méthode de vérification d'une ligne du corps du fichier initial
     *
     * @param ligne : ligne à vérifier
     * @throws FileCheckingException : erreur dans la format de la ligne
     */
    private void checkBodyLine(String ligne) throws FileCheckingException {
        if (ligne.length() != 9) {
            throw new FileCheckingException(Constant.ERR_FILE_ERRLINE + ligneCourante + Constant.ERR_FILE_ONLYONEPPN);
        }
        ligneCourante++;
    }
}
