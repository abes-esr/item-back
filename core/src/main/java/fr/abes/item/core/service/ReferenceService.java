package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.IndexRecherche;
import fr.abes.item.core.entities.item.TypeExemp;
import fr.abes.item.core.repository.item.IEtatDemandeDao;
import fr.abes.item.core.repository.item.ITypeExempDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReferenceService {
    private final IEtatDemandeDao etatDemandeDao;
    private final ITypeExempDao typeExempDao;

    public ReferenceService(IEtatDemandeDao etatDemandeDao, ITypeExempDao typeExempDao) {
        this.etatDemandeDao = etatDemandeDao;
        this.typeExempDao = typeExempDao;
    }


    public List<EtatDemande> findAllEtatDemande() {
        return etatDemandeDao.findAllForDisplay();
    }

    public EtatDemande findEtatDemandeById(Integer id) {
        Optional<EtatDemande> etatDemande = etatDemandeDao.findById(id);
        return etatDemande.orElse(null);
    }

    public List<TypeExemp> findAllTypeExemp() {
        return typeExempDao.findAll();
    }

    public Set<IndexRecherche> getIndexRechercheFromTypeExemp(Integer id) {
        return typeExempDao.findById(id).get().getIndexRechercheSet();
    }
}
