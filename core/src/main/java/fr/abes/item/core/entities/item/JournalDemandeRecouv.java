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
@Table(name="JOURNAL_DEMANDE_RECOUV")
@Getter
@Setter
public class JournalDemandeRecouv implements Serializable, GenericEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUM_JOURNAL")
    private Integer numJournal;
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name="DATE_ENTREE")
    private Date dateEntree;
    @ManyToOne @JoinColumn(name="JOU_USER_ID") @NotNull
    private Utilisateur user;
    @ManyToOne @JoinColumn(name="JOU_DEM_ID") @NotNull
    private DemandeRecouv demandeRecouv;
    @ManyToOne @JoinColumn(name="JOU_ETA_ID") @NotNull
    private EtatDemande etatDemande;

    public JournalDemandeRecouv(Date dateEntree, Utilisateur utilisateur, EtatDemande etatDemande, DemandeRecouv demandeRecouv) {
        this.dateEntree = dateEntree;
        this.user = utilisateur;
        this.demandeRecouv = demandeRecouv;
        this.etatDemande = etatDemande;
    }

    @Override
    public Integer getId() {
        return numJournal;
    }
}
