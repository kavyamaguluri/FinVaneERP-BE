package com.arcsetu.v2.config;

import com.arcsetu.v2.dao.accounts.IAccountGroupJpaAccess;
import com.arcsetu.v2.dao.accounts.IAccountLedgerJpaAccess;
import com.arcsetu.v2.dao.common.ILocationJpaAccess;
import com.arcsetu.v2.entity.accounts.AccountGroupEntity;
import com.arcsetu.v2.entity.accounts.AccountLedgerEntity;
import com.arcsetu.v2.entity.common.LocationEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IAccountGroupJpaAccess groupDao;
    private final IAccountLedgerJpaAccess ledgerDao;
    private final ILocationJpaAccess locationDao;

    @Override
    @Transactional
    public void run(String... args) {
        String tenantId = "1";

        if (groupDao.findByTenantIdOrderByNameAsc(tenantId).isEmpty()) {
            // Seed Primary Groups
            AccountGroupEntity assets = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Current Assets").nature("ASSET").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity fixedAssets = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Fixed Assets").nature("ASSET").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity liabilities = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Current Liabilities").nature("LIABILITY").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity equity = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Capital Account").nature("EQUITY").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity salesGrp = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Sales Accounts").nature("INCOME").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity purchaseGrp = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Purchase Accounts").nature("EXPENSE").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity directExp = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Direct Expenses").nature("EXPENSE").groupLevel(1).isBuiltin(true).build());
            AccountGroupEntity indirectExp = groupDao.save(AccountGroupEntity.builder().tenantId(tenantId).name("Indirect Expenses").nature("EXPENSE").groupLevel(1).isBuiltin(true).build());

            // Seed Standard Ledgers
            ledgerDao.save(AccountLedgerEntity.builder()
                    .tenantId(tenantId).code("CASH01").name("Cash-in-Hand").groupId(assets.getId())
                    .openingBalance(new BigDecimal("50000.00")).currentBalance(new BigDecimal("50000.00")).isActive(true).build());

            ledgerDao.save(AccountLedgerEntity.builder()
                    .tenantId(tenantId).code("BANK01").name("HDFC Bank Current A/c").groupId(assets.getId())
                    .openingBalance(new BigDecimal("250000.00")).currentBalance(new BigDecimal("250000.00")).isActive(true).build());

            ledgerDao.save(AccountLedgerEntity.builder()
                    .tenantId(tenantId).code("CAP01").name("Owner's Capital").groupId(equity.getId())
                    .openingBalance(new BigDecimal("300000.00")).currentBalance(new BigDecimal("300000.00")).isActive(true).build());

            ledgerDao.save(AccountLedgerEntity.builder()
                    .tenantId(tenantId).code("SALE01").name("Domestic Goods Sales (GST)").groupId(salesGrp.getId())
                    .openingBalance(BigDecimal.ZERO).currentBalance(BigDecimal.ZERO).isActive(true).build());

            ledgerDao.save(AccountLedgerEntity.builder()
                    .tenantId(tenantId).code("PURC01").name("Raw Material Purchases").groupId(purchaseGrp.getId())
                    .openingBalance(BigDecimal.ZERO).currentBalance(BigDecimal.ZERO).isActive(true).build());

            ledgerDao.save(AccountLedgerEntity.builder()
                    .tenantId(tenantId).code("RENT01").name("Office Rent").groupId(indirectExp.getId())
                    .openingBalance(BigDecimal.ZERO).currentBalance(BigDecimal.ZERO).isActive(true).build());
        }

        if (locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).isEmpty()) {
            locationDao.save(LocationEntity.builder()
                    .tenantId(tenantId)
                    .code("WH-MAIN")
                    .name("Main Central Warehouse")
                    .locationType("warehouse")
                    .stateCode("27") // Maharashtra
                    .address("Plot 14, MIDC Industrial Area, Pune")
                    .isActive(true)
                    .build());
        }
    }
}
