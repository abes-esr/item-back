package fr.abes.item.core.service;

import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.repository.item.IJournalDemandeExempDao;
import fr.abes.item.core.repository.item.IJournalDemandeModifDao;
import fr.abes.item.core.repository.item.IJournalDemandeRecouvDao;
import fr.abes.item.core.repository.item.IJournalDemandeSuppDao;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.List;

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

    public EtatDemande getDernierEtatConnuAvantArchivage(DemandeSupp demandeSupp) throws DemandeCheckingException {
        List<JournalDemandeSupp> journalDemandeSuppList = journalDemandeSuppDao.findAllByDemandeSupp_NumDemandeOrderByDateEntreeDesc(demandeSupp.getNumDemande());
        if (journalDemandeSuppList.size() > 1) {
            return journalDemandeSuppList.get(1).getEtatDemande();
        }
        throw new DemandeCheckingException("Pas d'état antérieur trouvé pour cette demande");
    }

    public EtatDemande getDernierEtatConnuAvantArchivage(DemandeModif demandeModif) throws DemandeCheckingException {
        List<JournalDemandeModif> journalDemandeModifList = journalDemandeModifDao.findAllByDemandeModif_NumDemandeOrderByDateEntreeDesc(demandeModif.getNumDemande());
        if (journalDemandeModifList.size() > 1) {
            return journalDemandeModifList.get(1).getEtatDemande();
        }
        throw new DemandeCheckingException("Pas d'état antérieur trouvé pour cette demande");
    }

    public EtatDemande getDernierEtatConnuAvantArchivage(DemandeRecouv demandeRecouv) throws DemandeCheckingException {
        List<JournalDemandeRecouv> journalDemandeRecouvList = journalDemandeRecouvDao.findAllByDemandeRecouv_NumDemandeOrderByDateEntreeDesc(demandeRecouv.getNumDemande());
        if (journalDemandeRecouvList.size() > 1) {
            return journalDemandeRecouvList.get(1).getEtatDemande();
        }
        throw new DemandeCheckingException("Pas d'état antérieur trouvé pour cette demande");
    }

    public EtatDemande getDernierEtatConnuAvantArchivage(DemandeExemp demandeExemp) throws DemandeCheckingException {
        List<JournalDemandeExemp> journalDemandeExempList = journalDemandeExempDao.findAllByDemandeExemp_NumDemandeOrderByDateEntreeDesc(demandeExemp.getNumDemande());
        if (journalDemandeExempList.size() > 1) {
            return journalDemandeExempList.get(1).getEtatDemande();
        }
        throw new DemandeCheckingException("Pas d'état antérieur trouvé pour cette demande");
    }
}
