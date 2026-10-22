package com.finvane.config;

import com.finvane.dao.accounts.IAccountGroupJpaAccess;
import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherItemJpaAccess;
import com.finvane.dao.accounts.IVoucherJpaAccess;
import com.finvane.dao.auth.IUserJpaAccess;
import com.finvane.dao.common.ILocationJpaAccess;
import com.finvane.dao.common.IPartyJpaAccess;
import com.finvane.entity.accounts.AccountGroupEntity;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.entity.auth.UserEntity;
import com.finvane.entity.common.LocationEntity;
import com.finvane.entity.common.PartyEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final IAccountGroupJpaAccess groupDao;
    private final IAccountLedgerJpaAccess ledgerDao;
    private final IPartyJpaAccess partyDao;
    private final ILocationJpaAccess locationDao;
    private final IVoucherJpaAccess voucherDao;
    private final IVoucherItemJpaAccess voucherItemDao;
    private final IUserJpaAccess userDao;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) {
        String tenantId = "1";
        log.info("Starting FinVane ERP database initialization and data seeding for tenant: {}", tenantId);

        // 1. Seed Users (Express demo credentials + admin)
        seedUsers(tenantId);

        // 2. Seed Account Groups
        Map<String, AccountGroupEntity> groupMap = seedGroups(tenantId);

        // 3. Seed Locations (4 multi-state warehouses & branches from seed.js)
        seedLocations(tenantId);

        // 4. Seed Parties (Customers and Vendors from seed.js)
        seedParties(tenantId);

        // 5. Seed Chart of Accounts Ledgers (29 standard ledgers matching seed.js)
        Map<String, AccountLedgerEntity> ledgerMap = seedLedgers(tenantId, groupMap);

        // 6. Seed Vouchers & Double-Entry Lines
        seedVouchers(tenantId, ledgerMap);

        log.info("FinVane ERP data initialization completed successfully!");
    }

    private void seedUsers(String tenantId) {
        // System Admin (Kavya)
        upsertUser("kavya_admin", "kavyasony7512@gmail.com", "Kavya Maguluri", "ADMIN", "Finvane@123", tenantId);

        // Standard Administrator
        upsertUser("admin", "admin@finvane.com", "System Admin", "ADMIN", "Finvane@123", tenantId);

        // Client Admin
        upsertUser("mukundan", "mukundan0812@gmail.com", "Mukundan", "CLIENT_ADMIN", "Finvane@123", tenantId);

        // Client Users
        upsertUser("operator", "operator@finvane.com", "Standard Operator", "CLIENT_USER", "Finvane@123", tenantId);
        upsertUser("accountant", "accountant@finvane.com", "Finance Accountant", "CLIENT_USER", "Finvane@123", tenantId);
    }

    private void upsertUser(String username, String email, String fullName, String role, String password, String tenantId) {
        UserEntity user = userDao.findByUsernameOrEmail(username.toLowerCase(), email.toLowerCase())
                .orElseGet(() -> UserEntity.builder()
                        .username(username.toLowerCase())
                        .email(email.toLowerCase())
                        .tenantId(tenantId)
                        .build());
        user.setUsername(username.toLowerCase());
        user.setEmail(email.toLowerCase());
        user.setFullName(fullName);
        user.setRole(role);
        user.setPassword(passwordEncoder.encode(password));
        user.setIsActive(true);
        userDao.save(user);
    }

    private Map<String, AccountGroupEntity> seedGroups(String tenantId) {
        Map<String, AccountGroupEntity> map = new HashMap<>();

        // Level 1 Primary Groups
        String[][] primary = {
            {"Current Assets", "ASSET"},
            {"Fixed Assets", "ASSET"},
            {"Current Liabilities", "LIABILITY"},
            {"Capital Account", "EQUITY"},
            {"Sales Accounts", "INCOME"},
            {"Purchase Accounts", "EXPENSE"},
            {"Direct Expenses", "EXPENSE"},
            {"Indirect Expenses", "EXPENSE"},
            {"Loans (Liability)", "LIABILITY"},
            {"Direct Incomes", "INCOME"},
            {"Indirect Incomes", "INCOME"},
            {"Investments", "ASSET"}
        };

        for (String[] p : primary) {
            String name = p[0];
            String nature = p[1];
            AccountGroupEntity grp = groupDao.findByTenantIdAndName(tenantId, name)
                    .orElseGet(() -> groupDao.save(AccountGroupEntity.builder()
                            .tenantId(tenantId)
                            .name(name)
                            .nature(nature)
                            .groupLevel(1)
                            .isBuiltin(true)
                            .build()));
            map.put(name, grp);
        }

        // Level 2 Sub-groups
        String[][] secondary = {
            {"Cash-in-Hand", "Current Assets", "ASSET"},
            {"Bank Accounts", "Current Assets", "ASSET"},
            {"Sundry Debtors", "Current Assets", "ASSET"},
            {"Stock-in-Hand", "Current Assets", "ASSET"},
            {"Input Tax Credit", "Current Assets", "ASSET"},
            {"Sundry Creditors", "Current Liabilities", "LIABILITY"},
            {"Duties & Taxes", "Current Liabilities", "LIABILITY"},
            {"Provisions", "Current Liabilities", "LIABILITY"},
            {"Profit & Loss Account", "Capital Account", "EQUITY"}
        };

        for (String[] s : secondary) {
            String name = s[0];
            String parentName = s[1];
            String nature = s[2];
            AccountGroupEntity parent = map.get(parentName);
            Long parentId = parent != null ? parent.getId() : null;

            AccountGroupEntity grp = groupDao.findByTenantIdAndName(tenantId, name)
                    .orElseGet(() -> groupDao.save(AccountGroupEntity.builder()
                            .tenantId(tenantId)
                            .name(name)
                            .parentId(parentId)
                            .nature(nature)
                            .groupLevel(2)
                            .isBuiltin(true)
                            .build()));
            map.put(name, grp);
        }

        return map;
    }

    private void seedLocations(String tenantId) {
        if (!locationDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).isEmpty()) {
            return;
        }

        locationDao.save(LocationEntity.builder()
                .tenantId(tenantId)
                .code("LOC-MUM")
                .name("Mumbai Central Warehouse")
                .locationType("warehouse")
                .stateCode("27")
                .address("Andheri East, Mumbai, Maharashtra")
                .gstin("27AAAPL1234C1Z5")
                .isActive(true)
                .build());

        locationDao.save(LocationEntity.builder()
                .tenantId(tenantId)
                .code("LOC-PUN")
                .name("Pune Regional Warehouse")
                .locationType("warehouse")
                .stateCode("27")
                .address("Hinjewadi, Pune, Maharashtra")
                .gstin("27AAAPL1234C1Z5")
                .isActive(true)
                .build());

        locationDao.save(LocationEntity.builder()
                .tenantId(tenantId)
                .code("LOC-DEL")
                .name("Delhi Branch Office")
                .locationType("branch")
                .stateCode("07")
                .address("Karol Bagh, New Delhi")
                .gstin("07AAAPL1234C1Z9")
                .isActive(true)
                .build());

        locationDao.save(LocationEntity.builder()
                .tenantId(tenantId)
                .code("LOC-BLR")
                .name("Bengaluru Retail Store")
                .locationType("retail_store")
                .stateCode("29")
                .address("Koramangala, Bengaluru, Karnataka")
                .gstin("29AAAPL1234C1Z1")
                .isActive(true)
                .build());
    }

    private void seedParties(String tenantId) {
        if (!partyDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).isEmpty()) {
            return;
        }

        partyDao.save(PartyEntity.builder()
                .tenantId(tenantId)
                .name("Sunrise Traders Pvt Ltd")
                .tradeName("Sunrise Traders")
                .partyType("CUSTOMER")
                .gstin("27AAAPL1234C1Z5")
                .pan("AAAPL1234C")
                .stateCode("27")
                .address("Andheri, Mumbai, Maharashtra")
                .email("accounts@sunrise.example")
                .phone("9820000001")
                .openingBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("325000.00"))
                .isActive(true)
                .build());

        partyDao.save(PartyEntity.builder()
                .tenantId(tenantId)
                .name("Delta Textiles LLP")
                .tradeName("Delta Textiles")
                .partyType("CUSTOMER")
                .gstin("07BBBPL5678D1Z2")
                .pan("BBBPL5678D")
                .stateCode("07")
                .address("Karol Bagh, Delhi")
                .email("billing@delta.example")
                .phone("9911000002")
                .openingBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("480000.00"))
                .isActive(true)
                .build());

        partyDao.save(PartyEntity.builder()
                .tenantId(tenantId)
                .name("Om Suppliers")
                .tradeName("Om Suppliers & Co")
                .partyType("VENDOR")
                .gstin("27CCCPL9012E1Z9")
                .pan("CCCPL9012E")
                .stateCode("27")
                .address("Vashi, Navi Mumbai, Maharashtra")
                .email("sales@omsuppliers.example")
                .phone("9820000003")
                .openingBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("540000.00"))
                .isActive(true)
                .build());

        partyDao.save(PartyEntity.builder()
                .tenantId(tenantId)
                .name("Kaveri Retail Chain")
                .tradeName("Kaveri Retail")
                .partyType("CUSTOMER")
                .gstin("29DDDPL3456F1Z4")
                .pan("DDDPL3456F")
                .stateCode("29")
                .address("Koramangala, Bengaluru, Karnataka")
                .email("ap@kaveriretail.example")
                .phone("9845000004")
                .openingBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("615000.00"))
                .isActive(true)
                .build());

        partyDao.save(PartyEntity.builder()
                .tenantId(tenantId)
                .name("Northwind Consulting Client")
                .tradeName("Northwind Consulting")
                .partyType("CUSTOMER")
                .gstin("29EEEPL6789H1Z3")
                .pan("EEEPL6789H")
                .stateCode("29")
                .address("Whitefield, Bengaluru, Karnataka")
                .email("ap@northwind.example")
                .phone("9900011122")
                .openingBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("400000.00"))
                .isActive(true)
                .build());

        partyDao.save(PartyEntity.builder()
                .tenantId(tenantId)
                .name("Godrej Furniture Ltd")
                .tradeName("Godrej Industrial")
                .partyType("VENDOR")
                .gstin("27FFFPL0123G1Z7")
                .pan("FFFPL0123G")
                .stateCode("27")
                .address("Vikhroli, Mumbai, Maharashtra")
                .email("orders@godrej.example")
                .phone("9820000008")
                .openingBalance(BigDecimal.ZERO)
                .currentBalance(new BigDecimal("400000.00"))
                .isActive(true)
                .build());
    }

    private Map<String, AccountLedgerEntity> seedLedgers(String tenantId, Map<String, AccountGroupEntity> groupMap) {
        Map<String, AccountLedgerEntity> ledgerMap = new HashMap<>();

        // 29 standard chart-of-accounts ledgers from Express seed.js
        Object[][] ledgers = {
            {"1000", "Cash in Hand", "Cash-in-Hand", new BigDecimal("50000.00"), new BigDecimal("150000.00")},
            {"1100", "Bank Account - HDFC", "Bank Accounts", new BigDecimal("500000.00"), new BigDecimal("2300000.00")},
            {"1200", "Accounts Receivable", "Sundry Debtors", BigDecimal.ZERO, new BigDecimal("1820000.00")},
            {"1300", "Inventory", "Stock-in-Hand", new BigDecimal("1200000.00"), new BigDecimal("3545000.00")},
            {"1350", "Fixed Assets - Gross Block", "Fixed Assets", new BigDecimal("65000.00"), new BigDecimal("1250000.00")},
            {"1360", "Accumulated Depreciation", "Fixed Assets", BigDecimal.ZERO, new BigDecimal("-185000.00")},
            {"1400", "Input CGST", "Input Tax Credit", BigDecimal.ZERO, new BigDecimal("145000.00")},
            {"1401", "Input SGST", "Input Tax Credit", BigDecimal.ZERO, new BigDecimal("145000.00")},
            {"1402", "Input IGST", "Input Tax Credit", BigDecimal.ZERO, new BigDecimal("98000.00")},
            {"2100", "Accounts Payable", "Sundry Creditors", BigDecimal.ZERO, new BigDecimal("940000.00")},
            {"2200", "Output CGST Payable", "Duties & Taxes", BigDecimal.ZERO, new BigDecimal("182000.00")},
            {"2201", "Output SGST Payable", "Duties & Taxes", BigDecimal.ZERO, new BigDecimal("182000.00")},
            {"2202", "Output IGST Payable", "Duties & Taxes", BigDecimal.ZERO, new BigDecimal("115000.00")},
            {"2300", "PF Payable", "Provisions", BigDecimal.ZERO, new BigDecimal("42000.00")},
            {"2301", "TDS Payable", "Duties & Taxes", BigDecimal.ZERO, new BigDecimal("28000.00")},
            {"2302", "Salary Payable", "Provisions", BigDecimal.ZERO, new BigDecimal("295000.00")},
            {"3000", "Owner's Capital", "Capital Account", new BigDecimal("5000000.00"), new BigDecimal("5000000.00")},
            {"3900", "Profit and Loss Account", "Profit & Loss Account", BigDecimal.ZERO, new BigDecimal("1604000.00")},
            {"4000", "Sales Revenue", "Sales Accounts", BigDecimal.ZERO, new BigDecimal("8420000.00")},
            {"5000", "Purchases", "Purchase Accounts", BigDecimal.ZERO, new BigDecimal("4850000.00")},
            {"5100", "Salary Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("1480000.00")},
            {"5200", "Rent Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("300000.00")},
            {"5300", "Other Operating Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("280000.00")},
            {"5301", "Utilities Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("165000.00")},
            {"5302", "Telephone & Internet Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("85000.00")},
            {"5303", "Marketing & Advertising Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("240000.00")},
            {"5900", "Depreciation Expense", "Indirect Expenses", BigDecimal.ZERO, new BigDecimal("140000.00")},
            {"6000", "Inter-Unit Stock Transfer - Out", "Indirect Expenses", BigDecimal.ZERO, BigDecimal.ZERO},
            {"6001", "Inter-Unit Stock Transfer - In", "Indirect Expenses", BigDecimal.ZERO, BigDecimal.ZERO}
        };

        for (Object[] row : ledgers) {
            String code = (String) row[0];
            String name = (String) row[1];
            String groupName = (String) row[2];
            BigDecimal opening = (BigDecimal) row[3];
            BigDecimal current = (BigDecimal) row[4];

            AccountGroupEntity grp = groupMap.get(groupName);
            Long groupId = grp != null ? grp.getId() : 1L;

            AccountLedgerEntity entity = ledgerDao.findByTenantIdAndCode(tenantId, code)
                    .orElseGet(() -> ledgerDao.save(AccountLedgerEntity.builder()
                            .tenantId(tenantId)
                            .code(code)
                            .name(name)
                            .groupId(groupId)
                            .openingBalance(opening)
                            .currentBalance(current)
                            .isActive(true)
                            .build()));
            ledgerMap.put(code, entity);
        }

        return ledgerMap;
    }

    private void seedVouchers(String tenantId, Map<String, AccountLedgerEntity> ledgerMap) {
        if (voucherDao.count() > 0) {
            return;
        }

        // Voucher 1: Opening Capital Infusion
        saveVoucher(tenantId, "JV-00001", "JOURNAL", LocalDate.of(2026, 4, 1),
                "Opening capital infusion into business", "REF-CAP-01",
                List.of(
                        new Line("1100", "DEBIT", new BigDecimal("500000.00"), "HDFC Bank opening infusion"),
                        new Line("3000", "CREDIT", new BigDecimal("500000.00"), "Owner's capital equity")
                ), ledgerMap);

        // Voucher 2: Office Rent
        saveVoucher(tenantId, "JV-00002", "PAYMENT", LocalDate.of(2026, 4, 10),
                "Office rent payment for HQ Andheri", "REF-RENT-APR",
                List.of(
                        new Line("5200", "DEBIT", new BigDecimal("25000.00"), "Rent expense"),
                        new Line("1100", "CREDIT", new BigDecimal("25000.00"), "Paid via HDFC Bank")
                ), ledgerMap);

        // Voucher 3: Purchase Invoice PI-00001
        saveVoucher(tenantId, "PI-JV-00001", "JOURNAL", LocalDate.of(2026, 5, 5),
                "Purchase invoice PI-00001 from Om Suppliers", "PI-00001",
                List.of(
                        new Line("5000", "DEBIT", new BigDecimal("113500.00"), "Goods purchase"),
                        new Line("1400", "DEBIT", new BigDecimal("10215.00"), "Input CGST 9%"),
                        new Line("1401", "DEBIT", new BigDecimal("10215.00"), "Input SGST 9%"),
                        new Line("2100", "CREDIT", new BigDecimal("133930.00"), "Accounts Payable - Om Suppliers")
                ), ledgerMap);

        // Voucher 4: Sales Invoice SI-00001
        saveVoucher(tenantId, "SI-JV-00001", "JOURNAL", LocalDate.of(2026, 5, 15),
                "Sales invoice SI-00001 to Sunrise Traders", "SI-00001",
                List.of(
                        new Line("1200", "DEBIT", new BigDecimal("69030.00"), "Accounts Receivable - Sunrise Traders"),
                        new Line("4000", "CREDIT", new BigDecimal("58500.00"), "Sales Revenue"),
                        new Line("2200", "CREDIT", new BigDecimal("5265.00"), "Output CGST 9%"),
                        new Line("2201", "CREDIT", new BigDecimal("5265.00"), "Output SGST 9%")
                ), ledgerMap);

        // Voucher 5: Sales Invoice SI-00002
        saveVoucher(tenantId, "SI-JV-00002", "JOURNAL", LocalDate.of(2026, 5, 28),
                "Sales invoice SI-00002 to Delta Textiles (Inter-state)", "SI-00002",
                List.of(
                        new Line("1200", "DEBIT", new BigDecimal("28000.00"), "Accounts Receivable - Delta Textiles"),
                        new Line("4000", "CREDIT", new BigDecimal("25000.00"), "Sales Revenue"),
                        new Line("2202", "CREDIT", new BigDecimal("3000.00"), "Output IGST 12%")
                ), ledgerMap);

        // Voucher 6: Vendor Payment
        saveVoucher(tenantId, "PMT-2026-001", "PAYMENT", LocalDate.of(2026, 6, 1),
                "Vendor settlement to Om Suppliers", "NEFT-OM-001",
                List.of(
                        new Line("2100", "DEBIT", new BigDecimal("50000.00"), "Vendor settlement"),
                        new Line("1100", "CREDIT", new BigDecimal("50000.00"), "Paid via HDFC Bank")
                ), ledgerMap);

        // Voucher 7: Customer Receipt
        saveVoucher(tenantId, "RCT-2026-001", "RECEIPT", LocalDate.of(2026, 6, 10),
                "Client invoice payment Sunrise Traders via NEFT", "NEFT-SUN-001",
                List.of(
                        new Line("1100", "DEBIT", new BigDecimal("40000.00"), "Received in HDFC Bank"),
                        new Line("1200", "CREDIT", new BigDecimal("40000.00"), "Cleared Sunrise Traders AR")
                ), ledgerMap);

        // Voucher 8: Contra Cash to Bank
        saveVoucher(tenantId, "CTR-2026-001", "CONTRA", LocalDate.of(2026, 6, 15),
                "Cash deposited to HDFC Bank", "CASH-DEP-01",
                List.of(
                        new Line("1100", "DEBIT", new BigDecimal("25000.00"), "Bank deposit"),
                        new Line("1000", "CREDIT", new BigDecimal("25000.00"), "Cash in Hand")
                ), ledgerMap);

        // Voucher 9: Payroll Run
        saveVoucher(tenantId, "PR-2026-06", "JOURNAL", LocalDate.of(2026, 6, 28),
                "Monthly Payroll processed for June 2026 (7 employees)", "PR-JUN-2026",
                List.of(
                        new Line("5100", "DEBIT", new BigDecimal("120000.00"), "Salary gross expense"),
                        new Line("2300", "CREDIT", new BigDecimal("14400.00"), "PF Payable deduction"),
                        new Line("2301", "CREDIT", new BigDecimal("6000.00"), "TDS Payable deduction"),
                        new Line("2302", "CREDIT", new BigDecimal("99600.00"), "Salary net payable")
                ), ledgerMap);

        // Voucher 10: Fixed Asset Acquisition
        saveVoucher(tenantId, "FA-ACQ-00001", "JOURNAL", LocalDate.of(2026, 7, 15),
                "Fixed Asset Acquisition - Office Laptop Dell Latitude FA-001", "INV-DELL-88",
                List.of(
                        new Line("1350", "DEBIT", new BigDecimal("65000.00"), "Dell Latitude Laptop gross cost"),
                        new Line("1100", "CREDIT", new BigDecimal("65000.00"), "Paid via HDFC Bank")
                ), ledgerMap);

        // Voucher 11: Vendor settlement
        saveVoucher(tenantId, "PV-2026-0042", "PAYMENT", LocalDate.of(2026, 10, 21),
                "Vendor settlement - Om Suppliers", "NEFT-OM-0042",
                List.of(
                        new Line("2100", "DEBIT", new BigDecimal("60000.00"), "Vendor payment"),
                        new Line("1100", "CREDIT", new BigDecimal("60000.00"), "Bank transfer")
                ), ledgerMap);

        // Voucher 12: Client receipt
        saveVoucher(tenantId, "RV-2026-0061", "RECEIPT", LocalDate.of(2026, 10, 20),
                "Client invoice payment via NEFT", "NEFT-REC-0061",
                List.of(
                        new Line("1100", "DEBIT", new BigDecimal("80000.00"), "Bank receipt"),
                        new Line("1200", "CREDIT", new BigDecimal("80000.00"), "Receivables cleared")
                ), ledgerMap);

        // Voucher 13: Contra
        saveVoucher(tenantId, "CV-2026-0015", "CONTRA", LocalDate.of(2026, 10, 19),
                "Cash deposited to HDFC Current Account", "DEP-OCT-19",
                List.of(
                        new Line("1100", "DEBIT", new BigDecimal("30000.00"), "Deposit to HDFC"),
                        new Line("1000", "CREDIT", new BigDecimal("30000.00"), "Cash withdrawn from cash box")
                ), ledgerMap);

        // Voucher 14: Month-end depreciation
        saveVoucher(tenantId, "JV-2026-0089", "JOURNAL", LocalDate.of(2026, 10, 22),
                "Month-end depreciation provision", "DEP-OCT-2026",
                List.of(
                        new Line("5900", "DEBIT", new BigDecimal("8500.00"), "Depreciation expense"),
                        new Line("1360", "CREDIT", new BigDecimal("8500.00"), "Accumulated depreciation reserve")
                ), ledgerMap);
    }

    private void saveVoucher(String tenantId, String voucherNo, String voucherType, LocalDate date,
                             String narration, String refNo, List<Line> lines, Map<String, AccountLedgerEntity> ledgerMap) {
        BigDecimal totalAmount = lines.stream()
                .filter(l -> "DEBIT".equalsIgnoreCase(l.entryType))
                .map(l -> l.amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        VoucherEntity voucher = VoucherEntity.builder()
                .tenantId(tenantId)
                .voucherNumber(voucherNo)
                .voucherType(voucherType)
                .voucherDate(date)
                .narration(narration)
                .referenceNo(refNo)
                .totalAmount(totalAmount)
                .items(new ArrayList<>())
                .build();

        for (Line l : lines) {
            AccountLedgerEntity ledger = ledgerMap.get(l.ledgerCode);
            Long ledgerId = ledger != null ? ledger.getId() : 1L;

            VoucherItemEntity item = VoucherItemEntity.builder()
                    .voucher(voucher)
                    .ledgerId(ledgerId)
                    .entryType(l.entryType)
                    .amount(l.amount)
                    .lineNarration(l.narration)
                    .build();
            voucher.getItems().add(item);
        }

        voucherDao.save(voucher);
    }

    private record Line(String ledgerCode, String entryType, BigDecimal amount, String narration) {}
}
