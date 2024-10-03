package fr.abes.item.core.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.core.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "ZONES_AUTORISEES")
@NoArgsConstructor
@Getter @Setter
@JsonIgnoreProperties({"zonesTypesExemp"})
public class ZonesAutorisees implements Serializable, GenericEntity<Integer> {
    @Column(name = "NUM_ZONE")
    @Id
    private Integer numZone;

    @Column(name = "LABEL_ZONE")
    private String labelZone;

    @Column(name = "INDICATEURS")
    private String indicateurs;

    @OneToMany(mappedBy = "zone", fetch = FetchType.EAGER)
    private List<SousZonesAutorisees> sousZonesAutorisees;

    @ManyToMany
    @JoinTable(name = "ZONES_AUTORISEES_TYPE_EXEMP",
        joinColumns = {@JoinColumn(name = "ZONESAUTORISEES_NUM_ZONE")},
        inverseJoinColumns = {@JoinColumn(name = "ZONESTYPESEXEMP_NUM_TYPE_EXEMP")})
    private Set<TypeExemp> zonesTypesExemp;

    @Override
    public Integer getId() {
        return this.numZone;
    }

}
