package fr.abes.item.service.impl;

import fr.abes.item.exception.QueryToSudocException;
import org.junit.jupiter.api.Test;

class DemandeRecouvServiceTest {
    @Test
    void getQueryToSudoc() throws QueryToSudocException {
        DemandeRecouvService service = new DemandeRecouvService();
        String result = service.getQueryToSudoc("DAT", new String[]{"a", "Téoàrtü", "ÛioëÂÄ"});
        System.out.printf(result);
    }
}