package fr.abes.item.core.exception;

public class FileTypeException extends Exception {
	public FileTypeException(String message) {
		super("Type de fichier incorrect : " + message);
	}

}
