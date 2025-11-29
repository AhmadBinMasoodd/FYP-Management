package com.company.fyp_management.config;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoderHolder {
	private static PasswordEncoder encoder;

	public PasswordEncoderHolder(PasswordEncoder encoder) {
		PasswordEncoderHolder.encoder = encoder;
	}

	public static PasswordEncoder getEncoder() {
		return encoder;
	}
}
