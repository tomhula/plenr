# Plenr

Web system for personal trainers to plan workouts with their clients. The application provides a comprehensive solution for managing workouts, users, and schedules.

## Features

- **User Management**: Create and manage user accounts with two roles (admin/trainer and regular user/client)
- **Training Planning**: Schedule workouts with a focus on efficiency and user experience
- **Notifications**: Send email notifications to users about their scheduled trainings

## Installation & Setup

### Prerequisites

- Docker
- MySQL database
- SMTP server for sending emails

### Using Docker (Recommended)

1. Create `docker-compose.yml` file. Example compose is [here](docker-compose.yml):
2. Start the application: `docker compose up -d`

### Development Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/tomasan7/plenr.git
   cd plenr
   ```
2. Run the backend: `./gradlew :server:run`. Or to generate mock data: `./gradlew :server:run --args="--mock"`
3. Set MySQL and SMTP connections in the `plenr.conf`
4. Run the frontend: `./gradlew :frontend:jsBrowserDevelopmentRun -t`

## Contact

Tomáš Hůla - [GitHub](https://github.com/tomasan7)  
Project Link: [https://github.com/tomasan7/plenr](https://github.com/tomasan7/plenr)
