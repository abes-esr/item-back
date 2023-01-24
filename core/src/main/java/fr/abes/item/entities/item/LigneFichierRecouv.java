package fr.abes.item.entities.item;

import fr.abes.item.entities.GenericEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@Table(name="LIGNE_FICHIER_RECOUV")
@NoArgsConstructor
@Getter @Setter
public class LigneFichierRecouv extends LigneFichier implements Serializable, GenericEntity<Integer>, ILigneFichier {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "NUM_LIGNEFICHIER")
    private Integer numLigneFichier;

    @Column(name = "INDEX_RECHERCHE")
    private String indexRecherche;

    @Column(name = "NB_REPONSES")
    private Integer nbReponses;

    @Column(name = "LISTE_PPN")
    private String listePpn;

    @ManyToOne
    @JoinColumn(name = "REF_DEMANDE") @NotNull
    private DemandeRecouv demandeRecouv;

    public LigneFichierRecouv(String indexRecherche, Integer traitee, Integer position, String retourSudoc, String numExemplaire, DemandeRecouv demandeRecouv) {
        super("", traitee, position, retourSudoc);
        this.indexRecherche = indexRecherche;
        this.demandeRecouv = demandeRecouv;
    }

    @Override
    public Integer getId() {
        return this.numLigneFichier;
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
