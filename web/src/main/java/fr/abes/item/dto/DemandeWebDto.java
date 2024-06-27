package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DemandeModifWebDto.class, name = "MODIF"),
        @JsonSubTypes.Type(value = DemandeExempWebDto.class, name = "EXEMP"),
        @JsonSubTypes.Type(value = DemandeRecouvWebDto.class, name = "RECOUV"),
})
@AllArgsConstructor
@NoArgsConstructor
public class DemandeWebDto {
    @JsonProperty("id")
    protected Integer id;
    @JsonProperty("rcr")
    protected String rcr;
    @JsonProperty("shortname")
    protected String shortName;
    @JsonProperty("iln")
    protected String iln;
    @JsonProperty("etatDemande")
    protected String etatDemande;
    @JsonProperty("commentaire")
    protected String commentaire;
    @JsonProperty("pourcentageProgressionTraitement")
    protected Integer pourcentageProgressionTraitement;
    @JsonProperty("dateCreation")
    protected String dateCreation;
    @JsonProperty("dateModification")
    protected String dateModification;

}
