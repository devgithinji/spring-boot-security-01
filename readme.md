#### Http Basic Authentication

* No login form -> browser provides login dialog
* Use Standard HTTP(No cookies, No Sessions)
* Credentials are weakly encoded using Base64 since
  Base64 is easily reversible
  Not secured unless used in conjuction with HTTPS
* No explicit logout. User is logged out when browser exits
