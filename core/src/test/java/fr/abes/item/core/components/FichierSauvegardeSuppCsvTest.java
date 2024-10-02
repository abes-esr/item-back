package fr.abes.item.core.components;

import com.google.common.collect.Lists;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.cbs.notices.Exemplaire;
import fr.abes.cbs.notices.TYPE_NOTICE;
import fr.abes.cbs.notices.Zone;
import fr.abes.item.core.service.ReferenceService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest(classes = {FichierSauvegardeSuppCsv.class})
class FichierSauvegardeSuppCsvTest {

    @Autowired
    private FichierSauvegardeSuppCsv fichierSauvegardeSuppCsv;

    @MockBean
    private ReferenceService referenceService;

    @Test
    void gererZonesAvec1Exemplaire() throws ZoneException {
        List<String> listZoneSousZone = List.of("930$c;$d;991$a".split(";"));

        Exemplaire exemplaire = new Exemplaire();
        Zone zone930 = new Zone("930", TYPE_NOTICE.EXEMPLAIRE);
        zone930.addSubLabel("$c", "test 930$c");
        zone930.addSubLabel("$d", "test 930$d");

        exemplaire.addZone(zone930);
        exemplaire.addZone("991", "$a", "test 991$a");

        String result = fichierSauvegardeSuppCsv.gererZones(listZoneSousZone, exemplaire);

        assertEquals("test 930$c;test 930$d;test 991$a", result);
    }

    @Test
    void gererZonesAvec1ExemplaireEtListeLongue() throws ZoneException {
        List<String> listZoneSousZone = List.of("915$a;917$a;930$a;$c;$d;$i;$j;991$a".split(";"));

        Exemplaire exemplaire = new Exemplaire();
        Zone zone930 = new Zone("930", TYPE_NOTICE.EXEMPLAIRE);
        zone930.addSubLabel("$c", "test 930$c");
        zone930.addSubLabel("$d", "test 930$d");

        exemplaire.addZone(zone930);
        exemplaire.addZone("991", "$a", "test 991$a");

        String result = fichierSauvegardeSuppCsv.gererZones(listZoneSousZone, exemplaire);

        assertEquals(";;;test 930$c;test 930$d;;;test 991$a", result);
    }

    @Test
    void gererZonesAvec1ExemplaireLong() throws ZoneException {
        List<String> listZoneSousZone = List.of("930$c;$i;$d".split(";"));

        Exemplaire exemplaire = new Exemplaire();
        Zone zone930 = new Zone("930", TYPE_NOTICE.EXEMPLAIRE);
        zone930.addSubLabel("$c", "test 930$c");
        zone930.addSubLabel("$d", "test 930$d");

        exemplaire.addZone(zone930);
        exemplaire.addZone("991", "$a", "test 991$a");

        String result = fichierSauvegardeSuppCsv.gererZones(listZoneSousZone, exemplaire);

        assertEquals("test 930$c;;test 930$d", result);
    }

    @Test
    void gereZonesAvecListeVide() throws ZoneException {
        List<String> listZoneSousZone = Lists.newArrayList();

        Exemplaire exemplaire = new Exemplaire();
        Zone zone930 = new Zone("930", TYPE_NOTICE.EXEMPLAIRE);
        zone930.addSubLabel("$c", "test 930$c");
        zone930.addSubLabel("$d", "test 930$d");

        exemplaire.addZone(zone930);
        exemplaire.addZone("991", "$a", "test 991$a");

        String result = fichierSauvegardeSuppCsv.gererZones(listZoneSousZone, exemplaire);

        assertNull(result);
    }
}
