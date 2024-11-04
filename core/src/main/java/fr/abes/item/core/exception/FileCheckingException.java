package fr.abes.item.core.exception;

public class FileCheckingException extends Exception {
	public FileCheckingException(String message) {
		super("Erreur dans l'analyse du fichier : " + message);
	}

}
