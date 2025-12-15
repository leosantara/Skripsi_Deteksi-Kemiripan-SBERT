import time

class ProgressBarService:
    # Shared State
    STATUS = {
        "is_running": False,
        "progress_percent": 0.0,
        "message": "Idle",
        "last_update": time.time()
    }

    @staticmethod
    def update(percent: float, message: str, is_running: bool = True):
        ProgressBarService.STATUS["progress_percent"] = percent
        ProgressBarService.STATUS["message"] = message
        ProgressBarService.STATUS["is_running"] = is_running
        ProgressBarService.STATUS["last_update"] = time.time()
        print(f" >> [PROGRESS] {percent}% - {message}")

    @staticmethod
    def get_status():
        return ProgressBarService.STATUS