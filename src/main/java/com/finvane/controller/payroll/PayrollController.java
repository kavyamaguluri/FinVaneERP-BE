package com.finvane.controller.payroll;

import com.finvane.dao.accounts.IAccountLedgerJpaAccess;
import com.finvane.dao.accounts.IVoucherItemJpaAccess;
import com.finvane.dao.accounts.IVoucherJpaAccess;
import com.finvane.dao.payroll.IEmployeeJpaAccess;
import com.finvane.dao.payroll.IPayrollRunJpaAccess;
import com.finvane.dao.payroll.IPayslipJpaAccess;
import com.finvane.entity.accounts.AccountLedgerEntity;
import com.finvane.entity.accounts.VoucherEntity;
import com.finvane.entity.accounts.VoucherItemEntity;
import com.finvane.entity.payroll.EmployeeEntity;
import com.finvane.entity.payroll.PayrollRunEntity;
import com.finvane.entity.payroll.PayslipEntity;
import com.finvane.model.common.ApiResponse;
import com.finvane.model.common.TenantContext;
import com.finvane.model.payroll.*;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v2/payroll")
@RequiredArgsConstructor
public class PayrollController {

    private final IEmployeeJpaAccess employeeDao;
    private final IPayrollRunJpaAccess payrollRunDao;
    private final IPayslipJpaAccess payslipDao;
    private final IVoucherJpaAccess voucherDao;
    private final IVoucherItemJpaAccess voucherItemDao;
    private final IAccountLedgerJpaAccess ledgerDao;

    // --- EMPLOYEES ---

    @GetMapping("/employees")
    public ApiResponse<List<EmployeeDTO>> getEmployees(@RequestParam(required = false, defaultValue = "false") boolean showExited) {
        String tenantId = TenantContext.getTenantId();
        List<EmployeeEntity> list = showExited
                ? employeeDao.findByTenantIdOrderByNameAsc(tenantId)
                : employeeDao.findByTenantIdAndIsActiveOrderByNameAsc(tenantId, 1L);

        List<EmployeeDTO> dtos = list.stream().map(this::toEmployeeDTO).toList();
        return ApiResponse.ok(dtos);
    }

    @PostMapping("/employees")
    public ApiResponse<EmployeeDTO> createEmployee(@RequestBody EmployeeDTO dto) {
        String tenantId = TenantContext.getTenantId();
        EmployeeEntity entity = EmployeeEntity.builder()
                .tenantId(tenantId)
                .name(dto.getName())
                .email(dto.getEmail())
                .designation(dto.getDesignation())
                .gender(dto.getGender())
                .panEnc(dto.getPan())
                .bankAccountEnc(dto.getBankAccount())
                .ifsc(dto.getIfsc())
                .monthlyCtc(dto.getMonthlyCtc() != null ? dto.getMonthlyCtc() : BigDecimal.ZERO)
                .dateOfJoining(dto.getDateOfJoining())
                .isActive(1L)
                .build();
        EmployeeEntity saved = employeeDao.save(entity);
        return ApiResponse.ok("Employee created successfully", toEmployeeDTO(saved));
    }

    @GetMapping("/employees/{id}")
    public ApiResponse<EmployeeDTO> getEmployeeById(@PathVariable Long id) {
        EmployeeEntity entity = employeeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        return ApiResponse.ok(toEmployeeDTO(entity));
    }

