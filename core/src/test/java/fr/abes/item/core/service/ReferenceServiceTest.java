package fr.abes.item.core.service;

import fr.abes.cbs.exception.CBSException;
import fr.abes.cbs.exception.ZoneException;
import fr.abes.item.core.entities.item.SousZonesAutorisees;
import fr.abes.item.core.entities.item.ZonesAutorisees;
import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.item.IEtatDemandeDao;
import fr.abes.item.core.repository.item.ITraitementDao;
import fr.abes.item.core.repository.item.ITypeExempDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = {ReferenceService.class})
class ReferenceServiceTest {
    @Autowired
    private ReferenceService referenceService;
    @MockBean
    private IEtatDemandeDao etatDemandeDao;
    @MockBean
    private ITypeExempDao typeExempDao;
    @MockBean
    private ITraitementDao traitementDao;
    @MockBean
    private IZonesAutoriseesDao iZonesAutoriseesDao;

    @Test
    void constructHeaderCsv() {
        // création de la zonesAutorisées1
        SousZonesAutorisees sousZonesAutorisees1 = new SousZonesAutorisees();
        sousZonesAutorisees1.setLibelle("$a");
        List<SousZonesAutorisees> sousZonesAutoriseesList1 = new ArrayList<>();
        sousZonesAutoriseesList1.add(sousZonesAutorisees1);
        ZonesAutorisees zonesAutorisees1 = new ZonesAutorisees();
        zonesAutorisees1.setLabelZone("917");
        zonesAutorisees1.setSousZonesAutorisees(sousZonesAutoriseesList1);

        // création de la zoneAutorisées2
        SousZonesAutorisees sousZonesAutorisees2 = new SousZonesAutorisees();
        sousZonesAutorisees2.setLibelle("$c");
        SousZonesAutorisees sousZonesAutorisees3 = new SousZonesAutorisees();
        sousZonesAutorisees3.setLibelle("$d");
        List<SousZonesAutorisees> sousZonesAutorisees2List = new ArrayList<>();
        sousZonesAutorisees2List.add(sousZonesAutorisees2);
        sousZonesAutorisees2List.add(sousZonesAutorisees3);
        ZonesAutorisees zonesAutorisees2 = new ZonesAutorisees();
        zonesAutorisees2.setLabelZone("930");
        zonesAutorisees2.setSousZonesAutorisees(sousZonesAutorisees2List);

        // création de la liste de test
        List<ZonesAutorisees> zonesAutoriseesList = new ArrayList<>();
        zonesAutoriseesList.add(zonesAutorisees1);
        zonesAutoriseesList.add(zonesAutorisees2);

        Mockito.when(iZonesAutoriseesDao.findAll()).thenReturn(zonesAutoriseesList);

        String test = referenceService.constructHeaderCsv();
        assertEquals("PPN;917$a;930$c;$d;\n",test);

    }
}
