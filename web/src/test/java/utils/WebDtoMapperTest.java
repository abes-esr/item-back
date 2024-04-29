package utils;

import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.utilitaire.UtilsMapper;
import fr.abes.item.dto.DemandeExempWebDto;
import fr.abes.item.dto.DemandeModifWebDto;
import fr.abes.item.dto.DemandeRecouvWebDto;
import fr.abes.item.utils.WebDtoMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import java.util.Calendar;

@ExtendWith({SpringExtension.class})
@SpringBootTest(classes = {UtilsMapper.class, WebDtoMapper.class})
public class WebDtoMapperTest {
    @Autowired
    UtilsMapper mapper;

    @Test
    void testMappingDemandeExemp() {
        Calendar cal = Calendar.getInstance();
        DemandeExemp demande = new DemandeExemp(10);
        demande.setRcr("341725201");
        demande.setShortname("test Demande");
        cal.set(2024, Calendar.APRIL, 24, 11, 50);
        demande.setDateCreation(cal.getTime());
        cal.set(2024, Calendar.MAY, 31, 19, 22);
        demande.setDateModification(cal.getTime());
        Utilisateur utilisateur = new Utilisateur(1, "test@test.com", "15");
        demande.setUtilisateur(utilisateur);
        demande.setIln("18");
        EtatDemande etat = new EtatDemande(1, "A compléter");
        demande.setEtatDemande(etat);
        demande.setCommentaire("commentaire test");
        demande.setPourcentageProgressionTraitement(23);
        TypeExemp typeExemp = new TypeExemp(1, "Monographies");
        demande.setTypeExemp(typeExemp);
        demande.setListeZones("200$a;210$c");
        IndexRecherche indexRecherche = new IndexRecherche(1, "PPN");
        demande.setIndexRecherche(indexRecherche);


        DemandeExempWebDto webDto = mapper.map(demande, DemandeExempWebDto.class);

        Assertions.assertEquals(10, webDto.getId());
        Assertions.assertEquals("341725201", webDto.getRcr());
        Assertions.assertEquals("18", webDto.getIln());
        Assertions.assertEquals("A compléter", webDto.getEtatDemande());
        Assertions.assertEquals("commentaire test", webDto.getCommentaire());
        Assertions.assertEquals(23, webDto.getPourcentageProgressionTraitement());
        Assertions.assertEquals("24/04/2024 11:50", webDto.getDateCreation());
        Assertions.assertEquals("31/05/2024 19:22", webDto.getDateModification());
        Assertions.assertEquals("Monographies", webDto.getTypeExemp());
        Assertions.assertEquals("PPN", webDto.getIndexRecherche());
    }

    @Test
    void testMappingDemandeModif() {
        Calendar cal = Calendar.getInstance();
        DemandeModif demande = new DemandeModif(10);
        demande.setRcr("341725201");
        demande.setShortname("test Demande");
        cal.set(2024, Calendar.APRIL, 24, 11, 50);
        demande.setDateCreation(cal.getTime());
        cal.set(2024, Calendar.MAY, 31, 19, 22);
        demande.setDateModification(cal.getTime());
        Utilisateur utilisateur = new Utilisateur(1, "test@test.com", "15");
        demande.setUtilisateur(utilisateur);
        demande.setIln("18");
        EtatDemande etat = new EtatDemande(1, "A compléter");
        demande.setEtatDemande(etat);
        demande.setCommentaire("commentaire test");
        demande.setPourcentageProgressionTraitement(23);
        demande.setZone("930");
        demande.setSousZone("$b");
        Traitement traitement = new Traitement(1, "Créer une nouvelle zone", "test");
        demande.setTraitement(traitement);


        DemandeModifWebDto webDto = mapper.map(demande, DemandeModifWebDto.class);

        Assertions.assertEquals(10, webDto.getId());
        Assertions.assertEquals("341725201", webDto.getRcr());
        Assertions.assertEquals("18", webDto.getIln());
        Assertions.assertEquals("A compléter", webDto.getEtatDemande());
        Assertions.assertEquals("commentaire test", webDto.getCommentaire());
        Assertions.assertEquals(23, webDto.getPourcentageProgressionTraitement());
        Assertions.assertEquals("24/04/2024 11:50", webDto.getDateCreation());
        Assertions.assertEquals("31/05/2024 19:22", webDto.getDateModification());
        Assertions.assertEquals("930 $b", webDto.getZoneEtSousZone());
        Assertions.assertEquals("Créer une nouvelle zone", webDto.getTraitement());
    }

    @Test
    void testMappingDemandeRecouv() {
        Calendar cal = Calendar.getInstance();
        DemandeRecouv demande = new DemandeRecouv(10);
        demande.setRcr("341725201");
        demande.setShortname("test Demande");
        cal.set(2024, Calendar.APRIL, 24, 11, 50);
        demande.setDateCreation(cal.getTime());
        cal.set(2024, Calendar.MAY, 31, 19, 22);
        demande.setDateModification(cal.getTime());
        Utilisateur utilisateur = new Utilisateur(1, "test@test.com", "15");
        demande.setUtilisateur(utilisateur);
        demande.setIln("18");
        EtatDemande etat = new EtatDemande(1, "A compléter");
        demande.setEtatDemande(etat);
        demande.setCommentaire("commentaire test");
        demande.setPourcentageProgressionTraitement(23);
        IndexRecherche indexRecherche = new IndexRecherche(1, "PPN");
        demande.setIndexRecherche(indexRecherche);


        DemandeRecouvWebDto webDto = mapper.map(demande, DemandeRecouvWebDto.class);

        Assertions.assertEquals(10, webDto.getId());
        Assertions.assertEquals("341725201", webDto.getRcr());
        Assertions.assertEquals("18", webDto.getIln());
        Assertions.assertEquals("A compléter", webDto.getEtatDemande());
        Assertions.assertEquals("commentaire test", webDto.getCommentaire());
        Assertions.assertEquals(23, webDto.getPourcentageProgressionTraitement());
        Assertions.assertEquals("24/04/2024 11:50", webDto.getDateCreation());
        Assertions.assertEquals("31/05/2024 19:22", webDto.getDateModification());
        Assertions.assertEquals("PPN", webDto.getIndexRecherche());
    }
}
