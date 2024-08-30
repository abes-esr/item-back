package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.*;

@Strategy(type = ILigneFichierDtoMapper.class, typeDemande = TYPE_DEMANDE.SUPP)
public class LigneFichierDtoSuppMapper implements ILigneFichierDtoMapper {

    @Override
    public LigneFichier getLigneFichierEntity(LigneFichierDto lfd)
    {
        LigneFichierDtoSupp lfdSupp = (LigneFichierDtoSupp) lfd;
        LigneFichierSupp lf = new LigneFichierSupp();
        lf.setNumLigneFichier(lfdSupp.getNumLigneFichier());
        lf.setPpn(lfdSupp.getPpn());
        lf.setEpn(lfdSupp.getEpn());
        lf.setRcr(lfdSupp.getRcr());
        lf.setValeurZone(lfdSupp.getValeurZone());
        lf.setTraitee(lfdSupp.getTraitee());
        lf.setPosition(lfdSupp.getPosition());
        lf.setRetourSudoc(lfdSupp.getRetourSudoc());
        lf.setDemandeSupp(new DemandeSupp(lfdSupp.getRefDemande()));
        return lf;
    }

    private LigneFichierDtoSuppMapper(){}

}
