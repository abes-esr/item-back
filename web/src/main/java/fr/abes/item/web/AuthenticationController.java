package fr.abes.item.web;

import fr.abes.item.core.exception.ForbiddenException;
import fr.abes.item.security.JwtAuthenticationResponse;
import fr.abes.item.security.JwtTokenProvider;
import fr.abes.item.security.LoginRequest;
import fr.abes.item.security.User;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/v1")
public class AuthenticationController {

	private final AuthenticationManager authenticationManager;

	private final JwtTokenProvider tokenProvider;

	public AuthenticationController(AuthenticationManager authenticationManager, JwtTokenProvider tokenProvider) {
		this.authenticationManager = authenticationManager;
		this.tokenProvider = tokenProvider;
	}

	@Operation(summary = "permet de s'authentifier et de récupérer un token.",
			description = "le token doit être utilisé pour accéder aux ressources protegées.")
	@PostMapping("/signin")
	public ResponseEntity<JwtAuthenticationResponse> authenticateUser(@RequestBody LoginRequest loginRequest) throws ForbiddenException, BadCredentialsException {
		Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword()));
		SecurityContextHolder.getContext().setAuthentication(authentication);
		User user = (User)authentication.getPrincipal();
		if (user.getAuthorities().isEmpty()) {
			throw new ForbiddenException("Ce login ne dispose pas des droits nécessaires pour accéder à Item");
		}
		String jwt = tokenProvider.generateToken(user);

		return ResponseEntity.ok(new JwtAuthenticationResponse(jwt, user.getUserNum(), user.getShortName(), user.getIln(), user.getRole(), user.getMail()));
	}

	@Operation(summary = "Permet de vérifier la validité d'un token")
	@GetMapping("/checkToken")
	public Boolean checkToken(HttpServletRequest request) {
		String jwt = tokenProvider.getJwtFromRequest(request);
		if (jwt == null) return false;
		return tokenProvider.validateToken(jwt);
	}
}
