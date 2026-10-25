# FinVaneERP Migration Inventory: Express.js to Java Spring Boot & Angular

## 1. Executive Summary
This document provides a comprehensive inventory of all architectural components, routes, database tables, business logic modules, and UI pages from the reference Express.js application (`erp-app-v27`) mapped to the target Java Spring Boot backend (`FinVaneERP-BE`) and Angular frontend (`FinVaneERP-FE`).

## 2. Express Route Modules Inventory (26 Modules)
| # | Express Route File | Base Path | Description | Endpoints Count | Target Spring Boot Controller |
|---|--------------------|-----------|-------------|-----------------|-------------------------------|
| 1 | `entities.js` | `/` | Business KYC & Multi-tenancy | 12 | `EntityController` |
| 2 | `auth.js` | `/auth` | Authentication & Session Mgmt | 8 | `AuthController` |
| 3 | `dashboard.js` | `/dashboard` | Overview, CFO Narrative, WC | 9 | `DashboardController` |
| 4 | `accounts.js` | `/accounts` | COA, Ledgers, Mappings | 18 | `ChartOfAccountsController` |
| 5 | `vouchers.js` | `/vouchers` | Day Book & Journal Vouchers | 14 | `VoucherController` |
| 6 | `ledger.js` | `/reports` | General Ledger, TB, P&L, BS | 22 | `LedgerController`, `ReportController` |
| 7 | `cashflow.js` | `/reports/cash-flow` | Direct & Indirect Cash Flow | 6 | `CashFlowController` |
| 8 | `invoices.js` | `/invoices` | Sales Invoices & E-way / IRN | 24 | `InvoiceController` |
| 9 | `purchaseBills.js` | `/purchase-bills` | Vendor Bills & RCM Payments | 16 | `PurchaseBillController` |
| 10 | `orders.js` | `/orders` | Sales & Purchase Orders | 18 | `OrderController` |
| 11 | `quotations.js` | `/quotations` | Sales Quotations & Estimates | 14 | `QuotationController` |
| 12 | `notes.js` | `/notes` | Credit & Debit Notes | 16 | `NotesController` |
| 13 | `assets.js` | `/assets` | Fixed Assets & Depreciation | 20 | `FixedAssetController` |
| 14 | `budgets.js` | `/budgets` | Budget Planning & Variances | 8 | `BudgetController` |
| 15 | `compliance.js` | `/compliance` | GSTR-1, 2B Reconcile, 3B, EPF | 14 | `ComplianceController` |
| 16 | `attachments.js` | `/attachments` | File Uploads & Document Storage | 6 | `AttachmentController` |
| 17 | `inventory.js` | `/inventory` | Stock Items, Lots, Direct Stock | 22 | `InventoryController` |
| 18 | `production.js` | `/production` | BOMs & Manufacturing Orders | 18 | `ProductionController` |
| 19 | `parties.js` | `/parties` | Customers & Vendors Directory | 14 | `PartyController` |
| 20 | `settlements.js` | `/settlements` | Payment Allocations & Aging | 8 | `SettlementController` |
| 21 | `locations.js` | `/locations` | Multi-warehouse & Branches | 10 | `LocationController` |
| 22 | `transfers.js` | `/transfers` | Inter-location Stock Transfers | 10 | `StockTransferController` |
| 23 | `payroll.js` | `/payroll` | Employees, Runs, Payslips | 18 | `PayrollController` |
| 24 | `banking.js` | `/banking` | Bank Accounts & Reconciliation | 12 | `BankingController` |
| 25 | `admin.js` | `/admin` | User Roles & Audit Logging | 10 | `UserController`, `AuditLogController` |
| 26 | `financialModel.js`| `/financial-model` | DCF, Projections & Valuation | 4 | `FinancialModelController` |

## 3. Database Schema Inventory (38 Tables)
| Table Name | Primary Key | Multi-tenant Key | Description |
|------------|-------------|------------------|-------------|
| `users` | `id` | Control DB / shared | Application operators and passwords |
| `entities` | `id` | Control DB / shared | Business profiles, KYC, PAN, GSTIN |
| `audit_log` | `id` | Control DB / shared | System activity trace and timestamps |
| `account_groups` | `id` | `tenant_id` | Schedule III and operational hierarchies |
| `accounts` | `id` | `tenant_id` | Ledgers (Asset, Liability, Equity, Income, Expense) |
| `vouchers` | `id` | `tenant_id` | Journal vouchers, receipts, payments |
| `voucher_lines` | `id` | `tenant_id` | Debit and Credit ledger balance entries |
| `parties` | `id` | `tenant_id` | Customers, Vendors, and GSTIN profiles |
| `items` | `id` | `tenant_id` | Inventory items, goods, and consulting services |
| `invoices` | `id` | `tenant_id` | Sales invoices, GST tax, IRN & QR codes |
| `invoice_lines` | `id` | `tenant_id` | Invoice itemized lines, HSN, tax breakdowns |
| `employees` | `id` | `tenant_id` | Employee master roster, monthly CTC, bank info |
| `payroll_runs` | `id` | `tenant_id` | Monthly processed payroll cycles |
| `payslips` | `id` | `tenant_id` | Salary slips, Basic, HRA, PF, TDS, Net pay |
| `locations` | `id` | `tenant_id` | Physical warehouses, branch locations |
| `bank_accounts` | `id` | `tenant_id` | Company bank accounts, IFSC, UPI handles |
| `bank_transactions` | `id` | `tenant_id` | Ledger bank entries & reconciliation |
| `budgets` | `id` | `tenant_id` | Monthly and periodical budget projections |
| `stock_transfers` | `id` | `tenant_id` | Stock transfers between warehouse locations |
| `stock_transfer_lines` | `id` | `tenant_id` | Items transferred between locations |
| `purchase_orders` | `id` | `tenant_id` | Vendor purchase orders |
| `purchase_order_lines` | `id` | `tenant_id` | Line items for purchase orders |
| `sales_orders` | `id` | `tenant_id` | Customer sales orders |
| `sales_order_lines` | `id` | `tenant_id` | Line items for sales orders |
| `quotations` | `id` | `tenant_id` | Customer quotation proposals |
| `quotation_lines` | `id` | `tenant_id` | Items on quotation proposals |
| `credit_notes` | `id` | `tenant_id` | Sales return and credit adjustments |
| `credit_note_lines` | `id` | `tenant_id` | Credit note itemized lines |
| `debit_notes` | `id` | `tenant_id` | Vendor purchase return notes |
| `debit_note_lines` | `id` | `tenant_id` | Debit note itemized lines |
| `fixed_assets` | `id` | `tenant_id` | Fixed asset register & asset classes |
| `asset_depreciation_runs` | `id` | `tenant_id` | Monthly/Annual depreciation posted runs |
| `asset_depreciation_lines`| `id` | `tenant_id` | SLM / WDV depreciation calculation lines |
| `boms` | `id` | `tenant_id` | Bill of Materials for manufacturing |
| `bom_lines` | `id` | `tenant_id` | Components required per manufactured unit |
| `production_orders` | `id` | `tenant_id` | Production execution and tracking |
| `production_order_lines` | `id` | `tenant_id` | Production line output reconciliation |
| `payment_allocations` | `id` | `tenant_id` | Bill-by-bill invoice payment matching |

## 4. Express Views Inventory (109 EJS Templates)
Every view file is accounted for and mapped to an Angular component in `UI_PAGE_MAPPING.md`.
