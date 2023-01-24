package fr.abes.item.dao.item;

import fr.abes.item.entities.item.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IUtilisateurDao extends JpaRepository<Utilisateur, Integer> {

}
