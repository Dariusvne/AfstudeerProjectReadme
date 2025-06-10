/// <reference types="cypress" />
declare namespace Cypress {
  interface Chainable<Subject> {

    loginWithAccessToken(): Chainable<Subject>;

    expectToastMessage(expectedMessage: string): Chainable<Subject>;
  }
}

let departureDate: string;
let returnDate: string;
let officeArrivalDate: string;
let nwdOne: string;
let nwdTwo: string;
let nwdThree: string;
let nonWorkingDaysString: string;
let purposeOfTravel: any;


Cypress.Commands.add('loginWithAccessToken', () => {
  const jwt = 'eyJhbGciOiJSUzI1NiIsImprdSI6Imh0dHBzOi8vbG9jYWxob3N0OjgwODAvdWFhL3Rva2VuX2tleXMiLCJraWQiOiJsZWdhY3ktdG9rZW4ta2V5IiwidHlwIjoiSldUIn0.eyJqdGkiOiI5MDJlYWQ4YjAzYTE0MjBmOGJjOGM1NWEwN2YwMTA3NCIsImNsaWVudF9hdXRoX21ldGhvZCI6Im5vbmUiLCJzdWIiOiI0NWZmYmU1YS05YjAxLTQwNzYtOGI2Ni1jZjVjZjgzYzczZjIiLCJzY29wZSI6WyJvcGVuaWQiLCJwcm9maWxlIiwicm9sZXMiLCJ1c2VyX2F0dHJpYnV0ZXMiXSwiY2xpZW50X2lkIjoiYkx6STFkNGJmYjIzdVVnUGxWNjFZbnNqRkd6SkZzdmw0RUF4eVFNODNBR2J6blBpOTR4ZUZITXAwcFFvaGxGQyIsImNpZCI6ImJMekkxZDRiZmIyM3VVZ1BsVjYxWW5zakZHekpGc3ZsNEVBeHlRTTgzQUdiem5QaTk0eGVGSE1wMHBRb2hsRkMiLCJhenAiOiJiTHpJMWQ0YmZiMjN1VWdQbFY2MVluc2pGR3pKRnN2bDRFQXh5UU04M0FHYnpuUGk5NHhlRkhNcDBwUW9obEZDIiwiZ3JhbnRfdHlwZSI6ImF1dGhvcml6YXRpb25fY29kZSIsInVzZXJfaWQiOiI0NWZmYmU1YS05YjAxLTQwNzYtOGI2Ni1jZjVjZjgzYzczZjIiLCJvcmlnaW4iOiJvcGVuYW0iLCJ1c2VyX25hbWUiOiJUQUFWQURBRCIsImVtYWlsIjoiRGFyaXVzLnZhbkVzc2VuQHN3aXNzY29tLmNvbSIsImF1dGhfdGltZSI6MTc0OTQ5Mjc2MywicmV2X3NpZyI6IjMwZDJiMzBhIiwiaWF0IjoxNzQ5NDkyNzY3LCJleHAiOjE3NDk1MDg5NjcsImlzcyI6Imh0dHBzOi8vc3NvLWNvcnByb290LXYyLW1mYS5kZXYtc2NhcHAuc3dpc3Njb20uY29tL29hdXRoL3Rva2VuIiwiemlkIjoidWFhIiwiYXVkIjpbIm9wZW5pZCIsImJMekkxZDRiZmIyM3VVZ1BsVjYxWW5zakZHekpGc3ZsNEVBeHlRTTgzQUdiem5QaTk0eGVGSE1wMHBRb2hsRkMiXX0.cjyZ_ScWwWPqqcG2RuqcAJTuQigm0M8NDj_AuSOaFMvuDFVMTSyS2mE1cr9G5rwYPya2pCR66jvn0PQnu-6KzieL8tO3zgBCTpQ-2LRh4_pdULsYOuMAy8ECWo35xt3NwMiVgT8IQYJ5LrdSvPR7RAOgxlH-gVdpl3VYAxQfkNqGSsad-7lzblzSIp4Q5DoiuDvVRvgzWCdA6srEYFQBtjSSpbB-jHU6a3fcWQX5FHP7XUJq2gC_3MiIg5uwda1C8tyqZ4Sbknz0DIPDR8FXdff3XOTKAIM67FaBVPi8io4E_NKGxs3DoYbha4K5at8lppxT1gZ-abA4MWutDbM70w'

    // Set access_token cookie
    cy.setCookie('access_token', jwt, {
      path: '/',         // good to specify
      httpOnly: false,   // must be false so Cypress can write it
      secure: false,     // false for localhost or HTTP
      sameSite: 'lax'    // typical for dev environments
    });

    // Optional: wait for cookie to be set
    cy.getCookie('access_token').should('exist');
});

Cypress.Commands.add('expectToastMessage', (expectedMessage: string) => {
  cy.document().then((doc) => {
    const getToastCardHTML = () => {
      const header = doc.querySelector('sdx-header');
      const toasts = header?.shadowRoot?.querySelector('sdx-toasts');
      const card = toasts?.shadowRoot?.querySelector('sdx-card');
      return card?.innerHTML?.trim() || null;
    };

    cy.wrap(null, { timeout: 10000 }).should(() => {
      const toastHTML = getToastCardHTML();
      console.log('Toast HTML content:', toastHTML);

      expect(toastHTML).to.contain(expectedMessage);
    });
  });
});

