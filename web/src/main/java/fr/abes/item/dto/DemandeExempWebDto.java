package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.abes.item.core.entities.item.DemandeExemp;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

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

    public DemandeExempWebDto(Integer id, String rcr, String shortName, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification, String typeExemp, String indexRecherche) {
        super(id, rcr, shortName, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
        this.typeExemp = typeExemp;
        this.indexRecherche = indexRecherche;
    }


    public DemandeExempWebDto(DemandeExemp demande) {
        DateFormat format = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        String dateCreation = format.format(demande.getDateCreation());
        String dateModification = format.format(demande.getDateModification());
        this.id = demande.getId();
        this.rcr = demande.getRcr();
        this.shortName = demande.getShortname();
        this.iln = demande.getIln();
        if (demande.getEtatDemande() != null)
            this.etatDemande = demande.getEtatDemande().getLibelle();
        this.commentaire = demande.getCommentaire();
        this.pourcentageProgressionTraitement = demande.getPourcentageProgressionTraitement();
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        if (demande.getTypeExemp() != null)
            this.typeExemp = demande.getTypeExemp().getLibelle();
        if (demande.getIndexRecherche() != null)
            this.indexRecherche = demande.getIndexRecherche().getLibelle();
    }
}
