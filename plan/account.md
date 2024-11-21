# Account

Note this documents contains only externally visible properties (no internal attributes like is_active)

## Properties
- **First name**
- **Last name**
- **Email** - Used to login and contact the user
- **Phone Number** - +000 111 222 333 format
- **Password** - Non-strict requirements

## User

Inherits account and has preferences.

- **Preferences**
  - Trainings a week
  - **Busy times** - Weekly periodic times, when the user is busy
  - **Notifications (Email, Phone)** - Training session arranged
    - **Training arranged** - When the user gets a training arranged by the admin
    - **Training moved** - When an already arranged training gets moved to another time (date)
    - **Training cancelled** - When an arranged training gets cancelled

### States

Describes different states a user account can be in and how it behaves.

- **Created** - Right after the admin creates the account. User now must set a password using a link in the email.
- **Active** - Account is active when the user sets the password for the account. When the user now logs in, he is prompted for initial configuration.
- **Configured** - User has already configured their account and is no longer prompted to do so after login.

## Admin

Inherits all account properties.

### States

- **No admin account exists** - No admin account is yet created when the application first starts. When anyone enters the site, they get prompted to create the initial admin account. This has a small security risk, that anyone who enters the site first can create the initial admin account, however, if an unwanted person creates the admin account, the actual admin can simply reinstall the application, since no data is there yet.
- **At least one admin account exists** - The application is set up.