package fr.abes.item.web;

import fr.abes.item.core.entities.item.Utilisateur;
import fr.abes.item.core.exception.ForbiddenException;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.security.CheckAccessToServices;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;
@RestController
@RequestMapping("/api/v1")
public class UtilisateurRestService {
	private final UtilisateurService utilisateurService;
	private final CheckAccessToServices checkAccessToServices;

	public UtilisateurRestService(UtilisateurService utilisateurService, CheckAccessToServices checkAccessToServices) {
		this.utilisateurService = utilisateurService;
		this.checkAccessToServices = checkAccessToServices;
	}

	/**
	 * Webservice de sauvegarde d'un utilisateur via méthode PUT
	 * @param id : identifiant de l'utilisateur à modifier
	 * @param util : utilisateur modifié
	 * @return l'utilisateur sauvegardé
	 */
	@PutMapping(value="/utilisateurs/{id}")
	@Operation(summary = "permet de mettre à jour les données de l'utilisateur (adresse mail)")
    public Utilisateur save(@PathVariable Integer id, @RequestBody Utilisateur util, HttpServletRequest request) throws ForbiddenException {
		checkAccessToServices.autoriserMajUtilisateurParUserNum(id, request.getAttribute("userNum").toString());
		util.setNumUser(id);
		return utilisateurService.save(util);
	}
}
