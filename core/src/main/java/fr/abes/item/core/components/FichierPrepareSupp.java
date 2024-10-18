package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

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

	public void controleIntegriteDesCorrespondances() throws IOException, FileCheckingException {
		List<String> epnSansCorrespondance = new ArrayList<>();
		try (FileReader fileReader = new FileReader(path.resolve(filename).toString());
			 BufferedReader reader = new BufferedReader(fileReader);) {
			reader.readLine();//cette ligne enleve le header
			reader.lines().forEach(line -> {
				if(line.split(";")[0].isEmpty()){
					epnSansCorrespondance.add(line.split(";")[2]);
				}
			});
		}
		if (!epnSansCorrespondance.isEmpty()) {
			throw new FileCheckingException("EPN sans correspondance : " + String.join(", ", epnSansCorrespondance));
		}
	}

}
