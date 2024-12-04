package fr.abes.item.web;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.*;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.service.impl.DemandeExempService;
import fr.abes.item.core.service.impl.DemandeModifService;
import fr.abes.item.core.service.impl.DemandeSuppService;
import fr.abes.item.dto.DemandeWebDto;
import fr.abes.item.dto.DtoBuilder;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v1")
public class DemandeRestService {
    private static final String LOGIN_MANAGER_INCONNU = "Pas de login rattaché au RCR de la demande, veuillez contacter l'assistance";
    private final DemandeExempService demandeExempService;
    private final DemandeModifService demandeModifService;
    private final DemandeSuppService demandeSuppService;
    private final CheckAccessToServices checkAccessToServices;
    private final DtoBuilder builder;
    private final StrategyFactory strategy;

    public DemandeRestService(DemandeExempService demandeExempService, DemandeModifService demandeModifService, DemandeSuppService demandeSuppService, CheckAccessToServices checkAccessToServices, DtoBuilder builder, StrategyFactory strategy) {
        this.demandeExempService = demandeExempService;
        this.demandeModifService = demandeModifService;
        this.demandeSuppService = demandeSuppService;
        this.checkAccessToServices = checkAccessToServices;
        this.builder = builder;
        this.strategy = strategy;
    }

    /**
     * Webservice : retour de l'ensemble des demandes pour un administrateur
     *
     * @param type    type de demande concernée par le webservice
     * @param request le requete avec ses attributs
     * @return Une liste de demandes
     */
    @GetMapping(value = "/demandes/{type}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "renvoie les demandes en fonction du rôle de l'utilisateur",
            description = "renvoie les demande terminées et en erreur de tout le monde et toutes les demande créées par cet iln")
    public List<DemandeWebDto> getAllActiveDemandes(@PathVariable("type") TYPE_DEMANDE type, @RequestParam("archive") boolean archive, @RequestParam("extension") boolean extension, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = ((UserDetails)authentication.getPrincipal()).getAuthorities().stream().findFirst().get().toString();

        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        if (role.equals("ADMIN")) {
            if (archive) {
                return (!extension) ? service.getAllArchivedDemandes(iln).stream().map(element -> builder.buildDemandeDtoWithNbLines(element, type)).collect(Collectors.toList()) : service.getAllArchivedDemandesAllIln().stream().map(element -> builder.buildDemandeDtoWithNbLines(element, type)).collect(Collectors.toList());
            }
            else {
                return (!extension) ? service.getAllActiveDemandesForAdmin(iln).stream().map(element -> builder.buildDemandeDtoWithNbLines(element, type)).collect(Collectors.toList()) : service.getAllActiveDemandesForAdminExtended().stream().map(element -> builder.buildDemandeDtoWithNbLines(element, type)).collect(Collectors.toList());
            }
        }
        //role USER
        return (archive) ? service.getAllArchivedDemandes(iln).stream().map(element -> builder.buildDemandeDtoWithNbLines(element, type)).collect(Collectors.toList()) : service.getActiveDemandesForUser(iln).stream().map(element -> builder.buildDemandeDtoWithNbLines(element, type)).collect(Collectors.toList());
    }

    /**
     * Webservice de récupération d'une demande par son identifiant
     *
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif
     * @return demandeModif correspondant à la recherche
     */
    @GetMapping(value = "/demandes/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "renvoie une demande précise")
    public DemandeWebDto getDemande(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDemandeDto(service.findById(id), type);
    }

    /**
     * Webservice de sauvegarde d'une demande via formulaire
     *
     * @param type type de demande concernée par le webservice
     * @param rcr  : rcr de la demandeModif à enregistrer
     * @return : la demandé modifiée
     */
    @PostMapping(value = "/demandes/{type}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de créer une nouvelle demande pour un rcr donné")
    public DemandeWebDto save(@PathVariable("type") TYPE_DEMANDE type, @RequestParam("rcr") String rcr, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserCreationDemandeParUserNum(rcr, request.getAttribute(Constant.USER_NUM).toString());
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        Demande demToReturn = service.creerDemande(rcr, Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()));
        return builder.buildDemandeDto(demToReturn, type);
    }

