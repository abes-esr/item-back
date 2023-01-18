package fr.abes.item.service.impl;

import fr.abes.item.exception.QueryToSudocException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DemandeExempServiceTest {

    @Test
    void getQueryToSudoc() throws QueryToSudocException {
        DemandeExempService service = new DemandeExempService();
        String result = service.getQueryToSudoc("DAT", "Autres ressources", new String[]{"a", "Téoàrtü", "ÛioëÂÄ"});
        System.out.printf(result);
    }
}