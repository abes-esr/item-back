package fr.abes.item.service.impl;

import fr.abes.item.constant.TYPE_DEMANDE;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.*;
import fr.abes.item.service.ILigneFichierRecouvService;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.factory.Strategy;
import fr.abes.item.utilitaire.Utilitaires;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Strategy(type= ILigneFichierService.class, typeDemande = {TYPE_DEMANDE.RECOUV})
public class LigneFichierRecouvService implements ILigneFichierRecouvService {
    @Autowired @Getter
    DaoProvider dao;

    @Override
    public int getNbLigneFichierTraiteeByDemande(int numDemande) {
        return getDao().getLigneFichierRecouv().getNbLigneFichierTraitee(numDemande);
    }

    @Override
    public List<LigneFichier> getLigneFichierTraitee(Integer numDemande) {
        List<LigneFichierRecouv> ligneFichierRecouvs = getDao().getLigneFichierRecouv().getLigneFichierTraitee(numDemande);
        List<LigneFichier> ligneFichiers = new ArrayList<>(ligneFichierRecouvs);
        return ligneFichiers;
    }

    @Override
    public int getNbLigneFichierSuccessByDemande(int numDemande) {
        return getDao().getLigneFichierRecouv().getNbLigneFichierSuccessByDemande(numDemande);
    }

    @Override
    public int getNbLigneFichierErrorByDemande(int numDemande) {
        return getDao().getLigneFichierRecouv().getNbLigneFichierErrorByDemande(numDemande);
    }

    @Override
    public int getNbLigneFichierTotalByDemande(int numDemande) {
        return getDao().getLigneFichierRecouv().getNbLigneFichierTotalByDemande(numDemande);
    }

    @Override
    public void saveFile(File file, Demande demande) {
        DemandeRecouv demandeRecouv = (DemandeRecouv) demande;
        try (BufferedReader reader = ReaderFactory.createBufferedReader(file)) {
            String line;
            //lecture à vide de la première ligne du fichier
            reader.readLine();
            int position = 0;

            while ((line = reader.readLine()) != null) {
                //Suppression des éventuels ; que l'utilisateur aurait pu oublier à la fin de certaines lignes
                line = Utilitaires.removeSemicolonFromEndOfLine(line);

                StringBuilder indexRecherche = new StringBuilder();
                String [] tabLine = line.split(";");
                for (int i = 0; i < demandeRecouv.getIndexRecherche().getIndexZones();i++) {
                    indexRecherche.append(tabLine[i]).append(";");
                }
                //création de la ligne fichier et enregistrement
                LigneFichierRecouv ligneFichierRecouv = new LigneFichierRecouv(indexRecherche.toString(), 0, position++, "", null, demandeRecouv);
                getDao().getLigneFichierRecouv().save(ligneFichierRecouv);
            }
        } catch (
                IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Integer numDemande) {
        List<LigneFichierRecouv> ligneFichierRecouvs = getDao().getLigneFichierRecouv().getLigneFichierbyDemande(numDemande);
        List<LigneFichier> ligneFichiers = new ArrayList<>(ligneFichierRecouvs);
        return ligneFichiers;
    }

    @Override
    public LigneFichier findById(Integer id) {
        return getDao().getLigneFichierRecouv().findById(id).orElse(null);
    }

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierRecouv ligneFichierRecouv = (LigneFichierRecouv) ligneFichier;
        return getDao().getLigneFichierRecouv().save(ligneFichierRecouv);
    }

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        getDao().getLigneFichierRecouv().deleteByDemandeRecouv((DemandeRecouv) demande);
    }
}
