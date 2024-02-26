package fr.abes.item.service;

import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.DemandeModif;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.entities.item.LigneFichierModif;

import java.io.File;
import java.util.List;

public interface ILigneFichierService {
    int getNbLigneFichierTraiteeByDemande(Demande demande);

    int getNbLigneFichierSuccessByDemande(Demande demande);

    int getNbLigneFichierErrorByDemande(Demande demande);

    int getNbLigneFichierTotalByDemande(Demande demande);

    int getNbReponseTrouveesByDemande(Demande demande);

    int getNbZeroReponseByDemande(Demande demande);

    int getNbUneReponseByDemande(Demande demande);

    int getNbReponseMultipleByDemande(Demande demande);

    void saveFile(File file, Demande demande);

    void deleteByDemande(Demande demande);

    List<LigneFichier> getLigneFichierbyDemande(Demande demande);

    LigneFichier findById(Integer id);

    LigneFichier save(LigneFichier ligneFichier);

    List<LigneFichier> getLigneFichierTraiteeByDemande(Demande demande);

    LigneFichier getLigneFichierbyDemandeEtPos(Demande demande, Integer numLigne);

    int getNbLigneFichierNonTraitee(Demande demande);
}
