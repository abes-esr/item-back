package fr.abes.item.traitement.model;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.LigneFichierExemp;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;

@NoArgsConstructor
public class LigneFichierDtoExemp extends LigneFichierDto implements ILigneFichierDtoService {
    @Getter @Setter
    private String requete;

    @Getter @Setter
    private String indexRecherche;

    @Getter @Setter
    private String listePpn;

    @Getter @Setter
    private String numExemplaire;

    @Getter @Setter
    private String L035;

    @Setter
    private Integer nbReponses;


    @Value("${batch.nbPpnInFileResult}")
    private Integer nbPpnInFileResult;

    public LigneFichierDtoExemp(LigneFichierExemp ligneFichierExemp) {
        super(ligneFichierExemp.getNumLigneFichier(), ligneFichierExemp.getTraitee(), ligneFichierExemp.getPosition(), ligneFichierExemp.getId(), ligneFichierExemp.getRetourSudoc(), ligneFichierExemp.getValeurZone());
        this.indexRecherche = ligneFichierExemp.getIndexRecherche();
        this.numExemplaire = ligneFichierExemp.getNumExemplaire();
        this.L035 = ligneFichierExemp.getL035();
        this.nbReponses = ligneFichierExemp.getNbReponse();
        this.listePpn = ligneFichierExemp.getListePpn();
    }

    @Override
    public String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult) {
        return new StringBuilder("\"").append(this.getRequete()).append("\"")
                .append(";").append(this.getNbReponses())
                .append(";").append((this.getNbReponses()!=0)? Utilitaires.getXPPN(this.getListePpn(), nbPpnInFileResult):"")
                .append(";").append((this.getL035() == null) ? "" : this.getL035())
                .append(";").append(getRetourSudoc())
                .toString();
    }

    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.EXEMP;
    }

    public Integer getNbReponses() {
        if(nbReponses==null){
            return 0;
        }
        return nbReponses;
    }
}
