package fr.abes.item.core.components;

import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.exception.StorageException;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;

@Setter
@Getter
@Component
public class FichierSauvegardeSuppTxt extends AbstractFichier implements Fichier {

    public void writePpnInFile(String ppn, Exemplaire exemplaire) throws StorageException {
        try (FileWriter fw = new FileWriter(this.getPath().resolve(this.getFilename()).toString(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(ppn);
            out.println("\n");
            out.println(exemplaire);
            out.println("\n");
            out.println("\n");
        } catch (IOException ex) {
            throw new StorageException("Impossible d'écrire dans le fichier de sauvegarde txt");
        }
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
        return Constant.ETATDEM_ATTENTE;
    }

    @Override
    public TYPE_DEMANDE getDemandeType() {
        return TYPE_DEMANDE.SUPP;
    }

    @Override
    public void generateFileName(Demande demande) {
        this.filename = Constant.FIC_SAUVEGARDE_NAME + demande.getId() + Constant.EXTENSIONTXT;
    }

    @Override
    public void checkFileContent(Demande d) throws FileCheckingException, IOException {
        //non implémentée
    }
}
