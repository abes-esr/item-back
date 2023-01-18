package fr.abes.item.exception;

public class FileLineException extends Exception {
    private static final long serialVersionUID = 1L;

    public FileLineException(String message){
        super("probleme lors du traitement de la ligne du fichier : " + message);
    }
}
