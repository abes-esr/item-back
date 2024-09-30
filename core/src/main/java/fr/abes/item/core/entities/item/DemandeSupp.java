package fr.abes.item.core.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.constant.TYPE_SUPPRESSION;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@Table(name = "DEMANDE_SUPP")
@NoArgsConstructor
@Getter
@Setter
@JsonIgnoreProperties({"ligneFichierSupps"})
public class DemandeSupp extends Demande {
    @Column(name = "TYPE_SUPPRESSION")
    @Enumerated(EnumType.STRING)
    private TYPE_SUPPRESSION typeSuppression;

    public DemandeSupp(Integer refDemande) {
        super(refDemande);
    }

    @Override
    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.SUPP;
    }

    @Getter @Setter @OneToMany(mappedBy = "demandeSupp", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<LigneFichierSupp> ligneFichierSupps;

    public DemandeSupp(String rcr, Date dateCreation, Date dateModification, TYPE_SUPPRESSION typeSuppression,
                        String comment, EtatDemande etatDemande, Utilisateur utilisateur) {
        super(rcr, dateCreation, dateModification, etatDemande, comment, utilisateur);
        this.typeSuppression = typeSuppression;
    }

    public DemandeSupp(Integer id, String rcr, Date dateCreation, Date dateModification,
                       String comment, EtatDemande etatDemande, Utilisateur utilisateur) {
        super(id, rcr, dateCreation, dateModification, etatDemande, comment, utilisateur);
    }
}
