package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.entities.item.LigneFichierModif;
import fr.abes.item.service.factory.Strategy;

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
