package fr.abes.item.core.components;

import fr.abes.cbs.notices.Exemplaire;
import fr.abes.item.core.constant.TYPE_DEMANDE;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.exception.FileCheckingException;
import lombok.Getter;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class FichierSauvegardeSupp implements Fichier{
    public List<Pair<String, List<Exemplaire>>> getPpnWithExemplairesList() {
        return ppnWithExemplairesList;
    }

    public void setPpnWithExemplairesList(List<Pair<String, List<Exemplaire>>> ppnWithExemplairesList) {
        this.ppnWithExemplairesList = ppnWithExemplairesList;
    }

    List<Pair<String, List<Exemplaire>>> ppnWithExemplairesList = new ArrayList<>();

    @Override
    public String getFilename() {
        return null;
    }

    @Override
    public void setPath(Path path) {

    }

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

    @Getter
    record Pair<K, V>(K key, V value) {}
}
