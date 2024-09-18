package fr.abes.item.core.components;


import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

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
        this.filename = Constant.FIC_INITIAL_NAME + demandeSupp.getTypeSuppression() + "_" + demandeSupp.getId() + Constant.EXTENSIONTXT;
    }
}
