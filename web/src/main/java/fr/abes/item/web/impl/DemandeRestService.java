package fr.abes.item.web.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.CommException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.constant.Constant;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.*;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.security.JwtTokenProvider;
import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.IDemandeRestService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
public class DemandeRestService extends AbstractRestService implements IDemandeRestService {
//    @ExceptionHandler
//    @ResponseStatus(HttpStatus.BAD_REQUEST)
//    public void handle(Exception e) {
//        log.warn("Returning HTTP Bad Request", e);
//    }

    /**
     * Webservice de récupération de la liste des demandeModifs de l'administrateur
     *
     * @return Liste de la totalité des demandeModifs
     */

    @Autowired
    JwtTokenProvider tokenProvider;

    @Autowired
    CheckAccessToServices checkAccessToServices;

    /**
     * Webservice : retour de l'ensemble des demandes pour un administrateur
     * @param type    type de demande concernée par le webservice
     * @param request le requete avec ses attributs
     * @return Une liste de demandes
     */
    @Override
    @GetMapping(value = "/demandes")
    @PreAuthorize("hasAuthority('ADMIN')")
    public List<Demande> getAllActiveDemandes(TYPE_DEMANDE type, boolean extension, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        switch (type) {
            case EXEMP:
                return (!extension) ? getService().getDemandeExemp().getAllActiveDemandesForAdmin(iln) : getService().getDemandeExemp().getAllActiveDemandesForAdminExtended();
            case MODIF:
                return (!extension) ? getService().getDemandeModif().getAllActiveDemandesForAdmin(iln) : getService().getDemandeModif().getAllActiveDemandesForAdminExtended();
            default:
                return (!extension) ? getService().getDemandeRecouv().getAllActiveDemandesForAdmin(iln) : getService().getDemandeRecouv().getAllActiveDemandesForAdminExtended();
        }
    }

    /**
     * Webservice : retour de l'ensemble des demandes pour un utilisateur
     * @return liste des demandeModifs non archivées de l'utilisateur
     */
    @Override
    @GetMapping(value = "/chercherDemandes")
    @PreAuthorize("hasAuthority('USER')")
    public List<Demande> chercher(TYPE_DEMANDE type, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        switch (type) {
            case MODIF: return getService().getDemandeModif().getActiveDemandesForUser(iln);
            case EXEMP: return getService().getDemandeExemp().getActiveDemandesForUser(iln);
            default: return getService().getDemandeRecouv().getActiveDemandesForUser(iln);
        }
    }

    /**
     * Webservices : retour des demandes archivées
     * @param type type de demande concernée par le webservice
     * @return liste des demandes archivées de l'utilisateur
     */
    @Override
    @GetMapping(value = "/chercherArchives")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    public List<Demande> getAllArchivedDemandes(TYPE_DEMANDE type, boolean extension, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        switch (type) {
            case MODIF: return (!extension) ? getService().getDemandeModif().getAllArchivedDemandes(iln) : getService().getDemandeModif().getAllArchivedDemandesAllIln();
            case EXEMP: return (!extension) ? getService().getDemandeExemp().getAllArchivedDemandes(iln) : getService().getDemandeExemp().getAllArchivedDemandesAllIln();
            default: return (!extension) ? getService().getDemandeRecouv().getAllArchivedDemandes(iln) : getService().getDemandeRecouv().getAllArchivedDemandesAllIln();
        }
    }

