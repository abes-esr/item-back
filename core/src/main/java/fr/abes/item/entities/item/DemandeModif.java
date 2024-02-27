package fr.abes.item.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.constant.TYPE_DEMANDE;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;
import java.util.Set;

import static jakarta.persistence.CascadeType.PERSIST;


/**
 * Une demande de modification. Chaque demande de modification contient
 * - un type de traitement (création / édition / suppression) : plusieurs demandes peuvent avoir le même type de traitement
 * - les lignes du fichier associé à la demande : chaque demande contient un fichier de plusieurs lignes. Chaque ligne
 * est enregistré comme ligne de la table LIGNE_FICHIER_MODIF présente en base
 * - les lignes de journal associée à la demande : chaque demande contient un journal qui montre l'ensemble des
 * modification sur cette demande.
 */
@Entity
@NoArgsConstructor
@Getter @Setter
@JsonIgnoreProperties({"ligneFichierModifs","journalDemandeModifs"})
@Table(name= "DEMANDE_MODIF")
public class DemandeModif extends Demande{
    @Column(name = "ZONE")
    private String zone;

    @Column(name = "SOUS_ZONE")
    private String sousZone;

    @ManyToOne(cascade={PERSIST})
    @JoinColumn(name = "DEM_TRAIT_ID")
    private Traitement traitement;

    @Getter @Setter @OneToMany(mappedBy = "demandeModif", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<LigneFichierModif> ligneFichierModifs;

    @Getter @Setter @OneToMany(mappedBy = "demandeModif", fetch = FetchType.LAZY, cascade = CascadeType.REMOVE)
    private Set<JournalDemandeModif> journalDemandeModifs;

    @SuppressWarnings("squid:S00107")
    public DemandeModif(String rcr, Date dateCreation, Date dateModification, String zone, String sousZone,
                        String comment, EtatDemande etatDemande, Utilisateur utilisateur, Traitement traitement) {
        super(rcr, dateCreation, dateModification, etatDemande, comment, utilisateur);
        this.zone = zone;
        this.sousZone = sousZone;
        this.traitement = traitement;
    }

    @SuppressWarnings("squid:S00107")
    public DemandeModif(Integer numDemande, String rcr, Date dateCreation, Date dateModification, String zone,
                        String sousZone, String comment, EtatDemande etatDemande, Utilisateur utilisateur,
                        Traitement traitement) {
        super(numDemande, rcr, dateCreation, dateModification, etatDemande, comment, utilisateur);
        this.zone = zone;
        this.sousZone = sousZone;
        this.traitement = traitement;
    }

    @SuppressWarnings("squid:S00107")
    public DemandeModif(Integer numDemande, String rcr, Date dateCreation, Date dateModification, String zone,
                        String sousZone, String comment, EtatDemande etatDemande, Utilisateur utilisateur,
                        Traitement traitement, String iln) {
        super(numDemande, rcr, dateCreation, dateModification, iln, etatDemande, comment, utilisateur);
        this.zone = zone;
        this.sousZone = sousZone;
        this.traitement = traitement;
    }

    public DemandeModif(Integer numDemande) {
        super(numDemande);
    }


    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.MODIF;
    }
}
