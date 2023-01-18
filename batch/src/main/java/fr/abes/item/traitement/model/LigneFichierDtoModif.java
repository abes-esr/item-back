package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.LigneFichierModif;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class LigneFichierDtoModif extends LigneFichierDto implements ILigneFichierDtoService  {

    @Getter @Setter
    private String ppn;
    @Getter @Setter
    private String rcr;
    @Getter @Setter
    private String epn;

    public LigneFichierDtoModif(Integer numLigneFichier, Integer traitee, Integer position, Integer refDemande, String retourSudoc, String valeurZone, String ppn, String rcr, String epn) {
        super(numLigneFichier, traitee, position, refDemande, retourSudoc, valeurZone);
        this.ppn = ppn;
        this.rcr = rcr;
        this.epn = epn;
    }

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
