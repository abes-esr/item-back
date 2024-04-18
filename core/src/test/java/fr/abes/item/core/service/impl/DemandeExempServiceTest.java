package fr.abes.item.core.service.impl;

import fr.abes.item.core.exception.QueryToSudocException;
import fr.abes.item.core.repository.baseXml.ILibProfileDao;
import fr.abes.item.core.repository.item.IDemandeExempDao;
import fr.abes.item.core.repository.item.ILigneFichierExempDao;
import fr.abes.item.core.repository.item.IZonesAutoriseesDao;
import fr.abes.item.core.service.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {DemandeExempService.class})
class DemandeExempServiceTest {
    @MockBean
    IDemandeExempDao demandeExempDao;
    @MockBean
    FileSystemStorageService storageService;
    @MockBean
    ILigneFichierService ligneFichierExempService;
    @MockBean
    ReferenceService referenceService;
    @MockBean
    JournalService journalService;
    @MockBean
    TraitementService traitementService;
    @MockBean
    IZonesAutoriseesDao zonesAutoriseesDao;
    @MockBean
    ILigneFichierExempDao ligneFichierExempDao;
    @MockBean
    ILibProfileDao libProfileDao;
    @Autowired
    private DemandeExempService service;

    @Test
    void getQueryToSudoc() throws QueryToSudocException {
        String result = service.getQueryToSudoc("DAT", "Autres ressources", new String[]{"a", "Téoàrtü", "ÛioëÂÄ"});
        System.out.printf(result);
    }
}