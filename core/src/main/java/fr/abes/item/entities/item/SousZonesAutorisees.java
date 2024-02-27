package fr.abes.item.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Entity
@NoArgsConstructor
@Table(name = "SOUS_ZONES_AUTORISEES")
@Getter @Setter
@JsonIgnoreProperties({"zone"})
public class SousZonesAutorisees implements Serializable, GenericEntity<Integer> {
    private static final long serialVersionUID = 1L;

    @Id
    @Column(name = "NUM_SOUS_ZONE")
    private Integer numSousZone;

    @Column(name = "LIBELLE")
    private String libelle;

    @Column(name = "MANDATORY")
    private Boolean mandatory;

    @ManyToOne
    @JoinColumn(name = "NUM_ZONE")
    private ZonesAutorisees zone;

    @Override
    public Integer getId() {
        return numSousZone;
    }

    public SousZonesAutorisees(Integer numSousZone, String libelle, ZonesAutorisees zone, Boolean mandatory) {
        this.numSousZone = numSousZone;
        this.libelle = libelle;
        this.zone = zone;
        this.mandatory = mandatory;
    }
}
