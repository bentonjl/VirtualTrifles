$(document).ready(function() {
	if(getCookie("loggedIn") == "yes") {
		$('#LogoutWrapper').show();
		$('#LoginWrapper').hide();
	} else {
		$('#LogoutWrapper').hide();
		$('#LoginWrapper').show();
	}
});