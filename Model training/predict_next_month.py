import pandas as pd
import joblib

def get_prediction(model=None):
    # Load model
    try:
        if model is None:
            model = joblib.load('payroll_prediction_model.joblib')
    except Exception as e:
        return {"error": str(e)}
    
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
        
    # Prepare features
    features = ['employee_count', 'total_gross', 'total_deductions', 'total_net', 
                'total_lwp', 'average_attendance', 'total_performance', 'total_festival_bonus']
    
    X_pred = last_month_data[features].copy()
    X_pred.columns = [f'{feat}_lag1' for feat in features]
    
    # Predict using RF
    rf_pred = float(model.predict(X_pred)[0])
    
    # Predict using Baseline (Naive)
    baseline_pred = float(last_month_data['total_net'].iloc[0])
    
    # Based on evaluation, Baseline is more accurate due to limited data
    # We will return the baseline prediction as the preferred one
    change_amount = baseline_pred - float(last_month_data['total_net'].iloc[0])
    change_pct = 0.0 # Naive forecast implies no change
    
    return {
        "predictionMonth": next_month,
        "predictionYear": next_year,
        "predictedNetPayroll": baseline_pred,
        "previousMonthNetPayroll": float(last_month_data['total_net'].iloc[0]),
        "changeAmount": change_amount,
        "changePercentage": change_pct,
        "modelUsed": "Naive Baseline",
        "forecastDisclaimer": "These are prototype estimates based on very limited historical data. Baseline is preferred over Random Forest due to overfitting on small sample size."
    }

def main():
    res = get_prediction()
    if "error" in res:
        print("Error:", res["error"])
    else:
        print(f"Forecasting for: {res['predictionMonth']}/{res['predictionYear']}")
        print(f"Based on actual data from previous month.")
        print("\n--- Next Month Forecast (Prototype Estimates) ---")
        print(f"Preferred Forecast ({res['modelUsed']}): {res['predictedNetPayroll']:.2f}")
        print(f"\nNote: {res['forecastDisclaimer']}")

if __name__ == '__main__':
    main()
