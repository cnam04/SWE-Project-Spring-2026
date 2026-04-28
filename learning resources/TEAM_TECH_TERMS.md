# Team Tech Terms

## Frontend
The part of the app the user sees and interacts with in the browser.

For our project:
- React UI
- search bar
- graph display
- buttons and menus

## Backend
The server-side code that handles logic and data processing.

For our project:
- Spring Boot API
- prerequisite logic
- request handling
- sending JSON back to the frontend

## Server
The machine or process running the backend code.

In casual conversation, people often use "server" and "backend" almost interchangeably.

## Database
Where persistent app data is stored.

For our project:
- PostgreSQL
- courses
- prerequisite relationships
- requirement data

## Client
The side that makes a request.

In our project, the browser/React app is the client.

## HTTP
The protocol used for communication between frontend and backend.

Common methods:
- GET = read data
- POST = create data
- PUT = update data
- DELETE = remove data

Learn more:
- https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview

## API
A defined way for one piece of software to talk to another.

In our project, React talks to the Spring Boot backend through an API.

Learn more:
- https://www.ibm.com/think/topics/rest-apis

## Endpoint
A specific backend URL the frontend can call.

Examples:
- `/api/courses`
- `/api/courses/CS301`
- `/api/courses/CS301/graph`

## Request
A message sent from the client to the server asking for something.

Example:
- React asks the backend for prerequisite data

## Response
The data the server sends back after handling a request.

Example:
- the backend returns course/prerequisite JSON

## JSON
A text format used to send structured data between frontend and backend.

Example:
```json
{
  "course": "CS301",
  "title": "Data Structures"
}
```

Learn more:
- https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Scripting/JSON

## Controller
A backend class that receives HTTP requests and returns responses.

Typical job:
- accept request
- call service layer
- return JSON

Learn more:
- https://spring.io/guides/gs/rest-service

## Service Layer
The part of the backend that contains the main application logic.

For our project:
- build prerequisite graphs
- apply requirement rules
- organize data for the frontend

## Repository
A backend layer used to read and write data from the database.

In Spring Boot, repositories often use JPA/Hibernate.

Learn more:
- https://spring.io/guides/gs/accessing-data-jpa

## Entity
A backend object that usually maps to a database table.

Example:
- a `Course` entity might map to the `courses` table

Learn more:
- https://www.baeldung.com/jpa-entities

## DTO
Data Transfer Object.

An object used to send data between layers, especially from backend to frontend.

Why use it:
- cleaner responses
- safer than exposing raw entities
- easier to shape data for the UI

Learn more:
- https://www.baeldung.com/java-dto-pattern

## Business Logic
The actual rules of how the app works.

For our project:
- what counts as satisfying a prerequisite
- how AND/OR requirement groups behave
- which courses are available or locked

## Recursion
When a function calls itself.

Useful for our project because a course can have prerequisites, and those prerequisites can have their own prerequisites.

Learn more:
- https://developer.mozilla.org/en-US/docs/Glossary/Recursion

## State
Data the frontend is currently holding onto and using to render the UI.

Examples:
- current search input
- selected course
- returned graph data

Learn more:
- https://react.dev/learn/state-a-components-memory

## Component
A reusable frontend building block in React.

Examples:
- search bar
- graph view
- course node
- side panel

## Props
Data passed from one React component to another.

## Graph
A set of nodes and connections.

In our project, courses are nodes and prerequisite relationships are edges.

## Tree
A type of graph with a root and children.

A prerequisite chain can look tree-like, though some course relationships may become more graph-like.

## Node
A single item in a graph.

In our project, a course is usually a node.

## Edge
A connection between two nodes.

In our project, an edge represents a prerequisite relationship.

## Full Stack
A project that includes frontend, backend, and database work.

Our app is full stack because it includes:
- React
- Spring Boot
- PostgreSQL

## The Most Important Rule
The frontend does not talk directly to the database.

The flow is:

`Frontend -> Backend -> Database`
`Frontend <- Backend <- Database`
