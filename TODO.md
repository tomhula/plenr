### Urgent TODOs
- Loading indication
- Input validation


- 
- Authentication/Authorization
- Server exception encapsulation
- Use better authentication (JWT?)
- Handle authentication cases. Token not being valid anymore. Basically some call failing due to invalid auth token, so authentication challenge should happen again.
- Add localisation
- Make sure to not send user contact details everywhere (entire UserDto)
- Share as much code as possible between client and server (Constants like URLs, paths, maximum length of fields, etc.)
- Fix database tables create only after accessing the api
- Fix error when mail server cannot be reached. (currently it crashes the frontend, which is bad. Ideally it should either abort the save, notify about the fact or retry later)
- Maybe consider usage of some kind of global styles? So each instance of a component does not repeat the inlined styles, but just has a class.
- Make password reset requests expire
- Use bootstrap form validation
  - Add user page
- Allow admin to see busy available times of users
