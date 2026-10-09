from fastapi import FastAPI, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel
import joblib
import pandas as pd
import logging
from typing import List

from predict_next_month import get_prediction
from detect_anomalies import detect_anomalies

# Setup logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

app = FastAPI(title="Om Sai Ventures AI Payroll API")

# Configure CORS for local Android development
app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Global models
models = {
    "prediction_model": None,
    "anomaly_pipeline": None
}

@app.on_event("startup")
def load_models():
    try:
        models["prediction_model"] = joblib.load('payroll_prediction_model.joblib')
        logger.info("Prediction model loaded successfully.")
    except Exception as e:
        logger.error(f"Failed to load prediction model: {e}")
        
    try:
        models["anomaly_pipeline"] = joblib.load('anomaly_detection_model.joblib')
        logger.info("Anomaly detection pipeline loaded successfully.")
    except Exception as e:
        logger.error(f"Failed to load anomaly detection pipeline: {e}")

# Pydantic Models
class HealthResponse(BaseModel):
    status: str
    prediction_model_loaded: bool
    anomaly_model_loaded: bool

class PredictionResponse(BaseModel):
    predictionMonth: int
    predictionYear: int
    predictedNetPayroll: float
    previousMonthNetPayroll: float
    changeAmount: float
    changePercentage: float
    modelUsed: str
    forecastDisclaimer: str

class AnomalyItem(BaseModel):
    employeeId: str
    employeeName: str
    payMonth: int
    payYear: int
    net: float
    gross: float
    deductions: float
    anomalyScore: float
    reason: str

class AnomalyResponse(BaseModel):
    totalRecordsAnalyzed: int
    anomalyCount: int
    anomalies: List[AnomalyItem]

@app.get("/health", response_model=HealthResponse)
def health_check():
    return HealthResponse(
        status="ok",
        prediction_model_loaded=models["prediction_model"] is not None,
        anomaly_model_loaded=models["anomaly_pipeline"] is not None
    )

@app.get("/ai/payroll-prediction", response_model=PredictionResponse)
def predict_payroll():
    if models["prediction_model"] is None:
        raise HTTPException(status_code=503, detail="Prediction model is not available.")
        
    try:
        res = get_prediction(model=models["prediction_model"])
        if "error" in res:
            raise HTTPException(status_code=500, detail=res["error"])
            
        return PredictionResponse(**res)
    except Exception as e:
        logger.error(f"Error in /ai/payroll-prediction: {e}")
        raise HTTPException(status_code=500, detail="Internal Server Error during prediction.")

@app.get("/ai/anomalies", response_model=AnomalyResponse)
def get_anomalies():
    if models["anomaly_pipeline"] is None:
        raise HTTPException(status_code=503, detail="Anomaly detection model is not available.")
        
    try:
        # We run it on the historical clean data as the prototype requirement.
        # In the future, this can be swapped with a repository fetching from Firebase.
        df_results = detect_anomalies('salary_slips_clean.csv', pipeline=models["anomaly_pipeline"])
        
        # Filter to only anomalies
        anomalies_df = df_results[df_results['anomaly_classification'] == 'Anomaly']
        
        # Build the response list
        anomalies_list = []
        for _, row in anomalies_df.iterrows():
            anomalies_list.append(AnomalyItem(
                employeeId=str(row['empId']),
                employeeName=str(row['empName']),
                payMonth=int(row['payMonth']),
                payYear=int(row['payYear']),
                net=float(row['net']),
                gross=float(row['gross']),
                deductions=float(row['ded']),
                anomalyScore=float(row['anomaly_score']),
                reason=str(row['anomaly_reason'])
            ))
            
        return AnomalyResponse(
            totalRecordsAnalyzed=len(df_results),
            anomalyCount=len(anomalies_list),
            anomalies=anomalies_list
        )
    except Exception as e:
        logger.error(f"Error in /ai/anomalies: {e}")
        raise HTTPException(status_code=500, detail="Internal Server Error during anomaly detection.")

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)
