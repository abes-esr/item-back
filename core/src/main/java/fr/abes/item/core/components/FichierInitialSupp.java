package fr.abes.item.core.components;


import fr.abes.item.core.constant.TYPE_DEMANDE;
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
}
