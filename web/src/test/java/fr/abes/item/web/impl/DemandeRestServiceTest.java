package fr.abes.item.web.impl;

import com.google.common.collect.Lists;
import fr.abes.item.core.entities.item.Demande;
import fr.abes.item.core.entities.item.DemandeExemp;
import fr.abes.item.core.entities.item.DemandeModif;
import fr.abes.item.core.entities.item.DemandeRecouv;
import fr.abes.item.core.service.JournalService;
import fr.abes.item.core.service.ReferenceService;
import fr.abes.item.core.service.TraitementService;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.core.service.impl.*;
import fr.abes.item.core.utilitaire.UtilsMapper;
import fr.abes.item.exception.RestResponseEntityExceptionHandler;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.web.DemandeRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {DemandeRestService.class, UtilsMapper.class})
@ExtendWith({SpringExtension.class})
class DemandeRestServiceTest {
    @Autowired
    WebApplicationContext context;

    @InjectMocks
    DemandeRestService controller;

    @MockBean
    DemandeExempService demandeExempService;
    @MockBean
    DemandeRecouvService demandeRecouvService;
    @MockBean
    DemandeModifService demandeModifService;
    @MockBean
    LigneFichierExempService ligneFichierExempService;
    @MockBean
    LigneFichierModifService ligneFichierModifService;
    @MockBean
    LigneFichierRecouvService ligneFichierRecouvService;
    @MockBean
    ReferenceService referenceService;
    @MockBean
    JournalService journalService;
    @MockBean
    UtilisateurService utilisateurService;
    @MockBean
    TraitementService traitementService;
    @MockBean
    CheckAccessToServices checkAccessToServices;
    @Autowired
    UtilsMapper mapper;

    List<Demande> demandeExemps = new ArrayList<>();
    List<Demande> demandeModifs = new ArrayList<>();
    List<Demande> demandeRecouvs = new ArrayList<>();
    MockMvc mockMvc;
    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(context.getBean(DemandeRestService.class)).setControllerAdvice(new RestResponseEntityExceptionHandler()).build();
        DemandeExemp demande1 = new DemandeExemp(1);
        demande1.setRcr("111111111");
        DemandeExemp demande2 = new DemandeExemp(2);
        demande2.setRcr("222222222");
        demandeExemps.addAll(Lists.newArrayList(demande1, demande2));

        DemandeModif demande3 = new DemandeModif(3);
        demande3.setRcr("3333333333");
        DemandeModif demande4 = new DemandeModif(4);
        demande4.setRcr("4444444444");
        demandeModifs.addAll(Lists.newArrayList(demande3, demande4));

