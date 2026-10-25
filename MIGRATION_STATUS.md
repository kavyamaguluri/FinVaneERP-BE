# FinVaneERP Migration Status Tracker

Status Legend:
- `NOT_STARTED`: Pending implementation.
- `IN_PROGRESS`: Work actively underway.
- `IMPLEMENTED`: Ported to Java Spring Boot / Angular.
- `TESTED`: Functionally, visually, and compilation verified against Express reference.
- `BLOCKED`: Blocked by specific documented external factor.

---

## 1. Foundation & Multi-Tenancy
| Feature | Express Reference | Target Component | Status | Notes |
|---------|-------------------|------------------|--------|-------|
| Multi-tenant context | `src/db.js` | `TenantFilter.java`, `TenantContext.java` | **TESTED** | Resolves `X-Entity-ID` & `X-Tenant-ID` |
| Business Switcher | `views/entities/picker.ejs` | `EntitiesComponent`, Topbar | **TESTED** | Switches tenant with zero data leak |
| Entity KYC & Setup | `views/entities/form.ejs` | `EntitiesComponent` | **TESTED** | Fullscreen layout, back button, KYC fields |
| User Authentication | `src/routes/auth.js` | `AuthController`, `LoginComponent` | **TESTED** | JWT + BCrypt + User Session |
| RBAC & Permissions | `src/middleware/rbac.js` | `SecurityConfig`, `JwtUtility` | **TESTED** | Admin vs Operator permissions |
| Audit Trail | `src/routes/admin.js` | `AuditLogController`, `AuditLogComponent` | **TESTED** | Action trace logged & exportable |

---

## 2. Executive Dashboard & Financial Summaries
| Feature | Express Reference | Target Component | Status | Notes |
|---------|-------------------|------------------|--------|-------|
| CFO Executive Narrative | `src/routes/dashboard.js` | `DashboardController`, `DashboardComponent` | **TESTED** | Auto-generated text, exact figures match |
| Overview Bar Chart | `views/dashboard.ejs` | `DashboardComponent` (`spBarData`) | **TESTED** | Sales vs Purchase vs Expenses vs Net |
| Revenue Mix Doughnut | `views/dashboard.ejs` | `DashboardComponent` (`revMixData`) | **TESTED** | Goods vs Service revenue |
| Working Capital Tab | `views/dashboard.ejs` | `DashboardComponent` (WC tab) | **TESTED** | DSO, DPO, Ratios, Cash Runway, Aging |
| Receivables Aging Doughnut | `views/dashboard.ejs` | `DashboardComponent` (`recAgingData`) | **TESTED** | Current, 1-30d, 31-60d, 60+d |
| Payables Aging Doughnut | `views/dashboard.ejs` | `DashboardComponent` (`payAgingData`) | **TESTED** | Current, 1-30d, 31-60d, 60+d |
| Budget Tab on Dashboard | `views/dashboard.ejs` | `DashboardComponent` (Budget tab) | **TESTED** | Empty state alert / Bar chart |
| Payroll & HR Tab | `views/dashboard.ejs` | `DashboardComponent` (HR tab) | **TESTED** | Headcount, MoM line chart, Gender stats |

---

## 3. Masters
| Feature | Express Reference | Target Component | Status | Notes |
|---------|-------------------|------------------|--------|-------|
| Chart of Accounts List | `views/accounts/list.ejs` | `ChartOfAccountsComponent`, `ChartOfAccountsController` | **TESTED** | Grouped by Nature, search & filter |
| COA Mapping | `views/accounts/coa_mapping.ejs`| `CoaMappingComponent` | **TESTED** | Schedule III tagging |
| Sector Mapping | `views/accounts/sector_mapping.ejs`| `SectorMappingComponent` | **TESTED** | Sector-specific P&L & BS lines |
| Locations Master | `views/locations/list.ejs` | `LocationsComponent`, `LocationController` | **TESTED** | Warehouse code, state, address |
| Parties (Customer/Vendor)| `views/parties/list.ejs` | `PartiesComponent`, `PartyController` | **TESTED** | GSTIN, PAN, credit limits, balances |
| Inventory Master | `views/inventory/list.ejs` | `InventoryComponent`, `InventoryController` | **TESTED** | Goods & Services, HSN, tax rates |
| Employees Master | `views/payroll/employees.ejs` | `EmployeesComponent`, `PayrollController` | **TESTED** | Roster, CTC, gender, active/exited |
| New/Edit Employee | `views/payroll/employee_form.ejs`| `EmployeeFormComponent`, `PayrollController` | **TESTED** | Full employee form |

---

