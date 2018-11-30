var createButton = function (postUrl) {
    var button = document.createElement("a");
    button.type = "button";
    button.text = "Add to Mangalib";
    button.classList.add("btn-comments");
    button.onclick = function () {
        $.post("http://heavensgat.es:8080/mangalib/mangas", postUrl, function(data, status){
            console.log("after");
        });
    };
    return button;
}
url = window.location.href;
if(url.includes("/manga/")){
    var container = document.getElementsByClassName("detailtopbtn")[0];
    var comment = document.getElementsByClassName("btn-comments")[0];
    container.removeChild(comment);
    container.appendChild(createButton(url));
}else{
    var listings = document.getElementsByClassName("cover-info");
    for(var index in listings){
        var listing = listings[index];
        var href = listing.getElementsByClassName("title")[0].getElementsByTagName("a")[0].href;
        listing.appendChild(createButton(href));
    }
}

