package fr.abes.item.core.entities.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name="LIGNE_FICHIER_RECOUV")
@NoArgsConstructor
@Getter @Setter
public class LigneFichierRecouv extends LigneFichier implements Serializable, ILigneFichier {
    @Column(name = "INDEX_RECHERCHE")
    private String indexRecherche;

    @Column(name = "NB_REPONSES")
    private Integer nbReponses;

    @Column(name = "LISTE_PPN")
    private String listePpn;

    @ManyToOne
    @JoinColumn(name = "REF_DEMANDE") @NotNull
    private DemandeRecouv demandeRecouv;

    public LigneFichierRecouv(String indexRecherche, Integer traitee, Integer position, String retourSudoc, DemandeRecouv demandeRecouv) {
        super(traitee, position, retourSudoc);
        this.indexRecherche = indexRecherche;
        this.demandeRecouv = demandeRecouv;
    }

    @Override
    public void setEntityAfterBatch(LigneFichier ligneFichier) {
        LigneFichierRecouv ligneFichierRecouv = (LigneFichierRecouv) ligneFichier;
        this.setNbReponses(ligneFichierRecouv.getNbReponses());
        this.setListePpn(ligneFichierRecouv.getListePpn());
        this.setRetourSudoc(ligneFichierRecouv.getRetourSudoc());
        this.setTraitee(1);
    }
}
