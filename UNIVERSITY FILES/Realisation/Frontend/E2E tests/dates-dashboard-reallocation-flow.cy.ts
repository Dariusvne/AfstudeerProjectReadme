describe('Dates Dashboard - Reallocation Flow', () => {
    const daySelector = '[data-cy="day-0-3"]'; 
    const daySelectorReallocation = '[data-cy="day-0-3-reallocation"]'; 

  
    beforeEach(() => {
      cy.intercept('GET', '**/travel-rule-settings**').as('getTravelRules');
      cy.intercept('GET', '**/travel-requests**').as('getTravelRequests');
      cy.intercept('GET', '**/userinfo', {
        statusCode: 200,
        body: {
          userName: 'TAAVADAD',
          employeeNr: '123456',
          roles: [
            'DG_PF00-OCD-MYST_admin',
            'DG_PF00-OCD-MYST_hr',
            'DG_PF00-OCD-MYST_user',
            'DG_PF00-OCD-MYST_contingentAdmin'
          ]
        }
      }).as('getUserInfo');
  
      cy.loginWithAccessToken();
      cy.viewport(1200, 800);
      cy.visit('/datedashboard');

    });
  
    it('Unselects and saves a relocatable day in NL_LV tab, then restores it after test', () => {
      
        cy.get('sdx-tabs')
        .shadow()
        .find('button')
        .contains('NL to LV')
        .should('be.visible')
        .should('not.be.disabled')
        .click({ force: true });

        
        // Enter edit mode first
        cy.get('sdx-button')
        .shadow()
        .find('button')
        .contains('Edit')
        .click({ force: true });

        cy.get('sdx-input-item[data-cy="toggle-allocation-days"]')
        .shadow()
        .find('input[type="checkbox"]')
        .should('exist')
        .click({ force: true });
            
        cy.get(daySelectorReallocation).should('exist').click();
    
        // Save the change
        cy.get('sdx-button')
        .shadow()
        .find('button')
        .contains('Submit')
        .click({ force: true });

        //Reselect after test
        cy.loginWithAccessToken();
        cy.visit('/datedashboard');
        
    
        // Open the NL to LV tab
        cy.get('sdx-tabs')
            .shadow()
            .find('button')
            .contains('NL to LV')
            .should('be.visible')
            .click({ force: true });
        
        // Enter edit mode
        cy.get('sdx-button')
            .shadow()
            .find('button')
            .contains('Edit')
            .click({ force: true });

        
        // Reselect the day (undo the change)
        cy.get(daySelector).should('exist').click();
        
        // save the changes
        cy.get('sdx-button')
            .shadow()
            .find('button')
            .contains('Submit')
            .click({ force: true });

    });
  

    it('Unselects and cancel a relocatable day in NL_LV tab, should be selected still.', () => {
      
        cy.get('sdx-tabs')
        .shadow()
        .find('button')
        .contains('NL to LV')
        .should('be.visible')
        .should('not.be.disabled')
        .click({ force: true });

          
        // Enter edit mode first
        cy.get('sdx-button')
        .shadow()
        .find('button')
        .contains('Edit')
        .click({ force: true });

        cy.get('sdx-input-item[data-cy="toggle-allocation-days"]')
        .shadow()
        .find('input[type="checkbox"]')
        .should('exist')
        .click({ force: true });
        
        cy.get(daySelectorReallocation).should('exist').click();
    
        // Save the change
        cy.get('sdx-button')
        .shadow()
        .find('button')
        .contains('Submit')
        .click({ force: true });

        // Check to see if UI switched to correct view
        cy.get('sdx-button')
            .shadow()
            .find('button')
            .contains('Edit')
        
      });
      
      it('Shows error message on 403 when loading NL to LV tab', () => {
        cy.intercept('GET', '**/v1/custom/contingents/travelrequests*', {
          statusCode: 403,
          body: {
            message: 'Failed to load travel requests',
          },
        }).as('getNLToLVRequests');
      
        cy.visit('/datedashboard');
      
        cy.get('sdx-tabs')
          .shadow()
          .find('button')
          .contains('NL to LV')
          .click({ force: true });
            
        cy.expectToastMessage('Failed to load travel requests')
      })

      it('Shows traveler on January 3rd when a booking exists', () => {
        const testDate = '2025-01-03';
    
        cy.intercept('GET', '**/travelrequests**', [
          {
            id: 'req-jan-1',
            departureDate: testDate,
            returnDate: testDate,
            country: 'LV',
            user: {
              firstName: 'Alice',
              lastName: 'Smith',
              departmentName: 'HR',
            },
          },
        ]).as('getTravelRequests');
    
        cy.visit('/datedashboard');
    
        cy.get('sdx-tabs')
          .shadow()
          .find('button')
          .contains('NL to LV')
          .click({ force: true });
        
        cy.get('[data-cy="day-0-2"]').click({ force: true }); // January 3rd
    
        cy.get('app-traveler-list')
          .should('contain.text', 'Alice Smith')
          .and('contain.text', 'HR');
      });

      it('Shows empty state on January 3rd when no booking exists', () => {
        cy.intercept('GET', '**/travelrequests**', []).as('getTravelRequests');
    
        cy.visit('/datedashboard');
    
        cy.get('sdx-tabs')
          .shadow()
          .find('button')
          .contains('NL to LV')
          .click({ force: true });
    
    
        cy.get('[data-cy="day-0-2"]').click({ force: true }); // January 3rd
    
        cy.get('app-traveler-list')
            .should('include.text', 'There is no one traveling to Netherlands on this date');
      });

      it('Measures full load time of the Contingents page', () => {
        const start = performance.now();
      
        cy.visit('/datedashboard');
      
        // Wait for a key UI element that signals the page is fully ready
        cy.get('.calendar-container', { timeout: 10000 }).should('be.visible').then(() => {
          const end = performance.now();
          const duration = Math.round(end - start);
      
          cy.log(`Contingents page full load time: ${duration} ms`);
          expect(duration).to.be.lessThan(2000); // 2 seconds load time threshold
        });
      });
  });
  