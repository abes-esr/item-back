package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = DemandeModifDto.class, name = "MODIF"),
        @JsonSubTypes.Type(value = DemandeExempDto.class, name = "EXEMP"),
        @JsonSubTypes.Type(value = DemandeRecouvDto.class, name = "RECOUV"),
})
public abstract class DemandeDto {
    @JsonProperty("id")
    protected Integer id;
    @JsonProperty("rcr")
    protected String rcr;
    @JsonProperty("utilisateur")
    protected Integer userNum;
    @JsonProperty("iln")
    protected Integer iln;
    @JsonProperty("etatDemande")
    protected Integer etatDemande;
    @JsonProperty("commentaire")
    protected String commentaire;
    @JsonProperty("pourcentageProgressionTraitement")
    protected Integer pourcentageProgressionTraitement;
}
