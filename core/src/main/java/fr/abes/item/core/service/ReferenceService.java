package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.EtatDemande;
import fr.abes.item.core.entities.item.IndexRecherche;
import fr.abes.item.core.entities.item.Traitement;
import fr.abes.item.core.entities.item.TypeExemp;
import fr.abes.item.core.repository.item.IEtatDemandeDao;
import fr.abes.item.core.repository.item.ITraitementDao;
import fr.abes.item.core.repository.item.ITypeExempDao;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReferenceService {
    private final IEtatDemandeDao etatDemandeDao;
    private final ITypeExempDao typeExempDao;
    private final ITraitementDao traitementDao;

    public ReferenceService(IEtatDemandeDao etatDemandeDao, ITypeExempDao typeExempDao, ITraitementDao traitementDao) {
        this.etatDemandeDao = etatDemandeDao;
        this.typeExempDao = typeExempDao;
        this.traitementDao = traitementDao;
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

    public TypeExemp findTypeExempById(Integer id) {
        Optional<TypeExemp> typeExemp = typeExempDao.findById(id);
        return typeExemp.orElseThrow();
    }

    public Set<IndexRecherche> getIndexRechercheFromTypeExemp(Integer id) {
        return typeExempDao.findById(id).get().getIndexRechercheSet();
    }

    /**
     * Retourner l'ensemble de la liste des traitements disponibles
     *
     * @return liste de tous les traitements
     */
    public List<Traitement> findAll() {
        return traitementDao.findAllByOrderByNumTraitementAsc();
    }

    public Traitement findTraitementById(Integer id) {
        Optional<Traitement> traitement = traitementDao.findById(id);
        return traitement.orElseThrow();
    }

    public Integer findTraitementByDemandeId(Integer id) {
        return traitementDao.findTraitementByDemandeModifId(id);
    }
}
