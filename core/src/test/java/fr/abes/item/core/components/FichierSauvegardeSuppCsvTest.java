package fr.abes.item.core.components;

import com.opencsv.CSVWriter;
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

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(classes = {FichierSauvegardeSuppCsv.class})
class FichierSauvegardeSuppCsvTest {

    @Autowired
    private FichierSauvegardeSuppCsv fichierSauvegardeSuppCsv;

    @MockBean
    private ReferenceService referenceService;

    CSVWriter csvWriter;

    @Test
    void gererZones() throws ZoneException {
        List<String> listZoneSousZone = List.of("917$a;930$a;$c;$d;$e;$i;$j;$v;$2;$l;$k;991$a;915$a;$b;$f;955$a;$k;$4;920$a;$b;$c".split(";"));

        String resultat = "";

        Exemplaire exemplaire1 = new Exemplaire();
        exemplaire1.addZone("930", "$c", "test 930$c");
        exemplaire1.addZone("930", "$d", "test 930$d");
        exemplaire1.addZone("991", "$a", "test 930$a");

        Zone zone1 = new Zone("917", TYPE_NOTICE.EXEMPLAIRE);

        assertEquals(listZoneSousZone, fichierSauvegardeSuppCsv.gererZones(listZoneSousZone, exemplaire1, resultat, null));

//        Exemplaire exemplaire2 = new Exemplaire();
//        exemplaire2.addZone("917", "$a", "test 917$a");
//        exemplaire2.addZone("991", "$a", "test 930$a");
//        exemplaire2.addZone("915", "$a", "test 915$a");
//        exemplaire2.addZone("915", "$c", "test 915$c");
//
//        assertEquals(, exemplaire2.getListeZones());
    }
}
