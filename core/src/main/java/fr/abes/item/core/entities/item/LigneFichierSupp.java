package fr.abes.item.core.entities.item;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

@Entity
@NoArgsConstructor
@Table(name= "LIGNE_FICHIER_SUPP")
@Getter @Setter
public class LigneFichierSupp extends LigneFichier implements Serializable, ILigneFichier {
    @Column(name="PPN")
    private String ppn;
    @Column(name="RCR")
    private String rcr;
    @Column(name="EPN")
    private String epn;
    @ManyToOne
    @JoinColumn(name = "REF_DEMANDE") @NotNull
    private DemandeSupp demandeSupp;

    public LigneFichierSupp(String ppn,
                            String rcr, String epn, String valeurZone, Integer position,
                            Integer traitee, String retourSudoc, DemandeSupp demandeSupp) {
        super(valeurZone, traitee, position, retourSudoc);
        this.ppn = ppn;
        this.rcr = rcr;
        this.epn = epn;
        this.demandeSupp = demandeSupp;
    }

    @Override
    public void setEntityAfterBatch(LigneFichier ligneFichier) {
        LigneFichierSupp ligneFichierSupp = (LigneFichierSupp) ligneFichier;
        this.setRetourSudoc(ligneFichierSupp.getRetourSudoc());
        this.setTraitee(1);
    }
}
