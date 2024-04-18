package fr.abes.item.core.entities.baseXml;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Le schéma est obligatoire ici
 * car les dao de la base Item doivent pouvoir accéder via les requêtes aux tables de la base XML
 */
@Entity
@Table(name="USER_PROFILE", schema = "AUTORITES")
@Getter
@Setter
@NoArgsConstructor
public class UserProfile {
    @Id
    @Column(name = "USER_NUM")
    private Integer userNum;

    @Column(name = "LIBRARY")
    private String library;

    @Column(name = "USER_GROUP")
    private String userGroup;

}
