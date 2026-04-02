from fastapi import FastAPI

app = FastAPI(title="Tumtum API", version="0.1.0")

@app.get("/")
async def root():
    return {"message": "Tumtum API"}