package fr.abes.item.entities.item;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.GenericEntity;
import fr.abes.item.entities.baseXml.LibProfile;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@Getter @Setter @ToString
@MappedSuperclass
public abstract class Demande implements Serializable, GenericEntity<Integer> {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name="NUM_DEMANDE")
    protected Integer numDemande;

    @Column(name = "RCR") @NotNull
    protected String rcr;

    @Transient
    private String shortname;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_CREATION")
    private Date dateCreation;

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "DATE_MODIFICATION")
    private Date dateModification;

    @ManyToOne
    @JoinColumn(name = "DEM_USER_ID") @NotNull
    private Utilisateur utilisateur;

    @Column(name = "ILN")
    private String iln;

    @ManyToOne
    @JoinColumn(name = "DEM_ETAT_ID") @NotNull
    private EtatDemande etatDemande;

    @Column(name = "COMMENTAIRE")
    private String commentaire;

    @Column(name = "POURCENTAGE_PROG_TRAITEMENT")
    private Integer pourcentageProgressionTraitement;

    public Demande(@NotNull String rcr, Date dateCreation, Date dateModification, @NotNull EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        this.rcr = rcr;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.etatDemande = etatDemande;
        this.commentaire = commentaire;
        this.utilisateur = utilisateur;
        this.pourcentageProgressionTraitement = 0;
    }

    public Demande(Integer numDemande, @NotNull String rcr, Date dateCreation, Date dateModification, String iln, @NotNull EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        this.numDemande = numDemande;
        this.rcr = rcr;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.iln = iln;
        this.etatDemande = etatDemande;
        this.commentaire = commentaire;
        this.utilisateur = utilisateur;
        this.pourcentageProgressionTraitement = 0;
    }

    @SuppressWarnings({"squid:S00107", "squid:S1172"})
    public Demande(Integer numDemande, @NotNull String rcr, Date dateCreation, Date dateModification, @NotNull EtatDemande etatDemande, String commentaire, Utilisateur utilisateur, String shortname) {
        this.numDemande = numDemande;
        this.rcr = rcr;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.etatDemande = etatDemande;
        this.commentaire = commentaire;
        this.shortname = shortname;
        this.utilisateur = utilisateur;
        this.pourcentageProgressionTraitement = 0;
    }

    @SuppressWarnings({"squid:S1172"})
    public Demande(Integer numDemande, @NotNull String rcr, Date dateCreation, Date dateModification, @NotNull EtatDemande etatDemande, String commentaire, Utilisateur utilisateur) {
        this.numDemande = numDemande;
        this.rcr = rcr;
        this.dateCreation = dateCreation;
        this.dateModification = dateModification;
        this.etatDemande = etatDemande;
        this.commentaire = commentaire;
        this.utilisateur = utilisateur;
        this.pourcentageProgressionTraitement = 0;
    }

    public Demande(Integer numDemande) {
        this.numDemande = numDemande;

        this.utilisateur = new Utilisateur(); //@NonNull values should not be set to null
        this.rcr = ""; //@NonNull values should not be set to null
        this.etatDemande = new EtatDemande(); //@NonNull values should not be set to null
        this.pourcentageProgressionTraitement = 0;
    }

    @Override
    public Integer getId() {
        return this.numDemande;
    }

    public abstract TYPE_DEMANDE getTypeDemande();

    public void feedIlnAndShortname(List<LibProfile> libProfileList){
        //Si l'iln de la demande est nul, on l'alimente avec la liste d'entités Libprofile récupérée précédemment
        if(this.iln == null) {
            for (LibProfile libProfile : libProfileList) {
                if (Objects.equals(libProfile.getRcr(), this.rcr)) {
                    this.iln = libProfile.getIln();
                }
            }
        }
        //Si le shortname de la demande est nul, on l'alimente avec la liste d'entités Libprofile récupérée précédemment
        if (this.shortname == null) {
            for (LibProfile libProfile : libProfileList) {
                if (libProfile.getRcr().equals(this.rcr)) {
                    this.shortname = libProfile.getShortName();
                }
            }
        }
    }
}