    @PatchMapping(value = "/demandes/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    public DemandeWebDto modifDemande(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, @RequestParam("rcr") Optional<String> rcr, @RequestParam("typeExemp") Optional<Integer> typeExemp, @RequestParam("typeSupp") Optional<TYPE_SUPPRESSION> typeSupp, @RequestParam("traitement") Optional<Integer> traitement, @RequestParam("commentaire") Optional<String> commentaire, HttpServletRequest request) throws ForbiddenException, UserExistException, UnknownDemandeException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        Demande demande = service.findById(id);
        if (demande != null) {
            if (rcr.isPresent()) {
                demande.setRcr(rcr.get());
                service.modifierShortNameDemande(demande);
                return builder.buildDemandeDto(service.save(demande), type);
            }
            if (type.equals(TYPE_DEMANDE.EXEMP) && typeExemp.isPresent()) {
                return builder.buildDemandeDto(demandeExempService.majTypeExemp(id, typeExemp.get()), type);
            }
            if (type.equals(TYPE_DEMANDE.MODIF) && traitement.isPresent()) {
                return builder.buildDemandeDto(demandeModifService.majTraitement(id, traitement.get()), type);
            }
            if (type.equals(TYPE_DEMANDE.SUPP) && typeSupp.isPresent()) {
                return builder.buildDemandeDto(demandeSuppService.majTypeSupp(id, typeSupp.get()), type);
            }
            if (commentaire.isPresent()) {
                demande.setCommentaire(commentaire.get());
                return builder.buildDemandeDto(service.save(demande), type);
            }
        }
        throw new UnknownDemandeException("Demande inconnue");
    }

    /**
     * Webservice de suppression d'une demandeModif
     *
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif à supprimer
     */
    @DeleteMapping(value = "/demandes/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de supprimer une demande")
    public void supprimer(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        service.deleteById(id);
    }

    @GetMapping(value = "/supprimerDemande/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de supprimer une demande tout en la conservant en base, elle passe en statut 10 invisible pour l'utilisateur sur l'interface web")
    public DemandeWebDto supprimerAvecConservationEnBase(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer numDemande, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        ILigneFichierService ligneFichierService = strategy.getStrategy(ILigneFichierService.class, type);
        Demande demande = service.findById(numDemande);
        ligneFichierService.deleteByDemande(demande);
        return builder.buildDemandeDto(service.changeStateCanceled(demande, Constant.ETATDEM_SUPPRIMEE), type);
    }

