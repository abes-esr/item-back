package fr.abes.item.core.entities.item;

import fr.abes.item.core.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name="JOURNAL_DEMANDE_SUPP")
@Getter
@Setter
public class JournalDemandeSupp implements Serializable, GenericEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUM_JOURNAL")
    private Integer numJournal;
    @Temporal(TemporalType.TIMESTAMP) @Column(name="DATE_ENTREE")
    private Date dateEntree;
    @ManyToOne @JoinColumn(name="JOU_USER_ID") @NotNull
    private Utilisateur user;
    @ManyToOne @JoinColumn(name="JOU_DEM_ID") @NotNull
    private DemandeSupp demandeSupp;
    @ManyToOne @JoinColumn(name="JOU_ETA_ID") @NotNull
    private EtatDemande etatDemande;

    public JournalDemandeSupp(Date dateEntree, Utilisateur utilisateur, EtatDemande etatDemande, DemandeSupp demandeSupp) {
        this.dateEntree = dateEntree;
        this.user = utilisateur;
        this.demandeSupp = demandeSupp;
        this.etatDemande = etatDemande;
    }

    @Override
    public Integer getId() {
        return numJournal;
    }
}
