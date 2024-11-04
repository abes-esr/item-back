package fr.abes.item.core.components;


import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
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
    public FichierInitialSupp(@Value("") final String filename) {
        super(filename);
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Demande demande) {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        this.filename = Constant.FIC_INITIAL_NAME + demandeSupp.getTypeSuppression().toString().toLowerCase() + "_" + demandeSupp.getId() + Constant.EXTENSIONTXT;
    }

    @Override
    public void checkFileContent(Demande demande) throws FileCheckingException, IOException {
        try (FileInputStream fis = new FileInputStream(path.resolve(filename).toString());
             BufferedReader bufLecteur = new BufferedReader(new InputStreamReader(fis, StandardCharsets.UTF_8))) {
            this.ligneCourante = 1;
            String ligne;
            while ((ligne = Utilitaires.checkBom(bufLecteur.readLine())) != null) {
                checkBodyLine(ligne);
            }

            //cas ou il y a trop de lignes dans le fichier
            if ((ligneCourante - 1) > Constant.MAX_LIGNE_FICHIER_INIT_SUPP) {
                throw new FileCheckingException(Constant.ERR_FILE_WRONGCONTENT);
            }
        }
    }
}
