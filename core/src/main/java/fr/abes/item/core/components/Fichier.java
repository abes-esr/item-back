package fr.abes.item.core.components;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;

import java.io.IOException;
import java.nio.file.Path;


public interface Fichier {	
	String getFilename();
	void setPath(Path path);
	int getType();
	TYPE_DEMANDE getDemandeType();
	void generateFileName(Demande numDemande);

	/**
	 * Méthode permettant de vérifier que le contenu du fichier correspond aux spécifications
	 * @throws FileCheckingException erreur dans la vérification du fichier
	 * @throws IOException erreur de lecture du fichier
	 */
	void checkFileContent(Demande d) throws FileCheckingException, IOException;
	
}
