package fr.abes.item.batch.mail.impl;

import fr.abes.item.batch.mail.IMailer;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.service.impl.LigneFichierModifService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.time.LocalDateTime;

@Slf4j
@Service
@Strategy(type = IMailer.class, typeDemande = TYPE_DEMANDE.MODIF)
public class MailerModif extends Mailer implements IMailer {
    private final Environment env;

    private final LigneFichierModifService service;
    public MailerModif(Environment env, LigneFichierModifService service) {
        this.env = env;
        this.service = service;
    }

    /**
     * Mail indiquant le début du traitement
     * @param mailDestinataire mail de l'utilisateur qui a lancé le trraitement (la demandeModif)
     * @param demande Demande concernée par le message
     */
    @Override
    public void mailDebutTraitement(String mailDestinataire, Demande demande){

        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_MODIFICATION_START + demande.getId() + " lancée - ILN " + demande.getIln()
                + " lancée.", "Bonjour, <br/>Votre demande de modification d'exemplaire n° " + demande.getId() + " a été lancée.<br />"+
                "<br />Cordialement.<br/>L'équipe ITEM'.");
        sendMail(requestJson);
    }

    @Override
    public void mailFinTraitement(String mailDestinataire, Demande demande, File f, LocalDateTime dateDebut, LocalDateTime dateFin) {
        int numDemande = demande.getId();
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_MODIFICATION_START + numDemande + " terminée - ILN " + demande.getIln(),
                "Bonjour,<br />Votre demande " + numDemande + " de modification d'exemplaires a bien été traitée.<br />" +
                        "Nombre d'exemplaires traités : " + service.getNbLigneFichierTraiteeByDemande(demande) + Constant.HTML_BALISE_BR +
                        "Nombre de traitements effectués avec succès : " + service.getNbLigneFichierSuccessByDemande(demande) + Constant.HTML_BALISE_BR +
                        "Nombre de traitements échoués : " + service.getNbLigneFichierErrorByDemande(demande) + Constant.HTML_BALISE_BR +
                        "Vous pouvez retrouver le résultat de votre demande depuis <a href='https://item.sudoc.fr/tableau'>le tableau de bord de l'application.</a> <br />" +
                        "Cordialement.<br/>Les services de l'Abes.");
        sendMailWithAttachment(requestJson, f);
    }

    /**
     * Mail indiquant l'echec du traitement
     * @param mailDestinataire mail de l'utilisateur qui a lancé le traitement
     * @param demande numéro de la demandeModif
     * @param dateDebut date  de début de traitement
     */
    @Override
    public void mailEchecTraitement(String mailDestinataire, Demande demande, LocalDateTime dateDebut){
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_MODIFICATION_START + demande.getId() + Constant.DEMANDE_MAIL_ECHEC + " - ILN " + demande.getIln(),
                "Bonjour,<br />Votre modification d'exemplaires -  N°" + demande.getId() + "n'a pas pu être exécutée. Une erreur vient de se produire sur ITEM. Dès que l'incident sera résolu vous recevrez un message vous indiquant la reprise du traitement. Cela ne nécessite aucune intervention de votre part." +
                        "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro/\" target=\"_blank\"> https://stp.abes.fr</a>"+
                        "<br />Cordialement.<br/>L'équipe ITEM.");
        sendMail(requestJson);
    }

    @Override
    public void mailAlertAdmin(String mailUtilisateur, Demande demande) {
        String requestJson = mailToJSON(mailAdmin, "Erreur dans Item / Modification" + " - ILN " + demande.getIln() + " / " + ((env.getActiveProfiles().length>0)?env.getActiveProfiles()[0]:"Local"), "Une erreur vient de se produire sur Item / Modification sur la demande" + demande.getId());
        sendMail(requestJson);

    }
}
