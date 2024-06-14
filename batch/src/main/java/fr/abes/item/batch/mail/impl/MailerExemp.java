package fr.abes.item.batch.mail.impl;


import fr.abes.item.batch.mail.IMailer;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.service.impl.LigneFichierExempService;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DecimalFormat;
import java.time.LocalDateTime;

@Service
@Strategy(type = IMailer.class, typeDemande = TYPE_DEMANDE.EXEMP)
public class MailerExemp extends Mailer implements IMailer {
    private final Environment env;
    private final LigneFichierExempService service;


    public MailerExemp(Environment env, LigneFichierExempService service) {
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
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_EXEMPLARISATION_START + ((DemandeExemp) demande).getTypeExemp().getLibelle() + " N°" + demande.getId() + Constant.DEMANDE_MAIL_DEBUT + " - ILN " + demande.getIln()
                , "Bonjour, <br/>Votre exemplarisation - " + demandeExemp.getTypeExemp().getLibelle() + " N° " + demande.getId() + " - est en cours de traitement.<br/>"
                + "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href='https://stp.abes.fr/node/3?origine=sudocpro' target='_blank'>https://stp.abes.fr/</a><br/>"
                + "<br/>Cordialement,<br/>L'équipe ITEM.");
        sendMail(requestJson);
    }

    @Override
    public void mailFinTraitement(String mailDestinataire, Demande demande, File f, LocalDateTime dateDebut, LocalDateTime dateFin) {
        DecimalFormat df = new DecimalFormat("0.00");
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        int nbExempCree = service.getNbLigneFichierSuccessByDemande(demandeExemp);
        int nbRechercheTotal = service.getNbLigneFichierTotalByDemande(demandeExemp);
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_EXEMPLARISATION_END + demandeExemp.getTypeExemp().getLibelle() + " N°" + demandeExemp.getId() + " - ILN " + demande.getIln(),
                "Bonjour,<br/>Votre exemplarisation - " + demandeExemp.getTypeExemp().getLibelle() + " N°" + demandeExemp.getId() + " - est terminée." + Constant.HTML_BALISE_BR +
                        "Bilan :" + Constant.HTML_BALISE_BR +
                        "Exemplarisation démarrée le : " + Constant.formatDate.format(dateDebut) + Constant.HTML_BALISE_BR +
                        "Exemplarisation terminée le : " + Constant.formatDate.format(dateFin) + Constant.HTML_BALISE_BR +
                        "Nb de requêtes : " + nbRechercheTotal + Constant.HTML_BALISE_BR +
                        "Nb exemplaires créés : " + nbExempCree + " soit " + df.format(((double)nbExempCree / (double)nbRechercheTotal) * 100) + "%" + Constant.HTML_BALISE_BR +
                        "Nb erreurs validation : " + service.getNbLigneFichierErrorByDemande(demandeExemp) + Constant.HTML_BALISE_BR  +
                        service.getNbReponseTrouveesByDemande(demandeExemp) + " notices trouvées" + Constant.HTML_BALISE_BR +
                        "Nb de 1 réponse : " + service.getNbUneReponseByDemande(demandeExemp) + Constant.HTML_BALISE_BR +
                        "Nb sans réponse : " + service.getNbZeroReponseByDemande(demandeExemp) + Constant.HTML_BALISE_BR +
                        "Nb plusieurs réponses : " + service.getNbReponseMultipleByDemande(demandeExemp) + Constant.HTML_BALISE_BR +
                        "Le fichier de résultat est à votre disposition en pièce jointe et sur votre interface ITEM. " + Constant.HTML_BALISE_BR +
                        "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro\" target=\"_blank\">https://stp.abes.fr</a>" + Constant.HTML_BALISE_BR +
                        "Cordialement," + Constant.HTML_BALISE_BR +
                        "L'équipe ITEM");
        sendMailWithAttachment(requestJson, f);
    }

    /**
     * Mail indiquant l'echec du traitement
     * @param mailDestinataire mail de l'utilisateur qui a lancé le traitement
     * @param demande demande sur laquelle porte le mail
     * @param dateDebut date de début du traitement
     */
    @Override
    public void mailEchecTraitement(String mailDestinataire, Demande demande, LocalDateTime dateDebut){
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_EXEMPLARISATION_START + demandeExemp.getTypeExemp().getLibelle() + " N°" + demandeExemp.getId() + "-" + Constant.DEMANDE_MAIL_ECHEC + " - ILN " + demande.getIln(),
                "Bonjour,<br/><br/>" +
                     "votre exemplarisation - " + demandeExemp.getTypeExemp().getLibelle() +" N°" + demandeExemp.getId() + " lancée le " + dateDebut + "n'a pas pu être traitée.<br/><br/>" +
                     "Il convient de ne pas relancer l’exemplarisation. Le traitement reprendra à l’endroit où l’erreur s’est produite : c’est-à-dire que le programme se relancera automatiquement dès que le dysfonctionnement sera résolu, sans intervention manuelle de votre part." +
                     "Vous recevrez alors un message vous indiquant la reprise automatique de l’exemplarisation.<br/><br/>" +
                     "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro/\" target=\"_blank\"> https://stp.abes.fr/node/3?origine=sudocpro</a><br/><br/>"+
                     "<br/>Cordialement.<br/>L'équipe ITEM.");
        sendMail(requestJson);
    }

    @Override
    public void mailAlertAdmin(String mailDestinataire, Demande demande) {
        String requestJson = mailToJSON(mailDestinataire+";"+mailAdmin, "Erreur dans Item / Exemplarisation" + " - ILN " + demande.getIln() + " / " + ((env.getActiveProfiles().length>0)?env.getActiveProfiles()[0]:"Local"), "Une erreur vient de se produire sur Item sur la demande" + demande.getId());
        sendMail(requestJson);
    }
}
