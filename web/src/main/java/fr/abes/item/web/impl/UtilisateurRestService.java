package fr.abes.item.web.impl;

import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.ForbiddenException;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.IUtilisateurRestService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;

@RestController
public class UtilisateurRestService extends AbstractRestService implements IUtilisateurRestService {

	@Autowired
	CheckAccessToServices checkAccessToServices;

	/**
	 * Webservice de sauvegarde d'un utilisateur via méthode PUT
	 * @param id : identifiant de l'utilisateur à modifier
	 * @param util : utilisateur modifié
	 * @return
	 */
	@Override
	@PutMapping(value="/utilisateurs/{id}")
    public Utilisateur save(@PathVariable Integer id, @RequestBody Utilisateur util, HttpServletRequest request) throws ForbiddenException {
		checkAccessToServices.autoriserMajUtilisateurParUserNum(id, request.getAttribute("userNum").toString());
		util.setNumUser(id);
		return getService().getUtilisateur().save(util);
	}
}
