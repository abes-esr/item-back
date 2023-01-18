package fr.abes.item.exception;

public class FileTypeException extends Exception {
	private static final long serialVersionUID = 1L;
	
	public FileTypeException(String message) {
		super("Type de fichier incorrect : " + message);
	}

}
