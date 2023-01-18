package fr.abes.item.dao.item;

import fr.abes.item.entities.item.Utilisateur;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IUtilisateurDao extends JpaRepository<Utilisateur, Integer> {
    @Query("select l.library from UserProfile l where l.userNum = :userNum")
    String findUserRcr(@Param("userNum") Integer userNum);
}
