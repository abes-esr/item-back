package fr.abes.item.core.entities;

import fr.abes.item.core.entities.item.DemandeModif;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Calendar;
import java.util.Date;

public class TestDemandeModif {
    Calendar cal;
    long dateTime;
    DemandeModif demandeModif;

    @BeforeEach
    public void setUp() {
        cal = Calendar.getInstance();
        dateTime = cal.getTimeInMillis();
        demandeModif = new DemandeModif();
    }

    @Test
    public void getDateCreation() {
        //Expected
        Date javaTypeDate = new Date(dateTime);
        System.out.println("Date at DateType format :" + javaTypeDate);

        //Actual
        Date sqlTypeDate = new java.sql.Date(dateTime);
        System.out.println("Date at SqlDateType format :" + sqlTypeDate);
        demandeModif.setDateCreation(sqlTypeDate);
        System.out.println("Date at DateType from SqlDateType format :" + demandeModif.getDateCreation());
    }
}