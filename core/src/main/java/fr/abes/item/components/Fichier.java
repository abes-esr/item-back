package fr.abes.item.components;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.exception.FileCheckingException;

import java.io.IOException;
import java.nio.file.Path;


public interface Fichier {	
	String getFilename();
	void setPath(Path path);
	int getType();
	TYPE_DEMANDE getDemandeType();
	void generateFileName(Integer numDemande);

	/**
	 * Méthode permettant de vérifier que le contenu du fichier correspond aux spécifications
	 * @throws FileCheckingException
	 */
	void checkFileContent(Demande d) throws FileCheckingException, IOException;
	
}
