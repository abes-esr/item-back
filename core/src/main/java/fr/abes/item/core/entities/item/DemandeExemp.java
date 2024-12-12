package fr.abes.item.core.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter @Setter
@JsonIgnoreProperties({"journalDemandes", "ligneFichierExemps"})
@Table(name="DEMANDE_EXEMP")
public class DemandeExemp extends Demande{

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEM_TYPE_EXEMP")
    private TypeExemp typeExemp;

    @Column(name = "LISTE_ZONES", length = 2000)
    private String listeZones;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "DEM_INDEX_RECHERCHE")
    private IndexRecherche indexRecherche;

    @OneToMany(mappedBy = "demandeExemp", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<LigneFichierExemp> ligneFichierExemps;

    @OneToMany(mappedBy = "demandeExemp", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<JournalDemandeExemp> journalDemandes;

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

    public DemandeExemp(Integer id, String rcr, Date dateCreation, Date dateModification, EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        super(id, rcr, dateCreation, dateModification, etatDemande, commentaire, utilisateur);
    }

    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.EXEMP;
    }
}
