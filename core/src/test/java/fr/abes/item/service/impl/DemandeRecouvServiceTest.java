package fr.abes.item.service.impl;

import fr.abes.item.exception.QueryToSudocException;
import fr.abes.item.repository.baseXml.ILibProfileDao;
import fr.abes.item.repository.item.IDemandeRecouvDao;
import fr.abes.item.repository.item.ILigneFichierRecouvDao;
import fr.abes.item.service.FileSystemStorageService;
import fr.abes.item.service.ILigneFichierService;
import fr.abes.item.service.ReferenceService;
import fr.abes.item.service.TraitementService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest(classes = {DemandeRecouvService.class})
class DemandeRecouvServiceTest {
    @Autowired
    DemandeRecouvService service;
    @MockBean
    ILigneFichierRecouvDao ligneFichierRecouvDao;
    @MockBean
    IDemandeRecouvDao demandeRecouvDao;
    @MockBean
    FileSystemStorageService storageService;
    @MockBean
    ReferenceService referenceService;
    @MockBean
    TraitementService traitementService;
    @MockBean
    ILigneFichierService ligneFichierRecouvService;
    @MockBean
    ILibProfileDao libProfileDao;
    @Test
    void getQueryToSudoc() throws QueryToSudocException {
        String result = service.getQueryToSudoc("DAT", new String[]{"a", "Téoàrtü", "ÛioëÂÄ"});
        System.out.printf(result);
    }
}