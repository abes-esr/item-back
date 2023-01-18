package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@NoArgsConstructor
@Getter @Setter
public abstract class LigneFichierDto implements Serializable, ILigneFichierDtoService {
    private Integer numLigneFichier;
    private Integer traitee;
    private Integer position;
    private Integer refDemande;
    private String retourSudoc;
    private String valeurZone;

    LigneFichierDto(Integer numLigneFichier, Integer traitee, Integer position, Integer refDemande, String retourSudoc, String valeurZone) {
        this.numLigneFichier = numLigneFichier;
        this.traitee = traitee;
        this.position = position;
        this.refDemande = refDemande;
        this.retourSudoc = retourSudoc;
        this.valeurZone = valeurZone;
    }

    public abstract TYPE_DEMANDE getTypeDemande();
}