## 4. Budgets
| Feature | Express Reference | Target Component | Status | Notes |
|---------|-------------------|------------------|--------|-------|
| Budget vs Actual Report | `views/budgets/list.ejs` | `BudgetListComponent`, `BudgetController` | **TESTED** | Variance computation & Bar chart |
| New Budget Entry | `views/budgets/form.ejs` | `BudgetFormComponent`, `BudgetController` (`/budgets/new`) | **TESTED** | Account select, dates, amount, notes |
| Edit Budget Entry | `views/budgets/form.ejs` | `BudgetFormComponent`, `BudgetController` (`/budgets/:id/edit`) | **TESTED** | Pre-fills and updates entry |
| Plan Budget (Monthly) | `views/budgets/plan.ejs` | `BudgetPlanComponent`, `BudgetController` (`/budgets/plan`) | **TESTED** | 12-month FY grid, totals & save |

---

## 5. Payroll
| Feature | Express Reference | Target Component | Status | Notes |
|---------|-------------------|------------------|--------|-------|
| Payroll Runs List | `views/payroll/runs.ejs` | `PayrollRunsComponent`, `PayrollController` | **TESTED** | Period, status, post state |
| Run Payroll Form | `views/payroll/run_form.ejs` | `PayrollRunFormComponent`, `PayrollController` | **TESTED** | Month, year, draft calculation |
| Payroll Summary Report | `views/payroll/report.ejs` | `PayrollReportComponent`, `PayrollController` | **TESTED** | KPIs, Monthly chart, per-employee |
| EPF / PF Audit Report | `views/reports/epf.ejs` | `EpfReportComponent`, `PayrollController` | **TESTED** | Wages, EE Share (12%), ER Share (3.67%), EPS (8.33%) |

---

## 6. Operations & Reports Pipeline
| Feature | Express Reference | Target Component | Status | Notes |
|---------|-------------------|------------------|--------|-------|
| Sales Invoices | `views/invoices/list.ejs` | `InvoicesComponent`, `InvoiceController` | **TESTED** | Full CRUD, lines, and automatic voucher posting |
| Day Book / Vouchers | `views/vouchers/day_book.ejs` | `DayBookComponent`, `VoucherController` | **TESTED** | Day Book & ledger impact |
| Voucher Entry | `views/vouchers/form.ejs` | `VoucherEntryComponent`, `VoucherController` | **TESTED** | Debit/Credit balance check |
| Banking & Reconcile | `views/banking/accounts.ejs` | `BankingComponent`, `BankingController` | **TESTED** | Bank accounts, statement feed, and reconciliations |
| Fixed Assets Register | `views/assets/dashboard.ejs` | `FixedAssetsComponent`, `FixedAssetController` | **TESTED** | Asset register and automated depreciation runs |
| Stock Transfers | `views/transfers/list.ejs` | `StockTransfersComponent` | **TESTED** | Inter-warehouse transit |
| Trial Balance | `views/reports/trial_balance.ejs`| `TrialBalanceComponent`, `ReportController` | **TESTED** | Balanced debit/credit totals |
| Profit & Loss Statement | `views/reports/profit_loss.ejs`| `ProfitLossComponent`, `ReportController` | **TESTED** | Revenue, COGS, Gross & Net |
| Balance Sheet | `views/reports/balance_sheet.ejs`| `BalanceSheetComponent`, `ReportController` | **TESTED** | Assets vs Liabilities & Equity |
| Cash Flow (Direct/Indirect)| `views/reports/cash_flow.ejs`| `CashFlowComponent`, `ReportController` | **TESTED** | Operating, Investing, Financing |
| Quotations (Sales) | `views/quotations/list.ejs` | `QuotationsComponent`, `QuotationController` | **TESTED** | Full list, statuses, + New Quotation dialog |
| Orders (Sales/Purchase) | `views/orders/list.ejs` | `OrdersComponent`, `OrderController` | **TESTED** | Sales/Purchase switcher, list & + New Order dialog |
| Production & BOMs | `views/production/index.ejs` | `ProductionComponent`, `ProductionController` | **TESTED** | Order runs, BOMs, raw materials, batch complete |
| GSTR-1 Outward Supplies | `views/reports/gstr1.ejs` | `Gstr1Component`, `ComplianceController` | **TESTED** | B2B, B2CL, B2CS, HSN summary, JSON/Excel |
| GSTR-2A/2B Reconcile | `views/compliance/gstr2_reconciliation.ejs` | `Gstr2ReconciliationComponent`, `ComplianceController` | **TESTED** | Upload 2B matching & mismatch detection |
| GSTR-3B Summary Return | `views/reports/gstr3b.ejs` | `Gstr3bComponent`, `ComplianceController` | **TESTED** | Table 3.1 outward, Table 4 eligible ITC |
| Direct Stock Valuation | `views/inventory/direct_stock.ejs` | `DirectStockComponent`, `InventoryController` | **TESTED** | Monthly physical stock entry & valuation |
| Schedule III Financials | `views/reports/financials.ejs` | `FinancialsSchedule3Component`, `ReportController` | **TESTED** | Division I BS, P&L, Notes to Accounts, PDF export |
