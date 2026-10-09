import pandas as pd
import numpy as np
import joblib

def compute_features(df):
    df = df.copy()
    # Ensure chronological order
    df = df.sort_values(by=['empId', 'payYear', 'payMonth']).reset_index(drop=True)
    
    df['deduction_ratio'] = np.where(df['gross'] > 0, df['ded'] / df['gross'], 0)
    df['performance_ratio'] = np.where(df['basic'] > 0, df['performance'] / df['basic'], 0)
    
    df['prev_net'] = df.groupby('empId')['net'].shift(1)
    df['net_salary_change_pct'] = np.where(
        (df['prev_net'].notna()) & (df['prev_net'] > 0), 
        (df['net'] - df['prev_net']) / df['prev_net'], 
        0
    )
    df['prev_net'] = df['prev_net'].fillna(df['net'])
    
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

def detect_anomalies(new_records_csv, pipeline=None):
    if pipeline is None:
        pipeline = joblib.load('anomaly_detection_model.joblib')
    
    df = pd.read_csv(new_records_csv)
    df_feat = compute_features(df)
    
    feature_cols = ['basic', 'gross', 'net', 'ded', 'lwp', 'paidDays', 'performance', 
                    'festivalBonus', 'otherDed', 'groupInsurance', 'lwpDeduction',
                    'deduction_ratio', 'performance_ratio', 'net_salary_change_pct', 'lwp_ratio']
    
    X = df_feat[feature_cols]
    
    preds = pipeline.predict(X)
    scores = pipeline.decision_function(X)
    
    df_feat['anomaly_classification'] = np.where(preds == -1, 'Anomaly', 'Normal')
    df_feat['anomaly_score'] = -scores
    
    df_feat['anomaly_reason'] = df_feat.apply(
        lambda row: explain_anomaly(row) if row['anomaly_classification'] == 'Anomaly' else "", axis=1
    )
    
    return df_feat

if __name__ == "__main__":
    # Example usage:
    # Here we just run it on the last month (which is the same as the eval set)
    print("Inference script ready. To use: import detect_anomalies and pass a CSV path.")
