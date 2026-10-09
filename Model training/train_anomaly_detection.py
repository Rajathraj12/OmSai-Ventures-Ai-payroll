import pandas as pd
import numpy as np
import joblib
import json
from sklearn.ensemble import IsolationForest
from sklearn.preprocessing import StandardScaler
from sklearn.pipeline import Pipeline

def compute_features(df):
    df = df.copy()
    # Sort chronologically by employee
    df = df.sort_values(by=['empId', 'payYear', 'payMonth']).reset_index(drop=True)
    
    # 1. deduction_ratio = ded / gross
    df['deduction_ratio'] = np.where(df['gross'] > 0, df['ded'] / df['gross'], 0)
    
    # 2. performance_ratio = performance / basic
    df['performance_ratio'] = np.where(df['basic'] > 0, df['performance'] / df['basic'], 0)
    
    # 3. net_salary_change_pct
    df['prev_net'] = df.groupby('empId')['net'].shift(1)
    df['net_salary_change_pct'] = np.where(
        (df['prev_net'].notna()) & (df['prev_net'] > 0), 
        (df['net'] - df['prev_net']) / df['prev_net'], 
        0
    )
    df['prev_net'] = df['prev_net'].fillna(df['net']) # fill first month
    
    # 4. lwp_ratio = lwp / days in payroll month
    # Assuming days in month = paidDays + lwp
    df['days_in_month'] = df['paidDays'] + df['lwp']
    df['lwp_ratio'] = np.where(df['days_in_month'] > 0, df['lwp'] / df['days_in_month'], 0)
    
    return df

def explain_anomaly(row):
    reasons = []
    if row['deduction_ratio'] > 0.3:
        reasons.append(f"Deduction ratio unusually high ({row['deduction_ratio']:.2%})")
    if row['performance_ratio'] > 0.2:
        reasons.append(f"Performance incentive unusually high ({row['performance_ratio']:.2%})")
    if abs(row['net_salary_change_pct']) > 0.15:
        reasons.append(f"Net salary changed sharply ({row['net_salary_change_pct']:.2%})")
    if row['lwp_ratio'] > 0.2:
        reasons.append(f"Leave without pay unusually high ({row['lwp_ratio']:.2%})")
        
    if not reasons:
        reasons.append("Unusual multidimensional pattern detected by Isolation Forest")
        
    return "; ".join(reasons)

def main():
    # Load Data
    df = pd.read_csv('salary_slips_clean.csv')
    df = compute_features(df)
    
    # Define features for model
    feature_cols = ['basic', 'gross', 'net', 'ded', 'lwp', 'paidDays', 'performance', 
                    'festivalBonus', 'otherDed', 'groupInsurance', 'lwpDeduction',
                    'deduction_ratio', 'performance_ratio', 'net_salary_change_pct', 'lwp_ratio']
    
    # Split: Train on first 11 months, Evaluate on last month (Sep 2026)
    # The max payYear and payMonth in our dataset is 2026, 9.
    # We will hold out payMonth == 9 & payYear == 2026.
    max_year = df['payYear'].max()
    max_month = df[df['payYear'] == max_year]['payMonth'].max()
    
    train_mask = ~((df['payYear'] == max_year) & (df['payMonth'] == max_month))
    eval_mask = ((df['payYear'] == max_year) & (df['payMonth'] == max_month))
    
    train_df = df[train_mask].copy()
    eval_df = df[eval_mask].copy()
    
    X_train = train_df[feature_cols]
    X_eval = eval_df[feature_cols]
    
    # Pipeline: Scaler + IsolationForest
    pipeline = Pipeline([
        ('scaler', StandardScaler()),
        ('model', IsolationForest(contamination=0.03, random_state=42))
    ])
    
    # Fit ONLY on train data to avoid data leakage
    pipeline.fit(X_train)
    
    # Save Model
    joblib.dump(pipeline, 'anomaly_detection_model.joblib')
    
    # Evaluate on held-out data
    eval_preds = pipeline.predict(X_eval)
    eval_scores = pipeline.decision_function(X_eval)
    
    eval_df['anomaly_classification'] = np.where(eval_preds == -1, 'Anomaly', 'Normal')
    # Convert decision_function to an anomaly score: 
    # Lower negative values are more anomalous in scikit-learn.
    # Let's invert it so higher positive = more anomalous score, for readability.
    eval_df['anomaly_score'] = -eval_scores 
    
    # Apply explanations for anomalies
    eval_df['anomaly_reason'] = eval_df.apply(
        lambda row: explain_anomaly(row) if row['anomaly_classification'] == 'Anomaly' else "", axis=1
    )
    
    anomalies = eval_df[eval_df['anomaly_classification'] == 'Anomaly']
    num_eval = len(eval_df)
    num_anom = len(anomalies)
    
    # Create evaluation metrics
    metrics = {
        'training_records': len(train_df),
        'evaluation_records': num_eval,
        'records_flagged': num_anom,
        'percentage_flagged': float(num_anom / num_eval * 100) if num_eval > 0 else 0,
        'contamination_param': 0.03,
        'features_used': feature_cols
    }
    
    with open('anomaly_detection_metrics.json', 'w') as f:
        json.dump(metrics, f, indent=4)
        
    # Save output for all evaluated records
    eval_df.to_csv('anomaly_detection_results.csv', index=False)
    
    # Print report
    print("--- Anomaly Detection Training Complete ---")
    print(f"Training records: {metrics['training_records']}")
    print(f"Evaluation records: {metrics['evaluation_records']}")
    print(f"Flagged Anomalies in evaluation: {metrics['records_flagged']} ({metrics['percentage_flagged']:.2f}%)")
    
    if num_anom > 0:
        print("\nTop Flagged Records (Evaluation Set):")
        top_anomalies = anomalies.sort_values(by='anomaly_score', ascending=False)
        for _, row in top_anomalies.head().iterrows():
            print(f"- Emp: {row['empName']} (ID: {row['empId']}) | Month: {row['payMonth']}/{row['payYear']} | Score: {row['anomaly_score']:.3f}")
            print(f"  Reason: {row['anomaly_reason']}")
            print(f"  [Net: {row['net']}, Prev Net: {row['prev_net']}, LWP: {row['lwp']}, Ded: {row['ded']}]")
    else:
        print("\nNo anomalies detected in the held-out evaluation set.")
        
    print("\nFiles saved: anomaly_detection_model.joblib, anomaly_detection_results.csv, anomaly_detection_metrics.json")

if __name__ == "__main__":
    main()
