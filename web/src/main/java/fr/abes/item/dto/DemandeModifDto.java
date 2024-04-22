package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@JsonTypeName("MODIF")
public class DemandeModifDto extends DemandeDto {
    @JsonProperty("zone")
    private String zone;
    @JsonProperty("sousZone")
    private String sousZone;
    @JsonProperty("traitement")
    private Integer traitement;

}
