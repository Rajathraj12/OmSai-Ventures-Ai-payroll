# Om Sai Ventures AI Payroll Prototype API

This is the standalone Python FastAPI backend that serves the AI forecasting and anomaly detection features to the Android application.

## 1. Installation

It is recommended to use a Python virtual environment:

```bash
# Create a virtual environment
python -m venv venv

# Activate it (Windows)
venv\Scripts\activate
# Activate it (Mac/Linux)
# source venv/bin/activate

# Install dependencies
pip install -r requirements.txt
```

## 2. Starting the API

Run the server using Uvicorn:

```bash
uvicorn api:app --reload --host 0.0.0.0 --port 8000
```
This will start the API on `http://127.0.0.0:8000`. 
By passing `--host 0.0.0.0`, the server is accessible across your local network.

## 3. Finding Your Local Network IP for Android Testing

To configure the Android app to connect to this API, you will need your computer's local IP address:

- **Windows**: Open Command Prompt and type `ipconfig`. Look for the "IPv4 Address" (e.g., `192.168.1.15`).
- **Mac/Linux**: Open Terminal and type `ifconfig` or `ip a`.

In your Android app's API configuration (usually a Retrofit builder or base URL constant), use `http://<YOUR_LOCAL_IP>:8000/`.

## 4. Endpoints & Swagger Documentation

Once running, you can view the interactive Swagger documentation and test the endpoints directly by visiting:
[http://localhost:8000/docs](http://localhost:8000/docs)

### Available Endpoints:
- `GET /health` - Check if the API is running and models are loaded.
- `GET /ai/payroll-prediction` - Get the preferred AI payroll forecast for the next month.
- `GET /ai/anomalies` - Analyze recent salary slips and return flagged anomalies with reasons.
