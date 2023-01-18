package fr.abes.item.exception;
import static fr.abes.item.constant.Constant.ERR_FILE_ERRLINE;

public class FileCheckingException extends Exception {

	private static final long serialVersionUID = 1L;

	public FileCheckingException(String message) {
		super("Erreur dans l'analyse du fichier : " + message);
	}

	public FileCheckingException(Integer ligneAvecErreur, String message) {
		super(ERR_FILE_ERRLINE + ligneAvecErreur + " : " + message);
	}
}
