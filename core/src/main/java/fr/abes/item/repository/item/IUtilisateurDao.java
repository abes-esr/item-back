package fr.abes.item.repository.item;

import fr.abes.item.configuration.ItemConfiguration;
import fr.abes.item.entities.item.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@ItemConfiguration
public interface IUtilisateurDao extends JpaRepository<Utilisateur, Integer> {

}
