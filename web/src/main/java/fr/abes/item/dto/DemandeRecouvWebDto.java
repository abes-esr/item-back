package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("RECOUV")
@NoArgsConstructor
public class DemandeRecouvWebDto extends DemandeWebDto {
    @JsonProperty("indexRecherche")
    private String indexRecherche;

    public DemandeRecouvWebDto(Integer id, String rcr, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification, String indexRecherche) {
        super(id, rcr, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
        this.indexRecherche = indexRecherche;
    }

}
