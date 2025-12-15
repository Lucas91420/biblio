package fr.ensitech.biblio.controller;

import fr.ensitech.biblio.entity.User;
import fr.ensitech.biblio.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;



@RestController
@CrossOrigin(origins = "http://localhost:8080")
@RequestMapping("/api/users")
public class UserController implements IUserController {


    @Autowired
    private IUserService userService;

    @PostMapping("/register")
    @Override
    public ResponseEntity<String> register(@RequestBody User user) {
        try {
            userService.register(user);
            return new ResponseEntity<>("User enregistré et inactive", HttpStatus.CREATED);
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("Erreur: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @Override
    @GetMapping("/activate/{email}")
    public ResponseEntity<String> activate(@RequestParam String email) {
        try {
            userService.activateAccount(email);
            return new ResponseEntity<>("Compte activé", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ==========================
    //  LOGIN ETAPE 1 : email + password
    // ==========================
    @PostMapping("/login")
    @Override
    public ResponseEntity<String> login(@RequestParam String email, @RequestParam String password) {
        try {
            User user = userService.login(email, password);

            // On renvoie la question de sécurité
            Map<String, String> body = new HashMap<>();
            body.put("email", user.getEmail());
            body.put("securityQuestion", user.getSecurityQuestion() != null
                    ? user.getSecurityQuestion().getLabel()
                    : "Aucune question de sécurité");
            // On retourne le JSON
            return new ResponseEntity<>(toJson(body), HttpStatus.OK);

        } catch (Exception e) {

            if (e.getMessage().contains("non activé")) {
                return new ResponseEntity<>("Votre compte n'est pas activé", HttpStatus.FORBIDDEN);
            }
            if (e.getMessage().contains("expiré")) {
                return new ResponseEntity<>("Mot de passe expiré, veuillez le renouveler", HttpStatus.FORBIDDEN);
            }

            return new ResponseEntity<>("Identifiants invalides", HttpStatus.UNAUTHORIZED);
        }
    }

    // ==========================
    //  LOGIN ETAPE 2 : réponse secrète
    // ==========================
    @PostMapping("/login/verify-security")
    @Override
    public ResponseEntity<String> verifySecurity(@RequestParam String email,
                                                 @RequestParam String answer) {
        try {
            userService.verifySecurityAnswer(email, answer);
            return new ResponseEntity<>("Authentification réussie", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Authentification échouée", HttpStatus.UNAUTHORIZED);
        }
    }

    @DeleteMapping("/unsubscribe")
    @Override
    public ResponseEntity<String> unsubscribe(@RequestParam String email) {
        try {
            userService.unsubscribe(email);
            return new ResponseEntity<>("User supprimé", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/profile")
    @Override
    public ResponseEntity<String> updateProfile(@PathVariable long id, @RequestBody User user) {
        try {
            userService.updateProfile(id, user);
            return new ResponseEntity<>("Profil mis à jour", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/{id}/{oldPwd}/{newPwd}")
    @Override
    public ResponseEntity<String> updatePassword(@PathVariable long id,
                                                 @PathVariable String oldPwd,
                                                 @PathVariable String newPwd) {
        try {
            userService.updatePassword(id, oldPwd, newPwd);
            return new ResponseEntity<>("Mot de passe mis à jour", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // ==========================
    //  PASSWORD RENEW
    //  PUT /api/users/{email}/password/renew
    // ==========================
    @PutMapping("/{email}/password/renew")
    @Override
    public ResponseEntity<String> renewPassword(@PathVariable String email,
                                                @RequestParam String oldPwd,
                                                @RequestParam String newPwd) {
        try {
            userService.renewPassword(email, oldPwd, newPwd);
            return new ResponseEntity<>("Mot de passe renouvelé", HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>("Erreur: " + e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    // petite aide pour renvoyer un JSON simple à partir d'une Map
    private String toJson(Map<String, String> map) {
        StringBuilder sb = new StringBuilder("{");
        int i = 0;
        for (Map.Entry<String, String> e : map.entrySet()) {
            if (i > 0) sb.append(",");
            sb.append("\"").append(e.getKey()).append("\":\"").append(e.getValue()).append("\"");
            i++;
        }
        sb.append("}");
        return sb.toString();
    }
}
