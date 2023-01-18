package fr.abes.item.entities.item;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@MappedSuperclass
@NoArgsConstructor
@Setter @Getter
public abstract class LigneFichier {
    @Column(name = "VALEUR_ZONE")
    private String valeurZone;
    @Column(name="TRAITEE")
    private Integer traitee;
    @Column(name="POS")
    private Integer position;
    @Column(name="RETOUR_SUDOC")
    private String retourSudoc;


    public LigneFichier(String valeurZone, Integer traitee, Integer position, String retourSudoc) {
        this.valeurZone = valeurZone;
        this.traitee = traitee;
        this.position = position;
        this.retourSudoc = retourSudoc;
    }
}
