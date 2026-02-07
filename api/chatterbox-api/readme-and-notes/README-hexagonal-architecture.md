# README Heaxagonal Architecture

## Project layout

- 3 main packages: application, application.domain, adapter (Other layouts do exist)
    - Other valuable packages: common, application.common
- Domain must not have any type imported from application
- Application must not have any type imported from adapter
- Adapter may access types in Application or Domain
    - Adapter may also reference types defined in Domain directly
- Application.usecase / service can access application.port.in or application.port.out
  (Necessary in some cases)

## Developer Debates and Discussions

As software developers, we make the decisions (the alternative being to blindly repeat what we see mentioned elsewhere)
With this awareness in mind, here are some decisions made in this repo:

- Publicly stated opinions that the authors of this repo challenge:
  - Some 3rd party libraries should be allowed in the Domain layer, as long as risk is accepted
  - Spring annotations are allowed in the Application layer
      - The 'pure' model of Hexagonal Architecture is used like this: must not have any Spring or Jakarta references in
        application / domain packages
      - However, by allowing Spring in the application layer, we can then place (@Service, @Transactional) there
- The Architectural-rules maven dependency is particularly useful here
  - It encodes the expectations of the project
  - A fun exercise is to remove support for a type in the application package and then see how easy it is to address the
    issues mentioned in the tests
- Johan was also super helpful in this repo, offering advice on Hexagonal Architecture
- Another pattern explored in this repo is the decision that all logging statements are defined in one place
  - Logs are grouped together into separate classes, but they are wired into 1 master Logging class
  - An outcome of this is that logging is consistent, duplication is reduced, and only 1 type need be imported
  - Also decouples from frameworks in the event of moving to PURE hexagonal architecture down the line
  - Tests that need logs are bloated though, as several logging types must be imported. The
    `@ImportSlf4jWebhookLogger` is a lovely solution to this problem
- AI Tools greatly assisted in making this repository. Originally using ChatGPT in the browser and then swapping to Junie
  inside the IDE; the project has grown to this point with their help
  - Other resources that helped a lot are `Collaboration custom scripts` repo
- And the biggest value here is, of course, Webhooks!

## Quick mental model

- Domain = business rules
- Application = use case orchestration
- Adapter = technical details
- Common = shared code, from that layer inwards

The classes in the domain package are anaemic; essentially just data transfer objects. Business logic has been
chosen to be placed in the application package as much as possible.

## Where do ports (contracts) live?

Different authors have different opinions on where ports live.

What matters is who owns the abstraction and what is being protected from change.

In this project:

- Application defines the ports
- Adapters implement (most of) them
- Domain stays pure and dependency-free as much as possible

For this project, this resource was used as a source of inspiration:
[Hexagonal Architecture | HappyCoders.eu](https://www.happycoders.eu/software-craftsmanship/hexagonal-architecture/)

## Pros and Cons:

Pros to using Hexagonal Architecture:

- Intentionally place implementation details in a space where replacement is straightforward,
  without needing a large refactor
- The domain is kept pure and dependency-free; focussed on Ubiquitous Language ("Business Terms")
- The application layer becomes clean, focused and reusable in the event that an adapter is replaced
- Much easier to onboard new developers:
    - Entry Points are found in the 'IN' package, Exit Points in the 'OUT' package
    - Inserting new code is predictable
- Testing is much easier - only inject what is needed exactly for the test
- Architecture and Structure speaks for itself, and aids in code review

Cons to using Hexagonal Architecture:

- More work to set up
- More rules to be aware of / follow
- Can be overkill for simple projects
- Poorly designed ports can leak implementation details
- Risk of anaemic domain models (domain is just data holder)
  When Hexagonal Architecture Shines

When Hexagonal Architecture Shines:

- Multiple inbound channels (webhooks, polling, async)
- Multiple outbound integrations (vendors, APIs, delivery targets)
- Systems expected to evolve or live long-term
- Teams that value clarity over short-term speed

When It May Be Overkill

- Simple CRUD services
- Prototypes or spike solutions
- Very small teams with short project lifetimes

## Diagram

![example-hexagonal-architecture-and-connections.svg](../example-hexagonal-architecture-and-connections.svg)