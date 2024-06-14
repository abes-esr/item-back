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
@Table(name="TRAITEMENT")
@JsonIgnoreProperties({"demandeModifs"})
@Getter @Setter
public class Traitement implements Serializable, GenericEntity<Integer> {
	@Id @Column(name="NUM_TRAITEMENT")
	private Integer numTraitement;
	@Column(name="LIBELLE")
	private String libelle;
	@Column(name="NOM_METHODE")
	private String nomMethode;
	@OneToMany(mappedBy = "traitement", fetch = FetchType.LAZY)
	private Set<DemandeModif> demandeModifs;
	
	public Traitement(Integer numTraitement, String libelle, String nomMethode) {
		super();
		this.numTraitement = numTraitement;
		this.libelle = libelle;
		this.nomMethode = nomMethode;
	}

	@Override
	public Integer getId() { return numTraitement; }

}
