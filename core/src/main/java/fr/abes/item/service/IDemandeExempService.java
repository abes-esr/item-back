package fr.abes.item.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.CommException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.DemandeCheckingException;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.exception.FileTypeException;
import fr.abes.item.exception.QueryToSudocException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

/**
 * Service permettant de créer un exemplaire.
 *
 * - getNoticeExemplaireAvantApres : se connecte au sudoc avec un login,
 *      lance une requete au sudoc avec un code recherche présent dans la table INDEX_RECHERCHE (DAT, ISBN, PPN, SOU)
 *      et l'index de recherche dans la table LIGNE_FICHIER_EXEMP
 *      recupère le numéro du prochaine exemplaire à créer dans la notice au format "xx", puis
 *      retourne un tableau de chaine de caractère :
 *      l'exemplaire sous forme de chaine.
 * - getExemplaireFromHeaderEtValeur : va prendre en entrée
 *      la liste des zones à créer
 *      les valeurs des zones à créer
 *      une zone 930 qui doit etre rajoutée ou non selon le cas
 *      une zone eXX ajoutée
 *      un objet exemplaire vide est alors crée, construit à partir des entrées ci-dessus puis retourné sous forme de chaine
 * - launchQueryToSudoc : retourne à partir du code (DAT, ISBN, PPN, SOU), le numéro du prochain exemplaire dans la
 *      notice à créer au format xx
 * - getLigneFichier : récupérer une ligne du fichier d'exemplarisation associé, dans
 *      une entité ligneFichierExemp.
 *      la ligne récupérée est récupérée à partir du numero de demande d'exemplarisation dans la table DEMANDE_EXEMP, associé
 *      à l'attribut REF_DEMANDE de la table LIGNE_FICHIER_EXEMP, et à partir du numero de ligne POS de la
 *      table LIGNE_FICHIER_EXEMP
 * - majTypeExemp : mise à jour du type d'exemplarisation à partir du numero de la demande d'exemplarisation
 * - getQueryToSudoc : fabrique la requête WINIBW à partir du sudoc
 */
public interface IDemandeExempService extends IDemandeService{
    boolean hasDonneeLocaleExistante();

    String[] getNoticeExemplaireAvantApres(DemandeExemp demande, LigneFichierExemp ligneFichier) throws CBSException, ZoneException, CommException;

    String creerExemplaireFromHeaderEtValeur(String header, String valeur, String rcr, String numExemp) throws CBSException, ZoneException;

    String creerDonneesLocalesFromHeaderEtValeur(String header, String valeur) throws ZoneException;

    String launchQueryToSudoc(DemandeExemp demande, String valeurs) throws CBSException, QueryToSudocException;

    LigneFichierExemp getLigneFichier(DemandeExemp demande, Integer numLigne);

    DemandeExemp majTypeExemp(Integer idDemande, TypeExemp typeExemp);

    Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException;

    Demande changeStateCanceled(Demande demande, int etatDemande);

    String stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException;

    String getTypeExempDemande(Integer idDemande);

    int getNbReponses();

    DemandeExemp creerDemande(String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur);

    String getQueryToSudoc(String codeIndex, String typeExemp, String[] valeur) throws QueryToSudocException;

    List<DemandeExemp> getIdNextDemandeToArchive();
    List<DemandeExemp> getIdNextDemandeToPlaceInDeletedStatus();
    List<DemandeExemp> getIdNextDemandeToDelete();
}
