package fr.abes.item.service;

import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.IndexRecherche;
import fr.abes.item.entities.item.TypeExemp;
import fr.abes.item.entities.item.ZonesAutorisees;

import java.util.List;

public interface IReferenceService {
    List<EtatDemande> findAllEtatDemande();

    EtatDemande findEtatDemandeById(Integer id);

    List<IndexRecherche> findAllIndex();

    IndexRecherche findIndexById(Integer id);

    List<IndexRecherche> findByLibelleIndex(String libelle);

    List<TypeExemp> findAllTypeExemp();

    TypeExemp findTypeExempById(Integer id);

    ZonesAutorisees findZonesById(Integer id);

    String getIndicateursByTypeExempAndLabelZone(String zone);
}
