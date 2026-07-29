package com.by.sasa.bistrovic.bookkeeping.management;

import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8080", "https://bookkeeping-management-b16af486dd67.herokuapp.com"})
public class UserController {

    private final UserService service;
    private final JwtService jwtService;    

    public UserController(UserService service, JwtService jwtService) {
        this.service = service;
        this.jwtService = jwtService;
    }

    @GetMapping
    public List<User> getUsers() {
        return service.getAllUsers();
    }

    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {

        User user = service.findByEmail(request.getEmail());

        if (user == null) {
            throw new RuntimeException("User not found");
        }

        if (!user.getPassword().equals(request.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        String token = jwtService.generateToken(user.getEmail());

        return new AuthResponse(token, user);
    }    

    @PostMapping("/get-token/{token}")
    public String getToken(@PathVariable String token) {

        String getToken = jwtService.generateToken(token);

        return getToken;
    }
    
    @PostMapping("/register")
    public AuthResponse createUser(@RequestBody User user) {
        
        User myUser = service.findByEmail(user.getEmail());

        if (myUser != null) {
            throw new RuntimeException("User already exists");
        }

        service.saveUser(user);
        String token = jwtService.generateToken(user.getEmail());
         
        return new AuthResponse(token, user);
    } 

    @GetMapping("/{id}")
    public User getUser(@PathVariable String id) {
        return service.getUser(id);
    }

    @DeleteMapping("/{id}")
    public void deleteUser(@PathVariable String id) {
        service.deleteUser(id);
    }
}
