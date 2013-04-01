function setCookie(name, value, days) {
	//initialize cookie expiration var    

    var expires = "";
    
    //Specify number of days to make cookie persistent
    if (days) {
        var date = new Date();
        // Convert number of days to milliseconds and add to current time to calculate expiration date
        date.setTime(date.getTime() + (days * 24 * 60 * 60 * 1000));
        expires = "; expires=" + date.toGMTString();
        
    }
    
    // set the cookie to the name, value, and expiration date
    document.cookie = name + "=" + value + expires + "; path=/";
}

function getCookie(name) {
    // Find the specified cookie and return its value
    var searchName = name + "=";
    var cookies = document.cookie.split(';');
    for(var i=0; i < cookies.length; i++) {
        var c = cookies[i];
        while (c.charAt(0) == ' ')
            c = c.substring(1, c.length);
        if (c.indexOf(searchName) == 0)
            return c.substring(searchName.length, c.length);    
    }
    return null;
}

function deleteCookie(name) {
    // Erase the specified cookie
    setCookie(name, "", -1);
}

function refreshCookie(name) {
	var value = getCookie(name);
	var date = new Date();
	date.setTime(date.getTime() + (2 * 60 * 60 * 1000));
	var expires = "; expires=" + date.toGMTString();
	
	// set the cookie to the name, value, and expiration date
    document.cookie = name + "=" + value + expires + "; path=/";
}

function checkCookieEnabled(nodeId) {
	setCookie('cookieTest','1',null);
	if(getCookie('cookieTest')) {
		deleteCookie('cookieTest');
	} else {
		document.getElementById(nodeId).style.display = 'block';
	}
}