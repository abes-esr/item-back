package fr.abes.item.dto;

import fr.abes.item.core.entities.item.DemandeSupp;

public class DemandeSuppWebDto extends DemandeWebDto {
    public DemandeSuppWebDto(Integer id, String rcr, String shortName, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification) {
        super(id, rcr, shortName, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
    }

    public DemandeSuppWebDto(DemandeSupp demande) {

    }
}
