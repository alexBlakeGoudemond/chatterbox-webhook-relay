describe('Observability - Prometheus scraping Chatterbox', () => {
    const PROMETHEUS_URL = 'http://localhost:9090/query'
    const METRIC_NAME = 'webhook_payload_successes_total'

    it('shows Chatterbox metric in Prometheus UI', () => {
        cy.visit(PROMETHEUS_URL)
        
        // Prometheus UI loaded
        cy.contains('Prometheus').should('be.visible')
        
        // Enter metric
        cy.get('[contenteditable="true"].cm-content')
            .first()
            .should('be.visible')
            .clear()
            .type(METRIC_NAME)
        
        // Execute query
        cy.contains('Execute').click()
        
        // Metric appears (allow scrape delay)
        cy.contains(METRIC_NAME, { timeout: 30000 })
            .should('be.visible')
    })
})
