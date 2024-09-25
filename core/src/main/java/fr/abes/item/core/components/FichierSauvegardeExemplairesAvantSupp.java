package fr.abes.item.core.components;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.LigneFichierSupp;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.item.ILigneFichierSuppDao;
import fr.abes.item.core.service.impl.DemandeSuppService;
import lombok.Getter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class FichierSauvegardeExemplairesAvantSupp extends AbstractFichier implements Fichier {
    List<Pair<String, List<Exemplaire>>> ppnWithExemplairesList = new ArrayList<>();
    private final DemandeSuppService demandeSuppService;
    private final ILigneFichierSuppDao iLigneFichierSuppDao;

    public FichierSauvegardeExemplairesAvantSupp(DemandeSuppService demandeSuppService,
                                                 ILigneFichierSuppDao iLigneFichierSuppDao) {
        this.demandeSuppService = demandeSuppService;
        this.iLigneFichierSuppDao = iLigneFichierSuppDao;
    }

    //TODO faire un TU pour controler en sortie l'attendu, la classe n'utilise pas le module batch
    public void feedListPpnWithExemplaires(Demande demande) throws ZoneException, QueryToSudocException, IOException, CBSException {
        //Récupère pour une demande la liste des lignes du fichier chacune contenant le numéro de ppn
        List<LigneFichierSupp> ligneFichierSuppList = iLigneFichierSuppDao.getLigneFichierbyDemande(demande.getNumDemande());
        //Parcours la liste de lignes et pour chacune ajoute le ppn avec les exemplaires existants
        for (LigneFichierSupp ligne : ligneFichierSuppList) {
            String ppn = ligne.getPpn();
            List<Exemplaire> exemplaires = demandeSuppService.getExemplairesExistants(ligne);
            ppnWithExemplairesList.add(new Pair<>(ppn, exemplaires));
        }
    }

    //TODO méthode qui formatera ppnWithExemplairesList selon l'agencement voulu dans le fichier.txt

    @Override
    public int getType() {
        return 0;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return null;
    }

    @Override
    public void generateFileName(Demande numDemande) {

    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {

    }

    // Classe utilitaire pour représenter une paire de valeurs
    @Getter
    record Pair<K, V>(K key, V value) {}
}
