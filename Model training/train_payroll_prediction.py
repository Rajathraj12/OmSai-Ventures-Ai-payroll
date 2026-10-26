"""
Evaluation and Training Script for Om Sai Ventures Payroll Prediction
=====================================================================

Runs chronological rolling-origin evaluation (without random shuffling) across
historical months, comparing:
1. Naive Baseline
2. 3-Month Moving Average
3. Linear Trend Forecast
4. Factor-Aware Employee-Level Engine

Generates and saves payroll_prediction_metrics.json and serialized model artifacts.
"""

import json
import joblib
import pandas as pd
from sklearn.ensemble import RandomForestRegressor
from payroll_engine import ModelEvaluator, FactorAwareForecaster

def main():
    print("=== Evaluating Payroll Forecasting Models ===")
    slips_df = pd.read_csv('salary_slips_clean.csv')
    monthly_df = pd.read_csv('monthly_payroll_totals.csv')
    
    print(f"Loaded {len(slips_df)} slips across {len(monthly_df)} historical months.")
    
    # 1. Run rigorous rolling-origin evaluation
    metrics = ModelEvaluator.evaluate_chronological(slips_df, monthly_df, start_fold=3)
    
    print(f"\nEvaluation Method: {metrics['evaluation_method']}")
    print(f"Periods Evaluated: {metrics['test_period_range']} ({metrics['test_periods_count']} test months)")
    print("\nModel Comparison Table:")
    print(f"{'Model':<26} | {'MAE (Rs.)':<10} | {'RMSE (Rs.)':<10} | {'MAPE (%)':<8}")
    print("-" * 62)
    for model_name, res in metrics['models'].items():
        print(f"{model_name:<26} | {res['mae']:<10.2f} | {res['rmse']:<10.2f} | {res['mape']:<8.2f}%")
        
    print("\nSummary Rationale:")
    print(metrics['summary'])
    
    # 2. Save evaluation metrics to JSON
    with open('payroll_prediction_metrics.json', 'w') as f:
        json.dump(metrics, f, indent=4)
    print("\nMetrics saved to payroll_prediction_metrics.json")
    
    # 3. Fit and persist the forecaster / fallback model object
    # For backward compatibility with any joblib loader expecting an object
    forecaster = FactorAwareForecaster(slips_df, monthly_df)
    joblib.dump(forecaster, 'payroll_prediction_model.joblib')
    print("Forecasting engine saved to payroll_prediction_model.joblib")

if __name__ == '__main__':
    main()
