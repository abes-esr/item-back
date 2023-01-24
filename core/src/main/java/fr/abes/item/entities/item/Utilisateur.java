package fr.abes.item.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.entities.GenericEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name = "UTILISATEUR")
@JsonIgnoreProperties({"demandeModifs", "journalDemandeModifs"})
@Getter @Setter
@NoArgsConstructor
public class Utilisateur implements Serializable, GenericEntity<Integer> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "NUM_USER")
    private Integer numUser;
    @Column(name = "EMAIL")
    private String email;
    @OneToMany(mappedBy = "utilisateur", fetch = FetchType.LAZY)
    private Set<DemandeModif> demandeModifs;
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY)
    private Set<JournalDemandeModif> journalDemandeModifs;
    @Transient
    private String iln;

    public Utilisateur(Integer numUser, String email) {
        this.numUser = numUser;
        this.email = email;
    }

    public Utilisateur(Integer numUser) {
        this.numUser = numUser;
    }

    public Utilisateur(Integer numUser, String email, String iln) {
        this.numUser = numUser;
        this.email = email;
        this.iln = iln;
    }

    public Integer getId() {
        return numUser;
    }
}
