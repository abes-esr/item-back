package fr.abes.item.web.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import fr.abes.item.core.entities.item.Utilisateur;
import fr.abes.item.core.exception.ForbiddenException;
import fr.abes.item.core.service.UtilisateurService;
import fr.abes.item.dto.DtoBuilder;
import fr.abes.item.exception.RestResponseEntityExceptionHandler;
import fr.abes.item.security.CheckAccessToServices;
import fr.abes.item.web.UtilisateurRestService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = {UtilisateurRestService.class, DtoBuilder.class, ObjectMapper.class})
public class UtilisateurRestServiceTest {
    @Autowired
    WebApplicationContext context;
    @InjectMocks
    UtilisateurRestService controller;
    @MockBean
    UtilisateurService service;
    @MockBean
    CheckAccessToServices checkAccessToServices;
    @Autowired
    DtoBuilder builder;
    @Autowired
    ObjectMapper mapper;
    MockMvc mockMvc;

    @BeforeEach
    void init() {
        this.mockMvc = MockMvcBuilders.standaloneSetup(context.getBean(UtilisateurRestService.class)).setControllerAdvice(new RestResponseEntityExceptionHandler()).build();
    }

    @Test
    void testSaveUtilisateur() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserMajUtilisateurParUserNum(1, "1");
        Utilisateur utilisateur = new Utilisateur(1, "", "1");
        Mockito.when(service.findById(1)).thenReturn(utilisateur);
        Utilisateur utilisateurSaved = new Utilisateur(1, "test@test.com", "1");
        Mockito.when(service.save(Mockito.any())).thenReturn(utilisateurSaved);

        this.mockMvc.perform(post("/api/v1/utilisateurs/1").content("test@test.com").requestAttr("userNum", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.userId").value("1"))
                .andExpect(jsonPath("$.email").value("test@test.com"));
    }

    @Test
    void testSaveUtilisateurForbidden() throws Exception {
        Mockito.doThrow(ForbiddenException.class).when(checkAccessToServices).autoriserMajUtilisateurParUserNum(1, "1");
        this.mockMvc.perform(post("/api/v1/utilisateurs/1").content("test@test.com").requestAttr("userNum", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isForbidden());
    }

    @Test
    void testSaveUtilisateurNoUser() throws Exception {
        Mockito.doNothing().when(checkAccessToServices).autoriserMajUtilisateurParUserNum(1, "1");
        Mockito.when(service.findById(1)).thenReturn(null);
        this.mockMvc.perform(post("/api/v1/utilisateurs/1").content("test@test.com").requestAttr("userNum", "1")
                        .contentType(MediaType.APPLICATION_JSON_VALUE).characterEncoding(StandardCharsets.UTF_8))
                .andExpect(status().isBadRequest());
    }
}
