package fr.abes.item.web;

import fr.abes.item.core.entities.item.Traitement;
import fr.abes.item.core.service.ReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class TraitementRestService {
	private final ReferenceService referenceService;

	public TraitementRestService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	/**
	 * Webservice de récupération de la liste de toutes les demandeModifs
	 * 
	 * @return Liste de la totalité des demandeModifs
	 */
	@GetMapping(value = "/traitements")
	@Operation(summary = "permet de récupérer la liste des traitements relatifs à une demandeModif")
	public List<Traitement> getTraitements() {
		return referenceService.findAll();
	}

	@GetMapping(value = "/traitementFromDemande/{id}")
	@Operation(summary = "permet de récupérer le type de traitement choisi pour une demande")
	public Integer getTraitementFromDemande(@PathVariable Integer id) {
		return referenceService.findTraitementByDemandeId(id);
	}
}
