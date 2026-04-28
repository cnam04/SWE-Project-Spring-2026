How this works:

Request flow:
Page (React Route) -> Handler -> util -> api.js -> Backend API

PAGES:
- defines the route-level screen, ex. VisualizationPage, AdminPage
- composes sections/components for that screen
- triggers actions (claim listing, fetch listings, open details)
- no low-level fetch code

COMPONENTS:
- reusable UI pieces (cards, sidebars, map panels, navbar)
- receives props and emits UI events back up
- should be mostly presentation + light interaction logic
- no backend contract knowledge

SERVICES:
- contains feature-level frontend business logic for api calls
- pure helper logic 
- calls api.js helpers to hit backend endpoints
- shapes request/response data for UI use
- knows app rules, doesn't render UI

API LAYER (api.js):
- central request utility for HTTP calls
- handles base request behavior and shared request plumbing
- returns raw/near-raw response payloads to services

Example: Show Prereqs
- Page: VisualizationPage renders visualization and course search sidebar
- Component: VisualizationCard renders the react flow component state returned by the backend
- Service:   VisualizationService sends claim/fetch request through api.js and handles logic for building graph in react flow
- api.js:   Performs HTTP call to backend endpoint and returns response payload

