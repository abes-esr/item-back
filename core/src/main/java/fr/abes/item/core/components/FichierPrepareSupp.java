package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		try (FileInputStream fileReader = new FileInputStream(path.resolve(filename).toString());
			BufferedReader reader = new BufferedReader(new InputStreamReader(fileReader, StandardCharsets.UTF_8))) {

			reader.readLine();//cette ligne enleve le header et le stoc

			reader.lines().forEach(line -> {
				Matcher m = Pattern.compile("^(?<PPN>\\d{8}[0-9X]);(?<RCR>\\d{9});(?<EPN>\\d{8}[0-9X])?;$").matcher(line);
				if (m.group("PPN").isEmpty()) {
					epnSansCorrespondance.add(m.group("EPN"));
				}
			});
		}
		if (!epnSansCorrespondance.isEmpty()) {
			throw new FileCheckingException("EPN sans correspondance : " + String.join(", ", epnSansCorrespondance));
		}
	}

}
