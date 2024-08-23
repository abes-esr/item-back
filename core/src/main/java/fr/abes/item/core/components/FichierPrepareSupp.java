package fr.abes.item.core.components;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FichierPrepareSupp extends FichierPrepare implements Fichier {

	public FichierPrepareSupp(@Value("") final String filename) {
		super(filename);
	}

	@Override
	public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.SUPP; }
}
