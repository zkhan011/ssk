# Architecture

The solution is split into a Spring Boot backend and Vite React frontend. The backend owns all final application, pass, QR token, check-in/check-out, approval, and audit state. The kiosk temporarily stores in-progress form data in browser session storage and never generates a valid pass without backend confirmation.
