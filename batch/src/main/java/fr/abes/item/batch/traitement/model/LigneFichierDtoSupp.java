package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichierSupp;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@Getter @Setter
public class LigneFichierDtoSupp extends LigneFichierDto implements ILigneFichierDtoService{
    private String ppn;
    private String rcr;
    private String epn;

    public LigneFichierDtoSupp(LigneFichierSupp ligneFichierSupp) {
        super(ligneFichierSupp.getNumLigneFichier(), ligneFichierSupp.getTraitee(), ligneFichierSupp.getPosition(), ligneFichierSupp.getId(), ligneFichierSupp.getRetourSudoc(), ligneFichierSupp.getValeurZone());
        this.ppn = ligneFichierSupp.getPpn();
        this.rcr = ligneFichierSupp.getRcr();
        this.epn = ligneFichierSupp.getEpn();
    }

    @Override
    public String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult) {
        return this.getPpn() + ";" + this.getRcr() + ";"
                + this.getEpn() + ";" + this.getRetourSudoc();
    }


    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.SUPP;
    }


}
