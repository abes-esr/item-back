package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.repository.item.*;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class ReferenceService {
    private final IEtatDemandeDao etatDemandeDao;
    private final ITypeExempDao typeExempDao;
    private final ITraitementDao traitementDao;
    private final IZonesAutoriseesDao iZonesAutoriseesDao;

    public ReferenceService(IEtatDemandeDao etatDemandeDao, ITypeExempDao typeExempDao, ITraitementDao traitementDao, IZonesAutoriseesDao iZonesAutoriseesDao) {
        this.etatDemandeDao = etatDemandeDao;
        this.typeExempDao = typeExempDao;
        this.traitementDao = traitementDao;
        this.iZonesAutoriseesDao = iZonesAutoriseesDao;
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

    public String constructHeaderCsv() {
        List<ZonesAutorisees> listZonesAutorisees = this.iZonesAutoriseesDao.findAll();
        StringBuilder headerCsv = new StringBuilder();
        headerCsv.append("PPN").append(";");
        for (ZonesAutorisees zonesAutorisees: listZonesAutorisees) {
            headerCsv.append(zonesAutorisees.getLabelZone());
            for (SousZonesAutorisees sousZonesAutorisees : zonesAutorisees.getSousZonesAutorisees()) {
                headerCsv.append(sousZonesAutorisees.getLibelle()).append(";");
            }
        }
        headerCsv.append("\n");
        return headerCsv.toString();
    }
}
