package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.entities.item.LigneFichierRecouv;
import fr.abes.item.service.factory.Strategy;

@Strategy(type = ILigneFichierDtoMapper.class, typeDemande = TYPE_DEMANDE.RECOUV)
public class LigneFichierDtoRecouvMapper implements ILigneFichierDtoMapper {
    @Override
    public LigneFichier getLigneFichierEntity(LigneFichierDto lfd) {
        LigneFichierDtoRecouv lfdRecouv = (LigneFichierDtoRecouv) lfd;
        LigneFichierRecouv lf = new LigneFichierRecouv();
        lf.setNumLigneFichier(lfdRecouv.getNumLigneFichier());
        lf.setIndexRecherche(lfdRecouv.getIndexRecherche());
        lf.setListePpn(lfdRecouv.getListePpn());
        lf.setNbReponses(lfdRecouv.getNbReponses());
        lf.setValeurZone(lfdRecouv.getValeurZone());
        lf.setTraitee(lfdRecouv.getTraitee());
        lf.setPosition(lfdRecouv.getPosition());
        lf.setRetourSudoc(lfdRecouv.getRetourSudoc());
        lf.setDemandeRecouv(new DemandeRecouv(lfdRecouv.getRefDemande()));
        return lf;
    }
}
