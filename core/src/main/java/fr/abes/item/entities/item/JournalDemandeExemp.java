package fr.abes.item.entities.item;

import fr.abes.item.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;

@Entity
@NoArgsConstructor
@Table(name="JOURNAL_DEMANDE_EXEMP")
@Getter @Setter
public class JournalDemandeExemp implements Serializable, GenericEntity<Integer> {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUM_JOURNAL")
    private Integer numJournal;
    @Temporal(TemporalType.DATE) @Column(name="DATE_ENTREE")
    private Date dateEntree;
    @ManyToOne @JoinColumn(name="JOU_USER_ID") @NotNull
    private Utilisateur user;
    @ManyToOne @JoinColumn(name="JOU_DEM_ID") @NotNull
    private DemandeExemp demandeExemp;
    @ManyToOne @JoinColumn(name="JOU_ETA_ID") @NotNull
    private EtatDemande etatDemande;


    public JournalDemandeExemp(Date dateEntree, Utilisateur user, EtatDemande etatDemande, DemandeExemp demandeExemp) {
        super();
        this.dateEntree = dateEntree;
        this.user = user;
        this.etatDemande = etatDemande;
        this.demandeExemp = demandeExemp;
    }

    @Override
    public Integer getId() { return numJournal; }
}
