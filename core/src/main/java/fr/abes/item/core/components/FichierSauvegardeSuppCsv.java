package fr.abes.item.core.components;

import com.opencsv.CSVWriter;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.StorageException;
import fr.abes.item.core.service.ReferenceService;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@Setter
@Getter
@Component
@NoArgsConstructor
public class FichierSauvegardeSuppCsv extends AbstractFichier implements Fichier {

    private ReferenceService referenceService;

    private CSVWriter csvWriter;

    public void writePpnInFile(String ppn, Exemplaire exemplaire) throws StorageException {
        // création de la liste de référence pour trouver l'emplacement de chaque zone et sous-zone
        List<String> listDeReference = referenceService.constructHeaderCsv();
        listDeReference.remove(0);

        String resultat = "";

        gererZones(listDeReference, exemplaire, resultat, null); // TODO passer une vrai Zone ou retirer le paramètre Zone dans l'appel de la méthode

        // ajout de la ligne
        this.csvWriter.writeNext(resultat.split(";"));
    }

    public String gererZones(List<String> listeZonesEtSousZones, Exemplaire exemplaire, String resultat, Zone zone) {
        if (listeZonesEtSousZones.isEmpty()) {
            return resultat;
        }
        String zoneSousZone = listeZonesEtSousZones.remove(0);
        if (zoneSousZone.startsWith("$")) {
            return gererSousZone(listeZonesEtSousZones, exemplaire, resultat, zone, zoneSousZone);
        } else {
            zone = exemplaire.findZone(zoneSousZone.split("\\$")[0],0);
            if (zone != null) {
                String sousZone = zone.findSubLabel(zoneSousZone.split("\\$")[1]);
                resultat += sousZone;
            }
            resultat += ";";
        }
        return gererZones(listeZonesEtSousZones, exemplaire, resultat, zone);
    }

    private String gererSousZone(List<String> listeZonesEtSousZones, Exemplaire exemplaire, String resultat, Zone zone, String sousZoneAChercher) {
        if (zone != null) {
            String sousZone = zone.findSubLabel(sousZoneAChercher);
            resultat += sousZone + ";";
        }
        return gererZones(listeZonesEtSousZones, exemplaire, resultat, zone);
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

    public void initWriter() throws IOException {
        this.csvWriter = new CSVWriter(Files.newBufferedWriter(this.path), ';', CSVWriter.NO_QUOTE_CHARACTER, CSVWriter.DEFAULT_ESCAPE_CHARACTER, CSVWriter.DEFAULT_LINE_END);
    }

    public void writeHeader() {
        this.csvWriter.writeNext((String[]) this.referenceService.constructHeaderCsv().toArray());
    }
}
