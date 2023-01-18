package fr.abes.item.web;

import fr.abes.item.entities.item.Traitement;
import io.swagger.annotations.ApiOperation;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@CrossOrigin(origins = "http://localhost:3000")
public interface ITraitementRestService {
    @GetMapping(value = "/traitements")
    @ApiOperation(value = "permet de récupérer la liste des traitements relatifs à une demandeModif")
    List<Traitement> getTraitements();

    @GetMapping(value = "/traitementFromDemande/{id}")
    @ApiOperation(value = "permet de récupérer le type de traitement choisi pour une demande")
    Integer getTraitementFromDemande(@PathVariable Integer id);
}
