package fr.abes.item.service;

import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.IndexRecherche;
import fr.abes.item.entities.item.TypeExemp;
import fr.abes.item.entities.item.ZonesAutorisees;
import fr.abes.item.repository.item.IEtatDemandeDao;
import fr.abes.item.repository.item.IIndexRechercheDao;
import fr.abes.item.repository.item.ITypeExempDao;
import fr.abes.item.repository.item.IZonesAutoriseesDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReferenceService {
    private final IEtatDemandeDao etatDemandeDao;
    private final IIndexRechercheDao indexRechercheDao;
    private final ITypeExempDao typeExempDao;
    private final IZonesAutoriseesDao zonesAutoriseesDao;

    public ReferenceService(IEtatDemandeDao etatDemandeDao, IIndexRechercheDao indexRechercheDao, ITypeExempDao typeExempDao, IZonesAutoriseesDao zonesAutoriseesDao) {
        this.etatDemandeDao = etatDemandeDao;
        this.indexRechercheDao = indexRechercheDao;
        this.typeExempDao = typeExempDao;
        this.zonesAutoriseesDao = zonesAutoriseesDao;
    }


    public List<EtatDemande> findAllEtatDemande() {
        return etatDemandeDao.findAllForDisplay();
    }

    public EtatDemande findEtatDemandeById(Integer id) {
        Optional<EtatDemande> etatDemande = etatDemandeDao.findById(id);
        return etatDemande.orElse(null);
    }

    public List<IndexRecherche> findAllIndex() {
        return indexRechercheDao.findAll();
    }

    public IndexRecherche findIndexById(Integer id) {
        Optional<IndexRecherche> indexRecherche = indexRechercheDao.findById(id);
        return indexRecherche.orElse(null);
    }

    public List<IndexRecherche> findByLibelleIndex(String libelle) {
        return indexRechercheDao.getIndexRecherchesByLibelle(libelle);
    }

    public List<TypeExemp> findAllTypeExemp() {
        return typeExempDao.findAll();
    }

    public TypeExemp findTypeExempById(Integer id) {
        Optional<TypeExemp> typeExemp = typeExempDao.findById(id);
        return typeExemp.orElse(null);
    }

    public ZonesAutorisees findZonesById(Integer id) {
        Optional<ZonesAutorisees> zone = zonesAutoriseesDao.findById(id);
        return zone.orElse(null);
    }

    public String getIndicateursByTypeExempAndLabelZone(String zone) {
        return zonesAutoriseesDao.getIndicateursByTypeExempAndLabelZone(zone);
    }

    public Set<IndexRecherche> getIndexRechercheFromTypeExemp(Integer id) {
        return typeExempDao.findById(id).get().getIndexRechercheSet();
    }
}
