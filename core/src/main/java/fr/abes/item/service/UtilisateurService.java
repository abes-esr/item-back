package fr.abes.item.service;

import fr.abes.item.dao.baseXml.ILibProfileDao;
import fr.abes.item.dao.baseXml.IUserProfileDao;
import fr.abes.item.dao.item.IRoleDao;
import fr.abes.item.dao.item.IUtilisateurDao;
import fr.abes.item.entities.baseXml.LibProfile;
import fr.abes.item.entities.baseXml.UserProfile;
import fr.abes.item.entities.item.Utilisateur;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Slf4j
@Service
public class UtilisateurService {
    private final IUtilisateurDao utilisateurDao;
    private final IUserProfileDao userProfileDao;

    private final ILibProfileDao libProfileDao;

    private final IRoleDao roleDao;

    public UtilisateurService(IUtilisateurDao utilisateurDao, IUserProfileDao userProfileDao, ILibProfileDao libProfileDao, IRoleDao roleDao) {
        this.utilisateurDao = utilisateurDao;
        this.userProfileDao = userProfileDao;
        this.libProfileDao = libProfileDao;
        this.roleDao = roleDao;
    }

    public Utilisateur findById(Integer id) {
        Optional<Utilisateur> user = utilisateurDao.findById(id);
        if (user.isPresent()) {
            Utilisateur utilisateur = user.get();
            setIlnOnUtilisateur(utilisateur);
            return utilisateur;
        }
        return null;
    }

    public String findRcrById(String id){
        return userProfileDao.findAllByUserNum(Integer.parseInt(id)).getLibrary();
    }

    public Utilisateur save(Utilisateur utilisateur) {
        return utilisateurDao.save(utilisateur);
    }

    public void deleteById(Integer id) {
        utilisateurDao.deleteById(id);
    }

    public void setIlnOnUtilisateur(Utilisateur utilisateur) {
        Optional<UserProfile> userProfile = userProfileDao.findById(utilisateur.getId());
        if (userProfile.isPresent()) {
            String rcr = userProfile.get().getLibrary().trim();
            Optional<LibProfile> libProfile = libProfileDao.findById(rcr);
            libProfile.ifPresent(profile -> utilisateur.setIln(profile.getIln()));
        }
    }

    public boolean isAdmin(Utilisateur utilisateur) {
        Optional<UserProfile> userProfile = userProfileDao.findById(utilisateur.getId());
        if (userProfile.isPresent()) {
            if (userProfile.get().getUserGroup().equals(roleDao.findById(1).get().getUserGroup())) {
                return true;
            }
        }
        return false;
    }

    public String findEmailById(Integer id) {
        Optional<Utilisateur> utilisateurOpt = utilisateurDao.findById(id);
        return utilisateurOpt.map(Utilisateur::getEmail).orElse(null);
    }
}
