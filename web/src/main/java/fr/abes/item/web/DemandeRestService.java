package fr.abes.item.web;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.*;
import fr.abes.item.core.service.JournalService;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.core.service.impl.*;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Date;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class DemandeRestService {
    private final DemandeExempService demandeExempService;
    private final DemandeRecouvService demandeRecouvService;
    private final DemandeModifService demandeModifService;
    private final LigneFichierExempService ligneFichierExempService;
    private final LigneFichierModifService ligneFichierModifService;
    private final LigneFichierRecouvService ligneFichierRecouvService;
    private final ReferenceService referenceService;
    private final JournalService journalService;
    private final UtilisateurService utilisateurService;
    private final TraitementService traitementService;
    private final CheckAccessToServices checkAccessToServices;

    public DemandeRestService(DemandeExempService demandeExempService, DemandeRecouvService demandeRecouvService, DemandeModifService demandeModifService, LigneFichierExempService ligneFichierExempService, LigneFichierModifService ligneFichierModifService, LigneFichierRecouvService ligneFichierRecouvService, ReferenceService referenceService, JournalService journalService, UtilisateurService utilisateurService, TraitementService traitementService, CheckAccessToServices checkAccessToServices) {
        this.demandeExempService = demandeExempService;
        this.demandeRecouvService = demandeRecouvService;
        this.demandeModifService = demandeModifService;
        this.ligneFichierExempService = ligneFichierExempService;
        this.ligneFichierModifService = ligneFichierModifService;
        this.ligneFichierRecouvService = ligneFichierRecouvService;
        this.referenceService = referenceService;
        this.journalService = journalService;
        this.utilisateurService = utilisateurService;
        this.traitementService = traitementService;
        this.checkAccessToServices = checkAccessToServices;
    }

    /**
     * Webservice : retour de l'ensemble des demandes pour un administrateur
     * @param type    type de demande concernée par le webservice
     * @param request le requete avec ses attributs
     * @return Une liste de demandes
     */
    @GetMapping(value = "/demandes")
    @PreAuthorize("hasAuthority('ADMIN')")
    @Operation(summary = "renvoie les demandes pour les administrateurs",
            description = "renvoie les demande terminées et en erreur de tout le monde et toutes les demandeModifs créées par cet iln")
    public List<Demande> getAllActiveDemandes(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("extension")boolean extension, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        return switch (type) {
            case EXEMP ->
                    (!extension) ? demandeExempService.getAllActiveDemandesForAdmin(iln) : demandeExempService.getAllActiveDemandesForAdminExtended();
            case MODIF ->
                    (!extension) ? demandeModifService.getAllActiveDemandesForAdmin(iln) : demandeModifService.getAllActiveDemandesForAdminExtended();
            default ->
                    (!extension) ? demandeRecouvService.getAllActiveDemandesForAdmin(iln) : demandeRecouvService.getAllActiveDemandesForAdminExtended();
        };
    }

    /**
     * Webservice : retour de l'ensemble des demandes pour un utilisateur
     * @return liste des demandeModifs non archivées de l'utilisateur
     */
    @GetMapping(value = "/chercherDemandes")
    @PreAuthorize("hasAuthority('USER')")
    @Operation(summary = "renvoie les demandes de modif pour ce usernum",
            description = "renvoie toutes les demandes créées par cet iln")
    public List<Demande> chercher(@RequestParam("type") TYPE_DEMANDE type, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        return switch (type) {
            case MODIF -> demandeModifService.getActiveDemandesForUser(iln);
            case EXEMP -> demandeExempService.getActiveDemandesForUser(iln);
            default -> demandeRecouvService.getActiveDemandesForUser(iln);
        };
    }

    /**
     * Webservices : retour des demandes archivées
     * @param type type de demande concernée par le webservice
     * @return liste des demandes archivées de l'utilisateur
     */
    @GetMapping(value = "/chercherArchives")
    @PreAuthorize("hasAnyAuthority('USER', 'ADMIN')")
    @Operation(summary = "renvoie les demandes archivées pour cet iln",
            description = "renvoie les demandeModifs archivées créées par cet iln")
    public List<Demande> getAllArchivedDemandes(@RequestParam("type")TYPE_DEMANDE type, @RequestParam("extension") boolean extension, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        return switch (type) {
            case MODIF ->
                    (!extension) ? demandeModifService.getAllArchivedDemandes(iln) : demandeModifService.getAllArchivedDemandesAllIln();
            case EXEMP ->
                    (!extension) ? demandeExempService.getAllArchivedDemandes(iln) : demandeExempService.getAllArchivedDemandesAllIln();
            default ->
                    (!extension) ? demandeRecouvService.getAllArchivedDemandes(iln) : demandeRecouvService.getAllArchivedDemandesAllIln();
        };
    }

    /**
     * Webservice de récupération d'une demandeModif par son identifiant
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif
     * @return demandeModif correspondant à la recherche
     */
    @GetMapping(value = "/demandes/{id}")
    @Operation(summary = "renvoie une demande précise")
    public Demande getDemande(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return switch (type) {
            case MODIF -> demandeModifService.findById(id);
            case EXEMP -> demandeExempService.findById(id);
            default -> demandeRecouvService.findById(id);
        };
    }

    /**
     * Webservice de sauvegarde d'une demandeModif via formulaire
     *
     * @param type type de demande concernée par le webservice
     * @param rcr  : rcr de la demandeModif à enregistrer
     * @return : la demandé modifiée
     */
    @GetMapping(value = "/creerdemande")
    @Operation(summary = "permet de créer une nouvelle demande de  modif pour un rcr donné")
    public Demande saveModif(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("rcr") String rcr, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserCreationDemandeParUserNum(rcr, request.getAttribute(Constant.USER_NUM).toString());
        Date datejour = new Date();
        Demande demToReturn;
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = demandeModifService.creerDemande(rcr, datejour, datejour, "", "", "", referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), utilisateurService.findById(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString())), null);
                demToReturn = demandeModifService.save(demandeModif);
                journalService.addEntreeJournal((DemandeModif) demToReturn, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION));
                break;
            case EXEMP:
                DemandeExemp demandeExemp = demandeExempService.creerDemande(rcr, datejour, datejour, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), "", utilisateurService.findById(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString())));
                demToReturn = demandeExempService.save(demandeExemp);
                journalService.addEntreeJournal((DemandeExemp) demToReturn, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION));
                break;
            default:
                DemandeRecouv demandeRecouv = demandeRecouvService.creerDemande(rcr, datejour, datejour, referenceService.findEtatDemandeById(Constant.ETATDEM_PREPARATION), "", utilisateurService.findById(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString())));
                demToReturn = demandeRecouvService.save(demandeRecouv);

        }
        return demToReturn;
    }

    /**
     * Webservice de suppression d'une demandeModif
     *
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif à supprimer
     */
    @DeleteMapping(value = "/demandes/{id}")
    @Operation(summary = "permet de supprimer une demande")
    public void supprimer(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                demandeModifService.deleteById(id);
                break;
            case EXEMP:
                demandeExempService.deleteById(id);
                break;
            default:
                demandeRecouvService.deleteById(id);

        }
    }

    @GetMapping(value ="/supprimerDemande")
    @Operation(summary = "permet de supprimer une demande tout en la conservant en base, elle passe en statut 10 invisible pour l'utilisateur sur l'interface web")
    public Demande supprimerAvecConservationEnBase(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande")Integer numDemande, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                DemandeModif demandeModif = demandeModifService.findById(numDemande);
                ligneFichierModifService.deleteByDemande(demandeModif);
                return demandeModifService.changeStateCanceled(demandeModif, Constant.ETATDEM_SUPPRIMEE);
            case EXEMP:
                DemandeExemp demandeExemp = demandeExempService.findById(numDemande);
                ligneFichierExempService.deleteByDemande(demandeExemp);
                return demandeExempService.changeStateCanceled(demandeExemp, Constant.ETATDEM_SUPPRIMEE);
            default:
                DemandeRecouv demandeRecouv = demandeRecouvService.findById(numDemande);
                ligneFichierRecouvService.deleteByDemande(demandeRecouv);
                return demandeRecouvService.changeStateCanceled(demandeRecouv, Constant.ETATDEM_SUPPRIMEE);
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
    @PutMapping(value = "/demandes/{id}")
    @Operation(summary= "permet de créer une nouvelle demande de modif")
    @SuppressWarnings("squid:S4684")
    public Demande saveModif(@PathVariable("id") Integer id, @RequestParam("dem") DemandeModif dem, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        dem.setNumDemande(id);
        return demandeModifService.save(dem);
    }


    @GetMapping(value = "/getTypeExemplarisationDemande/{id}")
    @Operation(summary = "permer de récupérer le type d'exemplarisation choisi pour une demande")
    public String getTypeExemplarisationDemande(@PathVariable("id")Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return demandeExempService.getTypeExempDemande(id);
    }


    @PutMapping(value = "/demandesExemp/{id}")
    @Operation(summary = "Mise à jour du type d'exemplarisation")
    public Demande saveExemp(@PathVariable("id")Integer id, @RequestParam("dem")DemandeExemp dem, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        dem.setNumDemande(id);
        return demandeExempService.save(dem);
    }

    @PutMapping(value = "/demandesRecouv/{id}")
    @Operation(summary = "Mise à jour d'une demande de recouvrement'")
    public Demande saveRecouv(@PathVariable("id")Integer id, @RequestParam("dem")DemandeRecouv dem, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        dem.setNumDemande(id);
        return demandeRecouvService.save(dem);
    }

    @PostMapping(value = "/majTypeExemp/{id}")
    public Demande majTypeExemp(@PathVariable("id")Integer id, @RequestParam("type") Integer type, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return demandeExempService.majTypeExemp(id, type);
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
     * @throws FileCheckingException erreur dans la vérification du fichier
     */
    @PostMapping("/uploadDemande")
    @Operation(summary = "permet de charger le fichier pour une demande")
    public String uploadDemande(@RequestParam("type")TYPE_DEMANDE type, @RequestParam("file")MultipartFile file, @RequestParam("numDemande")Integer numDemande, HttpServletRequest request)
            throws FileTypeException, FileCheckingException, DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        /*autorisation d'accès utilisateur - controle*/
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        Demande demande;
        /*récupération d'une entité demande*/
        switch (type) {
            case MODIF:
                demande = demandeModifService.findById(numDemande);
                /*si le numero d'utilisateur de l'entité demande correspond au numero d'utilisateur soumis par la requête du client*/
                if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
                    /*Initialisation des fichiers necessaires au traitement de la demande pour la modification*/
                    demandeModifService.initFiles(demande);
                    /*Stockage du fichier lié à la demande de modification*/
                    return demandeModifService.stockerFichier(file, demande);
                } else {
                    throw new DemandeCheckingException(Constant.ACCES_REFUSE);
                }
            case EXEMP:
                demande = demandeExempService.findById(numDemande);
                if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
                    demandeExempService.initFiles(demande);
                    return demandeExempService.stockerFichier(file, demande);
                } else {
                    throw new DemandeCheckingException(Constant.ACCES_REFUSE);
                }
            case RECOUV:
                demande = demandeRecouvService.findById(numDemande);
                if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
                    demandeRecouvService.initFiles(demande);
                    return demandeRecouvService.stockerFichier(file, demande);
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
    @GetMapping("/simulerLigne")
    @Operation(summary = "permet de simuler la modification d'un exemplaire", description="pour un exemplaire donné du fichier enrichi, renvoie un tableau contenant la notice avant et après modification")
    public String[] simulerLigne(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, @RequestParam("numLigne") Integer numLigne, HttpServletRequest request)
            throws CBSException, UserExistException, ForbiddenException, ZoneException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        switch (type) {
            case MODIF:
                try {
                    LigneFichierModif ligneFichierModif = (LigneFichierModif) ligneFichierModifService.getLigneFichierbyDemandeEtPos(demandeModifService.findById(numDemande), numLigne);
                    /*Notice init := notice avant traitement*/
                    String noticeInit = demandeModifService.getNoticeInitiale(demandeModifService.findById(numDemande), ligneFichierModif.getEpn());
                    String noticeInitStr = noticeInit.replace("\r", "\r\n");
                    /*Notice traitée := notice après traitement*/
                    Exemplaire noticeTraitee = demandeModifService.getNoticeTraitee(demandeModifService.findById(numDemande), noticeInit, ligneFichierModif);

                    return new String[]{
                            traitementService.getCbs().getPpnEncours(),
                            noticeInitStr,
                            noticeTraitee.toString().replace("\r", "\r\n")
                    };
                } catch (NullPointerException | IOException ex) {
                    throw new NullPointerException(Constant.FILE_END);
                }
            case EXEMP:
                try {
                    DemandeExemp demande = demandeExempService.findById(numDemande);
                    LigneFichierExemp ligneFichierExemp = (LigneFichierExemp) ligneFichierExempService.getLigneFichierbyDemandeEtPos(demande, numLigne);
                    return demandeExempService.getNoticeExemplaireAvantApres(demande, ligneFichierExemp);
                } catch (NullPointerException | IOException ex) {
                    throw new NullPointerException(Constant.FILE_END);
                }
            default:
                return new String[]{"Simulation impossible pour le recouvrement", ""};
        }
    }

    /**
     * Webservice permettant de passer une demande en attente
     * @param type       type de demande concernée par le webservice
     * @param numDemande numéro de la demande
     * @param request    requête http
     * @return la demande modifiée
     * @throws DemandeCheckingException : erreur dans la vérification de la demande
     * @throws UserExistException : erreur sur l'utilisateur accédant à la méthode
     * @throws ForbiddenException : erreur d'accès à la méthode
     */
    @GetMapping("/passerEnAttente")
    @Operation(summary = "permet de modifier le statut de la demande pour la passer à : en attente")
    public Demande passerEnAttente(@RequestParam("type")TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, HttpServletRequest request) throws DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        return switch (type) {
            case MODIF -> {
                DemandeModif demandeModif = demandeModifService.findById(numDemande);
                yield demandeModifService.changeState(demandeModif, Constant.ETATDEM_ATTENTE);
            }
            case EXEMP -> {
                DemandeExemp demandeExemp = demandeExempService.findById(numDemande);
                yield demandeExempService.changeState(demandeExemp, Constant.ETATDEM_ATTENTE);
            }
            default -> {
                DemandeRecouv demandeRecouv = demandeRecouvService.findById(numDemande);
                yield demandeRecouvService.changeState(demandeRecouv, Constant.ETATDEM_ATTENTE);
            }
        };

    }

    /**
     * Webservice permettant d'archiver une demande
     *
     * @param type       type de la demande à archiver
     * @param numDemande numéro de la demande
     * @param request    requête http
     * @return la demande archivée
     * @throws DemandeCheckingException : controle demande échoué
     * @throws UserExistException utilisateur non trouvé
     * @throws ForbiddenException controle d'accès échoué
     */
    @GetMapping("/archiverDemande")
    @Operation(summary = "permet de passer la demande en statut archivé")
    public Demande archiverDemande(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString());
        return switch (type) {
            case MODIF -> {
                DemandeModif demandeModif = demandeModifService.findById(numDemande);
                yield demandeModifService.archiverDemande(demandeModif);
            }
            case EXEMP -> {
                DemandeExemp demandeExemp = demandeExempService.findById(numDemande);
                yield demandeExempService.archiverDemande(demandeExemp);
            }
            default -> {
                DemandeRecouv demandeRecouv = demandeRecouvService.findById(numDemande);
                yield demandeRecouvService.archiverDemande(demandeRecouv);
            }
        };
    }

    /**
     * Webservice permettant de passer à l'étape précédente d'une demande
     *
     * @param type    type de demande concernée
     * @param id      id de la demande
     * @param request requête http
     * @return demande modifiée
     * @throws DemandeCheckingException : controle demande échoué
     * @throws IOException : erreur dans l'accès au fichier de la demande
     * @throws UserExistException utilisateur non trouvé
     * @throws ForbiddenException controle d'accès échoué
     */
    @GetMapping("/etapePrecedente/{id}")
    @Operation(summary = "permet de revenir à l'étape précédente dans le workflow de création d'une demande")
    public Demande previousStep(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws
            DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return switch (type) {
            case MODIF -> {
                DemandeModif demandeModif = demandeModifService.findById(id);
                yield demandeModifService.previousState(demandeModif);
            }
            case EXEMP -> {
                DemandeExemp demandeExemp = demandeExempService.findById(id);
                yield demandeExempService.previousState(demandeExemp);
            }
            default -> {
                DemandeRecouv demandeRecouv = demandeRecouvService.findById(id);
                yield demandeRecouvService.previousState(demandeRecouv);
            }
        };
    }

    /**
     * @param type type de demande : Exemplarisation, modification, recouvrement
     * @param id id de la demande
     * @param request requete http
     * @return demande modifiée
     * @throws DemandeCheckingException controle demande échoué
     * @throws UserExistException utilisateur non trouvé
     * @throws ForbiddenException controle d'accès échoué
     */
    @GetMapping("/etapeChoisie/{id}")
    @Operation(summary = "permet de revenir à une étape bien précise dans le workflow de création d'une demande")
    public Demande chosenStep(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, @RequestParam("etape") Integer etape, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        return switch (type) {
            case MODIF -> {
                DemandeModif demandeModif = demandeModifService.findById(id);
                yield demandeModifService.returnState(etape, demandeModif);
            }
            case EXEMP -> {
                DemandeExemp demandeExemp = demandeExempService.findById(id);
                yield demandeExempService.returnState(etape, demandeExemp);
            }
            case RECOUV -> throw new DemandeCheckingException(Constant.UNAVAILABLE_SERVICE + type);
        };
    }

    /**
     * Webservice permettant de récupérer le nombre de ligne du fichier d'une demande
     *
     * @param type    type de la demande concernée
     * @param id      id de la demande
     * @param request requête http
     * @return le nombre de lignes du fichier correspondant à la demande
     * @throws UserExistException utilisateur non trouvé
     * @throws ForbiddenException controle d'accès échoué
     */
    @GetMapping("/getNbLigneFichier/{id}")
    @Operation(summary = "permet de récupérer le nombre de ligne du fichier enrichi d'une demande")
    public Integer getNbLigneFichier(@RequestParam("type")TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString());
        Demande demande;
        return switch (type) {
            case MODIF -> {
                demande = demandeModifService.findById(id);
                yield ligneFichierModifService.getNbLigneFichierTotalByDemande(demande);
            }
            case EXEMP -> {
                demande = demandeModifService.findById(id);
                yield ligneFichierExempService.getNbLigneFichierTotalByDemande(demande);
            }
            default -> {
                demande = demandeModifService.findById(id);
                yield ligneFichierRecouvService.getNbLigneFichierTotalByDemande(demande);
            }
        };
    }
}
