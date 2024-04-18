package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichierModif;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class LigneFichierDtoModif extends LigneFichierDto implements ILigneFichierDtoService  {
    private String ppn;
    private String rcr;
    private String epn;

    public LigneFichierDtoModif(LigneFichierModif ligneFichierModif) {
        super(ligneFichierModif.getNumLigneFichier(), ligneFichierModif.getTraitee(), ligneFichierModif.getPosition(), ligneFichierModif.getId(), ligneFichierModif.getRetourSudoc(), ligneFichierModif.getValeurZone());
        this.ppn = ligneFichierModif.getPpn();
        this.rcr = ligneFichierModif.getRcr();
        this.epn = ligneFichierModif.getEpn();
    }

    @Override
    public String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult) {
        return this.getPpn() + ";" + this.getRcr() + ";"
                + this.getEpn() + ";"
                + ((this.getValeurZone() == null) ? "" : this.getValeurZone()) + ";" + this.getRetourSudoc();
    }


    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.MODIF;
    }

}
