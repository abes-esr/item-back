package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("MODIF")
@NoArgsConstructor
public class DemandeModifWebDto extends DemandeWebDto {
    @JsonProperty("zone")
    private String zoneEtSousZone;
    @JsonProperty("traitement")
    private String traitement;

    public DemandeModifWebDto(Integer id, String rcr, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification, String zoneEtSousZone, String traitement) {
        super(id, rcr, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
        this.zoneEtSousZone = zoneEtSousZone;
        this.traitement = traitement;
    }

}
