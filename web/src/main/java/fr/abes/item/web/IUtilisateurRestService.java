package fr.abes.item.web;

import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.ForbiddenException;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;

import javax.servlet.http.HttpServletRequest;


@CrossOrigin(origins = "http://localhost:3000")
public interface IUtilisateurRestService {

    @PutMapping(value="/utilisateurs/{id}")
    @ApiOperation(value = "permet de mettre à jour les données de l'utilisateur (adresse mail)")
    @SuppressWarnings("squid:S4684")
    Utilisateur save(@PathVariable Integer id, @RequestBody Utilisateur util, HttpServletRequest request) throws ForbiddenException;
}
