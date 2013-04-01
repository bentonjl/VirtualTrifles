$(document).ready(function() {
	if(getCookie("loggedIn") == "yes") {
		refreshCookie("loggedIn");
		refreshCookie("userID");
		window.location="/" + window.location.pathname.split("/")[1] + "/members/new-member-form-loggedIn.html";
	}
});

function validateForm() {
	if(!Modernizr.input.required) {
		// The required attribute is not supported, so must manually check required fields

		// First fill an array with all the elements
		var inputElements = document.getElementById("newMemberForm").elements;

		// Next, move through the array and check each element.
		for(var i=0; i < inputElements.length; i++) {
			// Check if element is required
			if(inputElements[i].hasAttribute("required")) {
				// Element found is required, now check if it has a value.
				// If no value found fails validation, and function returns false.
				if(inputElements[i].value == "") {
					alert("All fields marked with a * must be completed.");
					// I didn't want to do too much js, but we might want to indicate the 
					// particular invalid field that failed.  Maybe by changing background 
					// color or something
					return false;
				}
			}
		}
		// If you reach this point, everything worked out and browser can submit form
		//var firstName = $('#firstName').val();
		if(checkPassword(document.getElementById("password1")) && validatePassword(document.getElementById("password2"))) {
			newMemberSubmit();
		}
		return true;
	}
	if(checkPassword(document.getElementById("password1")) && validatePassword(document.getElementById("password2"))) {
		newMemberSubmit();
	}
} // end of validateForm()

function newMemberSubmit() {
	/* Collect form data */
	var userID;
	var firstName = $('#firstName').val();
	var lastName = $('#lastName').val();
	var email = $('#email').val();
	var userType = 'student';
	var classID = $('#courseID').val();
	var pass = $('#password1').val();
	
	var date = new Date();
	var createTime = date.getFullYear() + "-" + (date.getMonth() + 1) + "-" + date.getDate() + " " +
					 date.getHours() + ":" + date.getMinutes() + ":" + date.getSeconds();
	
	var newUserTest = isNewUser(email);
	if(newUserTest == 1) { // Only do this if the user is a new user
	/* Create INSERT into the User database */
		var insert = {'type' : 'INSERT',
					  'table' : 'User',
					  'insert' : [
					           {'attribute' : 'FirstName', 'value' : firstName},
					           {'attribute' : 'LastName', 'value' : lastName},
					           {'attribute' : 'EmailAddress', 'value' : email},
					           {'attribute' : 'UserType', 'value' : userType},
					           {'attribute' : 'CreateTime', 'value' : createTime}
					              ]
					 };
		$.ajax({
			type		:	"POST",
			url			:	"/" + window.location.pathname.split('/')[1] + "/include/database.jsp",
			data		:	JSON.stringify(insert),
			dataType	:	"json",
			success		:	function(data) {
				/* SUCCESS */
				userID = data.results[0].UserID;
				/* Use UserID to add class to hasClass table */
				var insert2 = {'type' : 'INSERT',
						  'table' : 'hasClass',
						  'insert' : [
						           {'attribute' : 'UserID', 'value' : userID},
						           {'attribute' : 'ClassID', 'value' : classID}
						             ]
						 };
				$.ajax({
					type		:	"POST",
					url			:	"/" + window.location.pathname.split('/')[1] + "/include/database.jsp",
					async		:	false,
					data		:	JSON.stringify(insert2),
					dataType	:	"json",
					success		:	function(data2) {
						/* SUCCESS */
					}
				});
				
				var insert3 = {'type' : 'INSERT',
						  'table' : 'Password',
						  'insert' : [
						           {'attribute' : 'UserID', 'value' : userID},
						           {'attribute' : 'Password', 'value' : pass}
						             ]
						 };
				$.ajax({
					type		:	"POST",
					url			:	"/" + window.location.pathname.split('/')[1] + "/include/database.jsp",
					async		:	false,
					data		:	JSON.stringify(insert3),
					dataType	:	"json",
					success		:	function(data3) {
						/* SUCCESS */
					}
				});
				
				alert("Congratulations! Your account was created successfully!");
				
				/* Log in the new user */
				var json = {"username" : email,
							"password" : pass};
				
				$.ajax({
					type		:	"POST",
					url			:	"/" + window.location.pathname.split('/')[1] + "/auth",
					data		:	json,
					dataType	:	"json",
					success		:	function(data) {
						if(data.loggedIn == "no") {
							/* send to login page */
							alert("Error submitting new user login credentials");
						} else if(data.loggedIn == "yes") {
							window.location="/" + window.location.pathname.split('/')[1] + "/members/account.html";
						}
					}
				});
			}
		});
	} else if(newUserTest == 0){ // Not a new user
		alert("This email address is already registered.")
	} else { // some error happened
		alert("An error has occurred while checking email address.")
	}
} // end of newMemberSubmit()

function checkPassword(arg) {
	if(arg.value.length < 6) {
		alert("Password must contain at least 6 characters.")
		return false;
	}
	return true;
}

function validatePassword(arg) {
	if(arg.value != document.getElementById("password1").value) {
		alert("Passwords do not match!");
		return false;
	}
	return true;
}

function isNewUser(username) {
	
	var rv = 0;
	
	var json = {'type' : 'query',
				'table' : 'User',
				'select' : ['*'],
				'where' : [
				       {'attribute' : 'EmailAddress', 'value' : username}
				           ]
			   };
	
	$.ajax({
		type		:	"POST",
		url			:	"../include/database.jsp",
		data		:	JSON.stringify(json),
		dataType	:	"json",
		async		:	false,
		success		:	function(data) {
			if(data == null) {
				rv = 1;
			}
		}
	});

	return rv;
}