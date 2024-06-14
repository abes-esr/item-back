package fr.abes.item.batch.mail;

import fr.abes.item.core.entities.item.Demande;

import java.io.File;
import java.time.LocalDateTime;

public interface IMailer {
    void mailDebutTraitement(String mailDestinataire, Demande demande);

    void mailFinTraitement(String mailDestinataire, Demande demande, File f, LocalDateTime dateDebut, LocalDateTime dateFin);

    void mailEchecTraitement(String mailDestinataire, Demande demande, LocalDateTime dateDebut);

    void mailAlertAdmin(String mailDestinataire, Demande demande);
}
