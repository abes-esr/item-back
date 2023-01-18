package fr.abes.item.entities.baseXml;

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

    public UserProfile(Integer userNum, String library, String userGroup){
        this.userNum = userNum;
        this.library = library;
        this.userGroup = userGroup;
    }
}
