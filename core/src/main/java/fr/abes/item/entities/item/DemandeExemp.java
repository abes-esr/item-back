package fr.abes.item.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.constant.TYPE_DEMANDE;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.util.Date;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter @Setter
@JsonIgnoreProperties({"journalDemandes", "ligneFichierExemps"})
@Table(name="DEMANDE_EXEMP")
public class DemandeExemp extends Demande{

    @ManyToOne
    @JoinColumn(name = "DEM_TYPE_EXEMP")
    private TypeExemp typeExemp;

    @Column(name = "LISTE_ZONES")
    private String listeZones;

    @OneToMany(mappedBy = "demandeExemp", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<JournalDemandeExemp> journalDemandes;

    @ManyToOne
    @JoinColumn(name = "DEM_INDEX_RECHERCHE")
    private IndexRecherche indexRecherche;

    @OneToMany(mappedBy = "demandeExemp", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<LigneFichierExemp> ligneFichierExemps;

    public DemandeExemp(Integer numDemande) {
        super(numDemande);
    }

    public DemandeExemp(String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur, TypeExemp typeExemp, IndexRecherche indexRecherche) {
        super(rcr, dateCreation, dateModification, etatDemande, commentaire, utilisateur);
        this.typeExemp = typeExemp;
        this.indexRecherche = indexRecherche;
    }
    public DemandeExemp(String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        super(rcr, dateCreation, dateModification, etatDemande, commentaire, utilisateur);
    }

    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.EXEMP;
    }
}
