package fr.abes.item.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Entity
@NoArgsConstructor
@Getter @Setter
@Table(name="INDEX_RECHERCHE")
@JsonIgnoreProperties({"demandes", "IndexTypeExemp"})
public class IndexRecherche implements Serializable, GenericEntity<Integer> {
    private static final long serialVersionUID = 1L;
    @Id
    @Column(name = "NUM_INDEX_RECHERCHE")
    private Integer numIndexRecherche;

    @Column(name = "LIBELLE")
    private String libelle;

    @OneToMany(mappedBy = "indexRecherche", fetch = FetchType.LAZY)
    private Set<DemandeExemp> demandes;

    @Column(name = "CODE")
    private String code;

    @Column(name = "INDEX_ZONES")
    private Integer indexZones;

    @ManyToMany
    @JoinTable(name = "INDEX_RECHERCHE_TYPE_EXEMP",
    joinColumns = @JoinColumn(name = "NUM_INDEX_RECHERCHE"),
    inverseJoinColumns = @JoinColumn(name = "NUM_TYPE_EXEMP"))
    private Set<TypeExemp> indexTypeExemp;

    @Override
    public Integer getId() {
        return this.numIndexRecherche;
    }

    public IndexRecherche(Integer numTypeRecherche) {
        this.numIndexRecherche = numTypeRecherche;
    }

    public IndexRecherche(Integer numTypeRecherche, String libelle) {
        this.numIndexRecherche = numTypeRecherche;
        this.libelle = libelle;
    }

    public IndexRecherche(Integer numIndexRecherche, String libelle, String code, Integer indexZones) {
        this.numIndexRecherche = numIndexRecherche;
        this.libelle = libelle;
        this.code = code;
        this.indexZones = indexZones;
    }
}
