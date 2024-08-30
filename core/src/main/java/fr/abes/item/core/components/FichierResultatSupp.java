package fr.abes.item.core.components;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FichierResultatSupp extends FichierResultatModif implements Fichier {
    @Autowired
    public FichierResultatSupp(@Value("") final String filename){
        this.filename = filename;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.SUPP; }
}
