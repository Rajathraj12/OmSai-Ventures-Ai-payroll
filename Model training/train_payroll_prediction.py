import pandas as pd
import numpy as np
import json
import joblib
from sklearn.ensemble import RandomForestRegressor
from sklearn.metrics import mean_absolute_error, mean_squared_error

def mean_absolute_percentage_error(y_true, y_pred):
    y_true, y_pred = np.array(y_true), np.array(y_pred)
    # Avoid division by zero
    mask = y_true != 0
    if not np.any(mask):
        return np.nan
    return np.mean(np.abs((y_true[mask] - y_pred[mask]) / y_true[mask])) * 100

def main():
    # 1. Load data
    df = pd.read_csv('monthly_payroll_totals.csv')
    df = df.sort_values(by=['payYear', 'payMonth']).reset_index(drop=True)
    
    print(f"Available historical months: {len(df)}")
    print(f"Period: {df['payMonth'].iloc[0]}/{df['payYear'].iloc[0]} to {df['payMonth'].iloc[-1]}/{df['payYear'].iloc[-1]}")
    
    # 2. Prepare features (Lag of 1 month)
    # We want to predict total_net at month T using data from month T-1
    features = ['employee_count', 'total_gross', 'total_deductions', 'total_net', 
                'total_lwp', 'average_attendance', 'total_performance', 'total_festival_bonus']
    
    df_lagged = pd.DataFrame()
    df_lagged['target_total_net'] = df['total_net'].iloc[1:].values
    
    for feat in features:
        df_lagged[f'{feat}_lag1'] = df[feat].iloc[:-1].values
    
    # Target month mapping just for tracking
    df_lagged['target_year'] = df['payYear'].iloc[1:].values
    df_lagged['target_month'] = df['payMonth'].iloc[1:].values

    # 3. Train-test split (Chronological)
    # We have 11 samples. Let's use the last month (index 10) for testing, 
    # and indices 0 to 9 for training.
    
    train_df = df_lagged.iloc[:-1]
    test_df = df_lagged.iloc[-1:]
    
    X_cols = [f'{feat}_lag1' for feat in features]
    
    X_train = train_df[X_cols]
    y_train = train_df['target_total_net']
    
    X_test = test_df[X_cols]
    y_test = test_df['target_total_net']
    
    # 4. Baseline Model (Naive forecast: next month = this month)
    # For baseline, prediction is just total_net_lag1
    baseline_preds = test_df['total_net_lag1']
    
    base_mae = mean_absolute_error(y_test, baseline_preds)
    base_rmse = np.sqrt(mean_squared_error(y_test, baseline_preds))
    base_mape = mean_absolute_percentage_error(y_test, baseline_preds)
    
    # 5. RandomForest Model
    rf = RandomForestRegressor(n_estimators=100, random_state=42)
    rf.fit(X_train, y_train)
    rf_preds = rf.predict(X_test)
    
    rf_mae = mean_absolute_error(y_test, rf_preds)
    rf_rmse = np.sqrt(mean_squared_error(y_test, rf_preds))
    rf_mape = mean_absolute_percentage_error(y_test, rf_preds)
    
    print("\n--- Evaluation on latest month ---")
    print(f"Target Month: {test_df['target_month'].iloc[0]}/{test_df['target_year'].iloc[0]}")
    print(f"Actual total_net: {y_test.iloc[0]:.2f}")
    print(f"Baseline (Naive) Prediction: {baseline_preds.iloc[0]:.2f}")
    print(f"RF Prediction: {rf_preds[0]:.2f}")
    
    print(f"\nBaseline Metrics -> MAE: {base_mae:.2f}, RMSE: {base_rmse:.2f}, MAPE: {base_mape:.2f}%")
    print(f"RF Metrics -> MAE: {rf_mae:.2f}, RMSE: {rf_rmse:.2f}, MAPE: {rf_mape:.2f}%")
    
    # Note on data size limitations
    limitations = "The dataset has only 12 monthly records, yielding 11 training samples. " \
                  "Machine learning models like Random Forest require significantly more data " \
                  "to capture complex patterns and avoid overfitting. The Naive Baseline is often " \
                  "more reliable in such extremely data-scarce environments."
    
    print("\nLimitations:")
    print(limitations)
    
    # 6. Train final model on ALL available lagged data and save
    X_all = df_lagged[X_cols]
    y_all = df_lagged['target_total_net']
    
    final_rf = RandomForestRegressor(n_estimators=100, random_state=42)
    final_rf.fit(X_all, y_all)
    
    # Save model
    joblib.dump(final_rf, 'payroll_prediction_model.joblib')
    
    # Save metrics and metadata
    metrics = {
        'evaluation_period': f"{test_df['target_month'].iloc[0]}/{test_df['target_year'].iloc[0]}",
        'baseline_mae': float(base_mae),
        'baseline_rmse': float(base_rmse),
        'baseline_mape': float(base_mape),
        'rf_mae': float(rf_mae),
        'rf_rmse': float(rf_rmse),
        'rf_mape': float(rf_mape),
        'limitations': limitations
    }
    with open('payroll_prediction_metrics.json', 'w') as f:
        json.dump(metrics, f, indent=4)
        
    print("\nModel saved to payroll_prediction_model.joblib")
    print("Metrics saved to payroll_prediction_metrics.json")

if __name__ == '__main__':
    main()
