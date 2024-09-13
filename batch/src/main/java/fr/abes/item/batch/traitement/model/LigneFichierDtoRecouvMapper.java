package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierRecouv;

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
        lf.setTraitee(lfdRecouv.getTraitee());
        lf.setPosition(lfdRecouv.getPosition());
        lf.setRetourSudoc(lfdRecouv.getRetourSudoc());
        lf.setDemandeRecouv(new DemandeRecouv(lfdRecouv.getRefDemande()));
        return lf;
    }
}
