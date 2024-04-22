package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.repository.item.IJournalDemandeExempDao;
import fr.abes.item.core.repository.item.IJournalDemandeModifDao;
import org.springframework.stereotype.Service;

import java.util.Calendar;

@Service
public class JournalService {
    private final IJournalDemandeExempDao journalDemandeExempDao;
    private final IJournalDemandeModifDao journalDemandeModifDao;

    public JournalService(IJournalDemandeExempDao journalDemandeExempDao, IJournalDemandeModifDao journalDemandeModifDao) {
        this.journalDemandeExempDao = journalDemandeExempDao;
        this.journalDemandeModifDao = journalDemandeModifDao;
    }

    public void addEntreeJournal(DemandeExemp demande, EtatDemande etat) {
        journalDemandeExempDao.save(new JournalDemandeExemp(Calendar.getInstance().getTime(), demande.getUtilisateur(), etat, demande));
    }

    public void addEntreeJournal(DemandeModif demandeModif, EtatDemande etat) {
        journalDemandeModifDao.save(new JournalDemandeModif(Calendar.getInstance().getTime(), demandeModif.getUtilisateur(), etat, demandeModif));
    }
}
