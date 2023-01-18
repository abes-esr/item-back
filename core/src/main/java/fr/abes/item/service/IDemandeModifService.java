package fr.abes.item.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.exception.FileTypeException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 *
 * Service de récupération des demandes de modification en cours.
 * Service récupérant également les notices, avant traitement, après traitement
 *
 * iln = numéro de l'utilisateur propriétaire des demandeModifs
 *
 * demandes de modification = table DEMANDE
 *
 * - chercher : recupérer la liste des demandes de modification que l'utilisateur à le droit de modifier,
 *      l'iln de l'utilisateur est passé en paramètre au moment de l'envoi de la requête
 * - chercherArchives : récupérer le liste des demandes de modification de l'utilisateur associées à son iln,
 *      qui sont à l'état 9 dans la table ETAT_DEMANDE
 * - getLigneFichier : récupérer une ligne du fichier de modification associé à une demande de modification, dans
 *      une entité ligneFichierModif.
 *      la ligne récupérée est récupérée à partir du numero de demande de modification dans la table DEMANDE, associé
 *      à l'attribut REF_DEMANDE de la table LIGNE_FICHIER, et à partir du numero de ligne NUM_LIGNEFICHIER de la
 *      table LIGNE_FICHIER
 * - getNoticeInitiale : permet de récupérer la notice initiale avant traitement
 *      identification au cbs à l'aide du rcr de la demande de modification
 *      epn : exemplaire associé à la notice à récupérer, l'exemplaire physique d'une blibliothèque étant identifié
 *      par son numero unique, l'epn.
 * - getNoticeTraitee : permet de récupérer la notice modifiée avant son envoi en production
 *      demandeModif : va permettre de savoir les zones et les sous zones sur lesquelles on souhaite modifier
 *      moticeInit : la notice initiale avant modification
 *      ligneFichierModif : les valeurs que l'on souhaite modifier rattachées aux zone et sous zones
 * - findAllAdmin : récupérer la liste des demandes de modification de tout les rcr associé à un iln,
 *      qu'importe leur statut (tous les status)
 * - getIdNextDemandeToproceed : retourne la prochaine demande en attente
 *
 */
public interface IDemandeModifService extends IDemandeService{
    LigneFichierModif getLigneFichier(DemandeModif demandeModif, Integer numLigne);

    String getNoticeInitiale(DemandeModif demandeModif, String epn) throws CBSException;

    String getNoticeTraitee(DemandeModif demandeModif, String noticeInit, LigneFichierModif ligneFichierModif) throws ZoneException;

    String stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException;

    DemandeModif creerDemande(String rcr, Date dateCreation, Date dateModification, String zone, String sousZone,
                              String comment, EtatDemande etatDemande, Utilisateur utilisateur, Traitement traitement);

    Demande changeStateCanceled(Demande demande, int etatDemande);

    List<DemandeModif> getListDemandesToClean();

    List<DemandeModif> getIdNextDemandeToArchive();
    List<DemandeModif> getIdNextDemandeToPlaceInDeletedStatus();
    List<DemandeModif> getIdNextDemandeToDelete();

}
