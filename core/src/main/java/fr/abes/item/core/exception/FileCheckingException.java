package fr.abes.item.core.exception;
import static fr.abes.item.core.constant.Constant.ERR_FILE_ERRLINE;

public class FileCheckingException extends Exception {
	public FileCheckingException(String message) {
		super("Erreur dans l'analyse du fichier : " + message);
	}

	public FileCheckingException(Integer ligneAvecErreur, String message) {
		super(ERR_FILE_ERRLINE + ligneAvecErreur + " : " + message);
	}
}
