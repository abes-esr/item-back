package fr.abes.item.service.impl;

import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.*;
import fr.abes.item.service.IJournalService;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class JournalService implements IJournalService {
    @Autowired
    @Getter
    DaoProvider dao;

    @Override
    public List<JournalDemandeExemp> findAllExemp() {
        return getDao().getJournalDemandeExemp().findAll();
    }

    @Override
    public JournalDemandeExemp findByIdExemp(Integer id) {
        Optional<JournalDemandeExemp> journalDemandeExemp = getDao().getJournalDemandeExemp().findById(id);
        return journalDemandeExemp.orElse(null);
    }

    @Override
    public void addEntreeJournal(DemandeExemp demande, EtatDemande etat) {
        getDao().getJournalDemandeExemp().save(new JournalDemandeExemp(new Date(), demande.getUtilisateur(), etat, demande));
    }

    @Override
    public List<JournalDemandeModif> findAllModif() {
        return dao.getJournalDemandeModif().findAll();
    }

    @Override
    public JournalDemandeModif findByIdModif(Integer id) {
        Optional<JournalDemandeModif> journalDemande = dao.getJournalDemandeModif().findById(id);
        return journalDemande.orElse(null);
    }

    @Override
    public void addEntreeJournal(DemandeModif demandeModif, EtatDemande etat) {
        dao.getJournalDemandeModif().save(new JournalDemandeModif(new Date(), demandeModif.getUtilisateur(), etat, demandeModif));
    }

    @Override
    public void removeEntreesJournal(DemandeModif demandeModif) {
        dao.getJournalDemandeModif().deleteAllLinesJournalDemandeModifByDemandeId(demandeModif.getNumDemande());
    }

    @Override
    public void removeEntreesJournal(DemandeExemp demandeExemp) {
        dao.getJournalDemandeExemp().deleteAllLinesJournalDemandeExempByDemandeId(demandeExemp.getNumDemande());
    }
}
