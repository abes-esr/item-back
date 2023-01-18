package fr.abes.item.mail.impl;

import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.service.service.ServiceProvider;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@Slf4j
@Strategy(type = IMailer.class, typeDemande = TYPE_DEMANDE.RECOUV)
public class MailerRecouv extends Mailer implements IMailer {
    @Autowired
    private Environment env;
    @Autowired @Getter
    private ServiceProvider service;
    @Autowired @Getter
    private DaoProvider dao;
    /**
     * Mail indiquant le début du traitement
     * @param mailDestinataire mail de l'utilisateur qui a lancé le trraitement (la demandeModif)
     * @param demande Demande concernée par le message
     */
    @Override
    public void mailDebutTraitement(String mailDestinataire, Demande demande) {
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_RECOUVREMENT_START + demande.getId() + Constant.DEMANDE_MAIL_DEBUT + " - ILN " + demande.getIln()
                , "Bonjour, <br/>votre taux de recouvrement - Recouvrement N°" + demande.getId() + " est en cours de traitement.<br />" +
                        "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro\" target=\"_blank\">https://stp.abes.fr/</a>" +
                "<br />Cordialement,<br/>L'équipe ITEM");
        sendMail(requestJson);
    }

    @Override
    public void mailFinTraitement(String mailDestinataire, Demande demande, File f, Date dateDebut, Date dateFin) {
        DateFormat formatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        int numDemande = demande.getId();
        int nbRechercheTotal = getService().getLigneFichierRecouv().getNbLigneFichierTotalByDemande(numDemande);
        int nbNoticesTrouvees = getDao().getLigneFichierRecouv().getNbReponseTrouveesByDemande(numDemande);
        int nbZeroReponse = getDao().getLigneFichierRecouv().getNbZeroReponseByDemande(numDemande);
        int nbUneReponse = getDao().getLigneFichierRecouv().getNbUneReponseByDemande(numDemande);
        int nbReponseMultiple = getDao().getLigneFichierRecouv().getNbReponseMultipleByDemande(numDemande);
        double tauxRecouv = ((double)nbNoticesTrouvees / (double)nbRechercheTotal) * 100;
        double tauxExemp = ((double)nbUneReponse / (double)nbRechercheTotal) * 100;
        DecimalFormat df = new DecimalFormat("0.00");
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_RECOUVREMENT_END + numDemande + " - ILN " + demande.getIln(),
                "Bonjour," + Constant.HTML_BALISE_BR +
                        "Votre taux de recouvrement N°" + numDemande + " est terminé." + Constant.HTML_BALISE_BR +
                        "Bilan : " + Constant.HTML_BALISE_BR +
                        "Taux de recouvrement démarré le : " + formatDate.format(dateDebut) + Constant.HTML_BALISE_BR +
                        "Nb de requêtes : " + nbRechercheTotal + Constant.HTML_BALISE_BR +
                        "Taux de recouvrement : " + df.format(tauxRecouv) + "%" + Constant.HTML_BALISE_BR +
                        "Taux de création possible d'exemplaires : " + df.format(tauxExemp) + "%" + Constant.HTML_BALISE_BR +
                        nbNoticesTrouvees + " notices trouvées sur " + nbRechercheTotal + Constant.HTML_BALISE_BR +
                        "Nombre 1 réponse : " + nbUneReponse + Constant.HTML_BALISE_BR +
                        "Nombre sans réponse : " + nbZeroReponse + Constant.HTML_BALISE_BR +
                        "Nombre de plusieurs réponses : " + nbReponseMultiple + Constant.HTML_BALISE_BR +
                        "Taux de recouvrement terminé le : " + formatDate.format(dateFin) + Constant.HTML_BALISE_BR +
                        "Le fichier de résultat est à votre disposition en pièce jointe et sur votre interface ITEM. " +
                        "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro\" target=\"_blank\">https://stp.abes.fr</a>" + Constant.HTML_BALISE_BR +
                        "Cordialement," + Constant.HTML_BALISE_BR +
                        "L'équipe ITEM");
        sendMailWithAttachment(requestJson, f);
    }

    @Override
    public void mailEchecTraitement(String mailDestinataire, Demande demande, Date dateDebut) {
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_RECOUVREMENT_START + demande.getId() + " - " + Constant.DEMANDE_MAIL_ECHEC + " - ILN " + demande.getIln(),
                "Votre taux de recouvrement - N°" + demande.getId() + " - n'a pas pu être exécuté. Une erreur vient de se produire sur ITEM. Dès que l'incident sera résolu vous recevrez un message vous indiquant la reprise du traitement. Cela ne nécessite aucune intervention de votre part.<br />" +
                        "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro/\" target=\"_blank\"> https://stp.abes.fr</a><br />" +
                        "<br />Cordialement,<br /> L'équipe ITEM");
        sendMail(requestJson);
    }

    @Override
    public void mailAlertAdmin(String mailUtilisateur, Demande demande) {
        String requestJson = mailToJSON(mailAdmin, "Erreur dans Item / Recouvrement " + " - ILN " + demande.getIln() + " / " + ((env.getActiveProfiles().length>0)?env.getActiveProfiles()[0]:"Local"), "Une erreur vient de se produire sur Item sur la demande" + demande.getId());
        sendMail(requestJson);
    }

    @Override
    public void mailRestartJob(List<Integer> listDemandes) {
        StringBuilder message = new StringBuilder("Les demandes de recouvrement suivantes ont été repassées automatiquement en attente : \n");
        listDemandes.forEach(d -> {
            message.append(d);
            message.append("\n");
        });
        String requestJson = mailToJSON(mailAdmin, "[ITEM] Redémarrage automatique des demandes de recouvrement", message.toString());
        sendMail(requestJson);
    }
}
