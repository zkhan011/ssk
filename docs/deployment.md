# Deployment Guide

Use `docker compose up --build` for a complete offline-capable stack once images and dependencies are prepared. Configure secrets with environment variables, back up PostgreSQL with `pg_dump`, and rotate container logs using the host Docker logging driver.
