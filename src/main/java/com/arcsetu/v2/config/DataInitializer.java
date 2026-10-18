package com.arcsetu.v2.config;

import com.arcsetu.v2.dao.accounts.IAccountGroupJpaAccess;
import com.arcsetu.v2.entity.accounts.AccountGroupEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.arcsetu.v2.dao.auth.IUserJpaAccess;
import com.arcsetu.v2.entity.auth.UserEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IAccountGroupJpaAccess groupDao;
    private final IUserJpaAccess userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String tenantId = "1";

        // Only ensure primary account category taxonomy exists if database is brand new
        if (groupDao.findByTenantIdOrderByNameAsc(tenantId).isEmpty()) {
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Current Assets").nature("ASSET").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Fixed Assets").nature("ASSET").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Current Liabilities").nature("LIABILITY").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Capital Account").nature("EQUITY").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Sales Accounts").nature("INCOME").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Purchase Accounts").nature("EXPENSE").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Direct Expenses").nature("EXPENSE").groupLevel(1).isBuiltin(true).build());
            groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Indirect Expenses").nature("EXPENSE").groupLevel(1).isBuiltin(true).build());
        }

        // Initialize / sync primary Admin account for Kavya (kavyasony7512@gmail.com / Finvane@123)
        String adminEmail = "kavyasony7512@gmail.com";
        userDao.findByEmail(adminEmail).ifPresentOrElse(
            existingAdmin -> {
                existingAdmin.setRole("ADMIN");
                existingAdmin.setPassword(passwordEncoder.encode("Finvane@123"));
                existingAdmin.setIsActive(true);
                userDao.save(existingAdmin);
            },
            () -> {
                userDao.save(UserEntity.builder()
                        .username("kavya_admin")
                        .email(adminEmail)
                        .fullName("Kavya Maguluri (System Admin)")
                        .password(passwordEncoder.encode("Finvane@123"))
                        .role("ADMIN")
                        .tenantId(tenantId)
                        .isActive(true)
                        .build());
            }
        );
    }
}
