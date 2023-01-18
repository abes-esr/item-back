package fr.abes.item.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.entities.GenericEntity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

@Entity
@Table(name = "ZONES_AUTORISEES")
@NoArgsConstructor
@Getter @Setter
@JsonIgnoreProperties({"zonesTypesExemp"})
public class ZonesAutorisees implements Serializable, GenericEntity<Integer> {
    private static final long serialVersionUID = 1L;

    @Column(name = "NUM_ZONE")
    @Id
    private Integer numZone;

    @Column(name = "LABEL_ZONE")
    private String labelZone;

    @Column(name = "INDICATEURS")
    private String indicateurs;

    @OneToMany(mappedBy = "zone", fetch = FetchType.LAZY)
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
