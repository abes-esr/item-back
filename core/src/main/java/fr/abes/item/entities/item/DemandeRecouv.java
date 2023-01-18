package fr.abes.item.entities.item;

import fr.abes.item.constant.TYPE_DEMANDE;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

@Entity
@Table(name = "DEMANDE_RECOUV")
@NoArgsConstructor
@Getter @Setter
public class DemandeRecouv extends Demande {
    @ManyToOne
    @JoinColumn(name = "DEM_INDEX_RECHERCHE")
    private IndexRecherche indexRecherche;

    public DemandeRecouv(@NotNull String rcr, Date dateCreation, Date dateModification, @NotNull EtatDemande etatDemande, String commentaire, Utilisateur utilisateur, IndexRecherche indexRecherche) {
        super(rcr, dateCreation, dateModification, etatDemande, commentaire, utilisateur);
        this.indexRecherche = indexRecherche;
    }

    public DemandeRecouv(String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        super(rcr, dateCreation, dateModification, etatDemande, commentaire, utilisateur);
    }

    public DemandeRecouv(Integer numDemande) {
        this.numDemande = numDemande;
    }

    @Override
    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.RECOUV;
    }
}
