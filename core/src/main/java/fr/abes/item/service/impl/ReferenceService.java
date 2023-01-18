package fr.abes.item.service.impl;

import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.IndexRecherche;
import fr.abes.item.entities.item.TypeExemp;
import fr.abes.item.entities.item.ZonesAutorisees;
import fr.abes.item.service.IReferenceService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ReferenceService implements IReferenceService {
    @Autowired @Getter
    DaoProvider dao;
    @Override
    public List<EtatDemande> findAllEtatDemande() {
        return getDao().getEtatDemande().findAllForDisplay();
    }

    @Override
    public EtatDemande findEtatDemandeById(Integer id) {
        Optional<EtatDemande> etatDemande = dao.getEtatDemande().findById(id);
        return etatDemande.orElse(null);
    }

    @Override
    public List<IndexRecherche> findAllIndex() {
        return getDao().getIndexRecherche().findAll();
    }

    @Override
    public IndexRecherche findIndexById(Integer id) {
        Optional<IndexRecherche> indexRecherche = getDao().getIndexRecherche().findById(id);
        return indexRecherche.orElse(null);
    }

    @Override
    public List<IndexRecherche> findByLibelleIndex(String libelle) {
        return getDao().getIndexRecherche().getIndexRecherchesByLibelle(libelle);
    }

    @Override
    public List<TypeExemp> findAllTypeExemp() {
        return getDao().getTypeExemp().findAll();
    }

    @Override
    public TypeExemp findTypeExempById(Integer id) {
        Optional<TypeExemp> typeExemp = getDao().getTypeExemp().findById(id);
        return typeExemp.orElse(null);
    }

    @Override
    public ZonesAutorisees findZonesById(Integer id) {
        Optional<ZonesAutorisees> zone = getDao().getZonesAutorisees().findById(id);
        return zone.orElse(null);
    }

    @Override
    public String getIndicateursByTypeExempAndLabelZone(String zone) {
        return getDao().getZonesAutorisees().getIndicateursByTypeExempAndLabelZone(zone);
    }
}
