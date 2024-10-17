package fr.abes.item.core.components;


import com.google.common.collect.Multimap;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Slf4j
@Component
public class FichierPrepare extends AbstractFichier implements Fichier {

	@Autowired
	public FichierPrepare(@Value("") final String filename) {
		this.filename = filename;
	}

	@Override
	public String getFilename() {
		return this.filename;
	}

	@Override
	public void setFilename(String filename) {
		this.filename = filename;
	}

	@Override
	public int getType() {
		return Constant.ETATDEM_PREPAREE;
	}


	@Override
	public TYPE_DEMANDE getDemandeType() {return TYPE_DEMANDE.MODIF; }

	@Override
	public void checkFileContent(Demande demandeModif) {
	//nothing to do
	}

	@Override
	public void generateFileName(Demande demande) {
		this.filename = Constant.FIC_PREPARE_NAME + demande.getId() + Constant.EXTENSIONCSV;
	}

	/**
	 * Méthode d'écriture de la première ligne dans le fichier
	 */
	public void ecrireEnTete() {
		try (FileWriter fw = new FileWriter(path.resolve(filename).toString(), true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)){
			out.println("PPN;RCR;EPN;");
		} catch (IOException ex) {
			log.error(Constant.ERROR_UNABLE_TO_CREATE_FILE);
		}
	}

	/**
	 * Méthode permetant d'alimenter le fichier à partir d'une chaine correspondant à une liste d'epn
	 * @param input résultat de l'appel à la fonction Oracle
	 * @param rcr rcr de la demandeModif à insérer dans le second champ du fichier
	 */
	public void alimenterEpn(String input, String listeppn, String rcr) {
		try (FileWriter fw = new FileWriter(path.resolve(filename).toString(), true);
				 BufferedWriter bw = new BufferedWriter(fw);
				 PrintWriter out = new PrintWriter(bw)) {
			String[] tabppn = listeppn.split(",");
			Multimap<String, String> resJson = Utilitaires.parseJson(input, false);
            for (String ppn : tabppn) {
                if (resJson.containsKey(ppn)) {
                    for (String epn : resJson.get(ppn)) {
                        out.println(ppn + ";" + rcr + ";" + epn + ";");
                    }
                } else
                    out.println(ppn + ";" + rcr + ";;");
            }
		} catch (IOException ex) {
			log.error(Constant.ERROR_UNABLE_TO_CREATE_FILE);
		}
	}

	/**
	 * Méthode permetant d'alimenter le fichier à partir d'une chaine correspondant à une liste de PPN
	 * @param input résultat de l'appel à la fonction Oracle
	 * @param rcr rcr de la demandeModif à insérer dans le second champ du fichier
	 */
	public void alimenterPpn(String input, String listeEpn, String rcr) {
		try (FileWriter fw = new FileWriter(path.resolve(filename).toString(), true);
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			String[] tabEpn = listeEpn.split(",");
			Multimap<String, String> resJson = Utilitaires.parseJson(input, true);
			for (String epn : tabEpn) {
				if (resJson.containsKey(epn)) {
					for (String ppn : resJson.get(epn)) {
						out.println(ppn + ";" + rcr + ";" + epn + ";");
					}
				} else
					out.println(";" + rcr + ";" + epn + ";");
			}
		} catch (IOException ex) {
			log.error(Constant.ERROR_UNABLE_TO_CREATE_FILE);
		}
	}

	/**
	 * Méthode qui permet de trier le contenu du fichier de correspondance
	 * @throws IOException renvoi une exception si le fichier ne peut être lu
	 */
	public void trierLignesDeCorrespondances() throws IOException {
		FileReader fileReader = new FileReader(path.resolve(filename).toString());
		BufferedReader reader = new BufferedReader(fileReader);

		List<String> correspondanceSortList = new ArrayList<>();
		String header = reader.readLine();//cette ligne enleve le header et le stock
		correspondanceSortList.add(header + "\n");
		reader.lines().sorted().forEach(line -> {
			correspondanceSortList.add(line+"\n");
		});
		reader.close();
		fileReader.close();
		String result = String.join("", correspondanceSortList);
		ecrireFichierTrie(result);
	}

	/**
	 * Méthode permettant d'écrire sur le fichier la liste des correspondances triées
	 * @param sortedLines String contenant la liste des correspondances triées
	 */
	private void ecrireFichierTrie(String sortedLines) {
		try (FileWriter fw = new FileWriter(path.resolve(filename).toString());
			 BufferedWriter bw = new BufferedWriter(fw);
			 PrintWriter out = new PrintWriter(bw)) {
			out.println(sortedLines);
		} catch (IOException ex) {
			log.error(Constant.ERROR_UNABLE_TO_CREATE_SORTED_FILE);
		}
	}

}
