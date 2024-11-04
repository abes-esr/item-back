package fr.abes.item.core.components;

import fr.abes.item.core.constant.Constant;
import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.exception.FileCheckingException;
import fr.abes.item.core.repository.item.ISousZonesAutoriseesDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import fr.abes.item.core.service.ReferenceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.*;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@DisplayName("test fichier exemplarisation")
@SpringBootTest(classes = FichierEnrichiExemp.class)
class TestFichierEnrichiExemp {
    @Autowired
    FichierEnrichiExemp composantFichier;
    @MockBean
    IZonesAutoriseesDao zonesAutoriseesDao;
    @MockBean
    ISousZonesAutoriseesDao sousZonesAutoriseesDao;
    @MockBean
    ReferenceService referenceService;


    DemandeExemp demande;


    @BeforeEach
    void init(){
        composantFichier.setPath(Paths.get("src/test/resources/fichierEnrichiExemp"));
        this.demande = new DemandeExemp("341725201", new Date(), new Date(), new EtatDemande(1), "", new Utilisateur(1), new TypeExemp(1), new IndexRecherche(1));
        when(zonesAutoriseesDao.getZonesByTypeExemp(anyInt())).thenReturn(getZonesAutorisees());
        when(sousZonesAutoriseesDao.getSousZonesAutoriseesByZone(anyString())).thenReturn(getSousZonesAutorisees());
        when(sousZonesAutoriseesDao.getSousZonesAutoriseesMandatory(Optional.ofNullable(demande.getTypeExemp().getId()))).thenReturn(getSousZonesMandatory());
        when(referenceService.getIndexRechercheFromTypeExemp(anyInt())).thenReturn(getIndexRecherche());
    }

    private List<String> getZonesAutorisees() {
        List<String> liste = new ArrayList<>();
        liste.add("915");
        liste.add("917");
        liste.add("930");
        liste.add("E856");
        return liste;
    }

    private List<String> getSousZonesAutorisees() {
        List<String> liste = new ArrayList<>();
        liste.add("$a");
        liste.add("$b");
        liste.add("$c");
        liste.add("$j");
        return liste;
    }

    private List<SousZonesAutorisees> getSousZonesMandatory() {
        ZonesAutorisees zone = new ZonesAutorisees();
        zone.setLabelZone("930");
        SousZonesAutorisees ssZone1 = new SousZonesAutorisees(1, "$j", zone, true);
        List<SousZonesAutorisees> listeZones = new ArrayList<>();
        listeZones.add(ssZone1);
        return listeZones;
    }

    private Set<IndexRecherche> getIndexRecherche() {
        Set<IndexRecherche> liste = new HashSet<>();
        liste.add(new IndexRecherche(1, "ISBN", "ISBN", 1));
        liste.add(new IndexRecherche(3, "PPN", "PPN", 1));
        liste.add(new IndexRecherche(4, "Numéro Source", "SOU", 1));
        liste.add(new IndexRecherche(5, "Date;Auteur;Titre", "DAT", 3));
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
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_INDEXINCONNU));
    }

    @Test
    void testZonesOk() throws FileCheckingException, IOException {
        composantFichier.setFilename("okzones.csv");
        composantFichier.checkFileContent(demande);
    }

    /**
     * Méthode vérifiant qu'une zone est bien autorisée
     */
    @Test
    void testZonesNok1() {
        //teste une zone non autorisée
        composantFichier.setFilename("nokzones1.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ZONENONAUTORISEE));
    }

    /**
     * Méthode vérifiant qu'une sous zone est bien autorisée pour une zone donnée
     */
    @Test
    void testZonesNok2() {
        //teste une sous zone non autorisée
        composantFichier.setFilename("nokzones2.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_SOUSZONENONAUTORISEE));
    }

    /**
     * Méthode vérifiant que le fichier contient bien une zone dans l'en-tête
     */
    @Test
    void testNoZone() {
        composantFichier.setFilename("noZone.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_NOZONE));
    }

    /**
     * Méthode vérifiant qu'une zone dans la ligne d'en tête a bien une sous zone fournie avec
     */
    @Test
    void testZoneSansSousZone() {
        composantFichier.setFilename("zoneSansSousZone.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ZONEINCOMPLETE));
    }

    /**
     * Méthode vérifiant qu'on a bien le même nombre de colonnes dans toutes les lignes du fichier
     */
    @Test
    void testNbColonnes() {
        composantFichier.setFilename("nokNbColonnes.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ERRLINE + "3 : " + Constant.ERR_FILE_WRONGNBCOLUMNS));
    }

    /**
     * Méthode vérifiant que le champ date est bien codé sur 4 chiffres
     */
    @Test
    void testDate() {
        composantFichier.setFilename("nokDate.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ERRLINE + "2 : " + Constant.ERR_FILE_DATENOK));
    }

    /**
     * Méthode vérifiant que le champ PPN est conforme
     */
    @Test
    void testPpn() {
        composantFichier.setFilename("nokPpn.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_ERRLINE + "2 : " + Constant.ERR_FILE_WRONGPPN));
    }

    /**
     * Méthode vérifiant le nombre de ligne maximum dans le fichier
     */
    @Test
    void testNbLignes() throws IOException, FileCheckingException{
        composantFichier.setFilename("nokNbLignes.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_TOOMUCH_EXEMP));
        composantFichier.setFilename("okNbLignes.csv");
        composantFichier.checkFileContent(demande);
    }

    /**
     * Méthode vérifiant que le controle sur l'index de recherche en fonction du type d'exemplarisation fonctionne bien
     */
    @Test
    void testIndexRecherche() {
        composantFichier.setFilename("nokIndexRecherche.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_INDEXINCONNU));
    }

    @Test
    void checkMandatoryZones() throws FileCheckingException, IOException {
        composantFichier.setFilename("nokMandatoryZones.csv");
        assertTrue(assertThrows(FileCheckingException.class, () -> composantFichier.checkFileContent(demande)).getMessage().contains(Constant.ERR_FILE_MANDATORY_ZONE_MISSING));
        composantFichier.setFilename("okMandatoryZones.csv");
        composantFichier.checkFileContent(demande);
    }

}
