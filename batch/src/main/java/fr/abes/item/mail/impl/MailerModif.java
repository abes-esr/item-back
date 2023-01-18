package fr.abes.item.mail.impl;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.Strategy;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;

@Slf4j
@Service
@Strategy(type = IMailer.class, typeDemande = TYPE_DEMANDE.MODIF)
public class MailerModif extends Mailer implements IMailer {
    @Autowired
    private Environment env;
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
    public void mailFinTraitement(String mailDestinataire, Demande demande, File f, Date dateDebut, Date dateFin) {
        int numDemande = demande.getId();
        ILigneFichierService ligneFichierService = factory.getStrategy(ILigneFichierService.class, demande.getTypeDemande());
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_MODIFICATION_START + numDemande + " terminée - ILN " + demande.getIln(),
                "Bonjour,<br />Votre demande " + numDemande + " de modification d'exemplaires a bien été traitée.<br />" +
                        "Nombre d'exemplaires traités : " + ligneFichierService.getNbLigneFichierTraiteeByDemande(numDemande) + Constant.HTML_BALISE_BR +
                        "Nombre de traitements effectués avec succès : " + ligneFichierService.getNbLigneFichierSuccessByDemande(numDemande) + Constant.HTML_BALISE_BR +
                        "Nombre de traitements échoués : " + ligneFichierService.getNbLigneFichierErrorByDemande(numDemande) + Constant.HTML_BALISE_BR +
                        "Vous pouvez retrouver le résultat de votre demande depuis <a href='https://item.sudoc.fr/tableau'>le tableau de bord de l'application.</a> <br />" +
                        "Cordialement.<br/>Les services de l'Abes.");
        sendMailWithAttachment(requestJson, f);
    }

    /**
     * Mail indiquant l'echec du traitement
     * @param mailDestinataire mail de l'utilisateur qui a lancé le traitement
     * @param demande numéro de la demandeModif
     * @param dateDebut
     */
    @Override
    public void mailEchecTraitement(String mailDestinataire, Demande demande, Date dateDebut){
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

    @Override
    public void mailRestartJob(List<Integer> listDemandes) {
        StringBuilder message = new StringBuilder("Les demandes de modification suivantes ont été repassées automatiquement en attente : \n");
        listDemandes.forEach(d -> {
            message.append(d);
            message.append("\n");
        });
        String requestJson = mailToJSON(mailAdmin, "[ITEM] Redémarrage automatique des demandes de modifications", message.toString());
        sendMail(requestJson);
    }
}
