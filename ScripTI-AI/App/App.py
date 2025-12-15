import uvicorn
from fastapi import FastAPI
from contextlib import asynccontextmanager
from Core.engine import ENGINE
from Controllers import similarity_controller, system_controller

# FUNGSI LIFESPAN (Pengganti on_event startup)
@asynccontextmanager
async def lifespan(app: FastAPI):
    # 1. Load Model & Resources saat server nyala
    print("🟢 Starting up... Loading AI Models...")
    ENGINE.load_resources()
    yield
    # 2. (Opsional) Code saat server mati bisa ditaruh disini
    print("🔴 Shutting down...")

# Init App dengan parameter lifespan
app = FastAPI(title="ScripTI AI Backend", lifespan=lifespan)

# Register Routers
# Pastikan di file controller ada variable 'router'
app.include_router(similarity_controller.router)
app.include_router(system_controller.router)

# Run Server
if __name__ == "__main__":
    uvicorn.run("App:app", host="0.0.0.0", port=8000, reload=True)