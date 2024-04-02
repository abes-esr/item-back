package fr.abes.item.web;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.CommException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.*;
import fr.abes.item.exception.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.List;


public interface IDemandeRestService {
    @GetMapping(value = "/demandes")
    @ApiOperation(value = "renvoie les demandes pour les administrateurs",
            notes = "renvoie les demande terminées et en erreur de tout le monde et toutes les demandeModifs créées par cet iln")
    List<Demande> getAllActiveDemandes(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @RequestParam(required = false, defaultValue = "false") boolean restriction, HttpServletRequest request);

    @GetMapping(value = "/chercherDemandes")
    @ApiOperation(value = "renvoie les demandes de modif pour ce usernum",
            notes = "renvoie toutes les demandes créées par cet iln")
    List<Demande> chercher(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, HttpServletRequest request);

    @GetMapping(value = "/chercherArchives")
    @ApiOperation(value = "renvoie les demandes archivées pour cet iln",
            notes = "renvoie les demandeModifs archivées créées par cet iln")
    List<Demande> getAllArchivedDemandes(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, boolean extension, HttpServletRequest request);

    @GetMapping(value = "/demandes/{id}")
    @ApiOperation(value = "renvoie une demande précise")
    Demande getDemande(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @PathVariable Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @GetMapping(value = "/creerdemande")
    @ApiOperation(value = "permet de créer une nouvelle demande de  modif pour un rcr donné")
    Demande saveModif(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @RequestParam String rcr, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @DeleteMapping(value = "/demandes/{id}")
    @ApiOperation(value = "permet de supprimer une demande")
    void supprimer(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @PathVariable Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @PutMapping(value = "/demandes/{id}")
    @ApiOperation(value = "permet de créer une nouvelle demande de modif")
    Demande saveModif(@PathVariable Integer id, @RequestBody DemandeModif dem, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @GetMapping(value = "/getTypeExemplarisationDemande/{id}")
    @ApiOperation(value = "permer de récupérer le type d'exemplarisation choisi pour une demande")
    String getTypeExemplarisationDemande(@PathVariable Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @PutMapping
    @ApiOperation(value = "Mise à jour du type d'exemplarisation")
    Demande saveExemp(@PathVariable Integer id, @RequestBody DemandeExemp dem, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @PutMapping
    @ApiOperation(value = "Mise à jour d'une demande de recouvrement'")
    Demande saveRecouv(@PathVariable Integer id, @RequestBody DemandeRecouv dem, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @PostMapping
    @ApiOperation(value = "Mise à jour du type d'exemplarisation")
    Demande majTypeExemp(@PathVariable Integer id, @RequestBody TypeExemp typeExemp, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @PostMapping("/uploadDemande")
    @ApiOperation(value = "permet de charger le fichier pour une demande")
    String uploadDemande(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type,
                         @RequestParam("file") MultipartFile file,
                         @RequestParam("numDemande") Integer numDemande,
                         HttpServletRequest request) throws DemandeCheckingException, FileTypeException, FileCheckingException, IOException, UserExistException, ForbiddenException;

    @GetMapping("/simulerLigne")
    @ApiOperation(value = "permet de simuler la modification d'un exemplaire", notes="pour un exemplaire donné du fichier enrichi, renvoie un tableau contenant la notice avant et après modification")
    String[] simulerLigne(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @RequestParam Integer numDemande, @RequestParam Integer numLigne, HttpServletRequest request) throws CBSException, UserExistException, ForbiddenException, QueryToSudocException, ZoneException, CommException, IOException;

    @GetMapping("/passerEnAttente")
    @ApiOperation(value = "permet de modifier le statut de la demande pour la passer à : en attente")
    Demande passerEnAttente(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @RequestParam Integer numDemande, HttpServletRequest request) throws DemandeCheckingException, UserExistException, ForbiddenException;

    @GetMapping("/archiverDemande")
    @ApiOperation(value = "permet de passer la demande en statut archivé")
    Demande archiverDemande(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @RequestParam Integer numDemande, HttpServletRequest request) throws DemandeCheckingException, UserExistException, ForbiddenException;

    @GetMapping("/etapePrecedente/{id}")
    @ApiOperation(value = "permet de revenir à l'étape précédente dans le workflow de création d'une demande")
    Demande previousStep(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @PathVariable Integer id, HttpServletRequest request) throws DemandeCheckingException, IOException, UserExistException, ForbiddenException;

    @GetMapping("/etapeChoisie/{id}")
    @ApiOperation(value = "permet de revenir à une étape bien précise dans le workflow de création d'une demande")
    Demande chosenStep(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @PathVariable Integer id, @RequestParam Integer etape, HttpServletRequest request) throws
            DemandeCheckingException, IOException, UserExistException, ForbiddenException;

    @GetMapping("/getNbLigneFichier/{id}")
    @ApiOperation(value = "permet de récupérer le nombre de ligne du fichier enrichi d'une demande")
    Integer getNbLigneFichier(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @PathVariable Integer id, HttpServletRequest request) throws UserExistException, ForbiddenException;

    @GetMapping("/supprimerDemande")
    @ApiOperation(value = "permet de supprimer une demande tout en la conservant en base, elle passe en statut 10 invisible pour l'utilisateur sur l'interface web")
    Demande supprimerAvecConservationEnBase(@RequestParam(required = false, defaultValue = "MODIF") TYPE_DEMANDE type, @RequestParam Integer numDemande, HttpServletRequest request) throws UserExistException, ForbiddenException, DemandeCheckingException;
}