    @PutMapping("/employees/{id}")
    public ApiResponse<EmployeeDTO> updateEmployee(@PathVariable Long id, @RequestBody EmployeeDTO dto) {
        EmployeeEntity entity = employeeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));

        entity.setName(dto.getName());
        entity.setEmail(dto.getEmail());
        entity.setDesignation(dto.getDesignation());
        entity.setGender(dto.getGender());
        if (dto.getPan() != null && !dto.getPan().isBlank()) entity.setPanEnc(dto.getPan());
        if (dto.getBankAccount() != null && !dto.getBankAccount().isBlank()) entity.setBankAccountEnc(dto.getBankAccount());
        entity.setIfsc(dto.getIfsc());
        if (dto.getMonthlyCtc() != null) entity.setMonthlyCtc(dto.getMonthlyCtc());
        if (dto.getDateOfJoining() != null) entity.setDateOfJoining(dto.getDateOfJoining());
        if (dto.getDateOfExit() != null) entity.setDateOfExit(dto.getDateOfExit());
        if (dto.getIsActive() != null) entity.setIsActive(dto.getIsActive());

        EmployeeEntity updated = employeeDao.save(entity);
        return ApiResponse.ok("Employee updated successfully", toEmployeeDTO(updated));
    }

    @PostMapping("/employees/{id}/exit")
    public ApiResponse<Void> markEmployeeExited(@PathVariable Long id, @RequestParam(required = false) String exitDate) {
        EmployeeEntity entity = employeeDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found with ID: " + id));
        entity.setIsActive(0L);
        entity.setDateOfExit(exitDate != null && !exitDate.isBlank() ? exitDate : LocalDate.now().toString());
        employeeDao.save(entity);
        return ApiResponse.ok("Employee marked as exited", null);
    }

    @DeleteMapping("/employees/{id}")
    public ApiResponse<Void> deleteEmployee(@PathVariable Long id) {
        employeeDao.deleteById(id);
        return ApiResponse.ok("Employee deleted", null);
    }

    // --- PAYROLL RUNS ---

    @GetMapping("/runs")
    public ApiResponse<List<PayrollRunDTO>> getPayrollRuns() {
        String tenantId = TenantContext.getTenantId();
        List<PayrollRunEntity> runs = payrollRunDao.findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(tenantId);
        List<PayslipEntity> allPayslips = payslipDao.findByTenantId(tenantId);
        Map<Long, List<PayslipEntity>> payslipMap = allPayslips.stream()
                .collect(Collectors.groupingBy(PayslipEntity::getPayrollRunId));

        List<PayrollRunDTO> result = new ArrayList<>();
        for (PayrollRunEntity r : runs) {
            List<PayslipEntity> slips = payslipMap.getOrDefault(r.getId(), Collections.emptyList());
            BigDecimal gross = slips.stream().map(s -> s.getGross() != null ? s.getGross() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pf = slips.stream().map(s -> s.getPf() != null ? s.getPf() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tds = slips.stream().map(s -> s.getTds() != null ? s.getTds() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net = slips.stream().map(s -> s.getNetPay() != null ? s.getNetPay() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

            result.add(PayrollRunDTO.builder()
                    .id(r.getId())
                    .periodMonth(r.getPeriodMonth())
                    .periodYear(r.getPeriodYear())
                    .status(r.getStatus())
                    .voucherId(r.getVoucherId())
                    .totalGross(gross)
                    .totalPf(pf)
                    .totalTds(tds)
                    .totalNet(net)
                    .employeeCount(slips.size())
                    .build());
        }
        return ApiResponse.ok(result);
    }

    @PostMapping("/runs")
    @Transactional
    public ApiResponse<PayrollRunDTO> createPayrollRun(@RequestParam int month, @RequestParam int year) {
        String tenantId = TenantContext.getTenantId();

        Optional<PayrollRunEntity> existing = payrollRunDao.findByTenantIdAndPeriodMonthAndPeriodYear(tenantId, month, year);
        if (existing.isPresent()) {
            throw new IllegalArgumentException("A payroll run for " + month + "/" + year + " already exists.");
        }

        List<EmployeeEntity> employees = employeeDao.findByTenantIdAndIsActiveOrderByNameAsc(tenantId, 1L);
        if (employees.isEmpty()) {
            throw new IllegalArgumentException("No active employees found to run payroll.");
        }

        PayrollRunEntity run = PayrollRunEntity.builder()
                .tenantId(tenantId)
                .periodMonth(month)
                .periodYear(year)
                .status("draft")
                .build();
        PayrollRunEntity savedRun = payrollRunDao.save(run);

        List<PayslipDTO> slips = new ArrayList<>();
        BigDecimal totalGross = BigDecimal.ZERO;
        BigDecimal totalPf = BigDecimal.ZERO;
        BigDecimal totalTds = BigDecimal.ZERO;
        BigDecimal totalNet = BigDecimal.ZERO;

        for (EmployeeEntity emp : employees) {
            BigDecimal ctc = emp.getMonthlyCtc() != null ? emp.getMonthlyCtc() : BigDecimal.ZERO;
            BigDecimal basic = ctc.multiply(BigDecimal.valueOf(0.5)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal hra = ctc.multiply(BigDecimal.valueOf(0.2)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal allowances = ctc.subtract(basic).subtract(hra).setScale(2, RoundingMode.HALF_UP);
            BigDecimal gross = basic.add(hra).add(allowances).setScale(2, RoundingMode.HALF_UP);
            BigDecimal pf = basic.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal tds = computeMonthlyTds(gross);
            BigDecimal netPay = gross.subtract(pf).subtract(tds).setScale(2, RoundingMode.HALF_UP);

            PayslipEntity slip = PayslipEntity.builder()
                    .tenantId(tenantId)
                    .payrollRunId(savedRun.getId())
                    .employeeId(emp.getId())
                    .basic(basic)
                    .hra(hra)
                    .allowances(allowances)
                    .gross(gross)
                    .pf(pf)
                    .tds(tds)
                    .netPay(netPay)
                    .build();
            payslipDao.save(slip);

            totalGross = totalGross.add(gross);
            totalPf = totalPf.add(pf);
            totalTds = totalTds.add(tds);
            totalNet = totalNet.add(netPay);

            slips.add(PayslipDTO.builder()
                    .payrollRunId(savedRun.getId())
                    .employeeId(emp.getId())
                    .employeeName(emp.getName())
                    .designation(emp.getDesignation())
                    .basic(basic)
                    .hra(hra)
                    .allowances(allowances)
                    .gross(gross)
                    .pf(pf)
                    .tds(tds)
                    .netPay(netPay)
                    .build());
        }

        PayrollRunDTO result = PayrollRunDTO.builder()
                .id(savedRun.getId())
                .periodMonth(month)
                .periodYear(year)
                .status("draft")
                .totalGross(totalGross)
                .totalPf(totalPf)
                .totalTds(totalTds)
                .totalNet(totalNet)
                .employeeCount(employees.size())
                .payslips(slips)
                .build();

        return ApiResponse.ok("Payroll draft run calculated successfully", result);
    }

    @GetMapping("/runs/{id}")
    public ApiResponse<PayrollRunDTO> getPayrollRunById(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        PayrollRunEntity run = payrollRunDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found with ID: " + id));

        List<PayslipEntity> slips = payslipDao.findByTenantIdAndPayrollRunId(tenantId, id);
        Map<Long, EmployeeEntity> empMap = employeeDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(EmployeeEntity::getId, e -> e, (a, b) -> a));

        List<PayslipDTO> slipDTOs = slips.stream().map(s -> {
            EmployeeEntity emp = empMap.get(s.getEmployeeId());
            return PayslipDTO.builder()
                    .id(s.getId())
                    .payrollRunId(s.getPayrollRunId())
                    .employeeId(s.getEmployeeId())
                    .employeeName(emp != null ? emp.getName() : "Employee #" + s.getEmployeeId())
                    .designation(emp != null ? emp.getDesignation() : "")
                    .basic(s.getBasic())
                    .hra(s.getHra())
                    .allowances(s.getAllowances())
                    .gross(s.getGross())
                    .pf(s.getPf())
                    .tds(s.getTds())
                    .netPay(s.getNetPay())
                    .build();
        }).toList();

        BigDecimal gross = slips.stream().map(s -> s.getGross() != null ? s.getGross() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal pf = slips.stream().map(s -> s.getPf() != null ? s.getPf() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal tds = slips.stream().map(s -> s.getTds() != null ? s.getTds() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal net = slips.stream().map(s -> s.getNetPay() != null ? s.getNetPay() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        PayrollRunDTO dto = PayrollRunDTO.builder()
                .id(run.getId())
                .periodMonth(run.getPeriodMonth())
                .periodYear(run.getPeriodYear())
                .status(run.getStatus())
                .voucherId(run.getVoucherId())
                .totalGross(gross)
                .totalPf(pf)
                .totalTds(tds)
                .totalNet(net)
                .employeeCount(slips.size())
                .payslips(slipDTOs)
                .build();

        return ApiResponse.ok(dto);
    }

    @PostMapping("/runs/{id}/post")
    @Transactional
    public ApiResponse<Void> postPayrollRun(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        PayrollRunEntity run = payrollRunDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found with ID: " + id));

        if ("processed".equalsIgnoreCase(run.getStatus())) {
            return ApiResponse.ok("Payroll run already processed", null);
        }

        List<PayslipEntity> slips = payslipDao.findByTenantIdAndPayrollRunId(tenantId, id);
        if (slips.isEmpty()) {
            throw new IllegalArgumentException("This payroll run has no payslips to post.");
        }

        BigDecimal totalGross = slips.stream().map(s -> s.getGross() != null ? s.getGross() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalPf = slips.stream().map(s -> s.getPf() != null ? s.getPf() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalTds = slips.stream().map(s -> s.getTds() != null ? s.getTds() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
        BigDecimal totalNet = slips.stream().map(s -> s.getNetPay() != null ? s.getNetPay() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

        // Find ledger accounts: 5100 (Salary Expense), 2300 (PF Payable), 2301 (TDS Payable), 2302 (Salary Payable)
        AccountLedgerEntity salaryExpense = getLedgerByCode(tenantId, "5100");
        AccountLedgerEntity pfPayable = getLedgerByCode(tenantId, "2300");
        AccountLedgerEntity tdsPayable = getLedgerByCode(tenantId, "2301");
        AccountLedgerEntity salaryPayable = getLedgerByCode(tenantId, "2302");

        String voucherNo = String.format("PR-%04d%02d", run.getPeriodYear(), run.getPeriodMonth());
        VoucherEntity v = VoucherEntity.builder()
                .tenantId(tenantId)
                .voucherNumber(voucherNo)
                .voucherDate(LocalDate.now())
                .voucherType("PAYROLL")
                .narration(String.format("Payroll for %02d/%04d", run.getPeriodMonth(), run.getPeriodYear()))
                .totalAmount(totalGross)
                .build();
        VoucherEntity savedV = voucherDao.save(v);

        List<VoucherItemEntity> items = new ArrayList<>();
        if (salaryExpense != null) {
            items.add(VoucherItemEntity.builder().voucher(savedV).ledgerId(salaryExpense.getId()).entryType("DEBIT").amount(totalGross).lineNarration("Gross Salaries").build());
        }
        if (pfPayable != null && totalPf.compareTo(BigDecimal.ZERO) > 0) {
            items.add(VoucherItemEntity.builder().voucher(savedV).ledgerId(pfPayable.getId()).entryType("CREDIT").amount(totalPf).lineNarration("PF Deduction").build());
        }
        if (tdsPayable != null && totalTds.compareTo(BigDecimal.ZERO) > 0) {
            items.add(VoucherItemEntity.builder().voucher(savedV).ledgerId(tdsPayable.getId()).entryType("CREDIT").amount(totalTds).lineNarration("TDS Deduction").build());
        }
        if (salaryPayable != null) {
            items.add(VoucherItemEntity.builder().voucher(savedV).ledgerId(salaryPayable.getId()).entryType("CREDIT").amount(totalNet).lineNarration("Net Salary Payable").build());
        }
        voucherItemDao.saveAll(items);

        run.setVoucherId(savedV.getId());
        run.setStatus("processed");
        payrollRunDao.save(run);

        return ApiResponse.ok("Payroll posted to accounts successfully", null);
    }

    @DeleteMapping("/runs/{id}")
    @Transactional
    public ApiResponse<Void> deletePayrollRun(@PathVariable Long id) {
        String tenantId = TenantContext.getTenantId();
        PayrollRunEntity run = payrollRunDao.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Payroll run not found with ID: " + id));

        if (run.getVoucherId() != null) {
            voucherDao.deleteById(run.getVoucherId());
        }
        payslipDao.deleteByTenantIdAndPayrollRunId(tenantId, id);
        payrollRunDao.deleteById(id);
        return ApiResponse.ok("Payroll run deleted successfully", null);
    }

    // --- PAYROLL REPORT ---

    @GetMapping("/report")
    public ApiResponse<PayrollReportDTO> getPayrollReport(
            @RequestParam(required = false) String from,
            @RequestParam(required = false) String to) {
        String tenantId = TenantContext.getTenantId();
        List<PayrollRunEntity> runs = payrollRunDao.findByTenantIdOrderByPeriodYearDescPeriodMonthDesc(tenantId)
                .stream().filter(r -> "processed".equalsIgnoreCase(r.getStatus())).toList();

        List<PayslipEntity> allSlips = payslipDao.findByTenantId(tenantId);
        Map<Long, List<PayslipEntity>> slipsByRun = allSlips.stream().collect(Collectors.groupingBy(PayslipEntity::getPayrollRunId));
        Map<Long, List<PayslipEntity>> slipsByEmp = allSlips.stream().collect(Collectors.groupingBy(PayslipEntity::getEmployeeId));
        Map<Long, EmployeeEntity> empMap = employeeDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(EmployeeEntity::getId, e -> e, (a, b) -> a));

        List<PayrollReportDTO.MonthlyPayrollSummary> monthly = new ArrayList<>();
        long maxHeadcount = 0;
        BigDecimal grandGross = BigDecimal.ZERO;
        BigDecimal grandPf = BigDecimal.ZERO;
        BigDecimal grandTds = BigDecimal.ZERO;
        BigDecimal grandNet = BigDecimal.ZERO;

        for (PayrollRunEntity r : runs) {
            List<PayslipEntity> slips = slipsByRun.getOrDefault(r.getId(), Collections.emptyList());
            long count = slips.size();
            maxHeadcount = Math.max(maxHeadcount, count);

            BigDecimal gross = slips.stream().map(s -> s.getGross() != null ? s.getGross() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pf = slips.stream().map(s -> s.getPf() != null ? s.getPf() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tds = slips.stream().map(s -> s.getTds() != null ? s.getTds() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net = slips.stream().map(s -> s.getNetPay() != null ? s.getNetPay() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

            grandGross = grandGross.add(gross);
            grandPf = grandPf.add(pf);
            grandTds = grandTds.add(tds);
            grandNet = grandNet.add(net);

            monthly.add(PayrollReportDTO.MonthlyPayrollSummary.builder()
                    .periodMonth(r.getPeriodMonth())
                    .periodYear(r.getPeriodYear())
                    .status(r.getStatus())
                    .headcount(count)
                    .totalGross(gross)
                    .totalPf(pf)
                    .totalTds(tds)
                    .totalNet(net)
                    .build());
        }

        List<PayrollReportDTO.EmployeePayrollSummary> byEmp = new ArrayList<>();
        for (Map.Entry<Long, List<PayslipEntity>> entry : slipsByEmp.entrySet()) {
            EmployeeEntity emp = empMap.get(entry.getKey());
            List<PayslipEntity> slips = entry.getValue();

            BigDecimal gross = slips.stream().map(s -> s.getGross() != null ? s.getGross() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal pf = slips.stream().map(s -> s.getPf() != null ? s.getPf() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal tds = slips.stream().map(s -> s.getTds() != null ? s.getTds() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);
            BigDecimal net = slips.stream().map(s -> s.getNetPay() != null ? s.getNetPay() : BigDecimal.ZERO).reduce(BigDecimal.ZERO, BigDecimal::add);

            byEmp.add(PayrollReportDTO.EmployeePayrollSummary.builder()
                    .id(entry.getKey())
                    .name(emp != null ? emp.getName() : "Employee #" + entry.getKey())
                    .designation(emp != null ? emp.getDesignation() : "")
                    .gender(emp != null ? emp.getGender() : "")
                    .monthsPaid((long) slips.size())
                    .totalGross(gross)
                    .totalPf(pf)
                    .totalTds(tds)
                    .totalNet(net)
                    .build());
        }
        byEmp.sort((a, b) -> b.getTotalGross().compareTo(a.getTotalGross()));

        PayrollReportDTO dto = PayrollReportDTO.builder()
                .monthly(monthly)
                .byEmployee(byEmp)
                .grandTotal(PayrollReportDTO.GrandPayrollSummary.builder()
                        .headcount(maxHeadcount)
                        .totalGross(grandGross)
                        .totalPf(grandPf)
                        .totalTds(grandTds)
                        .totalNet(grandNet)
                        .build())
                .build();

        return ApiResponse.ok(dto);
    }

    // --- EPF REPORT ---

    @GetMapping("/epf")
    public ApiResponse<EpfReportDTO> getEpfReport(@RequestParam(required = false) Integer month, @RequestParam(required = false) Integer year) {
        String tenantId = TenantContext.getTenantId();
        if (month == null || year == null) {
            LocalDate now = LocalDate.now();
            month = now.getMonthValue();
            year = now.getYear();
        }

        Optional<PayrollRunEntity> runOpt = payrollRunDao.findByTenantIdAndPeriodMonthAndPeriodYear(tenantId, month, year);
        List<PayslipEntity> slips = runOpt.isPresent()
                ? payslipDao.findByTenantIdAndPayrollRunId(tenantId, runOpt.get().getId())
                : Collections.emptyList();

        Map<Long, EmployeeEntity> empMap = employeeDao.findByTenantIdOrderByNameAsc(tenantId).stream()
                .collect(Collectors.toMap(EmployeeEntity::getId, e -> e, (a, b) -> a));

        List<EpfReportDTO.EpfEmployeeRow> rows = new ArrayList<>();
        BigDecimal totalWages = BigDecimal.ZERO;
        BigDecimal totalEe = BigDecimal.ZERO;
        BigDecimal totalEr = BigDecimal.ZERO;
        BigDecimal totalEps = BigDecimal.ZERO;

        for (PayslipEntity s : slips) {
            EmployeeEntity emp = empMap.get(s.getEmployeeId());
            BigDecimal basic = s.getBasic() != null ? s.getBasic() : BigDecimal.ZERO;
            BigDecimal gross = s.getGross() != null ? s.getGross() : BigDecimal.ZERO;
            BigDecimal ee = s.getPf() != null ? s.getPf() : basic.multiply(BigDecimal.valueOf(0.12)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal er = basic.multiply(BigDecimal.valueOf(0.0367)).setScale(2, RoundingMode.HALF_UP);
            BigDecimal eps = basic.multiply(BigDecimal.valueOf(0.0833)).setScale(2, RoundingMode.HALF_UP);

            totalWages = totalWages.add(basic);
            totalEe = totalEe.add(ee);
            totalEr = totalEr.add(er);
            totalEps = totalEps.add(eps);

            rows.add(EpfReportDTO.EpfEmployeeRow.builder()
                    .employeeId(s.getEmployeeId())
                    .employeeName(emp != null ? emp.getName() : "Employee #" + s.getEmployeeId())
                    .uan(emp != null && emp.getPanEnc() != null ? emp.getPanEnc() : "UAN" + s.getEmployeeId())
                    .grossWages(gross)
                    .epfWages(basic)
                    .epsWages(basic)
                    .edliWages(basic)
                    .eeShare(ee)
                    .erShare(er)
                    .epsShare(eps)
                    .build());
        }

        EpfReportDTO dto = EpfReportDTO.builder()
                .monthYear(String.format("%02d/%04d", month, year))
                .employeeCount((long) rows.size())
                .totalWages(totalWages)
                .totalEeShare(totalEe)
                .totalErShare(totalEr)
                .totalEpsShare(totalEps)
                .totalEpfRemittance(totalEe.add(totalEr).add(totalEps))
                .rows(rows)
                .build();

        return ApiResponse.ok(dto);
    }

    // --- Helpers ---

    private EmployeeDTO toEmployeeDTO(EmployeeEntity e) {
        return EmployeeDTO.builder()
                .id(e.getId())
                .name(e.getName())
                .email(e.getEmail())
                .designation(e.getDesignation())
                .gender(e.getGender())
                .pan(e.getPanEnc())
                .bankAccount(e.getBankAccountEnc())
                .bankMasked(e.getBankAccountEnc() != null ? "On file (encrypted)" : "-")
                .ifsc(e.getIfsc())
                .monthlyCtc(e.getMonthlyCtc())
                .dateOfJoining(e.getDateOfJoining())
                .dateOfExit(e.getDateOfExit())
                .isActive(e.getIsActive())
                .build();
    }

    private BigDecimal computeMonthlyTds(BigDecimal monthlyGross) {
        BigDecimal annual = monthlyGross.multiply(BigDecimal.valueOf(12));
        if (annual.compareTo(BigDecimal.valueOf(700000)) <= 0) {
            return BigDecimal.ZERO;
        }
        BigDecimal tax = BigDecimal.ZERO;
        BigDecimal rem = annual;
        if (rem.compareTo(BigDecimal.valueOf(1500000)) > 0) {
            tax = tax.add(rem.subtract(BigDecimal.valueOf(1500000)).multiply(BigDecimal.valueOf(0.30)));
            rem = BigDecimal.valueOf(1500000);
        }
        if (rem.compareTo(BigDecimal.valueOf(1200000)) > 0) {
            tax = tax.add(rem.subtract(BigDecimal.valueOf(1200000)).multiply(BigDecimal.valueOf(0.20)));
            rem = BigDecimal.valueOf(1200000);
        }
        if (rem.compareTo(BigDecimal.valueOf(1000000)) > 0) {
            tax = tax.add(rem.subtract(BigDecimal.valueOf(1000000)).multiply(BigDecimal.valueOf(0.15)));
            rem = BigDecimal.valueOf(1000000);
        }
        if (rem.compareTo(BigDecimal.valueOf(700000)) > 0) {
            tax = tax.add(rem.subtract(BigDecimal.valueOf(700000)).multiply(BigDecimal.valueOf(0.10)));
            rem = BigDecimal.valueOf(700000);
        }
        if (rem.compareTo(BigDecimal.valueOf(300000)) > 0) {
            tax = tax.add(rem.subtract(BigDecimal.valueOf(300000)).multiply(BigDecimal.valueOf(0.05)));
        }
        return tax.divide(BigDecimal.valueOf(12), 2, RoundingMode.HALF_UP);
    }

    private AccountLedgerEntity getLedgerByCode(String tenantId, String code) {
        return ledgerDao.findByTenantIdAndIsActiveTrueOrderByNameAsc(tenantId).stream()
                .filter(l -> code.equals(l.getCode()))
                .findFirst()
                .orElse(null);
    }
}
