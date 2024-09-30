package fr.abes.item.core.components;

import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.StorageException;
import fr.abes.item.core.service.ReferenceService;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

@Setter
@Getter
@Component
public class FichierSauvegardeSuppCsv extends AbstractFichier implements Fichier {

    private ReferenceService referenceService;

    private StringBuilder csvContent;

    public FichierSauvegardeSuppCsv() {
        this.csvContent = new StringBuilder();
    }

    public void writePpnInFile(String ppn, List<Exemplaire> exemplaires) throws StorageException {
        // TODO coder l'algo de remplissage du fichier csv (hors en-tête)
    }

    @Override
    public String getFilename() {
        return this.filename;
    }

    @Override
    public void setPath(Path path) {
        this.path = path;
    }

    @Override
    public int getType() {
        return Constant.ETATDEM_ATTENTE_2;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_SAUVEGARDE_NAME + demande.getId() + Constant.EXTENSIONCSV;
    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {
        //non implémentée
    }

    public void writeHeader() {
        this.csvContent.append(this.referenceService.constructHeaderCsv());
    }
}
