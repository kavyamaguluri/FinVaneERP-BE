# FinVaneERP API Endpoint Mapping: Express to Spring Boot

| Method | Express Route | Spring Boot Endpoint | Controller | Request Body / Query | Response Model | Status |
|--------|---------------|----------------------|------------|----------------------|----------------|--------|
| `GET` | `/dashboard` | `/api/v2/dashboard/metrics` | `DashboardController` | `X-Entity-ID` | `DashboardMetricsDTO` | **TESTED** |
| `GET` | `/entities` | `/api/v2/entities` | `EntityController` | None | `List<EntityEntity>` | **TESTED** |
| `POST` | `/entities/new` | `/api/v2/entities` | `EntityController` | `EntityEntity` | `EntityEntity` | **TESTED** |
| `GET` | `/entities/:id/edit` | `/api/v2/entities/:id` | `EntityController` | `id` | `EntityEntity` | **TESTED** |
| `POST` | `/entities/:id/edit` | `/api/v2/entities/:id` | `EntityController` | `EntityEntity` | `EntityEntity` | **TESTED** |
| `POST` | `/entities/select` | `/api/v2/entities/switch/:id` | `EntityController` | `id` | `EntityEntity` | **TESTED** |
| `POST` | `/auth/login` | `/api/v2/auth/login` | `AuthController` | `LoginRequestDTO` | `AuthResponseDTO` | **TESTED** |
| `GET` | `/accounts` | `/api/v2/accounts/ledgers` | `ChartOfAccountsController` | `search`, `page`, `size` | `PageResponse<AccountLedgerDTO>` | **TESTED** |
| `GET` | `/accounts/groups`| `/api/v2/accounts/groups` | `ChartOfAccountsController` | None | `List<AccountGroupEntity>` | **TESTED** |
| `GET` | `/vouchers` | `/api/v2/vouchers` | `VoucherController` | `page`, `size`, `search` | `PageResponse<VoucherDTO>` | **TESTED** |
| `POST` | `/vouchers/new` | `/api/v2/vouchers` | `VoucherController` | `VoucherDTO` | `VoucherDTO` | **TESTED** |
| `GET` | `/locations` | `/api/v2/locations` | `LocationController` | None | `List<LocationEntity>` | **TESTED** |
| `GET` | `/parties` | `/api/v2/parties` | `PartyController` | `type` | `List<PartyEntity>` | **TESTED** |
| `GET` | `/admin/users` | `/api/v2/admin/users` | `UserController` | None | `List<UserSummaryDTO>` | **TESTED** |
| `GET` | `/admin/audit-log` | `/api/v2/admin/audit-logs` | `AuditLogController` | `page`, `size` | `PageResponse<AuditLogEntity>` | **TESTED** |
| `GET` | `/reports/trial-balance` | `/api/v2/reports/trial-balance` | `ReportController` | `from`, `to` | `TrialBalanceDTO` | **TESTED** |
| `GET` | `/reports/profit-loss` | `/api/v2/reports/profit-loss` | `ReportController` | `from`, `to` | `ProfitLossDTO` | **TESTED** |
| `GET` | `/reports/balance-sheet`| `/api/v2/reports/balance-sheet`| `ReportController` | `to` | `BalanceSheetDTO` | **TESTED** |
| `GET` | `/reports/cash-flow` | `/api/v2/reports/cash-flow` | `CashFlowController` | `from`, `to` | `CashFlowDTO` | **TESTED** |
| `GET` | `/budgets` | `/api/v2/budgets` | `BudgetController` | `from`, `to` | `List<BudgetEntryDTO>` | **TESTED** |
| `POST` | `/budgets/new` | `/api/v2/budgets` | `BudgetController` | `BudgetEntryDTO` | `BudgetEntryDTO` | **TESTED** |
| `POST` | `/budgets/:id/edit` | `/api/v2/budgets/:id` | `BudgetController` | `BudgetEntryDTO` | `BudgetEntryDTO` | **TESTED** |
| `POST` | `/budgets/:id/delete` | `/api/v2/budgets/:id` | `BudgetController` | `id` | `ApiResponse<Void>` | **TESTED** |
| `GET` | `/payroll/employees`| `/api/v2/payroll/employees` | `PayrollController` | `showExited` | `List<EmployeeDTO>` | **TESTED** |
| `GET` | `/payroll/runs` | `/api/v2/payroll/runs` | `PayrollController` | None | `List<PayrollRunDTO>` | **TESTED** |
| `GET` | `/payroll/report` | `/api/v2/payroll/report` | `PayrollController` | `from`, `to` | `PayrollReportDTO` | **TESTED** |
