function loginSubmit() {
	
	var json = {"username" : $('#username').val(),
				"password" : $('#password').val()};
	
	var root = window.location.pathname.split('/')[1];
	var url = getCookie("goToURL");

	if(url == null || url == "") {
		setCookie("goToURL", "/" + root + "/members/account.html", 2);
	}

	$.ajax({
		type		:	"POST",
		url			:	"/" + root + "/auth",
		data		:	json,
		dataType	:	"json",
		success		:	function(data) {
			if(data.loggedIn == "no") {
				/* send to login page */
				$(':text').val('');
				$('input[type="password"]').val('');
				$('#errorText').html(data.message);
			} else if(data.loggedIn == "yes") {
				self.location=data.goToURL;
			}
		}
	});
}

function logout() {
	deleteCookie("goToURL");
	deleteCookie("loggedIn");
	deleteCookie("userID");
	//window.location="/" + window.location.pathname.split('/')[1] + "/index.html";

}

function logout_test() {
	var loc = window.location;
	var redirect = loc.protocol + "//" + loc.hostname + "/" + loc.pathname.split('/')[1] + "/";
	alert("You will now be redirected to: " + loc.protocol + "//" + loc.hostname + "/" + loc.pathname.split('/')[1] + "/");
	logout();
	window.location.replace(redirect);
	
}