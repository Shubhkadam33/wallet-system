package com.techpulse.wallet.service;




import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.techpulse.wallet.dto.RegisterRequest;
import com.techpulse.wallet.dto.RegisterResponse;
import com.techpulse.wallet.entity.User;
import com.techpulse.wallet.entity.Wallet;
import com.techpulse.wallet.enums.Role;
import com.techpulse.wallet.repository.UserRepository;
import com.techpulse.wallet.repository.WalletRepository;

import com.techpulse.wallet.dto.AuthResponse;
import com.techpulse.wallet.dto.LoginRequest;
import org.springframework.security.authentication.BadCredentialsException;

import com.techpulse.wallet.security.JwtService;
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final WalletRepository walletRepository;
    private final PasswordEncoder passwordEncoder;
    
    private final JwtService jwtService;
    
    public AuthService(
            UserRepository userRepository,
            WalletRepository walletRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.walletRepository = walletRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {

        String email = request.getEmail().trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email is already registered");
        }

        User user = new User();
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.USER);

        User savedUser = userRepository.save(user);

        Wallet wallet = new Wallet();
        wallet.setUser(savedUser);

        walletRepository.save(wallet);

        return new RegisterResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                "Registration successful. Wallet created.");
    }
    

public AuthResponse login(LoginRequest request) {

    String email = request.getEmail().trim().toLowerCase();

    User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new BadCredentialsException(
                    "Invalid email or password"));

    if (!passwordEncoder.matches(
            request.getPassword(), user.getPassword())) {
        throw new BadCredentialsException(
                "Invalid email or password");
    }

    org.springframework.security.core.userdetails.UserDetails userDetails =
            org.springframework.security.core.userdetails.User
                    .withUsername(user.getEmail())
                    .password(user.getPassword())
                    .roles(user.getRole().name())
                    .build();

    String token = jwtService.generateToken(userDetails);

    return new AuthResponse(token, user.getEmail());
}

}
