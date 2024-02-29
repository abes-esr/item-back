package fr.abes.item.web;

import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.service.ReferenceService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1")
public class EtatDemandeRestService {
	private final ReferenceService referenceService;

	public EtatDemandeRestService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	@GetMapping(value="/EtatDemande")
	@Operation(summary = "permet de récupérer la liste des états possible d'une demandeModif")
	public List<EtatDemande> getEtatDemandes() {
		return referenceService.findAllEtatDemande();
	}
}
