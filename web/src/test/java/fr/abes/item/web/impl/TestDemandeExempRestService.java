package fr.abes.item.web.impl;

import fr.abes.item.core.entities.item.*;
import fr.abes.item.core.repository.item.IDemandeExempDao;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class TestDemandeExempRestService {
    @Mock private IDemandeExempDao myDao;

    private final DemandeExemp myEntity = new DemandeExemp();

    @Test
    void givenDemandeExamp_WhenDatabaseInsertion_ThenValidDemandeExampInBase(){
        MockitoAnnotations.initMocks(this);
        Mockito.when(myDao.save(myEntity)).thenReturn(createTestEntity());

        DemandeExemp actual = myDao.save(myEntity);

        System.out.println(actual.getRcr());

        assertThat(actual.getRcr()).isEqualTo("341725201");
    }

    private DemandeExemp createTestEntity() {
        DemandeExemp myEntityResult = new DemandeExemp("341725201", new Date(), new Date(), new EtatDemande(1), "", new Utilisateur(1), new TypeExemp(1), new IndexRecherche(1));
        myEntityResult.setDateModification(new Date());

        return myEntityResult;
    }

}