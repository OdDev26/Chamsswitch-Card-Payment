document.addEventListener("DOMContentLoaded", function() {
    var dataContainer = document.getElementById('dataContainer');
    var messageFromJava = dataContainer.getAttribute('data-message');
    document.write(messageFromJava)
});