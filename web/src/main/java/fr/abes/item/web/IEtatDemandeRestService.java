package fr.abes.item.web;

import fr.abes.item.entities.item.EtatDemande;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;


public interface IEtatDemandeRestService {
    @GetMapping(value = "/EtatDemande")
    @ApiOperation(value = "permet de récupérer la liste des états possible d'une demandeModif")
    List<EtatDemande> getEtatDemandes();
}
