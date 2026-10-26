import os
import pandas as pd
from payroll_engine import FactorAwareForecaster, FestivalConfig

def get_prediction(
    model=None,
    slips_path: str = 'salary_slips_clean.csv',
    monthly_path: str = 'monthly_payroll_totals.csv',
    include_festival: bool = True,
    custom_festival_bonus: float = None
):
    """
    Generate next month's payroll forecast using the history-based,
    factor-aware payroll forecasting engine.
    """
    try:
        if not os.path.exists(slips_path) or not os.path.exists(monthly_path):
            return {"error": f"Required dataset files not found at {slips_path} or {monthly_path}"}
            
        slips_df = pd.read_csv(slips_path)
        monthly_df = pd.read_csv(monthly_path)
        
        forecaster = FactorAwareForecaster(slips_df, monthly_df)
        forecast_res = forecaster.forecast_next_month(
            include_festival=include_festival,
            custom_festival_bonus=custom_festival_bonus
        )
        return forecast_res
    except Exception as e:
        return {"error": str(e)}

def main():
    res = get_prediction()
    if "error" in res:
        print("Error:", res["error"])
        return
        
    print(f"=== Forecast for: {res['predictionMonth']}/{res['predictionYear']} ===")
    print(f"Model: {res['modelUsed']}")
    print(f"Previous Month Actual Net: Rs. {res['previousMonthNetPayroll']:,.2f}")
    print(f"Base Predicted Net:        Rs. {res['basePredictedNetPayroll']:,.2f}")
    if res.get('festivalAdjustment', 0) > 0:
        print(f"Festival Adjustment ({res.get('festivalName')}): +Rs. {res['festivalAdjustment']:,.2f}")
    print(f"Final Predicted Net:       Rs. {res['predictedNetPayroll']:,.2f}")
    print(f"Change:                    Rs. {res['changeAmount']:+,.2f} ({res['changePercentage']:+.2f}%)")
    print(f"\n--- Key Forecast Factors ---")
    print(f"Expected Staff Count:     {res['expectedEmployeeCount']}")
    print(f"Avg LWP Days / Employee:  {res['estimatedAverageLwp']:.2f} days")
    print(f"Estimated LWP Deductions: Rs. {res['estimatedLwpDeductions']:,.2f}")
    print(f"Expected Performance:     Rs. {res['expectedPerformanceIncentives']:,.2f}")
    print(f"\nAssumptions:")
    for a in res.get('forecastAssumptions', []):
        print(f" - {a}")

if __name__ == '__main__':
    main()
