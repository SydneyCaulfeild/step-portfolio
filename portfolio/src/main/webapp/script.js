// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
 
/**
 * Adds a random fact to the page.
 */
function addRandomFact() {
  const facts =
      ['I have never broken a bone', 'My last name spells the word "field" wrong', 'My favourite food is peanut butter', 'I was born in Toronto', 'I used to play provincial-level soccer', 'I have been in a hot air balloon'];
 
  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];
 
  // Add it to the page.
  const factContainer = document.getElementById('random-fact');
  factContainer.innerText = fact;
}

/**
 * Inserts a navigation bar.
 */
fetch("navbar.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("header").innerHTML = data;
                });

/**
 * Inserts a footer.
 */
fetch("footer.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("footer").innerHTML = data;
                });

/**
 * Inserts a list of my favourite cities.
 */

//fetching the JSON arraylist string from the server
function fetchFavoriteCities() {
  fetch('/data').then(response => response.json()).then((citiesList) => {
    console.log(citiesList);
    const citiesListElement = document.getElementById('cities-list-container');
    citiesListElement.innerHTML = '';
    citiesList.forEach((city) => {
        citiesListElement.appendChild(createListElement(city));
    });
  });
}

// Creates an <li> element containing text. 
function createListElement(text) {
  const liElement = document.createElement('li');
  liElement.innerText = text;
  return liElement;
}

/**
 * Builds the comments UI.
 */
//function called by blogposts.html's body onload 
function displayComments() {
  let commentsQuantity = document.getElementById('commentsQuantity').value;
  fetch("/add-comment?commentsQuantity="+commentsQuantity).then(response => response.json()).then((comments) => {
      const commentContainer = document.getElementById('comments-container');
      commentContainer.innerHTML = "";
      comments.forEach((comment) => {
        commentContainer.appendChild(createComment(comment))
      });
    });
}

function createComment(comment) {
  const nameElement = createHTML('h4', comment.name);
  const timeElement = createHTML('h5', comment.time);

  let headerHTML = document.createElement('div');
  headerHTML.className = "comment-heading";
  let headerElements = [nameElement, timeElement]
  headerElements.forEach((htmlElement) => {
    headerHTML.appendChild(htmlElement)
  });
  
  const contentElement = createHTML('h4', comment.message);

  let commentHTML = document.createElement('div');
  commentHTML.className = "comment";
  
  let commentElements = [headerHTML, contentElement];
  commentElements.forEach((htmlElement) => {
    commentHTML.appendChild(htmlElement)
  });
  return commentHTML;
}

function createHTML(type, content) {
    const htmlElement = document.createElement(type);
    htmlElement.innerHTML = content;
    return htmlElement;
}

/**
 * Builds the UI for the chart.
 */
google.charts.load('current', {'packages':['corechart']});
google.charts.setOnLoadCallback(drawChart);

/** Fetches data and uses it to create a chart. */
function drawChart() {
  fetch('/transit-data').then(response => response.json())
  .then((accessibilityRating) => {
    const data = new google.visualization.DataTable();
    data.addColumn('string', 'Province');
    data.addColumn('number', 'Percentage');
    Object.keys(accessibilityRating).forEach((province) => {
      data.addRow([province, accessibilityRating[province]]);
    });

    const options = {
      'title': 'Percentage of publicly owned public transit passenger stations and terminals that are accessible',
      'width':1000,
      'height':1500
    };

    const chart = new google.visualization.LineChart(
        document.getElementById('chart-container'));
    chart.draw(data, options);
  });
}