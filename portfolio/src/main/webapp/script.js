fetch("navbar.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("header").innerHTML = data;
                });

fetch("footer.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("footer").innerHTML = data;
                });
    