        DemandeRecouv demande5 = new DemandeRecouv(5);
        demande5.setRcr("555555555");
        DemandeRecouv demande6 = new DemandeRecouv(6);
        demande6.setRcr("666666666");
        demandeRecouvs.addAll(Lists.newArrayList(demande5, demande6));
    }
    @Test
    @WithMockUser(authorities = {"ADMIN"})
    void testGetAllActiveDemandes() throws Exception {
        Mockito.when(demandeExempService.getAllActiveDemandesForAdminExtended()).thenReturn(this.demandeExemps);
        this.mockMvc.perform(get("/api/v1/demandes?type=EXEMP&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));

        Mockito.when(demandeModifService.getAllActiveDemandesForAdminExtended()).thenReturn(this.demandeModifs);
        this.mockMvc.perform(get("/api/v1/demandes?type=MODIF&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].rcr").value("3333333333"))
                .andExpect(jsonPath("$[1].id").value("4"))
                .andExpect(jsonPath("$[1].rcr").value("4444444444"));

        Mockito.when(demandeRecouvService.getAllActiveDemandesForAdminExtended()).thenReturn(this.demandeRecouvs);
        this.mockMvc.perform(get("/api/v1/demandes?type=RECOUV&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"))
                .andExpect(jsonPath("$[0].rcr").value("555555555"))
                .andExpect(jsonPath("$[1].id").value("6"))
                .andExpect(jsonPath("$[1].rcr").value("666666666"));

        Mockito.when(demandeExempService.getAllActiveDemandesForAdmin("1")).thenReturn(this.demandeExemps);
        this.mockMvc.perform(get("/api/v1/demandes?type=EXEMP&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));

        Mockito.when(demandeModifService.getAllActiveDemandesForAdmin("1")).thenReturn(this.demandeModifs);
        this.mockMvc.perform(get("/api/v1/demandes?type=MODIF&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].rcr").value("3333333333"))
                .andExpect(jsonPath("$[1].id").value("4"))
                .andExpect(jsonPath("$[1].rcr").value("4444444444"));

        Mockito.when(demandeRecouvService.getAllActiveDemandesForAdmin("1")).thenReturn(this.demandeRecouvs);
        this.mockMvc.perform(get("/api/v1/demandes?type=RECOUV&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"))
                .andExpect(jsonPath("$[0].rcr").value("555555555"))
                .andExpect(jsonPath("$[1].id").value("6"))
                .andExpect(jsonPath("$[1].rcr").value("666666666"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testChercher() throws Exception {
        Mockito.when(demandeExempService.getActiveDemandesForUser("1")).thenReturn(this.demandeExemps);
        this.mockMvc.perform(get("/api/v1/chercherDemandes?type=EXEMP&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));

        Mockito.when(demandeModifService.getActiveDemandesForUser("1")).thenReturn(this.demandeModifs);
        this.mockMvc.perform(get("/api/v1/chercherDemandes?type=MODIF&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].rcr").value("3333333333"))
                .andExpect(jsonPath("$[1].id").value("4"))
                .andExpect(jsonPath("$[1].rcr").value("4444444444"));

        Mockito.when(demandeRecouvService.getActiveDemandesForUser("1")).thenReturn(this.demandeRecouvs);
        this.mockMvc.perform(get("/api/v1/chercherDemandes?type=RECOUV&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"))
                .andExpect(jsonPath("$[0].rcr").value("555555555"))
                .andExpect(jsonPath("$[1].id").value("6"))
                .andExpect(jsonPath("$[1].rcr").value("666666666"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetAllArchivedDemandes() throws Exception {
        Mockito.when(demandeExempService.getAllArchivedDemandesAllIln()).thenReturn(this.demandeExemps);
        this.mockMvc.perform(get("/api/v1/chercherArchives?type=EXEMP&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));

        Mockito.when(demandeModifService.getAllArchivedDemandesAllIln()).thenReturn(this.demandeModifs);
        this.mockMvc.perform(get("/api/v1/chercherArchives?type=MODIF&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].rcr").value("3333333333"))
                .andExpect(jsonPath("$[1].id").value("4"))
                .andExpect(jsonPath("$[1].rcr").value("4444444444"));

        Mockito.when(demandeRecouvService.getAllArchivedDemandesAllIln()).thenReturn(this.demandeRecouvs);
        this.mockMvc.perform(get("/api/v1/chercherArchives?type=RECOUV&extension=true").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"))
                .andExpect(jsonPath("$[0].rcr").value("555555555"))
                .andExpect(jsonPath("$[1].id").value("6"))
                .andExpect(jsonPath("$[1].rcr").value("666666666"));

        Mockito.when(demandeExempService.getAllArchivedDemandes("1")).thenReturn(this.demandeExemps);
        this.mockMvc.perform(get("/api/v1/chercherArchives?type=EXEMP&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("1"))
                .andExpect(jsonPath("$[0].rcr").value("111111111"))
                .andExpect(jsonPath("$[1].id").value("2"))
                .andExpect(jsonPath("$[1].rcr").value("222222222"));

        Mockito.when(demandeModifService.getAllArchivedDemandes("1")).thenReturn(this.demandeModifs);
        this.mockMvc.perform(get("/api/v1/chercherArchives?type=MODIF&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("3"))
                .andExpect(jsonPath("$[0].rcr").value("3333333333"))
                .andExpect(jsonPath("$[1].id").value("4"))
                .andExpect(jsonPath("$[1].rcr").value("4444444444"));

        Mockito.when(demandeRecouvService.getAllArchivedDemandes("1")).thenReturn(this.demandeRecouvs);
        this.mockMvc.perform(get("/api/v1/chercherArchives?type=RECOUV&extension=false").requestAttr("iln", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("5"))
                .andExpect(jsonPath("$[0].rcr").value("555555555"))
                .andExpect(jsonPath("$[1].id").value("6"))
                .andExpect(jsonPath("$[1].rcr").value("666666666"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetDemande1() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(1, "1");

        Mockito.when(demandeExempService.findById(1)).thenReturn((DemandeExemp) this.demandeExemps.get(0));
        this.mockMvc.perform(get("/api/v1/demandes/1?type=EXEMP").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.rcr").value("111111111"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetDemande2() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(3, "1");

        Mockito.when(demandeModifService.findById(3)).thenReturn((DemandeModif) this.demandeModifs.get(0));
        this.mockMvc.perform(get("/api/v1/demandes/3?type=MODIF").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("3"))
                .andExpect(jsonPath("$.rcr").value("333333333"));
    }

    @Test
    @WithMockUser(authorities = {"USER"})
    void testGetDemande3() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserAccesDemandeParIln(5, "1");

        Mockito.when(demandeRecouvService.findById(5)).thenReturn((DemandeRecouv) this.demandeRecouvs.get(0));
        this.mockMvc.perform(get("/api/v1/demandes/5?type=RECOUV").requestAttr("userNum", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("5"))
                .andExpect(jsonPath("$.rcr").value("555555555"));
    }
}