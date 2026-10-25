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
| `GET` | `/reports/cash-flow` | `/api/v2/reports/cash-flow` | `ReportController` | `from`, `to` | `CashFlowDTO` | **TESTED** |
| `GET` | `/reports/financials` | `/api/v2/reports/financials` | `ReportController` | `to` | `FinancialsSchedule3DTO` | **TESTED** |
| `GET` | `/budgets` | `/api/v2/budgets` | `BudgetController` | `from`, `to` | `List<BudgetEntryDTO>` | **TESTED** |
| `POST` | `/budgets/new` | `/api/v2/budgets` | `BudgetController` | `BudgetEntryDTO` | `BudgetEntryDTO` | **TESTED** |
| `GET` | `/budgets/:id` | `/api/v2/budgets/:id` | `BudgetController` | `id` | `BudgetEntryDTO` | **TESTED** |
| `PUT` | `/budgets/:id` | `/api/v2/budgets/:id` | `BudgetController` | `BudgetEntryDTO` | `BudgetEntryDTO` | **TESTED** |
| `DELETE`| `/budgets/:id` | `/api/v2/budgets/:id` | `BudgetController` | `id` | `ApiResponse<Void>` | **TESTED** |
| `GET` | `/budgets/plan` | `/api/v2/budgets/plan` | `BudgetController` | `fy` | `BudgetPlanDTO` | **TESTED** |
| `POST` | `/budgets/plan` | `/api/v2/budgets/plan` | `BudgetController` | `BudgetPlanSaveDTO`| `ApiResponse<Void>` | **TESTED** |
| `GET` | `/payroll/employees`| `/api/v2/payroll/employees` | `PayrollController` | `showExited` | `List<EmployeeDTO>` | **TESTED** |
| `POST` | `/payroll/employees`| `/api/v2/payroll/employees` | `PayrollController` | `EmployeeDTO` | `EmployeeDTO` | **TESTED** |
| `GET` | `/payroll/employees/:id`| `/api/v2/payroll/employees/:id` | `PayrollController` | `id` | `EmployeeDTO` | **TESTED** |
| `PUT` | `/payroll/employees/:id`| `/api/v2/payroll/employees/:id` | `PayrollController` | `EmployeeDTO` | `EmployeeDTO` | **TESTED** |
| `POST` | `/payroll/employees/:id/exit`| `/api/v2/payroll/employees/:id/exit`| `PayrollController` | `exitDate` | `ApiResponse<Void>` | **TESTED** |
| `GET` | `/payroll/runs` | `/api/v2/payroll/runs` | `PayrollController` | None | `List<PayrollRunDTO>` | **TESTED** |
| `POST` | `/payroll/runs` | `/api/v2/payroll/runs` | `PayrollController` | `month`, `year` | `PayrollRunDTO` | **TESTED** |
| `GET` | `/payroll/runs/:id` | `/api/v2/payroll/runs/:id` | `PayrollController` | `id` | `PayrollRunDTO` | **TESTED** |
| `POST` | `/payroll/runs/:id/post`| `/api/v2/payroll/runs/:id/post`| `PayrollController` | `id` | `ApiResponse<Void>` | **TESTED** |
| `GET` | `/payroll/report` | `/api/v2/payroll/report` | `PayrollController` | `from`, `to` | `PayrollReportDTO` | **TESTED** |
| `GET` | `/payroll/epf` | `/api/v2/payroll/epf` | `PayrollController` | `month`, `year` | `EpfReportDTO` | **TESTED** |
| `GET` | `/invoices` | `/api/v2/invoices` | `InvoiceController` | `type` | `List<InvoiceDTO>` | **TESTED** |
| `POST` | `/invoices` | `/api/v2/invoices` | `InvoiceController` | `InvoiceDTO` | `InvoiceDTO` | **TESTED** |
| `GET` | `/invoices/:id` | `/api/v2/invoices/:id` | `InvoiceController` | `id` | `InvoiceDTO` | **TESTED** |
| `DELETE`| `/invoices/:id` | `/api/v2/invoices/:id` | `InvoiceController` | `id` | `ApiResponse<Void>` | **TESTED** |
| `GET` | `/quotations` | `/api/v2/quotations` | `QuotationController` | None | `List<QuotationDTO>` | **TESTED** |
| `POST` | `/quotations` | `/api/v2/quotations` | `QuotationController` | `QuotationDTO` | `QuotationDTO` | **TESTED** |
| `GET` | `/quotations/:id`| `/api/v2/quotations/:id` | `QuotationController` | `id` | `QuotationDTO` | **TESTED** |
| `GET` | `/orders` | `/api/v2/orders` | `OrderController` | `type` | `List<OrderDTO>` | **TESTED** |
| `POST` | `/orders` | `/api/v2/orders` | `OrderController` | `OrderDTO` | `OrderDTO` | **TESTED** |
| `GET` | `/orders/:id` | `/api/v2/orders/:id` | `OrderController` | `id`, `type` | `OrderDTO` | **TESTED** |
| `GET` | `/inventory` | `/api/v2/inventory/items` | `InventoryController` | `itemType` | `List<ItemDTO>` | **TESTED** |
| `POST` | `/inventory` | `/api/v2/inventory/items` | `InventoryController` | `ItemDTO` | `ItemDTO` | **TESTED** |
| `GET` | `/inventory/direct-stock`| `/api/v2/inventory/direct-stock`| `InventoryController` | None | `List<DirectStockDTO>` | **TESTED** |
| `POST` | `/inventory/direct-stock`| `/api/v2/inventory/direct-stock`| `InventoryController` | `DirectStockDTO` | `DirectStockDTO` | **TESTED** |
| `GET` | `/production/boms`| `/api/v2/production/boms` | `ProductionController` | None | `List<BomDTO>` | **TESTED** |
| `POST` | `/production/boms`| `/api/v2/production/boms` | `ProductionController` | `BomDTO` | `BomDTO` | **TESTED** |
| `GET` | `/production/orders`| `/api/v2/production/orders` | `ProductionController` | None | `List<ProductionOrderDTO>` | **TESTED** |
| `POST` | `/production/orders`| `/api/v2/production/orders` | `ProductionController` | `ProductionOrderDTO` | `ProductionOrderDTO` | **TESTED** |
| `POST` | `/production/orders/:id/complete`| `/api/v2/production/orders/:id/complete`| `ProductionController`| `id` | `ApiResponse<Void>` | **TESTED** |
| `GET` | `/compliance/gstr1`| `/api/v2/compliance/gstr1` | `ComplianceController` | `from`, `to` | `Gstr1DTO` | **TESTED** |
| `GET` | `/compliance/gstr2-reconcile`| `/api/v2/compliance/gstr2-reconcile`| `ComplianceController`| None | `Gstr2ReconcileDTO` | **TESTED** |
| `GET` | `/compliance/gstr3b`| `/api/v2/compliance/gstr3b` | `ComplianceController` | `month`, `year` | `Gstr3bDTO` | **TESTED** |
| `GET` | `/banking` | `/api/v2/banking/accounts` | `BankingController` | None | `List<BankAccountEntity>` | **TESTED** |
| `POST` | `/banking/new` | `/api/v2/banking/accounts` | `BankingController` | `BankAccountEntity` | `BankAccountEntity` | **TESTED** |
| `GET` | `/banking/transactions`| `/api/v2/banking/transactions`| `BankingController` | `accountId` | `List<BankTransactionEntity>` | **TESTED** |
| `GET` | `/assets` | `/api/v2/assets` | `FixedAssetController` | None | `List<FixedAssetEntity>` | **TESTED** |
| `POST` | `/assets/new` | `/api/v2/assets` | `FixedAssetController` | `FixedAssetEntity` | `FixedAssetEntity` | **TESTED** |
| `GET` | `/assets/depreciation-runs`| `/api/v2/assets/depreciation-runs`| `FixedAssetController`| None | `List<AssetDepreciationRunEntity>`| **TESTED** |
| `POST` | `/assets/run-depreciation`| `/api/v2/assets/run-depreciation`| `FixedAssetController`| `month`, `year` | `AssetDepreciationRunEntity` | **TESTED** |
