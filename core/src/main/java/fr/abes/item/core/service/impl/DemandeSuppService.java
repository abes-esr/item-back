package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.exception.DemandeCheckingException;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.FileTypeException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeSuppDao;
import fr.abes.item.core.service.FileSystemStorageService;
import fr.abes.item.core.service.IDemandeService;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.service.UtilisateurService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@Strategy(type = IDemandeService.class, typeDemande = {TYPE_DEMANDE.SUPP})
public class DemandeSuppService extends DemandeService implements IDemandeService {
    private final IDemandeSuppDao demandeSuppDao;
    private final FileSystemStorageService storageService;
    private final ReferenceService referenceService;
    private final UtilisateurService utilisateurService;

    public DemandeSuppService(ILibProfileDao libProfileDao, IDemandeSuppDao demandeSuppDao, FileSystemStorageService storageService, ReferenceService referenceService, UtilisateurService utilisateurService) {
        super(libProfileDao);
        this.demandeSuppDao = demandeSuppDao;
        this.storageService = storageService;
        this.referenceService = referenceService;
        this.utilisateurService = utilisateurService;
    }

    @Override
    public Demande save(Demande entity) {
        return null;
    }

    @Override
    public Demande findById(Integer id) {
        return null;
    }

    @Override
    public Demande creerDemande(String rcr, Integer userNum) {
        return null;
    }

    @Override
    public Demande archiverDemande(Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public void deleteById(Integer id) {

    }

    @Override
    public void initFiles(Demande demande) throws FileTypeException {

    }

    @Override
    public void stockerFichier(MultipartFile file, Demande demande) throws IOException, FileTypeException, FileCheckingException, DemandeCheckingException {

    }

    @Override
    public Demande previousState(Demande demande) throws DemandeCheckingException, IOException {
        return null;
    }

    @Override
    public Demande closeDemande(Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public List<Demande> getActiveDemandesForUser(String iln) {
        return null;
    }

    @Override
    public Demande getIdNextDemandeToProceed(int minHour, int maxHour) {
        return null;
    }

    @Override
    public String getInfoHeaderFichierResultat(Demande demande, LocalDateTime dateDebut) {
        return null;
    }

    @Override
    public Demande changeState(Demande demande, int etatDemande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public Demande changeStateCanceled(Demande demande, int etatDemande) {
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandes(String iln) {
        return null;
    }

    @Override
    public List<Demande> getAllArchivedDemandesAllIln() {
        return null;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdminExtended() {
        List<DemandeSupp> demandeSupps = demandeSuppDao.getAllActiveDemandesModifForAdminExtended();
        List<Demande> demandesList = new ArrayList<>(demandeSupps);
        setIlnShortNameOnList(demandesList);
        return demandesList;
    }

    @Override
    public List<Demande> getAllActiveDemandesForAdmin(String iln) {
        return null;
    }

    @Override
    public Demande returnState(Integer etape, Demande demande) throws DemandeCheckingException {
        return null;
    }

    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws CBSException, ZoneException, IOException {
        return new String[0];
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToArchive() {
        return null;
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToPlaceInDeletedStatus() {
        return null;
    }

    @Override
    public List<? extends Demande> getIdNextDemandeToDelete() {
        return null;
    }

    @Override
    public String getQueryToSudoc(String code, String type, String[] valeurs) throws QueryToSudocException {
        return null;
    }
}
