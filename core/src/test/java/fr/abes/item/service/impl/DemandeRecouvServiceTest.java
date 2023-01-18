package fr.abes.item.service.impl;

import fr.abes.item.exception.QueryToSudocException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DemandeRecouvServiceTest {
    @Test
    void replacementOfDiacriticalAccents() {
        String var = "é, è, ê, ë, à, â, ä, î, ï, ô, ö, ù, û, ü, ÿ, æ, œ, ç, ñ";
        DemandeRecouvService service = new DemandeRecouvService();
        String var2 = service.replacementOfDiacriticalAccents(var);
        Assertions.assertEquals("e, e, e, e, a, a, a, i, i, o, o, u, u, u, y, ae, oe, c, n", var2);
    }

    @Test
    void getQueryToSudoc() throws QueryToSudocException {
        DemandeRecouvService service = new DemandeRecouvService();
        String result = service.getQueryToSudoc("DAT", new String[]{"a", "Téoàrtü", "ÛioëÂÄ"});
        System.out.printf(result);
    }
}