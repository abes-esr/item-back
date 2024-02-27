package fr.abes.item.entities.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Getter @Setter
@NoArgsConstructor
@Table(name="LIGNE_FICHIER_EXEMP")
public class LigneFichierExemp extends LigneFichier implements Serializable, ILigneFichier {
    private static final long serialVersionUID = 1L;


    @Column(name = "INDEX_RECHERCHE")
    private String indexRecherche;

    @Column(name = "NUM_EXEMPLAIRE")
    private String numExemplaire;

    @Column(name = "L035")
    private String L035;

    @Column(name = "NB_REPONSE")
    private Integer nbReponse;

    @Column(name = "LISTE_PPN")
    private String listePpn;

    @ManyToOne
    @JoinColumn(name = "REF_DEMANDE") @NotNull
    private DemandeExemp demandeExemp;


    public LigneFichierExemp(String indexRecherche, String valeurZone, Integer traitee, Integer position, String retourSudoc, String numExemplaire, DemandeExemp demandeExemp, String listePpn) {
        super(valeurZone, traitee, position, retourSudoc);
        this.indexRecherche = indexRecherche;
        this.numExemplaire = numExemplaire;
        this.demandeExemp = demandeExemp;
        this.listePpn = listePpn;
    }

    @Override
    public void setEntityAfterBatch(LigneFichier ligneFichier) {
        LigneFichierExemp ligneFichierExemp = (LigneFichierExemp)ligneFichier;
        this.setRetourSudoc(ligneFichierExemp.getRetourSudoc());
        this.setNumExemplaire(ligneFichierExemp.getNumExemplaire());
        this.setL035(ligneFichierExemp.getL035());
        this.setNbReponse(ligneFichierExemp.getNbReponse());
        this.setTraitee(1);
        this.setListePpn(ligneFichierExemp.getListePpn());
    }
}
