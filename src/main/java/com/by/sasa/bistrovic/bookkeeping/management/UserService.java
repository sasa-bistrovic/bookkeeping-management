package com.by.sasa.bistrovic.bookkeeping.management;

import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;

    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> getAllUsers() {
        return repo.findAll();
    }

    public User saveUser(User user) {
        return repo.save(user);
    }

    public User getUser(String id) {
        return repo.findById(id).orElse(null);
    }
    
    public User findByEmail(String email) {
        return repo.findByEmail(email).orElse(null);
    }    

    public void deleteUser(String id) {
        repo.deleteById(id);
    }

}
