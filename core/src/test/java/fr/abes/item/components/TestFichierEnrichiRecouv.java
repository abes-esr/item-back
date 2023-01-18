package fr.abes.item.components;

import fr.abes.item.constant.Constant;
import fr.abes.item.dao.impl.DaoProvider;
import fr.abes.item.entities.item.DemandeRecouv;
import fr.abes.item.entities.item.EtatDemande;
import fr.abes.item.entities.item.IndexRecherche;
import fr.abes.item.entities.item.Utilisateur;
import fr.abes.item.exception.FileCheckingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Answers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@DisplayName("Test fichier recouvrement")
public class TestFichierEnrichiRecouv {
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private DaoProvider dao;

    @InjectMocks
    private FichierEnrichiRecouv composantFichier;

    private DemandeRecouv demande;

    @BeforeEach
    void init(){
        composantFichier = new FichierEnrichiRecouv();
        composantFichier.setPath(Paths.get("src/test/resources/fichierEnrichiRecouv"));
        MockitoAnnotations.initMocks(this);
        this.demande = new DemandeRecouv("341725201", new Date(), new Date(), new EtatDemande(1), "", new Utilisateur(1), new IndexRecherche(1));
        when(dao.getIndexRecherche().findAll()).thenReturn(getIndexRecherche());
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
