package com.company.fyp_management.entity;

import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import com.company.fyp_management.config.PasswordEncoderHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Method;

/**
 * Encodes entity password fields before saving. Targets entities that expose getPassword()/setPassword(String).
 * Avoids double-encoding by checking for common BCrypt prefix ($2a$ or $2y$).
 */
public class PasswordEncodingListener {

	private void tryEncode(Object entity) {
		try {
			Method getPassword = entity.getClass().getMethod("getPassword");
			Method setPassword = entity.getClass().getMethod("setPassword", String.class);
			Object cur = getPassword.invoke(entity);
			if (cur != null) {
				String curStr = cur.toString();
				// skip if already encoded (common BCrypt prefix)
				if (curStr.startsWith("$2a$") || curStr.startsWith("$2y$") || curStr.startsWith("$2b$")) {
					return;
				}
				PasswordEncoder encoder = PasswordEncoderHolder.getEncoder();
				if (encoder != null) {
					String encoded = encoder.encode(curStr);
					setPassword.invoke(entity, encoded);
				}
			}
		} catch (NoSuchMethodException nsme) {
			// entity has no get/set password methods - nothing to do
		} catch (Exception e) {
			// ignore encoding errors to avoid blocking persistence; log in real app
		}
	}

	@PrePersist
	@PreUpdate
	public void encodePassword(Object entity) {
		tryEncode(entity);
	}
}
