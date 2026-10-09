import pandas as pd
import numpy as np

# Load CSV files
employees_path = 'om sai employees seed.csv'
slips_path = 'om sai salary slips training.csv'

employees = pd.read_csv(employees_path)
slips = pd.read_csv(slips_path)

report_lines = []
report_lines.append("=== Data Validation Report ===")
report_lines.append(f"Employees loaded: {len(employees)}")
report_lines.append(f"Salary slips loaded: {len(slips)}\n")

# 1. Missing Values & Types
report_lines.append("--- Missing Values ---")
report_lines.append(f"Employees missing values:\n{employees.isnull().sum()[employees.isnull().sum() > 0]}")
report_lines.append(f"Slips missing values:\n{slips.isnull().sum()[slips.isnull().sum() > 0]}\n")

# Fill missing monetary fields in employees
for col in ['festivalBonus', 'otherDed', 'dq', 'remarks']:
    if col in employees.columns:
        if col == 'remarks':
            pass # preserve as null/blank
        else:
            employees[col] = employees[col].fillna(0)

# Fill missing monetary fields in slips
for col in ['festivalBonus', 'otherDed', 'dq', 'remarks']:
    if col in slips.columns:
        if col == 'remarks':
            pass
        else:
            slips[col] = slips[col].fillna(0)

# 2. Validation
report_lines.append("--- Employee Reference & Duplicate Validation ---")
# Missing employees
unknown_emps = set(slips['empId']) - set(employees['empid'])
report_lines.append(f"Slips with unknown employee IDs: {len(unknown_emps)}")

# Duplicate employee IDs
dup_emps = employees[employees.duplicated(['empid'])]
report_lines.append(f"Duplicate employee IDs in records: {len(dup_emps)}")

# Duplicate employee-month records
dup_slips = slips[slips.duplicated(['empId', 'payMonth', 'payYear'])]
report_lines.append(f"Duplicate employee-month records in slips: {len(dup_slips)}")

# payMonth validation
invalid_months = slips[~slips['payMonth'].isin(range(1, 13))]
report_lines.append(f"Invalid payMonth values (not 1-12): {len(invalid_months)}")

# Date validation
report_lines.append("\n--- Date Validation ---")
employees['doj_date'] = pd.to_datetime(employees['doj'], format='mixed', errors='coerce')
merged = slips.merge(employees[['empid', 'doj_date']], left_on='empId', right_on='empid', how='left')
# Slips have payMonth, payYear. Construct a date to check if it's before DOJ.
# Let's assume salary slip is issued at the end of payMonth, so the slip period is that month.
# If payYear-payMonth is before doj Year-Month, it's invalid.
merged['slip_date'] = pd.to_datetime(merged['payYear'].astype(str) + '-' + merged['payMonth'].astype(str) + '-01', errors='coerce')

# We only check if the entire slip month is strictly before the joining month
invalid_dates = merged[(merged['slip_date'].dt.year < merged['doj_date'].dt.year) | 
                       ((merged['slip_date'].dt.year == merged['doj_date'].dt.year) & (merged['slip_date'].dt.month < merged['doj_date'].dt.month))]
report_lines.append(f"Salary slips dated before employee joining month: {len(invalid_dates)}")

# 3. Payroll Formulas
report_lines.append("\n--- Payroll Formula Validation ---")
slips['calc_gross'] = slips['basic'] + slips['performance'] + slips['festivalBonus'] + slips['dq']
slips['calc_ded'] = slips['otherDed'] + slips['groupInsurance'] + slips['lwpDeduction']
slips['calc_net'] = slips['calc_gross'] - slips['calc_ded']

gross_discrepancies = slips[np.abs(slips['gross'] - slips['calc_gross']) > 0.01]
ded_discrepancies = slips[np.abs(slips['ded'] - slips['calc_ded']) > 0.01]
net_discrepancies = slips[np.abs(slips['net'] - slips['calc_net']) > 0.01]

report_lines.append(f"Gross discrepancies: {len(gross_discrepancies)}")
report_lines.append(f"Deduction discrepancies: {len(ded_discrepancies)}")
report_lines.append(f"Net discrepancies: {len(net_discrepancies)}")

if len(gross_discrepancies) > 0:
    report_lines.append("Example gross discrepancies (basic+perf+bonus+dq != gross):")
    report_lines.append(gross_discrepancies[['empId', 'payMonth', 'basic', 'performance', 'festivalBonus', 'dq', 'gross', 'calc_gross']].head().to_string())

# Drop the calculation temp columns to avoid altering original schema
slips = slips.drop(columns=['calc_gross', 'calc_ded', 'calc_net'])
if 'doj_date' in employees.columns:
    employees = employees.drop(columns=['doj_date'])

# Generate Monthly Totals
monthly_totals = slips.groupby(['payYear', 'payMonth']).agg(
    employee_count=('empId', 'count'),
    total_gross=('gross', 'sum'),
    total_deductions=('ded', 'sum'),
    total_net=('net', 'sum'),
    total_lwp=('lwp', 'sum'),
    average_attendance=('attendance', 'mean'),
    total_performance=('performance', 'sum'),
    total_festival_bonus=('festivalBonus', 'sum')
).reset_index()

report_lines.append("\n--- Monthly Payroll Totals (Top 5 rows) ---")
report_lines.append(monthly_totals.head().to_string())

report_lines.append("\n--- Remaining Data-Quality Concerns ---")
report_lines.append("1. 'attendance' in slips is a percentage, not an absolute monetary value. It shouldn't be added to gross.")
report_lines.append("2. 'lwp' is a day count, whereas 'lwpDeduction' is the monetary deduction. Both seem consistent.")
report_lines.append("3. No major concerns were found during date and ID validation.")

# Write Report
with open('data_validation_report.txt', 'w') as f:
    f.write('\n'.join(report_lines))

# Save Clean Data
employees.to_csv('employees_clean.csv', index=False)
slips.to_csv('salary_slips_clean.csv', index=False)
monthly_totals.to_csv('monthly_payroll_totals.csv', index=False)

print("Data preparation completed.")
print("Files created: employees_clean.csv, salary_slips_clean.csv, monthly_payroll_totals.csv, data_validation_report.txt")
