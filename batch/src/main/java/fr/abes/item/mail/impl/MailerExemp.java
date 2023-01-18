package fr.abes.item.mail.impl;


import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeExemp;
import fr.abes.item.mail.IMailer;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.service.service.ServiceProvider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Service;

import java.io.File;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;

@NoArgsConstructor
@Service
@Strategy(type = IMailer.class, typeDemande = TYPE_DEMANDE.EXEMP)
public class MailerExemp extends Mailer implements IMailer {
    @Autowired
    private Environment env;
    @Autowired @Getter
    private DaoProvider dao;

    @Autowired @Getter
    private ServiceProvider service;
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
    public void mailFinTraitement(String mailDestinataire, Demande demande, File f, Date dateDebut, Date dateFin) {
        DateFormat formatDate = DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT);
        DecimalFormat df = new DecimalFormat("0.00");
        DemandeExemp demandeExemp = (DemandeExemp) demande;
        ILigneFichierService ligneFichierService = factory.getStrategy(ILigneFichierService.class, demande.getTypeDemande());
        int nbExempCree = ligneFichierService.getNbLigneFichierSuccessByDemande(demandeExemp.getId());
        int nbRechercheTotal = getService().getLigneFichierExemp().getNbLigneFichierTotalByDemande(demandeExemp.getId());
        String requestJson = mailToJSON(mailDestinataire, Constant.DEMANDE_EXEMPLARISATION_END + demandeExemp.getTypeExemp().getLibelle() + " N°" + demandeExemp.getId() + " - ILN " + demande.getIln(),
                "Bonjour,<br/>Votre exemplarisation - " + demandeExemp.getTypeExemp().getLibelle() + " N°" + demandeExemp.getId() + " - est terminée." + Constant.HTML_BALISE_BR +
                        "Bilan :" + Constant.HTML_BALISE_BR +
                        "Exemplarisation démarrée le : " + formatDate.format(dateDebut) + Constant.HTML_BALISE_BR +
                        "Exemplarisation terminée le : " + formatDate.format(dateFin) + Constant.HTML_BALISE_BR +
                        "Nb de requêtes : " + nbRechercheTotal + Constant.HTML_BALISE_BR +
                        "Nb exemplaires créés : " + nbExempCree + " soit " + df.format(((double)nbExempCree / (double)nbRechercheTotal) * 100) + "%" + Constant.HTML_BALISE_BR +
                        "Nb erreurs validation : " + ligneFichierService.getNbLigneFichierErrorByDemande(demandeExemp.getId()) + Constant.HTML_BALISE_BR  +
                        getDao().getLigneFichierExemp().getNbReponseTrouveesByDemande(demandeExemp.getId()) + " notices trouvées" + Constant.HTML_BALISE_BR +
                        "Nb de 1 réponse : " + getDao().getLigneFichierExemp().getNbUneReponseByDemande(demandeExemp.getId()) + Constant.HTML_BALISE_BR +
                        "Nb sans réponse : " + getDao().getLigneFichierExemp().getNbZeroReponseByDemande(demandeExemp.getId()) + Constant.HTML_BALISE_BR +
                        "Nb plusieurs réponses : " + getDao().getLigneFichierExemp().getNbReponseMultipleByDemande(demandeExemp.getId()) + Constant.HTML_BALISE_BR +
                        "Le fichier de résultat est à votre disposition en pièce jointe et sur votre interface ITEM. " + Constant.HTML_BALISE_BR +
                        "Pour toute information complémentaire, merci de bien vouloir déposer une demande sur le guichet d'assistance : <a href=\"https://stp.abes.fr/node/3?origine=sudocpro\" target=\"_blank\">https://stp.abes.fr</a>" + Constant.HTML_BALISE_BR +
                        "Cordialement," + Constant.HTML_BALISE_BR +
                        "L'équipe ITEM");
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

    @Override
    public void mailRestartJob(List<Integer> listDemandes) {
        StringBuilder message = new StringBuilder("Les demandes d'exemplarisation suivantes ont été repassées automatiquement en attente : \n");
        listDemandes.forEach(d -> {
            message.append(d);
            message.append("\n");
        });
        String requestJson = mailToJSON(mailAdmin, "[ITEM] Redémarrage automatique des demandes d'exemplarisation", message.toString());
        sendMail(requestJson);
    }
}
