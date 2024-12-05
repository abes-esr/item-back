package fr.abes.item.core.entities.item;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.abes.item.core.entities.GenericEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Set;

@Entity
@Table(name="ETAT_DEMANDE")
@Getter @Setter
@JsonIgnoreProperties({"journalDemandeModifs", "demandeModifs", "journalDemandeExemps","demandeExemps"})
public class EtatDemande implements Serializable, GenericEntity<Integer> {
	@Id @Column(name="NUM_ETAT")
	private Integer numEtat;
	@Column(name="LIBELLE")
	private String libelle;
	@OneToMany(mappedBy = "etatDemande", fetch = FetchType.LAZY)
	private Set<DemandeModif> demandeModifs;
	@OneToMany(mappedBy = "etatDemande", fetch = FetchType.LAZY)
	private Set<JournalDemandeModif> journalDemandeModifs;
	@OneToMany(mappedBy = "etatDemande", fetch = FetchType.LAZY)
	private Set<DemandeExemp> demandeExemps;
	@OneToMany(mappedBy = "etatDemande", fetch = FetchType.LAZY)
	private Set<JournalDemandeExemp> journalDemandeExemps;

	public EtatDemande() {
	}

	public EtatDemande(Integer numEtat, String libelle) {
		this.numEtat = numEtat;
		this.libelle = libelle;
	}

	public EtatDemande(Integer numEtat) {
		this.numEtat = numEtat;
	}

	@Override
	public Integer getId() { return numEtat; }


	@Override
	public String toString() {
		return "EtatDemande{" +
				"libelle='" + libelle + '\'' +
				", numEtat=" + numEtat +
				'}';
	}
}
