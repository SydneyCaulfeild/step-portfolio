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
 * Adds a random greeting to the page.
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

//function to insert a navigation bar at the top of every page
fetch("navbar.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("header").innerHTML = data;
                });

//function to insert a footer at the top of every page
fetch("footer.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("footer").innerHTML = data;
                });

//fetching the welcome message
function fetchWelcome() {
  fetch('/data').then(response => response.text()).then((message) => {
    document.getElementById('welcome-container').innerText = message;
  });
}