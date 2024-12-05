package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.repository.item.IJournalDemandeExempDao;
import fr.abes.item.core.repository.item.IJournalDemandeModifDao;
import fr.abes.item.core.repository.item.IJournalDemandeRecouvDao;
import fr.abes.item.core.repository.item.IJournalDemandeSuppDao;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class JournalService {
    private final IJournalDemandeExempDao journalDemandeExempDao;
    private final IJournalDemandeModifDao journalDemandeModifDao;
    private final IJournalDemandeRecouvDao journalDemandeRecouvDao;
    private final IJournalDemandeSuppDao journalDemandeSuppDao;

    public JournalService(IJournalDemandeExempDao journalDemandeExempDao, IJournalDemandeModifDao journalDemandeModifDao, IJournalDemandeRecouvDao journalDemandeRecouvDao, IJournalDemandeSuppDao journalDemandeSuppDao) {
        this.journalDemandeExempDao = journalDemandeExempDao;
        this.journalDemandeModifDao = journalDemandeModifDao;
        this.journalDemandeRecouvDao = journalDemandeRecouvDao;
        this.journalDemandeSuppDao = journalDemandeSuppDao;
    }

    public void addEntreeJournal(DemandeExemp demande, EtatDemande etat) {
        journalDemandeExempDao.save(new JournalDemandeExemp(Calendar.getInstance().getTime(), demande.getUtilisateur(), etat, demande));
    }

    public void addEntreeJournal(DemandeModif demandeModif, EtatDemande etat) {
        journalDemandeModifDao.save(new JournalDemandeModif(Calendar.getInstance().getTime(), demandeModif.getUtilisateur(), etat, demandeModif));
    }

    public void addEntreeJournal(DemandeRecouv demandeRecouv, EtatDemande etat) {
        journalDemandeRecouvDao.save(new JournalDemandeRecouv(Calendar.getInstance().getTime(), demandeRecouv.getUtilisateur(), etat, demandeRecouv));
    }

    public void addEntreeJournal(DemandeSupp demandeSupp, EtatDemande etat) {
        journalDemandeSuppDao.save(new JournalDemandeSupp(Calendar.getInstance().getTime(), demandeSupp.getUtilisateur(), etat, demandeSupp));
    }
}