    @GetMapping(value = "/getTypeExemplarisationDemande/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permer de récupérer le type d'exemplarisation choisi pour une demande")
    public String getTypeExemplarisationDemande(@PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), TYPE_DEMANDE.EXEMP);
        return demandeExempService.getLibelleTypeExempDemande(id);
    }


    /**
     * Webservice de chargement d'un fichier dans une demande
     *
     * @param type       type de demande concernée par le webservice
     * @param file       : fichier à uploader
     * @param numDemande : demandeModif à laquelle rattacher le fichier
     * @throws ForbiddenException    accès interdit à l'utilisateur (mauvaise authentification)
     * @throws UserExistException    utilisateur non présent dans la base de donnée (id inconnu)
     * @throws FileTypeException     le type de fichier est incorrect, non supporté pour le traitement
     * @throws FileCheckingException erreur dans la vérification du fichier
     */
    @PostMapping("/uploadDemande/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de charger le fichier pour une demande")
    public void uploadDemande(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer numDemande, @RequestParam("file") MultipartFile file, HttpServletRequest request)
            throws FileTypeException, FileCheckingException, DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        Demande demande = service.findById(numDemande);
        if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
            service.initFiles(demande);
            service.stockerFichier(file, demande);
        } else {
            throw new DemandeCheckingException(Constant.ACCES_REFUSE);
        }
    }

    /**
     * Webservice de simulation d'une ligne du fichier enrichi
     *
     * @param type       : type de demande concernée par le webservice
     * @param numDemande : demande concernée
     * @param numLigne   : numéro de la ligne dans le fichier correspondant à la simulation
     * @return : tableau contenant les notices avant et après simulation
     */
    @GetMapping("/simulerLigne/{type}/{id}/{numLigne}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de simuler la modification d'un exemplaire", description = "pour un exemplaire donné du fichier enrichi, renvoie un tableau contenant la notice avant et après modification")
    public String[] simulerLigne(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer numDemande, @PathVariable("numLigne") Integer numLigne, HttpServletRequest request)
            throws CBSException, UserExistException, ForbiddenException, ZoneException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        ILigneFichierService ligneFichierService = strategy.getStrategy(ILigneFichierService.class, type);
        try {
            Demande demande = service.findById(numDemande);
            LigneFichier ligneFichier = ligneFichierService.getLigneFichierbyDemandeEtPos(demande, numLigne);
            return ligneFichierService.getNoticeExemplaireAvantApres(demande, ligneFichier);
        } catch (CBSException e) {
            //adaptation du message en cas de login manager manquant
            if (e.getMessage().equals("Code d'accès non reconnu"))
                throw new CBSException(Level.ERROR, LOGIN_MANAGER_INCONNU);
            else
                throw e;
        } catch (IOException e) {
            log.error(e.getMessage());
            throw new CBSException(Level.ERROR, Constant.FILE_END);
        }
    }

    /**
     * Webservice permettant de passer une demande en attente
     *
     * @param type       type de demande concernée par le webservice
     * @param numDemande numéro de la demande
     * @param request    requête http
     * @return la demande modifiée
     * @throws DemandeCheckingException : erreur dans la vérification de la demande
     * @throws UserExistException       : erreur sur l'utilisateur accédant à la méthode
     * @throws ForbiddenException       : erreur d'accès à la méthode
     */
    @PatchMapping("/passerEnAttente/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de modifier le statut de la demande pour la passer à : en attente")
    public DemandeWebDto passerEnAttente(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer numDemande, HttpServletRequest request) throws DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDemandeDto(service.changeState(service.findById(numDemande), Constant.ETATDEM_ATTENTE), type);
    }

    /**
     * Webservice permettant d'archiver une demande
     *
     * @param type       type de la demande à archiver
     * @param numDemande numéro de la demande
     * @param request    requête http
     * @return la demande archivée
     * @throws DemandeCheckingException : controle demande échoué
     * @throws UserExistException       utilisateur non trouvé
     * @throws ForbiddenException       controle d'accès échoué
     */
    @GetMapping("/archiverDemande/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de passer la demande en statut archivé")
    public DemandeWebDto archiverDemande(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer numDemande, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDemandeDto(service.archiverDemande(service.findById(numDemande)), type);
    }

    /**
     * Webservice permettant de passer à l'étape précédente d'une demande
     *
     * @param type    type de demande concernée
     * @param id      id de la demande
     * @param request requête http
     * @return demande modifiée
     * @throws DemandeCheckingException : controle demande échoué
     * @throws IOException              : erreur dans l'accès au fichier de la demande
     * @throws UserExistException       utilisateur non trouvé
     * @throws ForbiddenException       controle d'accès échoué
     */
    @PatchMapping("/etapePrecedente/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de revenir à l'étape précédente dans le workflow de création d'une demande")
    public DemandeWebDto previousStep(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws
            DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDemandeDto(service.previousState(service.findById(id)), type);
    }

    /**
     * @param type    type de demande : Exemplarisation, modification, recouvrement
     * @param id      id de la demande
     * @param request requete http
     * @return demande modifiée
     * @throws DemandeCheckingException controle demande échoué
     * @throws UserExistException       utilisateur non trouvé
     * @throws ForbiddenException       controle d'accès échoué
     */
    @PatchMapping("/etapeChoisie/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de revenir à une étape bien précise dans le workflow de création d'une demande")
    public DemandeWebDto chosenStep(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, @RequestParam("etape") Integer etape, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDemandeDto(service.returnState(etape, service.findById(id)), type);
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
    @GetMapping("/nbLignesFichier/{type}/{id}")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "permet de récupérer le nombre de ligne du fichier enrichi d'une demande")
    public Integer getNbLigneFichier(@PathVariable("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        ILigneFichierService ligneFichierService = strategy.getStrategy(ILigneFichierService.class, type);
        return ligneFichierService.getNbLigneFichierTotalByDemande(service.findById(id));
    }


    @PatchMapping("stopDemandeSupp/{id}")
    public DemandeWebDto stopDemandeSupp(@PathVariable("id") Integer id, HttpServletRequest request) throws ForbiddenException, UserExistException, UnknownDemandeException, DemandeCheckingException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), TYPE_DEMANDE.SUPP);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, TYPE_DEMANDE.SUPP);
        DemandeSupp demandeSupp = (DemandeSupp) service.findById(id);
        if(demandeSupp != null){
            return builder.buildDemandeDto(service.changeState(demandeSupp, Constant.ETATDEM_INTEROMPU),TYPE_DEMANDE.SUPP);
        }
        throw new UnknownDemandeException("Demande inconnue");

    }
}
