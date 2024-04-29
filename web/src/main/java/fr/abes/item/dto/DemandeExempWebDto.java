package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonTypeName("EXEMP")
@NoArgsConstructor
@AllArgsConstructor
public class DemandeExempWebDto extends DemandeWebDto {
    @JsonProperty("typeExemp")
    private String typeExemp;
    @JsonProperty("indexRecherche")
    private String indexRecherche;

    public DemandeExempWebDto(Integer id, String rcr, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification, String typeExemp, String indexRecherche) {
        super(id, rcr, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
        this.typeExemp = typeExemp;
        this.indexRecherche = indexRecherche;
    }

}
