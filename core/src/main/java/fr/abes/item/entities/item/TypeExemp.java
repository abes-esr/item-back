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
@NoArgsConstructor
@Getter @Setter
@Table(name="TYPE_EXEMP")
@JsonIgnoreProperties({"demandes", "indexRechercheSet"})
public class TypeExemp implements Serializable, GenericEntity<Integer> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "NUM_TYPE_EXEMP")
    private Integer numTypeExemp;

    @Column(name = "LIBELLE")
    private String libelle;

    @OneToMany(mappedBy = "typeExemp", fetch = FetchType.LAZY)
    private Set<DemandeExemp> demandes;

    @ManyToMany
    @JoinTable(name = "INDEX_RECHERCHE_TYPE_EXEMP",
            joinColumns = @JoinColumn(name = "NUM_TYPE_EXEMP"),
            inverseJoinColumns = @JoinColumn(name = "NUM_INDEX_RECHERCHE"))
    private Set<IndexRecherche> indexRechercheSet;

    public TypeExemp(Integer numTypeExemp) {
        this.numTypeExemp = numTypeExemp;
    }

    @Override
    public Integer getId(){
        return this.numTypeExemp;
    }
}
