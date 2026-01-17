# README Heaxagonal Architecture

## Common layout 

- 3 main packages: application, domain, infrastructure (Other layouts do exist)
- Domain must not have any type imported from application
- Application must not have any type imported from infrastructure
- Infrastructure may access types in Application or Domain
  - Infrastructure may also reference Domain directly (if port is in domain)
- Can make decisions: 
  - Application must not have any Spring or Jakarta references
  - May allow @Service or @Transactional in Application only

## Quick mental model

- Domain = business rules
- Application = use case orchestration
- Infrastructure = technical details

## Where do ports live?

Common options:

### Option A (classic hexagonal):

- Domain defines inbound/outbound ports (interfaces)

### Option B (clean architecture style):

- Application defines inbound use-case interfaces
- Domain defines repository ports
