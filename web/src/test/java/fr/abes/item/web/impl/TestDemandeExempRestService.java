package fr.abes.item.web.impl;

import fr.abes.item.dao.item.IDemandeExempDao;
import fr.abes.item.entities.item.*;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import java.util.Date;

class TestDemandeExempRestService {
    @Mock private IDemandeExempDao myDao;

    private DemandeExemp myEntity = new DemandeExemp();

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