$(document).ready(function() {

	if(getCookie("loggedIn") == "yes") {
		refreshCookie("loggedIn");
		refreshCookie("userID");
		/* Load User Information */
		var query = {'type' : 'query',
					 'table' : 'User',
					 'select' : ['*'],
					 'join' : [
				          {'table' : 'hasClass', 'lhs' : 'User.UserID', 'rhs' : 'hasClass.UserID'},
				          {'table' : 'Class', 'lhs' : 'hasClass.ClassID', 'rhs' : 'Class.ClassID'}
				          	  ],
				      'where' : [
				          {'attribute' : 'User.UserID', 'value' : getCookie("userID")}
				                ]
					};

		$.ajax({
			type		:	"POST",
			url			:	"../include/database.jsp",
			data		:	JSON.stringify(query),
			dataType	:	"json",
			success		:	function(data) {
				$('#jsp-emailAddr').html(data.results[0].EmailAddress);
				$('#jsp-firstName').html(data.results[0].FirstName);
				$('#jsp-lastName').html(data.results[0].LastName);
				$('#jsp-courseName').html(data.results[0].ClassName);
				$('#jsp-regDate').html(data.results[0].CreateTime);
				$('#jsp-lastLogin').html(data.results[0].LastLogin);
				$('#jsp-avatar').attr('src','../img/avatars/' + data.results[0].UserID + '.jpg');
				/* Query to get professor's name */
				var subQuery = {'type' : 'query',
								'table' : 'User',
								'select' : ['*'],
								'join' : [
								          {'table' : 'hasClass', 'lhs' : 'User.UserID', 'rhs' : 'hasClass.UserID'},
								          {'table' : 'Class', 'lhs' : 'hasClass.ClassID', 'rhs' : 'Class.ClassID'}
								         ],
								'where' : [
					                     {'attribute' : 'hasClass.ClassID', 'value' : data.results[0].ClassID},
					                     {'attribute' : 'User.UserType', 'value' : 'instructor'}
					                      ]
								};
				$.ajax({
					type		:	"POST",
					url			:	"../include/database.jsp",
					data		:	JSON.stringify(subQuery),
					dataType	:	"json",
					success		:	function(subData) {
						$('#jsp-profName').html(subData.results[0].LastName);
					}
				});
			}
		}); 
	} else {
		/* Not logged in, send to login page */
		deleteCookie("goToURL");
		setCookie("goToURL", window.location.pathname, 1);
		window.location="account-login-page.html";
	}

});

/* Contact Info Editing */
function editContactInfo() {
	var emailAddr = $('#jsp-emailAddr').html();
	$('#jsp-emailAddr').html("<input id='editEmail' type='text' placeholder='" + emailAddr + "'></input>&nbsp&nbsp<input type='button' value='save' onclick='submitEmailChange()'></input>");
}

function submitEmailChange() {
	var newValue = $('#editEmail').val();
	var userID = getCookie("userID");
	
	var update = { 'type' : 'update',
				   'table' : 'User' ,
				   'set' : [
				       {'attribute' : 'EmailAddress', 'value' : newValue}
				            ],
				   'where' : [
				       {'attribute' : 'UserID', 'value' : userID}
				              ]
				 };
	
	$.ajax({
		type		:	"POST",
		url			:	"../include/database.jsp",
		data		:	JSON.stringify(update),
		dataType	:	"json",
		success		:	function(data) {
			alert(data.message);
			document.location.reload(true);
		}
	});
}

/* Personal Info Editing */
function editPersonalInfo() {
	var firstName = $('#jsp-firstName').html();
	var lastName = $('#jsp-lastName').html();
	$('#jsp-firstName').html("<input id='editFirstName' type='text' placeholder='" + firstName + "'></input><!--&nbsp&nbsp<input type='button' value='save' onclick='submitNameChange()'></input>-->");
	$('#jsp-lastName').html("<input id='editLastName' type='text' placeholder='" + lastName + "'></input>&nbsp&nbsp<input type='button' value='save' onclick='submitNameChange()'></input>");
}

function submitNameChange() {
	var newFirstName = $('#editFirstName').val();
	var newLastName = $('#editLastName').val();
	var userID = getCookie("userID");
	
	if(newFirstName == "") {
		newFirstName = $('#editFirstName').attr("placeholder");
	}
	if(newLastName == "") {
		newLastName = $('#editLastName').attr("placeholder");
	}	
	
	var update = { 'type' : 'update',
			   	   'table' : 'User' ,
			   	   'set' : [
			   	            {'attribute' : 'FirstName', 'value' : newFirstName},
			   	            {'attribute' : 'LastName', 'value' : newLastName}
			   	           ],
			   	   'where' : [
			   	            {'attribute' : 'UserID', 'value' : userID}
			   	             ]
			
				 };
	$.ajax({
		type		:	"POST",
		url			:	"../include/database.jsp",
		data		:	JSON.stringify(update),
		dataType	:	"json",
		success		:	function(data) {
			alert(data.message);
			document.location.reload(true);
		}
	});
}