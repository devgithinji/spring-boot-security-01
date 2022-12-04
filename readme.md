#### Form Based Authentication

Its a programatic method of authentication used to mitigate the fact that each request has to be authenticated in Basic Auth

Most implementations of the form-based auth use standard HTML form fields to pass userame and password values to the server via POST request

The server validates the credentials provided and creates a session tied to a unique token stored in a cookie and passed between the client the serveron each http request

If the cookie is invalid or the user is logged out the server then redirects to the login page
