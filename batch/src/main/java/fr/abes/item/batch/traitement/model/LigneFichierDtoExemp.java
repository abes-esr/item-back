package fr.abes.item.batch.traitement.model;

import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichierExemp;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LigneFichierDtoExemp extends LigneFichierDto implements ILigneFichierDtoService {

    private String requete;

    private String indexRecherche;

    private String listePpn;

    private String numExemplaire;

    private String L035;

    private Integer nbReponses;

    private String valeurZone;

    public LigneFichierDtoExemp(LigneFichierExemp ligneFichierExemp) {
        super(ligneFichierExemp.getNumLigneFichier(), ligneFichierExemp.getTraitee(), ligneFichierExemp.getPosition(), ligneFichierExemp.getId(), ligneFichierExemp.getRetourSudoc());
        this.valeurZone = ligneFichierExemp.getValeurZone();
        this.indexRecherche = ligneFichierExemp.getIndexRecherche();
        this.numExemplaire = ligneFichierExemp.getNumExemplaire();
        this.L035 = ligneFichierExemp.getL035();
        this.nbReponses = ligneFichierExemp.getNbReponse();
        this.listePpn = ligneFichierExemp.getListePpn();
    }

    @Override
    public String getValeurToWriteInFichierResultat(Demande demande, Integer nbPpnInFileResult) {
        return "\"" + this.getRequete() + "\"" +
                ";" + this.getNbReponses() +
                ";" + ((this.getNbReponses() != 0) ? Utilitaires.getXPPN(this.getListePpn(), nbPpnInFileResult) : "") +
                ";" + ((this.getL035() == null) ? "" : this.getL035()) +
                ";" + getRetourSudoc();
    }

    public TYPE_DEMANDE getTypeDemande() {
        return TYPE_DEMANDE.EXEMP;
    }

    public Integer getNbReponses() {
        return (nbReponses==null) ? 0 : nbReponses;
    }
}
