package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.LigneFichierRecouv;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

@NoArgsConstructor
@Component
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
        super(ligneFichierRecouv.getNumLigneFichier(), ligneFichierRecouv.getTraitee(), ligneFichierRecouv.getPosition(), ligneFichierRecouv.getId(), ligneFichierRecouv.getRetourSudoc(), ligneFichierRecouv.getValeurZone());
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
        return new StringBuilder("\"").append(this.getRequete()).append("\"")
                .append(";").append(this.getNbReponses())
                .append(";").append((this.getNbReponses()!=0)? Utilitaires.getXPPN(this.getListePpn(), nbPpnInFileResult):"")
                .append(";")
                .toString();
    }

    public Integer getNbReponses() {
        if(nbReponses==null){
            return 0;
        }
        return nbReponses;
    }
}
