package fr.abes.item.mail;

import fr.abes.item.entities.item.Demande;

import java.io.File;
import java.util.Date;
import java.util.List;

public interface IMailer {
    void mailDebutTraitement(String mailDestinataire, Demande demande);

    void mailFinTraitement(String mailDestinataire, Demande demande, File f, Date dateDebut, Date dateFin);

    void mailEchecTraitement(String mailDestinataire, Demande demande, Date dateDebut);

    void mailAlertAdmin(String mailDestinataire, Demande demande);

    void mailRestartJob(List<Integer> listeDemandes);

}
