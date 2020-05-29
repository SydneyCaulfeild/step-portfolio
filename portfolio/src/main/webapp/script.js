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
      ['I have never broken a bone', 'My last name spells the word "field" wrong', 'My favourite food is peanut butter'];
 
  // Pick a random greeting.
  const fact = facts[Math.floor(Math.random() * facts.length)];
 
  // Add it to the page.
  const factContainer = document.getElementById('random-fact');
  factContainer.innerText = fact;
}

//insert a navigation bar at the top of all pages
fetch("navbar.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("header").innerHTML = data;
                });

//add a footer at the bottom of all pages
fetch("footer.html")
                .then(response => {
                    return response.text();
                })
                .then(data => {
                    document.querySelector("footer").innerHTML = data;
                });
