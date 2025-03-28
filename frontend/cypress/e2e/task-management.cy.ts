describe('Task Management', () => {
  beforeEach(() => {
    cy.visit('/')
    // Wait for initial load
    cy.contains('Task Manager').should('be.visible')
  })

  it('should create a new task and update it', () => {
    // Create a new task
    cy.get('input[type="text"]').type('Create a presentation for next week{enter}')
    
    // Wait for task to be created
    cy.contains('Create a presentation for next week').should('be.visible')
    
    // Click reply button on the task
    cy.get('button').contains('reply').first().click()
    
    // Update the task
    cy.get('input[type="text"]').type('Change deadline to Friday{enter}')
    
    // Verify update was successful
    cy.contains('Successfully updated task').should('be.visible')
  })

  it('should handle task completion', () => {
    // Create a task
    cy.get('input[type="text"]').type('Test task for completion{enter}')
    
    // Wait for task to be created
    cy.contains('Test task for completion').should('be.visible')
    
    // Click the complete button (unchecked circle icon)
    cy.get('button').find('[data-testid="RadioButtonUncheckedIcon"]').first().click()
    
    // Verify task is marked as completed (check for strikethrough or completed status)
    cy.contains('Test task for completion')
      .should('have.css', 'text-decoration', 'line-through')
  })

  it('should handle task refinement conversation', () => {
    // Start with a vague task
    cy.get('input[type="text"]').type('I want to learn programming{enter}')
    
    // AI should ask for more details
    cy.contains('Please provide more details').should('be.visible')
    
    // Provide additional details
    cy.get('input[type="text"]').type('I want to learn Python for data science{enter}')
    
    // Verify task was created with refined details
    cy.contains('Python').should('be.visible')
    cy.contains('data science').should('be.visible')
  })
})