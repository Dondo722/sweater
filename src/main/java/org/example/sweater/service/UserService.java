package org.example.sweater.service;

import org.example.sweater.domain.Role;
import org.example.sweater.domain.User;
import org.example.sweater.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.UUID;

@Service
public class UserService implements UserDetailsService {
    @Autowired
    private UserRepository userRepo;

    @Autowired
    private MailSenderService mailSenderService;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return userRepo.findByUsername(s);
    }

    public boolean addUser(User user) {
        User userFromDB = userRepo.findByUsername(user.getUsername());
        if (userFromDB == null) {
            user.setActive(true);
            user.setRoles(Collections.singleton(Role.USER));
            user.setActivationCode(UUID.randomUUID().toString());
            userRepo.save(user);

            if (!StringUtils.isEmpty(user.getEmail())){
                String message = String.format(
                        "Hello, %s! \n " +
                                "Welcome to Sweater. Please, visit next link:" +
                                "http://localhost:8080/activate/%s",
                        user.getUsername(), user.getActivationCode());
                mailSenderService.send(user.getEmail(),"Activation code", message);
            }

            return true;
        }
        return false;
    }

    public boolean activateUser(String code) {
        User user = userRepo.findByActivationCode(code);

        if (user == null) {
            return false;
        }

        user.setActivationCode(null);
        userRepo.save(user);
        return true;
    }
}
