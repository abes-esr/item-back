package fr.abes.item.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.constant.Constant;
import fr.abes.item.repository.baseXml.ILibProfileDao;
import fr.abes.item.repository.item.IDemandeExempDao;
import fr.abes.item.repository.item.ILigneFichierExempDao;
import fr.abes.item.repository.item.IZonesAutoriseesDao;
import fr.abes.item.service.impl.DemandeExempService;
import fr.abes.item.service.impl.LigneFichierExempService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.text.SimpleDateFormat;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@DisplayName("Test couche service / demande d'exemplarisation")
@SpringBootTest(classes = DemandeExempService.class)
public class TestDemandeExempService {
    @MockBean
    IDemandeExempDao demandeExempDao;
    @MockBean
    FileSystemStorageService storageService;
    @MockBean
    LigneFichierExempService ligneFichierExempService;
    @MockBean
    ReferenceService referenceService;
    @MockBean
    JournalService journalService;
    @MockBean
    TraitementService traitementService;
    @MockBean
    IZonesAutoriseesDao dao;
    @MockBean
    ILigneFichierExempDao ligneFichierExempDao;
    @MockBean
    ILibProfileDao libProfileDao;

    @Autowired
    private DemandeExempService demandeExempService;

    private final String RCR = "341725201";
    private final String NUMEXEMP = "01";

    SimpleDateFormat formater;

    Date aujourdhui;

    @BeforeEach
    void init() {
        when(dao.getIndicateursByTypeExempAndLabelZone(Mockito.anyString())).thenReturn("##");
        formater = new SimpleDateFormat("dd-MM-yyyy HH:mm");
        aujourdhui = new Date();
    }

    @Test
    void getExempWithOneValue() throws ZoneException, CBSException {
        String header = "930 $c";
        String valeur = "test";

        assertThat(demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP)).contains(
                "e01 $bx\r" +
                "930 ##$b341725201$ctest\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r");
    }

    @Test
    void getExempWithOneZoneAndTwoSousZones() throws ZoneException, CBSException {
        String header = "930 $d;$c";
        String valeur = "testd;testc";
        assertThat(demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP)).contains(
                "e01 $bx\r" +
                "930 ##$b341725201$ctestc$dtestd\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r");
    }

    @Test
    void getExempWithTwoZonesAndOneSousZone() throws ZoneException, CBSException {
        String header = "930 $d;915 $a";
        String valeur = "testd;testa";
        assertThat(demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP)).contains(
                "e01 $bx\r" +
                "915 ##$atesta\r" +
                "930 ##$b341725201$dtestd\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r");
    }

    @Test
    void getExempWithTwoZonesAndTwoSousZone() throws ZoneException, CBSException {
        String header = "930 $d;$c;915 $a;$b";
        String valeur = "testd;testc;test2;test3";
        assertThat(demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP)).contains(
                "e01 $bx\r" +
                "915 ##$atest2$btest3\r" +
                "930 ##$b341725201$ctestc$dtestd\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r");
    }

    @Test
    void getExempWithThreeZonesAndTwoSousZone() throws ZoneException, CBSException {
        String header = "E317 $a;E856 $u;$9;930 $j";
        String valeur = "DeGruyter LN;http://buadistant;2018-145;g;";
        String notice = demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP);
        assertThat(notice).contains(
                "e01 $bx\r" +
                "930 ##$b341725201$jg\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r" +
                "E317 ##$aDeGruyter LN\r" +
                "E856 ##$uhttp://buadistant$92018-145\r");
    }

    @Test
    void getExempWithALotOfEverything() throws ZoneException, CBSException {
        String header = "930 $d;$c;915 $a;$b";
        String valeur = "testd;testc;test2;test3";
        String notice = demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP);
        assertThat(notice).isEqualTo(
                "e01 $bx\r" +
                "915 ##$atest2$btest3\r" +
                "930 ##$b341725201$ctestc$dtestd\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r");
    }

    @Test
    void getExempWithALotOfEverything2() throws ZoneException, CBSException {
        String header = "E856$u;$l;$z;930$a;$j;955 41$a;$k;$4;991$a;E702$3";
        String valeur = "https://federation.unimes.fr:8443/login?url=https://rd.springer.com/journal/766;UN | DIP;[Springer Journal Archives - Licence Nationale - Accès UNîmes] (1996)-(2014);Springer Journal Archives;g;1996;2014;Springer Journal Archives;Springer-revues-UN;test";
        String notice = demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP);
        assertThat(notice).isEqualTo(
                "e01 $bx\r" +
                "930 ##$b341725201$aSpringer Journal Archives$jg\r" +
                "955 ##$a1996$k2014$4Springer Journal Archives\r" +
                "991 ##$aSpringer-revues-UN\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r" +
                "E702 ##$3test\r" +
                "E856 ##$lUN | DIP$uhttps://federation.unimes.fr:8443/login?url=https://rd.springer.com/journal/766$z[Springer Journal Archives - Licence Nationale - Accès UNîmes] (1996)-(2014)\r");
    }

    @Test
    void getExempWithMaxZonesAndSouszones() throws ZoneException, CBSException {
        // GIVEN
        String header = "915 $a;$b;917 $a;930 $c;$d;$e;$a;$i;$v;$2;991 $a;999 $a;$b;$c;$i;$o;$s;$z;E316 $a;E317 $a;E319 $a;$b;$c;$d;$x";
        String valeur = "915a;915b;917a;930c;930d;930e;930a;930i;930v;9302;991a;999a;999b;999c;999i;999o;999s;999z;E316a;E317a;E319a;E319b;E319c;E319d;E319x";

        // WHEN
        String notice = demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP);
        // THEN
        assertThat(notice).isEqualTo(
                "e01 $bx\r" +
                "915 ##$a915a$b915b\r" +
                "917 ##$a917a\r" +
                "930 ##$b341725201$c930c$d930d$e930e$a930a$i930i$v930v$29302\r" +
                "991 ##$a991a\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r" +
                "999 ##$a999a$b999b$c999c$i999i$o999o$s999s$z999z\r" +
                "E316 ##$aE316a\r" +
                "E317 ##$aE317a\r" +
                "E319 ##$aE319a$bE319b$cE319c$dE319d$xE319x\r");
    }

    @Test
    void getExempWithSomeEmptyTag() throws ZoneException, CBSException {
        String header = "915 $a;930 $c;$d;$a;$j";
        String valeur = ";DROIT;SDR;M31733;a";

        String notice = demandeExempService.creerExemplaireFromHeaderEtValeur(header, valeur, RCR, NUMEXEMP);
        assertThat(notice).isEqualTo(
                "e01 $bx\r" +
                "930 ##$b341725201$cDROIT$dSDR$aM31733$ja\r" +
                "991 ##$a" + Constant.TEXTE_991_CREA + " le " + formater.format(aujourdhui) + "\r");

    }


}
