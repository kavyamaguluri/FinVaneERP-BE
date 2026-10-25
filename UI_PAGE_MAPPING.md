# FinVaneERP UI Page & Route Mapping: Express Views to Angular

| # | Express EJS Template | Express URL | Target Angular Component | Angular Route | Status |
|---|----------------------|-------------|--------------------------|---------------|--------|
| 1 | `login.ejs` | `/auth/login` | `LoginComponent` | `/login` | **TESTED** |
| 2 | `entities/picker.ejs` | `/entities` | `EntitiesComponent` | `/entities` | **TESTED** |
| 3 | `entities/form.ejs` | `/entities/new` | `EntitiesComponent` | `/entities/new` | **TESTED** |
| 4 | `entities/form.ejs` | `/entities/:id/edit` | `EntitiesComponent` | `/entities/:id/edit` | **TESTED** |
| 5 | `dashboard.ejs` | `/dashboard` | `DashboardComponent` | `/dashboard` | **TESTED** |
| 6 | `accounts/list.ejs` | `/accounts` | `ChartOfAccountsComponent` | `/accounts` | **TESTED** |
| 7 | `accounts/coa_mapping.ejs` | `/accounts/coa-mapping` | `CoaMappingComponent` | `/accounts/coa-mapping` | **TESTED** |
| 8 | `accounts/sector_mapping.ejs`| `/accounts/sector-mapping` | `SectorMappingComponent` | `/accounts/sector-mapping`| **TESTED** |
| 9 | `locations/list.ejs` | `/locations` | `LocationsComponent` | `/locations` | **TESTED** |
| 10 | `parties/list.ejs` | `/parties` | `PartiesComponent` | `/parties` | **TESTED** |
| 11 | `inventory/list.ejs` | `/inventory` | `InventoryComponent` | `/inventory` | **TESTED** |
| 12 | `payroll/employees.ejs` | `/payroll/employees` | `EmployeesComponent` | `/payroll/employees` | **TESTED** |
| 13 | `payroll/employee_form.ejs` | `/payroll/employees/new` | `EmployeeFormComponent` | `/payroll/employees/new` | **TESTED** |
| 14 | `payroll/employee_form.ejs` | `/payroll/employees/:id/edit` | `EmployeeFormComponent` | `/payroll/employees/:id/edit`| **TESTED** |
| 15 | `payroll/runs.ejs` | `/payroll/runs` | `PayrollRunsComponent` | `/payroll/runs` | **TESTED** |
| 16 | `payroll/run_form.ejs` | `/payroll/runs/new` | `PayrollRunFormComponent` | `/payroll/runs/new` | **TESTED** |
| 17 | `payroll/report.ejs` | `/payroll/report` | `PayrollReportComponent` | `/payroll/report` | **TESTED** |
| 18 | `budgets/list.ejs` | `/budgets` | `BudgetListComponent` | `/budgets` | **TESTED** |
| 19 | `budgets/form.ejs` | `/budgets/new` | `BudgetFormComponent` | `/budgets/new` | **TESTED** |
| 20 | `budgets/form.ejs` | `/budgets/:id/edit` | `BudgetFormComponent` | `/budgets/:id/edit` | **TESTED** |
| 21 | `budgets/plan.ejs` | `/budgets/plan` | `BudgetPlanComponent` | `/budgets/plan` | **TESTED** |
| 22 | `invoices/list.ejs` | `/invoices` | `InvoicesComponent` | `/invoices` | **TESTED** |
| 23 | `notes/list.ejs` | `/notes` | `NotesComponent` | `/notes` | **TESTED** |
| 24 | `vouchers/list.ejs` | `/vouchers` | `DayBookComponent` | `/vouchers` | **TESTED** |
| 25 | `vouchers/form.ejs` | `/vouchers/new` | `VoucherEntryComponent` | `/vouchers/new` | **TESTED** |
| 26 | `transfers/list.ejs` | `/transfers` | `StockTransfersComponent` | `/transfers` | **TESTED** |
| 27 | `banking/accounts.ejs` | `/banking` | `BankingComponent` | `/banking` | **TESTED** |
| 28 | `assets/dashboard.ejs` | `/assets/dashboard` | `FixedAssetsComponent` | `/assets/dashboard` | **TESTED** |
| 29 | `reports/trial_balance.ejs` | `/reports/trial-balance` | `TrialBalanceComponent` | `/reports/trial-balance` | **TESTED** |
| 30 | `reports/balance_sheet.ejs` | `/reports/balance-sheet` | `BalanceSheetComponent` | `/reports/balance-sheet` | **TESTED** |
| 31 | `reports/profit_loss.ejs` | `/reports/profit-loss` | `ProfitLossComponent` | `/reports/profit-loss` | **TESTED** |
| 32 | `reports/cash_flow.ejs` | `/reports/cash-flow` | `CashFlowComponent` | `/reports/cash-flow` | **TESTED** |
| 33 | `financial_model/index.ejs`| `/financial-model` | `FinancialModelComponent` | `/financial-model` | **TESTED** |
| 34 | `admin/users.ejs` | `/admin/users` | `UserManagementComponent` | `/admin/users` | **TESTED** |
| 35 | `admin/audit_log.ejs` | `/admin/audit-log` | `AuditLogComponent` | `/admin/audit-log` | **TESTED** |
| 36 | `quotations/list.ejs` | `/quotations` | `QuotationsComponent` | `/quotations` | **IN_PROGRESS** |
| 37 | `orders/list.ejs` | `/orders` | `OrdersComponent` | `/orders` | **IN_PROGRESS** |
| 38 | `production/index.ejs` | `/production` | `ProductionComponent` | `/production` | **IN_PROGRESS** |
| 39 | `reports/gstr1.ejs` | `/reports/gstr1` | `Gstr1Component` | `/reports/gstr1` | **IN_PROGRESS** |
| 40 | `reports/gstr3b.ejs` | `/reports/gstr3b` | `Gstr3bComponent` | `/reports/gstr3b` | **IN_PROGRESS** |
| 41 | `reports/epf.ejs` | `/reports/epf` | `EpfComponent` | `/reports/epf` | **IN_PROGRESS** |
