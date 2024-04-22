package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeName("EXEMP")
public class DemandeExempDto extends DemandeDto {
    @JsonProperty("typeExemp")
    private Integer typeExemp;
    @JsonProperty("listeZones")
    private String listeZones;
    @JsonProperty("indexRecherche")
    private Integer indexRecherche;
}