    /**
     * Webservice de récupération d'une demandeModif par son identifiant
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif
     * @return demandeModif correspondant à la recherche
     */
    @Override
    @GetMapping(value = "/demandes/{id}")
    public Demande getDemande(TYPE_DEMANDE type, Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF: return getService().getDemandeModif().findById(id);
            case EXEMP: return getService().getDemandeExemp().findById(id);
            default: return getService().getDemandeRecouv().findById(id);
        }
    }

    /**
     * Webservice de sauvegarde d'une demandeModif via formulaire
     *
     * @param type type de demande concernée par le webservice
     * @param rcr  : rcr de la demandeModif à enregistrer
     * @return : la demandé modifiée
     */
    @Override
    @GetMapping(value = "/creerdemande")
    public Demande saveModif(TYPE_DEMANDE type, String rcr, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserCreationDemandeParUserNum(rcr, request.getAttribute(Constant.USER_NUM).toString());
        Date datejour = new Date();
        Demande demToReturn;
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = getService().getDemandeModif().creerDemande(rcr, datejour, datejour, "", "", "", getService().getReference().findEtatDemandeById(Constant.ETATDEM_PREPARATION), getService().getUtilisateur().findById(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString())), null);
                demToReturn = getService().getDemandeModif().save(demandeModif);
                getService().getJournal().addEntreeJournal((DemandeModif) demToReturn, getService().getReference().findEtatDemandeById(Constant.ETATDEM_PREPARATION));
                break;
            case EXEMP:
                DemandeExemp demandeExemp = getService().getDemandeExemp().creerDemande(rcr, datejour, datejour, getService().getReference().findEtatDemandeById(Constant.ETATDEM_PREPARATION), "", getService().getUtilisateur().findById(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString())));
                demToReturn = getService().getDemandeExemp().save(demandeExemp);
                getService().getJournal().addEntreeJournal((DemandeExemp) demToReturn, getService().getReference().findEtatDemandeById(Constant.ETATDEM_PREPARATION));
                break;
            default:
                DemandeRecouv demandeRecouv = getService().getDemandeRecouv().creerDemande(rcr, datejour, datejour, getService().getReference().findEtatDemandeById(Constant.ETATDEM_PREPARATION), "", getService().getUtilisateur().findById(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString())));
                demToReturn = getService().getDemandeRecouv().save(demandeRecouv);

        }
        return demToReturn;
    }

    /**
     * Webservice de suppression d'une demandeModif
     *
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif à supprimer
     */
    @Override
    @DeleteMapping(value = "/demandes/{id}")
    public void supprimer(TYPE_DEMANDE type, Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                getService().getDemandeModif().deleteById(id);
                break;
            case EXEMP:
                getService().getDemandeExemp().deleteById(id);
                break;
            default:
                getService().getDemandeRecouv().deleteById(id);

        }
    }

    @Override
    @GetMapping(value ="/supprimerDemande")
    public Demande supprimerAvecConservationEnBase(TYPE_DEMANDE type, Integer numDemande, HttpServletRequest request) throws UserExistException, ForbiddenException, DemandeCheckingException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = (DemandeModif) getService().getDemandeModif().findById(numDemande);
                getService().getLigneFichierModif().deleteByDemande(demandeModif);
                return getService().getDemandeModif().changeStateCanceled(demandeModif, Constant.ETATDEM_SUPPRIMEE);
            case EXEMP:
                DemandeExemp demandeExemp = (DemandeExemp) getService().getDemandeExemp().findById(numDemande);
                getService().getLigneFichierExemp().deleteByDemande(demandeExemp);
                return getService().getDemandeExemp().changeStateCanceled(demandeExemp, Constant.ETATDEM_SUPPRIMEE);
            default:
                DemandeRecouv demandeRecouv = (DemandeRecouv) getService().getDemandeRecouv().findById(numDemande);
                getService().getLigneFichierRecouv().deleteByDemande(demandeRecouv);
                return getService().getDemandeRecouv().changeStateCanceled(demandeRecouv, Constant.ETATDEM_SUPPRIMEE);
        }
    }



    /**
     * Webservice de sauvegarde d'une demandeModif via méthode PUT
     * <p>
     * Suppression erreur DTO Persistent entities should not be used as arguments of "@RequestMapping" methods
     *
     * @param id  : identifiant de la demandeModif
     * @param dem : la demandeModif à enregistrer
     * @return : la demandeModif modifiée
     */
    @Override
    @PutMapping(value = "/demandes/{id}")
    @SuppressWarnings("squid:S4684")
    public Demande saveModif(Integer id, DemandeModif dem, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        dem.setNumDemande(id);
        return getService().getDemandeModif().save(dem);
    }

    @Override
    @GetMapping(value = "/getTypeExemplarisationDemande/{id}")
    public String getTypeExemplarisationDemande(Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return getService().getDemandeExemp().getTypeExempDemande(id);
    }


    @Override
    @PutMapping(value = "/demandesExemp/{id}")
    public Demande saveExemp(Integer id, DemandeExemp dem, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        dem.setNumDemande(id);
        return getService().getDemandeExemp().save(dem);
    }

    @Override
    @PutMapping(value = "/demandesRecouv/{id}")
    public Demande saveRecouv(Integer id, DemandeRecouv dem, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        dem.setNumDemande(id);
        return getService().getDemandeRecouv().save(dem);
    }

    @Override
    @PostMapping(value = "/majTypeExemp/{id}")
    public Demande majTypeExemp(Integer id, TypeExemp typeExemp, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return getService().getDemandeExemp().majTypeExemp(id, typeExemp);
    }


    /**
     * Webservice de chargement d'un fichier dans une demande
     *
     * @param type       type de demande concernée par le webservice
     * @param file       : fichier à uploader
     * @param numDemande : demandeModif à laquelle rattacher le fichier
     * @return : messager indiquant le résultat de l'upload
     * @throws ForbiddenException    accès interdit à l'utilisateur (mauvaise authentification)
     * @throws UserExistException    utilisateur non présent dans la base de donnée (id inconnu)
     * @throws FileTypeException     le type de fichier est incorrect, non supporté pour le traitement
     * @throws FileCheckingException
     */
    @Override
    @PostMapping("/uploadDemande")
    public String uploadDemande(TYPE_DEMANDE type, MultipartFile file, Integer numDemande, HttpServletRequest request)
            throws FileTypeException, FileCheckingException, DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        /*autorisation d'accès utilisateur - controle*/
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        Demande demande;
        /*récupération d'une entité demande*/
        switch (type) {
            case MODIF:
                demande = getService().getDemandeModif().findById(numDemande);
                /*si le numero d'utilisateur de l'entité demande correspond au numero d'utilisateur soumis par la requête du client*/
                if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
                    /*Initialisation des fichiers necessaires au traitement de la demande pour la modification*/
                    getService().getDemandeModif().initFiles(demande);
                    /*Stockage du fichier lié à la demande de modification*/
                    return getService().getDemandeModif().stockerFichier(file, demande);
                } else {
                    throw new DemandeCheckingException(Constant.ACCES_REFUSE);
                }
            case EXEMP:
                demande = getService().getDemandeExemp().findById(numDemande);
                if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
                    getService().getDemandeExemp().initFiles(demande);
                    return getService().getDemandeExemp().stockerFichier(file, demande);
                } else {
                    throw new DemandeCheckingException(Constant.ACCES_REFUSE);
                }
            case RECOUV:
                demande = getService().getDemandeRecouv().findById(numDemande);
                if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
                    getService().getDemandeRecouv().initFiles(demande);
                    return getService().getDemandeRecouv().stockerFichier(file, demande);
                } else {
                    throw new DemandeCheckingException(Constant.ACCES_REFUSE);
                }
            default:
                return "Type de demande inconnu";
        }
    }

    /**
     * Webservice de simulation d'une ligne du fichier enrichi
     * @param type : type de demande concernée par le webservice
     * @param numDemande : demande concernée
     * @param numLigne : numéro de la ligne dans le fichier correspondant à la simulation
     * @return : tableau contenant les notices avant et après simulation
     */
    @Override
    @GetMapping("/simulerLigne")
    public String[] simulerLigne(TYPE_DEMANDE type, @RequestParam Integer numDemande, @RequestParam Integer numLigne, HttpServletRequest request)
            throws CBSException, UserExistException, ForbiddenException, ZoneException, CommException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                try {
                    LigneFichierModif ligneFichierModif = getService().getDemandeModif().getLigneFichier((DemandeModif) getService().getDemandeModif().findById(numDemande), numLigne);
                    /*Notice init := notice avant traitement*/
                    Exemplaire noticeInit = getService().getDemandeModif().getNoticeInitiale((DemandeModif) getService().getDemandeModif().findById(numDemande), ligneFichierModif.getEpn());
                    String noticeInitStr = noticeInit.toString().replace("\r", "\r\n");
                    /*Notice traitée := notice après traitement*/
                    Exemplaire noticeTraitee = getService().getDemandeModif().getNoticeTraitee((DemandeModif) getService().getDemandeModif().findById(numDemande), noticeInit, ligneFichierModif);

                    return new String[]{
                            getService().getTraitement().getCbs().getPpnEncours(),
                            noticeInitStr,
                            noticeTraitee.toString().replace("\r", "\r\n")
                    };
                } catch (NullPointerException ex) {
                    throw new NullPointerException(Constant.FILE_END);
                }
            case EXEMP:
                try {
                    DemandeExemp demande = (DemandeExemp) getService().getDemandeExemp().findById(numDemande);
                    LigneFichierExemp ligneFichierExemp = getService().getDemandeExemp().getLigneFichier(demande, numLigne);
                    return getService().getDemandeExemp().getNoticeExemplaireAvantApres(demande, ligneFichierExemp);
                } catch (NullPointerException ex) {
                    throw new NullPointerException(Constant.FILE_END);
                }
            default:
                return new String[]{"Simulation impossible pour le recouvrement", ""};
        }
    }

    /**
     * Webservice permettant de passer une demande en attente
     *
     * @param type       type de demande concernée par le webservice
     * @param numDemande numéro de la demande
     * @param request    requête http
     * @return
     * @throws DemandeCheckingException
     * @throws UserExistException
     * @throws ForbiddenException
     */
    @Override
    @GetMapping("/passerEnAttente")
    public Demande passerEnAttente(TYPE_DEMANDE type, Integer numDemande, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = (DemandeModif) getService().getDemandeModif().findById(numDemande);
                return getService().getDemandeModif().changeState(demandeModif, Constant.ETATDEM_ATTENTE);
            case EXEMP:
                DemandeExemp demandeExemp = (DemandeExemp) getService().getDemandeExemp().findById(numDemande);
                return getService().getDemandeExemp().changeState(demandeExemp, Constant.ETATDEM_ATTENTE);
            default:
                DemandeRecouv demandeRecouv = (DemandeRecouv) getService().getDemandeRecouv().findById(numDemande);
                return getService().getDemandeRecouv().changeState(demandeRecouv, Constant.ETATDEM_ATTENTE);
        }

    }

    /**
     * Webservice permettant d'archiver une demande
     *
     * @param type       type de la demande à archiver
     * @param numDemande numéro de la demande
     * @param request    requête http
     * @return
     * @throws DemandeCheckingException
     * @throws UserExistException
     * @throws ForbiddenException
     */
    @Override
    @GetMapping("/archiverDemande")
    public Demande archiverDemande(TYPE_DEMANDE type, Integer numDemande, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = (DemandeModif) getService().getDemandeModif().findById(numDemande);
                return getService().getDemandeModif().archiverDemande(demandeModif);
            case EXEMP:
                DemandeExemp demandeExemp = (DemandeExemp) getService().getDemandeExemp().findById(numDemande);
                return getService().getDemandeExemp().archiverDemande(demandeExemp);
            default:
                DemandeRecouv demandeRecouv = (DemandeRecouv) getService().getDemandeRecouv().findById(numDemande);
                return getService().getDemandeRecouv().archiverDemande(demandeRecouv);
        }
    }

    /**
     * Webservice permettant de passer à l'étape précédente d'une demande
     *
     * @param type    type de demande concernée
     * @param id      id de la demande
     * @param request requête http
     * @return demande modifiée
     * @throws DemandeCheckingException
     * @throws IOException
     * @throws UserExistException
     * @throws ForbiddenException
     */
    @Override
    @GetMapping("/etapePrecedente/{id}")
    public Demande previousStep(TYPE_DEMANDE type, Integer id, HttpServletRequest request) throws
            DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = (DemandeModif) getService().getDemandeModif().findById(id);
                return getService().getDemandeModif().previousState(demandeModif);
            case EXEMP:
                DemandeExemp demandeExemp = (DemandeExemp) getService().getDemandeExemp().findById(id);
                return getService().getDemandeExemp().previousState(demandeExemp);
            default:
                DemandeRecouv demandeRecouv = (DemandeRecouv) getService().getDemandeRecouv().findById(id);
                return getService().getDemandeRecouv().previousState(demandeRecouv);
        }
    }

    /**
     * @param type type de demande : Exemplarisation, modification, recouvrement
     * @param id id de la demande
     * @param request requete http
     * @return demande modifiée
     * @throws DemandeCheckingException controle demande échoué
     * @throws IOException erreur d'entrée sortie
     * @throws UserExistException utilisateur non trouvé
     * @throws ForbiddenException controle d'accès échoué
     */
    @Override
    @GetMapping("/etapeChoisie/{id}")
    public Demande chosenStep(TYPE_DEMANDE type, Integer id, Integer etape, HttpServletRequest request) throws
            DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                    DemandeModif demandeModif = (DemandeModif) getService().getDemandeModif().findById(id);
                    return getService().getDemandeModif().returnState(etape, demandeModif);
            case EXEMP:
                    DemandeExemp demandeExemp = (DemandeExemp) getService().getDemandeExemp().findById(id);
                    return getService().getDemandeExemp().returnState(etape, demandeExemp);
            case RECOUV:
                throw new DemandeCheckingException(Constant.UNAVAILABLE_SERVICE + type.toString());
            default:
                throw new DemandeCheckingException(Constant.SERVICE_NOT_RECOGNIZE_DEMANDE_TYPE);
        }
    }

    /**
     * Webservice permettant de récupérer le nombre de ligne du fichier d'une demande
     *
     * @param type    type de la demande concernée
     * @param id      id de la demande
     * @param request requête http
     * @return
     * @throws UserExistException
     * @throws ForbiddenException
     */
    @Override
    @GetMapping("/getNbLigneFichier/{id}")
    public Integer getNbLigneFichier(TYPE_DEMANDE type, Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                return getService().getLigneFichierModif().getNbLigneFichierTotalByDemande(id);
            case EXEMP:
                return getService().getLigneFichierExemp().getNbLigneFichierTotalByDemande(id);
            default:
                return getService().getLigneFichierRecouv().getNbLigneFichierTotalByDemande(id);
        }
    }
}
