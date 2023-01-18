package fr.abes.item.service;

import fr.abes.item.entities.item.Demande;
import fr.abes.item.entities.item.LigneFichier;
import fr.abes.item.entities.item.LigneFichierRecouv;

import java.io.File;
import java.util.List;

public interface ILigneFichierService {
    int getNbLigneFichierTraiteeByDemande(int numDemande);

    int getNbLigneFichierSuccessByDemande(int numDemande);

    int getNbLigneFichierErrorByDemande(int numDemande);

    int getNbLigneFichierTotalByDemande(int numDemande);

    void saveFile(File file, Demande demande);

    void deleteByDemande(Demande demande);

    List<LigneFichier> getLigneFichierbyDemande(Integer numDemande);

    LigneFichier findById(Integer id);

    LigneFichier save(LigneFichier ligneFichier);

    List<LigneFichier> getLigneFichierTraitee(Integer numDemande);

}
