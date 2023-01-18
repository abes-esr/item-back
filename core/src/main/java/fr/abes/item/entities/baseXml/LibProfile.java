package fr.abes.item.entities.baseXml;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Le schéma est obligatoire ici
 * car les dao de la base Item doivent pouvoir accéder via les requêtes aux tables de la base XML
 */
@Entity
@Table(name="LIB_PROFILE", schema = "AUTORITES")
@Getter @Setter
@NoArgsConstructor
public class LibProfile {
    @Id
    @Column(name="RCR")
    private String rcr;

    @Column(name="SHORT_NAME")
    private String shortName;

    @Column(name = "ILN")
    private String iln;

    public LibProfile(String rcr, String shortName, String iln) {
        this.rcr = rcr;
        this.shortName = shortName;
        this.iln = iln;
    }
}
