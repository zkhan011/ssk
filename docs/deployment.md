# Deployment Guide

Use `docker compose up --build` for a complete offline-capable stack once images and dependencies are prepared. Configure secrets with environment variables, back up SQL Server with `sqlcmd / Q "BACKUP DATABASE kiosk TO DISK = 'kiosk.bak'"`, and rotate container logs using the host Docker logging driver.
