package fr.abes.item.core.components;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FichierResultatExemp extends FichierResultat implements Fichier {
    @Autowired
    public FichierResultatExemp(@Value("") final String filename){
        this.filename = filename;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.EXEMP; }
}
