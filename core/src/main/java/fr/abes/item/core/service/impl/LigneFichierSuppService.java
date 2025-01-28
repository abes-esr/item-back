package fr.abes.item.core.service.impl;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.NoticeConcrete;
import fr.abes.item.core.configuration.factory.Strategy;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.dto.ExemplaireWithTypeDto;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeSupp;
import fr.abes.item.core.entities.item.LigneFichier;
import fr.abes.item.core.entities.item.LigneFichierSupp;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.item.ILigneFichierSuppDao;
import fr.abes.item.core.service.ILigneFichierService;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.utilitaire.Utilitaires;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.Level;
import org.mozilla.universalchardet.ReaderFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Strategy(type = ILigneFichierService.class, typeDemande = {TYPE_DEMANDE.SUPP})
@Service
public class LigneFichierSuppService implements ILigneFichierService {
    private final ILigneFichierSuppDao dao;
    private final TraitementService traitementService;
    private final ReentrantLock lock = new ReentrantLock();


    public LigneFichierSuppService(ILigneFichierSuppDao dao, TraitementService traitementService) {
        this.dao = dao;
        this.traitementService = traitementService;
    }

    @Override
    @Transactional
    public void saveFile(File file, Demande demande) {
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        try (BufferedReader reader = ReaderFactory.createBufferedReader(file)) {
            String line;
            String firstLine = reader.readLine(); //ne pas prendre en compte la première ligne avec les en-tête

            if (firstLine == null) {
                log.error(Constant.ERROR_FIRST_LINE_OF_FILE_NULL);
            }

            int position = 0;
            List<LigneFichierSupp> listToSave = new ArrayList<>();
            while ((line = reader.readLine()) != null) {
                Pattern regexp = Pattern.compile(Constant.LIGNE_FICHIER_SERVICE_PATTERN_SANS_VALEUR);
                Matcher colsFinded = regexp.matcher(line);
                String ppn = "";
                String rcr = "";
                String epn = "";
                while (colsFinded.find()) {
                    if (colsFinded.group("ppn") != null)
                        ppn = Utilitaires.addZeros(colsFinded.group("ppn"), Constant.TAILLEMAX);
                    if (colsFinded.group("rcr") != null)
                        rcr = Utilitaires.addZeros(colsFinded.group("rcr"), Constant.TAILLEMAX);
                    if (colsFinded.group("epn") != null)
                        epn = Utilitaires.addZeros(colsFinded.group("epn"), Constant.TAILLEMAX);
                }
                LigneFichierSupp lf = new LigneFichierSupp(ppn, rcr, epn, position++, 0, "", demandeSupp);
                listToSave.add(lf);
            }
            dao.saveAll(listToSave);
        } catch (IOException e) {
            log.error(e.getMessage());
        }
    }

    @Override
    public List<LigneFichier> getLigneFichierbyDemande(Demande demande) {
        List<LigneFichierSupp> ligneFichierSupps = dao.getLigneFichierbyDemande(demande.getId());
        return new ArrayList<>(ligneFichierSupps);
    }

    @Override
    public LigneFichierSupp findById(Integer id) {
        return dao.findById(id).orElse(null);
    }

    @Override
    public LigneFichier save(LigneFichier ligneFichier) {
        LigneFichierSupp ligneFichierSupp = (LigneFichierSupp) ligneFichier;
        return dao.save(ligneFichierSupp);
    }

    @Override
    public int getNbLigneFichierTraiteeByDemande(Demande demande) {
        return dao.getNbLigneFichierTraitee(demande.getId());
    }

    @Override
    public List<LigneFichier> getLigneFichierTraiteeByDemande(Demande demande) {
        List<LigneFichierSupp> ligneFichierSupps = dao.getLigneFichierTraitee(demande.getId());
        return new ArrayList<>(ligneFichierSupps);
    }

    @Override
    public LigneFichier getLigneFichierbyDemandeEtPos(Demande demande, Integer numLigne) {
        return dao.getLigneFichierbyDemandeEtPos(demande.getId(), numLigne);
    }

    @Override
    public int getNbLigneFichierNonTraitee(Demande demande) {
        return dao.getNbLigneFichierNonTraitee(demande.getId());
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
        return dao.getNbLigneFichierTotal(demande.getId());
    }

