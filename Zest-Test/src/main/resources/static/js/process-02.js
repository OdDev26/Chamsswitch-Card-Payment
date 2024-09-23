document.addEventListener("DOMContentLoaded", function() {
    var acsUrlContainer = document.getElementById('acsUrl');
    var mdlContainer = document.getElementById('md');
    var jwtContainer = document.getElementById('jwt');
    var acsUrl = acsUrlContainer.getAttribute('data-message');
    var mdl = mdlContainer.getAttribute('data-message');
    var jwt = jwtContainer.getAttribute('data-message');
  document.write(`<body onload="form1.submit()"><form id="form1" action=${acsUrl} method="post"><input name="JWT" value=${jwt}><input name="MD" value=${md}> </form></body>`
  );
 });
