function loginButton() {
    var userName = document.getElementById("username").value;
    var passWrd = document.getElementById("password").value;
    if (userName== "test" && passWrd == "test") {
        writeCookie("trifles", userName, 365);
        window.location ="../trifles-project/about3.html";
    }    
    else
        alert("Oops");
}

function logoutButton() {
    userName = readCookie('trifles')
    if (userName) {
        alert(":( We\'ll miss you!" + userName);
       eraseCookie('trifles');
       window.location = "../trifles-project/about3.html";

    }
}

function checkLoggedIn() {
    var loggedIn = readCookie("trifles");
    if (loggedIn) {
        document.getElementById("expand").style.visibility = "hidden";
        document.getElementById("expand").style.height = "0px";
        document.getElementById("hide_button").style.visibility = "hidden";
        document.getElementById("hide_button").style.height = "0px";
        document.getElementById("logout_button").style.visibility = "visible";
    }
    else {
        document.getElementById("expand").style.visibility = "visible";
        //document.getElementById("hide_button").style.visibility = "hidden";
        document.getElementById("logout_button").style.visibility = "hidden";
        }
}



