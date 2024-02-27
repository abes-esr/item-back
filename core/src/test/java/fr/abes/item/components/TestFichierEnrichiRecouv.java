package fr.abes.item.components;

import fr.abes.item.constant.Constant;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.IndexRecherche;
import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.FileCheckingException;
import fr.abes.item.repository.item.IIndexRechercheDao;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test fichier recouvrement")
@SpringBootTest(classes = {FichierEnrichiRecouv.class})
public class TestFichierEnrichiRecouv {
    @Autowired
    private FichierEnrichiRecouv composantFichier;

    @MockBean
    private IIndexRechercheDao indexRechercheDao;

    private DemandeRecouv demande;

    @BeforeEach
    void init(){
        composantFichier.setPath(Paths.get("src/test/resources/fichierEnrichiRecouv"));
        this.demande = new DemandeRecouv("341725201", new Date(), new Date(), new EtatDemande(1), "", new Utilisateur(1), new IndexRecherche(1));
    }


    private List<IndexRecherche> getIndexRecherche() {
        List<IndexRecherche> liste = new ArrayList<>();
        liste.add(new IndexRecherche(1, "ISSN", "ISS", 1));
        liste.add(new IndexRecherche(2, "Date;Auteur;Titre", "DAT", 3));
        liste.add(new IndexRecherche(3, "ISBN", "ISB", 1));
        return liste;
    }

    @Test
    void testIndexRechercheok() throws FileCheckingException, IOException {
        Mockito.when(indexRechercheDao.findAll()).thenReturn(getIndexRecherche());
        composantFichier.setFilename("okindex.csv");
        composantFichier.checkFileContent(demande);
    }

    /**
     * Méthode vérifiant que l'index de la première colonne est autorisé
     */
    @Test
    void testIndexRechercheNok() {
        composantFichier.setFilename("nokindex.csv");
        assertThat(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_INDEXINCONNU)).isTrue();
    }

    /**
     * Méthode vérifiant qu'on a bien le même nombre de colonnes dans toutes les lignes du fichier
     */
    @Test
    void testNbColonnes() {
        composantFichier.setFilename("nokNbColonnes.csv");
        assertThat(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ERRLINE + "3" + Constant.ERR_FILE_WRONGNBCOLUMNS));
    }

    /**
     * Méthode vérifiant qu'on a pas de ligne vide dans le fichier
     */
    @Test
    void testLigneVide() {
        composantFichier.setFilename("nokLigneVide.csv");
        assertThat(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_LIGNE_ANORNALE));
    }
    /**
     * Méthode vérifiant que le champ date est bien codé sur 4 chiffres
     */
    @Test
    void testDate() {
        composantFichier.setFilename("nokDate.csv");
        assertThat(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ERRLINE + "2" + Constant.ERR_FILE_DATENOK));
    }

    /**
     * Méthode vérifiant que le champ PPN est conforme
     */
    @Test
    void testPpn() {
        composantFichier.setFilename("nokPpn.csv");
        assertThat(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ERRLINE + "2" + Constant.ERR_FILE_WRONGPPN));
    }
}
