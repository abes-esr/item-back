package fr.abes.item.web.impl;

import fr.abes.item.entities.item.Traitement;
import fr.abes.item.web.AbstractRestService;
import fr.abes.item.web.ITraitementRestService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class TraitementRestService extends AbstractRestService implements ITraitementRestService {

	/**
	 * Webservice de récupération de la liste de toutes les demandeModifs
	 * 
	 * @return Liste de la totalité des demandeModifs
	 */
	@Override
	@GetMapping(value = "/traitements")
	public List<Traitement> getTraitements() {
		return getService().getTraitement().findAll();
	}

	@Override
	@GetMapping(value = "/traitementFromDemande/{id}")
	public Integer getTraitementFromDemande(@PathVariable Integer id) {
		return getService().getTraitement().findTraitementByDemandeId(id);
	}
}
