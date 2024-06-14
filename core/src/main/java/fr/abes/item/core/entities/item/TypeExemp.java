package fr.abes.item.core.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.core.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name="TYPE_EXEMP")
@JsonIgnoreProperties({"demandes", "indexRechercheSet"})
public class TypeExemp implements Serializable, GenericEntity<Integer> {
    @Id
    @Column(name = "NUM_TYPE_EXEMP")
    private Integer numTypeExemp;

    @Column(name = "LIBELLE")
    private String libelle;

    @OneToMany(mappedBy = "typeExemp", fetch = FetchType.LAZY)
    private Set<DemandeExemp> demandes;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(name = "INDEX_RECHERCHE_TYPE_EXEMP",
            joinColumns = @JoinColumn(name = "NUM_TYPE_EXEMP"),
            inverseJoinColumns = @JoinColumn(name = "NUM_INDEX_RECHERCHE"))
    private Set<IndexRecherche> indexRechercheSet;

    public TypeExemp(Integer numTypeExemp) {
        this.numTypeExemp = numTypeExemp;
    }

    public TypeExemp(int numTypeExemp, String libelle) {
        this.numTypeExemp = numTypeExemp;
        this.libelle = libelle;
    }

    @Override
    public Integer getId(){
        return this.numTypeExemp;
    }
}
