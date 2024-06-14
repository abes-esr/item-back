package fr.abes.item.core.exception;

public class DemandeCheckingException extends Exception {
	public DemandeCheckingException(String message) {
		super("Erreur dans la demande : " + message);
	}
}
