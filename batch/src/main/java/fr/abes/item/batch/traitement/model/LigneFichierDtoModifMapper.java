package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierModif;

@Strategy(type = ILigneFichierDtoMapper.class, typeDemande = TYPE_DEMANDE.MODIF)
public class LigneFichierDtoModifMapper implements ILigneFichierDtoMapper {

    @Override
    public LigneFichier getLigneFichierEntity(LigneFichierDto lfd)
    {
        LigneFichierDtoModif lfdModif = (LigneFichierDtoModif) lfd;
        LigneFichierModif lf = new LigneFichierModif();
        lf.setNumLigneFichier(lfdModif.getNumLigneFichier());
        lf.setPpn(lfdModif.getPpn());
        lf.setEpn(lfdModif.getEpn());
        lf.setRcr(lfdModif.getRcr());
        lf.setValeurZone(lfdModif.getValeurZone());
        lf.setTraitee(lfdModif.getTraitee());
        lf.setPosition(lfdModif.getPosition());
        lf.setRetourSudoc(lfdModif.getRetourSudoc());
        lf.setDemandeModif(new DemandeModif(lfdModif.getRefDemande()));
        return lf;
    }

    private LigneFichierDtoModifMapper(){}

}
