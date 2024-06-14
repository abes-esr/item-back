package fr.abes.item.core.components;


import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@NoArgsConstructor
public class FichierResultat extends AbstractFichier implements Fichier {
	
	@Autowired
	public FichierResultat(@Value("") final String filename){
		this.filename = filename;
	}
 
	@Override
	public int getType() {
		return Constant.ETATDEM_ENCOURS;
	}

	@Override
	public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.MODIF; }

	@Override
	public void checkFileContent(Demande demande) {
		//nothing to do
	}

	@Override
	public void generateFileName(Integer numDemande) {
		this.filename = Constant.FIC_RESULTAT_NAME + numDemande + Constant.EXTENSIONCSV;
		
	}

}
