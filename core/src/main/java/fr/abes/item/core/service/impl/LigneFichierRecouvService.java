package fr.abes.item.core.service.impl;

import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierRecouv;
import fr.abes.item.core.repository.item.ILigneFichierRecouvDao;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.utilitaire.Utilitaires;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
@Strategy(type= ILigneFichierService.class, typeDemande = {TYPE_DEMANDE.RECOUV})
public class LigneFichierRecouvService implements ILigneFichierService {
    private final ILigneFichierRecouvDao dao;

    public LigneFichierRecouvService(ILigneFichierRecouvDao dao) {
        this.dao = dao;
    }


    @Override
    public List<LigneFichier> getLigneFichierTraiteeByDemande(Demande demande) {
        List<LigneFichierRecouv> ligneFichierRecouvs = dao.getLigneFichierTraitee(demande.getId());
        return new ArrayList<>(ligneFichierRecouvs);
    }

    @Override
    public LigneFichier getLigneFichierbyDemandeEtPos(Demande demande, Integer numLigne) {
        return dao.getLigneFichierbyDemandeEtPos(demande.getNumDemande(), numLigne);
    }

    @Override
    public int getNbLigneFichierNonTraitee(Demande demande) {
        return dao.getNbLigneFichierNonTraitee(demande.getId());
    }


    @Override
    public int getNbLigneFichierTraiteeByDemande(Demande demande) {
        return dao.getNbLigneFichierTraitee(demande.getId());
    }

    @Override
    public int getNbLigneFichierSuccessByDemande(Demande demande) {
        return dao.getNbLigneFichierSuccessByDemande(demande.getId());
    }

    @Override
    public int getNbLigneFichierErrorByDemande(Demande demande) {
        return dao.getNbLigneFichierErrorByDemande(demande.getId());
    }

    @Override
    public int getNbLigneFichierTotalByDemande(Demande demande) {
        return dao.getNbLigneFichierTotalByDemande(demande.getId());
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
                LigneFichierRecouv ligneFichierRecouv = new LigneFichierRecouv(indexRecherche.toString(), 0, position++, null, demandeRecouv);
                dao.save(ligneFichierRecouv);
            }
        } catch (
                IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Demande demande) {
        List<LigneFichierRecouv> ligneFichierRecouvs = dao.getLigneFichierbyDemande(demande.getId());
        return new ArrayList<>(ligneFichierRecouvs);
    }

    @Override
    public LigneFichier findById(Integer id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierRecouv ligneFichierRecouv = (LigneFichierRecouv) ligneFichier;
        return dao.save(ligneFichierRecouv);
    }

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        dao.deleteByDemandeRecouv((DemandeRecouv) demande);
    }

    @Override
    public int getNbReponseTrouveesByDemande(Demande demande) {
        return dao.getNbReponseTrouveesByDemande(demande.getId());
    }

    @Override
    public int getNbZeroReponseByDemande(Demande demande) {
        return 0;
    }

    @Override
    public int getNbUneReponseByDemande(Demande demande) {
        return 0;
    }

    @Override
    public int getNbReponseMultipleByDemande(Demande demande) {
        return 0;
    }
}
