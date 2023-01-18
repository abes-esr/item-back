package fr.abes.item.web.impl;

import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.IEtatDemandeRestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EtatDemandeRestService extends AbstractRestService implements IEtatDemandeRestService {
	@Override
	@GetMapping(value="/EtatDemande")
	public List<EtatDemande> getEtatDemandes() {
		return getService().getReference().findAllEtatDemande();
	}
}
