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

## Arrange Trainings
- Quick user availability hover (no delay before appearance)
- Participant list in training view
- Each user has unique color?
- Space out user availability and make it wider
- Users names next to user availabilities
- Make timetable fill rest of the page or at least make it bigger

## Admin home page
- Center buttons
- Color based on training type

## Training calendar
- Spacing between trainings in a single day
- Make trainings fixed width (or grow + max-width)
