# How Our App Works

## Overview

Our app has 3 main parts:

- **Frontend**: the React app the user sees and interacts with
- **Backend**: the Spring Boot app that handles logic and data access
- **Database**: the PostgreSQL database that stores course and prerequisite data

---

## Main Flow

1. The user searches for a course in the frontend.
2. The frontend sends an HTTP request to the backend.
3. The backend receives the request in a controller.
4. The service layer performs the prerequisite logic.
5. The repository layer gets the needed data from PostgreSQL.
6. The backend returns JSON.
7. The frontend renders the result as a graph or tree.

---

## Request Flow Example

### Frontend
The user searches for `CS 350`.

The React app sends a request like:

```http
GET /api/courses/CS350/graph
```

### Backend
The Spring Boot backend:

- receives the request
- finds the course
- looks up its prerequisites
- recursively builds the prerequisite structure
- returns graph-ready JSON

### Frontend Again
React receives the JSON and:

- creates nodes and edges
- displays the prerequisite graph
- applies colors or labels

---

## Responsibilities

### Frontend (React)
Handles:

- layout
- search input
- graph rendering
- styling
- user interaction

Should not handle:

- database access
- heavy prerequisite logic
- business rules

### Backend (Spring Boot)
Handles:

- API endpoints
- business logic
- recursive prerequisite processing
- data transformation into JSON
- communication with the database

Should not handle:

- UI layout
- visual styling
- browser interaction

### Database (PostgreSQL)
Handles:

- storing course data
- storing prerequisite relationships
- persistent data retrieval

Should not handle:

- UI rendering
- frontend logic
- application business logic

---

## Mental Model

```text
User -> React Frontend -> Spring Boot Backend -> PostgreSQL Database
User <- React Frontend <- Spring Boot Backend <- PostgreSQL Database
```

---

## Key Beginner Concepts

### Client vs Server
- **Client**: the side making the request
- **Server**: the side responding to the request

In our app:
- React in the browser is the client
- Spring Boot is the server

Resource:
- [MDN: Client-server overview](https://developer.mozilla.org/en-US/docs/Learn_web_development/Extensions/Server-side/First_steps/Client-Server_overview)

### HTTP
HTTP is how the frontend and backend communicate.

Common methods:
- `GET`: retrieve data
- `POST`: create data
- `PUT`: update data
- `DELETE`: remove data

Resource:
- [MDN: Overview of HTTP](https://developer.mozilla.org/en-US/docs/Web/HTTP/Guides/Overview)

### API
An API is the way one piece of software talks to another.

In our project, the frontend uses the backend API to request course data.

Resource:
- [IBM: What is a REST API?](https://www.ibm.com/think/topics/rest-apis)

### JSON
JSON is the format used to send structured data between backend and frontend.

Example:

```json
{
  "course": "CS301",
  "title": "Data Structures",
  "prerequisites": []
}
```

Resource:
- [MDN: JSON](https://developer.mozilla.org/en-US/docs/Learn_web_development/Core/Scripting/JSON)

### Controller
A controller receives HTTP requests and returns responses.

Example job:
- accept `GET /api/courses/CS301`
- call the service layer
- return JSON

Resource:
- [Spring: Building a RESTful Web Service](https://spring.io/guides/gs/rest-service)

### Service Layer
The service layer contains the main application logic.

For our app, this is where prerequisite graph-building logic belongs.

### Repository Layer
A repository fetches data from the database.

In Spring Boot, repositories often use JPA/Hibernate to access PostgreSQL.

Resource:
- [Spring: Accessing Data with JPA](https://spring.io/guides/gs/accessing-data-jpa)

### Database
The database stores persistent structured data.

In our app, PostgreSQL stores:
- courses
- prerequisite relationships
- requirement groupings

Resource:
- [PostgreSQL Tutorial](https://www.postgresql.org/docs/current/tutorial-start.html)

### Recursion
Recursion means a function calls itself.

This is useful because prerequisites can have their own prerequisites.

Example:
- CS 400 requires CS 300
- CS 300 requires CS 200
- CS 200 requires CS 100

Resource:
- [MDN: Recursion](https://developer.mozilla.org/en-US/docs/Glossary/Recursion)

### DTO
A DTO (Data Transfer Object) is an object used to send data from the backend to the frontend.

It helps keep API responses clean and separate from database entities.

Resource:
- [Baeldung: DTO Pattern](https://www.baeldung.com/java-dto-pattern)

### Entity
An entity is a backend object that maps to a database table.

Example:
- a `Course` entity may map to a `courses` table

Resource:
- [Baeldung: JPA Entities](https://www.baeldung.com/jpa-entities)

### Graph / Tree
Our visualization is a graph-like structure.

A tree is a kind of graph with parent-child structure.

Resource:
- [React Flow Learn](https://reactflow.dev/learn)

### State
State is data the frontend remembers and reacts to.

Examples:
- current search input
- returned graph data
- selected course

Resource:
- [React: State](https://react.dev/learn/state-a-components-memory)

---

## Most Important Idea

The frontend does **not** talk directly to the database.

The flow is:

**Frontend -> Backend -> Database**

not:

**Frontend -> Database**

---

## One Concrete Example

### User action
The user searches for `CS101`.

### Frontend request
```http
GET /api/courses/CS101/graph
```

### Backend flow
- controller receives the request
- service builds the prerequisite graph
- repository fetches data from PostgreSQL
- backend returns JSON

### Frontend result
React renders the graph on the page.

---

## Common Misunderstandings

### Does React talk directly to PostgreSQL?
No. React talks to the backend. The backend talks to PostgreSQL.

### Why not do all prerequisite logic in the frontend?
Because business logic and recursive data processing belong in the backend.

### Is the backend the same thing as the server?
Not exactly.

- **Backend**: the server-side code
- **Server**: the machine or process running that code

### Does the database return data to React?
No. The database returns data to the backend, and the backend returns JSON to React.

---

## Team Split

### Frontend work
- React components
- search UI
- graph rendering
- styling
- state handling

### Backend work
- controllers
- services
- repositories
- graph-building logic
- API response design

### Database work
- schema design
- course/prerequisite tables
- relationships
- seed/test data

---

## Summary

1. User searches for a course in React.
2. React sends an HTTP request to Spring Boot.
3. Spring Boot processes the request.
4. Spring Boot queries PostgreSQL.
5. Spring Boot returns JSON.
6. React renders the prerequisite visualization.