    @Override
    public int getNbReponseTrouveesByDemande(Demande demande) {
        return 0;
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

    @Override
    @Transactional
    public void deleteByDemande(Demande demande) {
        dao.deleteByDemandeSupp((DemandeSupp) demande);
    }

    @Override
    public String[] getNoticeExemplaireAvantApres(Demande demande, LigneFichier ligneFichier) throws CBSException, ZoneException, IOException {
        LigneFichierSupp ligneFichierSupp = (LigneFichierSupp) ligneFichier;
        DemandeSupp demandeSupp = (DemandeSupp) demande;
        try {
            lock.lock();
            traitementService.authenticate("M" + demandeSupp.getRcr());
            List<Exemplaire> exemplairesExistants = getExemplairesExistants(ligneFichierSupp.getPpn());
            //On ne conserve que les EPN de son RCR
            exemplairesExistants = exemplairesExistants.stream().filter(exemplaire -> exemplaire.findZone("930", 0).findSubLabel("$b").equals(demandeSupp.getRcr())).toList();
            if (exemplairesExistants.isEmpty()) {
                return new String[] {
                        ligneFichierSupp.getPpn(),
                        "Pas d'exemplaire pour ce RCR",
                        "Pas d'exemplaire pour ce RCR"
                };
            }
            List<Exemplaire> exemplairesRestants = suppExemlaire(exemplairesExistants, ligneFichierSupp.getEpn());

            return new String[]{
                    ligneFichierSupp.getPpn(),
                    exemplairesExistants.stream().map(exemplaire -> exemplaire.toString().replace("\r", "\r\n")).collect(Collectors.joining("\r\n\r\n")),
                    exemplairesRestants.stream().map(exemplaire -> exemplaire.toString().replace("\r", "\r\n")).collect(Collectors.joining("\r\n\r\n"))
            };
        }catch (QueryToSudocException ex) {
            throw new CBSException(Level.ERROR, ex.getMessage());
        } finally {
            traitementService.disconnect();
            lock.unlock();
        }
    }


    private List<Exemplaire> suppExemlaire(List<Exemplaire> exemplairesExistants, String epn) {
        return exemplairesExistants.stream().filter(exemplaire -> !exemplaire.findZone("A99", 0).getValeur().equals(epn)).collect(Collectors.toList());
    }

    public List<Exemplaire> getExemplairesExistants(String ppn) throws IOException, QueryToSudocException, CBSException, ZoneException {
        String query = "che ppn " + ppn;
        traitementService.getCbs().search(query);
        int nbReponses = traitementService.getCbs().getNbNotices();
        return switch (nbReponses) {
            case 0 -> throw new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND);
            case 1 -> {
                String notice = traitementService.getCbs().getClientCBS().mod("1", String.valueOf(traitementService.getCbs().getLotEncours()));
                String exemplaires = Utilitaires.getExemplairesExistants(notice);
                List<Exemplaire> exempList = new ArrayList<>();
                if (!exemplaires.isEmpty()) {
                    for (String s : exemplaires.split("\r\r\r")) {
                        if (!s.isEmpty())
                            exempList.add(new Exemplaire(s));
                    }
                }
                yield exempList;
            }
            default ->
                    throw new QueryToSudocException(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + traitementService.getCbs().getListePpn());
        };
    }

    public ExemplaireWithTypeDto getExemplairesAndTypeDoc(String ppn) throws QueryToSudocException, IOException, CBSException, ZoneException {
        String query = "che ppn " + ppn;
        traitementService.getCbs().search(query);
        int nbReponses = traitementService.getCbs().getNbNotices();
        ExemplaireWithTypeDto exempWithTypeDto = new ExemplaireWithTypeDto();
        return switch (nbReponses) {
            case 0 -> throw new QueryToSudocException(Constant.ERR_FILE_NOTICE_NOT_FOUND);
            case 1 -> {
                NoticeConcrete notice = traitementService.getCbs().editerNoticeConcrete("1");
                exempWithTypeDto.setType(notice.getNoticeBiblio().findZone("008", 0).findSubLabel("$a").substring(0,2));
                exempWithTypeDto.addExemplaires(notice.getExemplaires());
                yield exempWithTypeDto;
            }
            default ->
                    throw new QueryToSudocException(Constant.ERR_FILE_MULTIPLES_NOTICES_FOUND + traitementService.getCbs().getListePpn());
        };
    }

    @Override
    public String getQueryToSudoc(String code, Integer type, String[] valeurs) throws QueryToSudocException {
        return null;
    }
}
