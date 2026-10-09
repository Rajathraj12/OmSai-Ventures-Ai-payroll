import pandas as pd
import joblib

def main():
    # Load model
    model = joblib.load('payroll_prediction_model.joblib')
    
    # Load data
    df = pd.read_csv('monthly_payroll_totals.csv')
    df = df.sort_values(by=['payYear', 'payMonth']).reset_index(drop=True)
    
    # The last available month is the input for the next month forecast
    last_month_data = df.iloc[-1:]
    
    # Determine the next month
    last_year = int(last_month_data['payYear'].iloc[0])
    last_month = int(last_month_data['payMonth'].iloc[0])
    
    next_month = last_month + 1
    next_year = last_year
    if next_month > 12:
        next_month = 1
        next_year += 1
        
    print(f"Forecasting for: {next_month}/{next_year}")
    print(f"Based on actual data from: {last_month}/{last_year}")
    
    # Prepare features
    features = ['employee_count', 'total_gross', 'total_deductions', 'total_net', 
                'total_lwp', 'average_attendance', 'total_performance', 'total_festival_bonus']
    
    X_pred = last_month_data[features].copy()
    # rename columns to match the training features (_lag1)
    X_pred.columns = [f'{feat}_lag1' for feat in features]
    
    # Predict using RF
    rf_pred = model.predict(X_pred)[0]
    
    # Predict using Baseline (Naive)
    baseline_pred = last_month_data['total_net'].iloc[0]
    
    print("\n--- Next Month Forecast (Prototype Estimates) ---")
    print(f"Baseline (Naive) Forecast: {baseline_pred:.2f}")
    print(f"Random Forest Forecast: {rf_pred:.2f}")
    
    print("\nNote: These are prototype estimates based on very limited historical data.")

if __name__ == '__main__':
    main()
