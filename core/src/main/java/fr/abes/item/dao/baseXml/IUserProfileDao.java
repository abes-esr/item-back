package fr.abes.item.dao.baseXml;

import fr.abes.item.entities.baseXml.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface IUserProfileDao extends JpaRepository<UserProfile, Integer> {
    UserProfile findAllByUserNum(Integer userNum);
}
