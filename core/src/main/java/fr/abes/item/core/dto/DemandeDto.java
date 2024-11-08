package fr.abes.item.core.dto;

import fr.abes.item.core.entities.baseXml.LibProfile;
import fr.abes.item.core.entities.item.Demande;
import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Objects;

@Getter
@Setter
public class DemandeDto {
    private Demande demande;

    @Column(name = "NB_LIGNEFICHIER")
    private Integer nbLignes;

    public DemandeDto(Demande demande, Integer nbLignes) {
        this.demande = demande;
        this.nbLignes = nbLignes;
    }

    public DemandeDto(Demande demande) {
        this.demande = demande;
    }

    public String getRcr() {
        return this.demande.getRcr();
    }

    public void feedIlnAndShortname(List<LibProfile> libProfileList){
        //Si l'iln de la demande est nul, on l'alimente avec la liste d'entités Libprofile récupérée précédemment
        if(this.demande.getIln() == null) {
            for (LibProfile libProfile : libProfileList) {
                if (Objects.equals(libProfile.getRcr(), this.demande.getRcr())) {
                    this.demande.setIln(libProfile.getIln());
                }
            }
        }
        //Si le shortname de la demande est nul, on l'alimente avec la liste d'entités Libprofile récupérée précédemment
        if (this.demande.getShortname() == null) {
            for (LibProfile libProfile : libProfileList) {
                if (libProfile.getRcr().equals(this.demande.getRcr())) {
                    this.demande.setShortname(libProfile.getShortName());
                }
            }
        }
    }
}
