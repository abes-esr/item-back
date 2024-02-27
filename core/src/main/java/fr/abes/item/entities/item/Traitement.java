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
@Table(name="TRAITEMENT")
@JsonIgnoreProperties({"demandeModifs"})
public class Traitement implements Serializable, GenericEntity<Integer> {
	private static final long serialVersionUID = 1L;
	@Getter @Setter @Id @Column(name="NUM_TRAITEMENT")
	private Integer numTraitement;
	@Getter @Setter @Column(name="LIBELLE")
	private String libelle;
	@Getter @Setter @Column(name="NOM_METHODE")
	private String nomMethode;
	@Getter @Setter @OneToMany(mappedBy = "traitement", fetch = FetchType.LAZY)
	private Set<DemandeModif> demandeModifs;
	
	public Traitement(Integer numTraitement, String libelle, String nomMethode) {
		super();
		this.numTraitement = numTraitement;
		this.libelle = libelle;
		this.nomMethode = nomMethode;
	}
	
	public Traitement(Integer numTraitement) {
		this.numTraitement = numTraitement;
	}

	@Override
	public Integer getId() { return numTraitement; }

}
