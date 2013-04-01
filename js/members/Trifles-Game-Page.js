$(document).ready(function() {
	if(getCookie("loggedIn") != "yes") {
		/* Not logged in, send to login page */
		deleteCookie("goToURL");
		setCookie("goToURL", window.location.pathname, 1);
		window.location="login-form.html";
	} else {
		refreshCookie("loggedIn");
		refreshCookie("userID");
	}
});