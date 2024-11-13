package fr.abes.item.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeName;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import fr.abes.item.core.dto.DemandeDto;
import fr.abes.item.core.entities.item.DemandeSupp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

@Getter
@Setter
@JsonTypeName("SUPP")
@NoArgsConstructor
public class DemandeSuppWebDto extends DemandeWebDto {
    @JsonProperty("typeSuppression")
    private String typeSuppression;
    @JsonProperty("nbExemplaires")
    protected Integer nbExemplaires;
    public DemandeSuppWebDto(Integer id, String rcr, String shortName, String iln, String etatDemande, String commentaire, Integer pourcentageProgressionTraitement, String dateCreation, String dateModification, String typeSuppression) {
        super(id, rcr, shortName, iln, etatDemande, commentaire, pourcentageProgressionTraitement, dateCreation, dateModification);
        this.typeSuppression = typeSuppression;
    }

    public DemandeSuppWebDto(DemandeSupp demande) {
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

        if (demande.getTypeSuppression() != null)
            this.typeSuppression = demande.getTypeSuppression().toString();
    }

    /**
     * Constructeurs qui gèrent l'ajout du nombre de lignes dans la demande
     * @param demandeDto un objet DemandeDto
     */
    public DemandeSuppWebDto(DemandeDto demandeDto) {
        DemandeSupp demande = (DemandeSupp) demandeDto.getDemande();
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

        if (demande.getTypeSuppression() != null)
            this.typeSuppression = demande.getTypeSuppression().toString();
        this.nbExemplaires = demandeDto.getNbLignes();
    }
}
