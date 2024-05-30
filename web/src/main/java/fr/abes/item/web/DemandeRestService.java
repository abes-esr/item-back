package fr.abes.item.web;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.configuration.factory.StrategyFactory;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.*;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.service.impl.DemandeExempService;
import fr.abes.item.dto.DemandeWebDto;
import fr.abes.item.dto.DtoBuilder;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

//TODO : vérifier les autorisations sur les accès controller : fait en partie
@Slf4j
@RestController
@RequestMapping("/api/v1")
public class DemandeRestService {
    private final DemandeExempService demandeExempService;
    private final CheckAccessToServices checkAccessToServices;
    private final DtoBuilder builder;
    private final StrategyFactory strategy;

    public DemandeRestService(DemandeExempService demandeExempService, CheckAccessToServices checkAccessToServices, DtoBuilder builder, StrategyFactory strategy) {
        this.demandeExempService = demandeExempService;
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
    @GetMapping(value = "/demandes")
    @PreAuthorize("hasAnyAuthority('USER','ADMIN')")
    @Operation(summary = "renvoie les demandes en fonction du rôle de l'utilisateur",
            description = "renvoie les demande terminées et en erreur de tout le monde et toutes les demande créées par cet iln")
    public List<DemandeWebDto> getAllActiveDemandes(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("archive") boolean archive, @RequestParam("extension") boolean extension, HttpServletRequest request) {
        String iln = request.getAttribute("iln").toString();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String role = ((UserDetails)authentication.getPrincipal()).getAuthorities().stream().findFirst().get().toString();

        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        if (role.equals("ADMIN")) {
            if (archive) {
                return (!extension) ? service.getAllArchivedDemandes(iln).stream().map(element -> builder.buildDto(element, type)).collect(Collectors.toList()) : service.getAllArchivedDemandesAllIln().stream().map(element -> builder.buildDto(element, type)).collect(Collectors.toList());
            }
            else {
                return (!extension) ? service.getAllActiveDemandesForAdmin(iln).stream().map(element -> builder.buildDto(element, type)).collect(Collectors.toList()) : service.getAllActiveDemandesForAdminExtended().stream().map(element -> builder.buildDto(element, type)).collect(Collectors.toList());
            }
        }
        //role USER
        return (archive) ? service.getAllArchivedDemandes(iln).stream().map(element -> builder.buildDto(element, type)).collect(Collectors.toList()) : service.getActiveDemandesForUser(iln).stream().map(element -> builder.buildDto(element, type)).collect(Collectors.toList());
    }

    /**
     * Webservice de récupération d'une demandeModif par son identifiant
     *
     * @param type type de demande concernée par le webservice
     * @param id   : identifiant de la demandeModif
     * @return demandeModif correspondant à la recherche
     */
    @GetMapping(value = "/demandes/{id}")
    @Operation(summary = "renvoie une demande précise")
    public DemandeWebDto getDemande(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDto(service.findById(id), type);
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
    public DemandeWebDto save(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("rcr") String rcr, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserCreationDemandeParUserNum(rcr, request.getAttribute(Constant.USER_NUM).toString());
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        Demande demande = service.creerDemande(rcr, Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()));
        Demande demToReturn = service.save(demande);
        return builder.buildDto(demToReturn, type);
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
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        service.deleteById(id);
    }

    @GetMapping(value = "/supprimerDemande")
    @Operation(summary = "permet de supprimer une demande tout en la conservant en base, elle passe en statut 10 invisible pour l'utilisateur sur l'interface web")
    public DemandeWebDto supprimerAvecConservationEnBase(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        ILigneFichierService ligneFichierService = strategy.getStrategy(ILigneFichierService.class, type);
        Demande demande = service.findById(numDemande);
        ligneFichierService.deleteByDemande(demande);
        return builder.buildDto(service.changeStateCanceled(demande, Constant.ETATDEM_SUPPRIMEE), type);
    }


    /**
     * Webservice de sauvegarde d'une demande via méthode PUT
     * <p>
     * Suppression erreur DTO Persistent entities should not be used as arguments of "@RequestMapping" methods
     *
     * @param id  : identifiant de la demandeModif
     * @param dem : la demandeModif à enregistrer
     * @return : la demandeModif modifiée
     */
    @PutMapping(value = "/demandes/{id}")
    @Operation(summary = "permet de créer une nouvelle demande")
    @SuppressWarnings("squid:S4684")
    public DemandeWebDto save(@PathVariable("id") Integer id, @RequestParam("dem") Demande dem, @RequestParam("type") TYPE_DEMANDE type, HttpServletRequest request) throws UserExistException, ForbiddenException {
        //TODO : a revoir, ne devrait pas recevoir un objet entier en requestParam
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        dem.setNumDemande(id);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDto(service.save(dem), type);
    }


    @GetMapping(value = "/getTypeExemplarisationDemande/{id}")
    @Operation(summary = "permer de récupérer le type d'exemplarisation choisi pour une demande")
    public String getTypeExemplarisationDemande(@PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), TYPE_DEMANDE.EXEMP);
        return demandeExempService.getLibelleTypeExempDemande(id);
    }

    @PostMapping(value = "/majTypeExemp/{id}")
    public DemandeWebDto majTypeExemp(@PathVariable("id") Integer id, @RequestParam("type") Integer type, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), TYPE_DEMANDE.EXEMP);
        return builder.buildDto(demandeExempService.majTypeExemp(id, type), TYPE_DEMANDE.EXEMP);
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
    public String uploadDemande(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("file") MultipartFile file, @RequestParam("numDemande") Integer numDemande, HttpServletRequest request)
            throws FileTypeException, FileCheckingException, DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        Demande demande = service.findById(numDemande);
        if (demande.getUtilisateur().getNumUser().equals(Integer.parseInt(request.getAttribute(Constant.USER_NUM).toString()))) {
            service.initFiles(demande);
            return service.stockerFichier(file, demande);
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
    @GetMapping("/simulerLigne")
    @Operation(summary = "permet de simuler la modification d'un exemplaire", description = "pour un exemplaire donné du fichier enrichi, renvoie un tableau contenant la notice avant et après modification")
    public String[] simulerLigne(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, @RequestParam("numLigne") Integer numLigne, HttpServletRequest request)
            throws CBSException, UserExistException, ForbiddenException, ZoneException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        ILigneFichierService ligneFichierService = strategy.getStrategy(ILigneFichierService.class, type);
        try {
            Demande demande = service.findById(numDemande);
            LigneFichier ligneFichier = ligneFichierService.getLigneFichierbyDemandeEtPos(demande, numLigne);
            return service.getNoticeExemplaireAvantApres(demande, ligneFichier);
        } catch (IOException e) {
            throw new NullPointerException(Constant.FILE_END);
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
    @GetMapping("/passerEnAttente")
    @Operation(summary = "permet de modifier le statut de la demande pour la passer à : en attente")
    public DemandeWebDto passerEnAttente(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, HttpServletRequest request) throws DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDto(service.changeState(service.findById(numDemande), Constant.ETATDEM_ATTENTE), type);
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
    @GetMapping("/archiverDemande")
    @Operation(summary = "permet de passer la demande en statut archivé")
    public DemandeWebDto archiverDemande(@RequestParam("type") TYPE_DEMANDE type, @RequestParam("numDemande") Integer numDemande, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(numDemande, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDto(service.archiverDemande(service.findById(numDemande)), type);
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
    @GetMapping("/etapePrecedente/{id}")
    @Operation(summary = "permet de revenir à l'étape précédente dans le workflow de création d'une demande")
    public DemandeWebDto previousStep(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws
            DemandeCheckingException, IOException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDto(service.previousState(service.findById(id)), type);
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
    @GetMapping("/etapeChoisie/{id}")
    @Operation(summary = "permet de revenir à une étape bien précise dans le workflow de création d'une demande")
    public DemandeWebDto chosenStep(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, @RequestParam("etape") Integer etape, HttpServletRequest request) throws
            DemandeCheckingException, UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        return builder.buildDto(service.returnState(etape, service.findById(id)), type);
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
    public Integer getNbLigneFichier(@RequestParam("type") TYPE_DEMANDE type, @PathVariable("id") Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException {
        checkAccessToServices.autoriserAccesDemandeParIln(id, request.getAttribute(Constant.USER_NUM).toString(), type);
        IDemandeService service = strategy.getStrategy(IDemandeService.class, type);
        ILigneFichierService ligneFichierService = strategy.getStrategy(ILigneFichierService.class, type);
        return ligneFichierService.getNbLigneFichierTotalByDemande(service.findById(id));
    }
}
