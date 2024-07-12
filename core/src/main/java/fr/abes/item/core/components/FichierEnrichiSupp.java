package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;

import java.io.IOException;

public class FichierEnrichiSupp extends AbstractFichier implements Fichier {
    @Override
    public int getType() {
        return Constant.ETATDEM_ACOMPLETER;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Integer numDemande) {
        this.filename = Constant.FIC_ENRICHI_NAME + numDemande + Constant.EXTENSIONCSV;
    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {

    }
}
