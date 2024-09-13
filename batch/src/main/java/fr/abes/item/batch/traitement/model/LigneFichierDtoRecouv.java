package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichierRecouv;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
public class LigneFichierDtoRecouv extends LigneFichierDto implements ILigneFichierDtoService {
    @Getter @Setter
    private String requete;
    @Getter
    private String indexRecherche;
    @Setter
    private Integer nbReponses;
    @Getter @Setter
    private String listePpn;

    public LigneFichierDtoRecouv(LigneFichierRecouv ligneFichierRecouv) {
        super(ligneFichierRecouv.getNumLigneFichier(), ligneFichierRecouv.getTraitee(), ligneFichierRecouv.getPosition(), ligneFichierRecouv.getId(), ligneFichierRecouv.getRetourSudoc());
        this.indexRecherche = ligneFichierRecouv.getIndexRecherche();
        this.nbReponses = ligneFichierRecouv.getNbReponses();
        this.listePpn = ligneFichierRecouv.getListePpn();
    }

    @Override
    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.RECOUV;
    }

    @Override
    public String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult) {
        return "\"" + this.getRequete() + "\"" +
                ";" + this.getNbReponses() +
                ";" + ((this.getNbReponses() != 0) ? Utilitaires.getXPPN(this.getListePpn(), nbPpnInFileResult) : "") +
                ";";
    }

    public Integer getNbReponses() {
        if(nbReponses==null){
            return 0;
        }
        return nbReponses;
    }
}
