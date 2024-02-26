package fr.abes.item.service;

import fr.abes.item.dao.item.IJournalDemandeExempDao;
import fr.abes.item.dao.item.IJournalDemandeModifDao;
import fr.abes.item.entities.item.*;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JournalService {
    private final IJournalDemandeExempDao journalDemandeExempDao;
    private final IJournalDemandeModifDao journalDemandeModifDao;

    public JournalService(IJournalDemandeExempDao journalDemandeExempDao, IJournalDemandeModifDao journalDemandeModifDao) {
        this.journalDemandeExempDao = journalDemandeExempDao;
        this.journalDemandeModifDao = journalDemandeModifDao;
    }

    public List<JournalDemandeExemp> findAllExemp() {
        return journalDemandeExempDao.findAll();
    }

    public JournalDemandeExemp findByIdExemp(Integer id) {
        Optional<JournalDemandeExemp> journalDemandeExemp = journalDemandeExempDao.findById(id);
        return journalDemandeExemp.orElse(null);
    }

    public void addEntreeJournal(DemandeExemp demande, EtatDemande etat) {
        journalDemandeExempDao.save(new JournalDemandeExemp(new Date(), demande.getUtilisateur(), etat, demande));
    }

    public List<JournalDemandeModif> findAllModif() {
        return journalDemandeModifDao.findAll();
    }

    public JournalDemandeModif findByIdModif(Integer id) {
        Optional<JournalDemandeModif> journalDemande = journalDemandeModifDao.findById(id);
        return journalDemande.orElse(null);
    }

    public void addEntreeJournal(DemandeModif demandeModif, EtatDemande etat) {
        journalDemandeModifDao.save(new JournalDemandeModif(new Date(), demandeModif.getUtilisateur(), etat, demandeModif));
    }

    public void removeEntreesJournal(DemandeModif demandeModif) {
        journalDemandeModifDao.deleteAllLinesJournalDemandeModifByDemandeId(demandeModif.getNumDemande());
    }

    public void removeEntreesJournal(DemandeExemp demandeExemp) {
        journalDemandeExempDao.deleteAllLinesJournalDemandeExempByDemandeId(demandeExemp.getNumDemande());
    }
}
