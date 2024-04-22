package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeName("RECOUV")
public class DemandeRecouvDto extends DemandeDto {
    @JsonProperty("indexRecherche")
    private Integer indexRecherche;
}
