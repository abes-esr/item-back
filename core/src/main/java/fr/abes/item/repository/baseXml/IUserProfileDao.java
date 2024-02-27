package fr.abes.item.repository.baseXml;

import fr.abes.item.configuration.BaseXMLConfiguration;
import fr.abes.item.entities.baseXml.UserProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
@BaseXMLConfiguration
public interface IUserProfileDao extends JpaRepository<UserProfile, Integer> {
    UserProfile findAllByUserNum(Integer userNum);
}
