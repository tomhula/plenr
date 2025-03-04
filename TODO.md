- Authentication/Authorization
- Server exception encapsulation
- Use better authentication (JWT?)
- Fix initial /login route not showing in the address bar
- Handle authentication cases. Token not being valid anymore. Basically some call failing due to invalid auth token, so authentication challenge should happen again.
- Add possibility to log-out.
- Add localisation
- Make sure the navigation stack actually is how it should be (go back pops and does not add)
- Make sure to not send user contact details everywhere (entire UserDto)
- Share as much code as possible between client and server (Constants like URLs, paths, maximum length of fields, etc.)
- Fix database tables create only after accessing the api
- Fix "Create" button on admin creation page
- Fix error when mail server cannot be reached. (currently it crashes the frontend, which is bad. Ideally it should either abort the save, notify about the fact or retry later)
- Maybe consider usage of some kind of global styles? So each instance of a component does not repeat the inlined styles, but just has a class.
- Make password reset requests expire
- Fix routing
  - Repro steps:
    1. Log out
    2. Log in
    3. Arrange trainings
    4. Now try going back, does not work

## Arrange Trainings
- Quick user availability hover (no delay before appearance)
- Participant list in training view
- Each user has unique color?
- Space out user availability and make it wider
- Users names next to user availabilities
