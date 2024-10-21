package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class FichierPrepareSupp extends FichierPrepare implements Fichier {

	public FichierPrepareSupp(@Value("") final String filename) {
		super(filename);
	}

	@Override
	public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.SUPP; }

	@Override
	public void generateFileName(Demande demande) {
		this.filename = Constant.FIC_CORRESPONDANCE_NAME + demande.getId() + Constant.EXTENSIONCSV;
	}
}
