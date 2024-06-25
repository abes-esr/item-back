package fr.abes.item.web;

import fr.abes.item.core.entities.item.Utilisateur;
import fr.abes.item.core.exception.ForbiddenException;
import fr.abes.item.core.exception.UserExistException;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.dto.DtoBuilder;
import fr.abes.item.dto.UtilisateurWebDto;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1")
public class UtilisateurRestService {
    private final UtilisateurService utilisateurService;
    private final CheckAccessToServices checkAccessToServices;

    private final DtoBuilder builder;

    public UtilisateurRestService(UtilisateurService utilisateurService, CheckAccessToServices checkAccessToServices, DtoBuilder builder) {
        this.utilisateurService = utilisateurService;
        this.checkAccessToServices = checkAccessToServices;
        this.builder = builder;
    }

    /**
     * Webservice de création d'un utilisateur
     *
     * @param id    : identifiant de l'utilisateur à modifier
     * @param email : adresse mail de l'utilisateur à modifier
     * @return l'utilisateur sauvegardé
     */
    @PostMapping(value = "/utilisateurs/{id}")
    @Operation(summary = "permet de mettre à jour les données de l'utilisateur (adresse mail)")
    public UtilisateurWebDto save(@PathVariable("id") Integer id, @RequestBody String email, HttpServletRequest request) throws ForbiddenException {
        checkAccessToServices.autoriserMajUtilisateurParUserNum(id, request.getAttribute("userNum").toString());
        Utilisateur utilisateur = new Utilisateur(id, email);
        return builder.buildUtilisateurDto(utilisateurService.save(utilisateur));
    }

    /**
     * Webservice de modification du mail d'un utilisateur
     *
     * @param id    : identifiant de l'utilisateur à modifier
     * @param email : adresse mail de l'utilisateur à modifier
     * @return l'utilisateur sauvegardé
     */
    @PatchMapping(value = "/utilisateurs/{id}")
    @Operation(summary = "permet de mettre à jour les données de l'utilisateur (adresse mail)")
    public UtilisateurWebDto changeMail(@PathVariable("id") Integer id, @RequestBody String email, HttpServletRequest request) throws ForbiddenException, UserExistException {
        checkAccessToServices.autoriserMajUtilisateurParUserNum(id, request.getAttribute("userNum").toString());
        Utilisateur utilisateur = utilisateurService.findById(Integer.parseInt(request.getAttribute("userNum").toString()));
        if (utilisateur != null) {
            utilisateur.setEmail(email);
            return builder.buildUtilisateurDto(utilisateurService.save(utilisateur));
        }
        throw new UserExistException("Utilisateur inexistant");
    }
}
