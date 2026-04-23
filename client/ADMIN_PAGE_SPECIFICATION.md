Here’s a solid first-pass spec for the **Admin Page** that fits your current project instead of drifting into extra features.

This matches your original requirements that the tech administrator should have a protected admin login and be able to modify the course database, including course/prerequisite updates . It also matches your current course API plan, where the backend already supports listing courses, loading a course with a full prerequisite tree, editing course fields separately, and replacing the full prerequisite tree when prerequisites are edited . Your class design also supports recursive prerequisite structures with `COURSE`, `AND`, and `OR` requirement components, so the admin UI should be built around editing a tree, not just a flat checklist .

## Admin Page Specification

### 1. Purpose

The Admin Page allows authorized admins to manage system data through a protected interface. The page must let an admin switch between a **Users view** and a **Courses view**. In the Courses view, the admin must be able to:

* view all courses
* select a course and view its prerequisite tree
* edit course information
* edit the course’s prerequisite structure

### 2. Access Control

* Only authenticated users with the `admin` role may access the Admin Page.
* Non-admin users attempting to access the page should be blocked and shown an authorization error or redirected away.
* The Admin Page should not be visible in navigation for non-admin users.

### 3. Page Structure

The Admin Page contains:

* a page title: **Admin**
* a top-level segmented control or tabs:

  * **Users**
  * **Courses**
* one active view at a time

Default behavior:

* when the page opens, the **Courses** tab should be the default active tab

That’s the right default because course maintenance is clearly the core admin need in your requirements doc, while user management is secondary and more basic right now .

---

## 4. Users View Specification

### 4.1 Purpose

The Users view gives admins a simple way to inspect system users.

### 4.2 Displayed Data

The Users view should display a table of users. Each row should show:

* user id
* name
* email
* role
* created at

Optional if available later:

* linked student id
* school student id

### 4.3 User Actions

For MVP, the admin should be able to:

* view all users
* search/filter users by name, email, or role
* select a user to view more details

Good constraint: keep this view mostly read-only for now unless you actually need admin user editing. Do not invent a bunch of user-management complexity unless your professor specifically wants it.

### 4.4 Empty State

If no users exist:

* show “No users found.”

### 4.5 Error State

If the users list fails to load:

* show an error message
* allow retry

---

## 5. Courses View Specification

### 5.1 Purpose

The Courses view is the main admin workspace for managing course data and prerequisite rules.

### 5.2 Layout

Use a 2-panel layout:

**Left panel**

* searchable list/table of all courses

**Right panel**

* selected course details
* prerequisite tree viewer
* edit controls

This is the cleanest setup because admins need to browse a lot of courses quickly and then inspect/edit one at a time.

### 5.3 Course List

The Courses view must show all courses in a table or list.

Each course row should display:

* course code
* crn
* title
* credits
* attributes
* optional active/inactive status later

The list should support:

* search by course code
* search by title
* optional search by CRN
* sorting by course code or title

This lines up with your project’s search-oriented design and existing strategy-based course search direction .

### 5.4 Course Selection

When an admin selects a course:

* the system loads full course details
* the system loads the course’s prerequisite tree
* the selected course is highlighted in the list
* the right panel updates to show the selected course

This maps directly to your planned `GET /api/courses/{id}` endpoint, which returns both course fields and a user-friendly prerequisite tree JSON .

---

## 6. Selected Course Detail Section

When a course is selected, display:

### 6.1 Basic Course Information

* course id
* course code
* crn
* title
* credits
* attributes

### 6.2 Prerequisite Tree Section

Display the course’s prerequisite tree in a readable visual form.

If the course has no prerequisites:

* show “This course has no prerequisites.”

If the course has prerequisites:

* render the tree using nested groups for:

  * `COURSE`
  * `AND`
  * `OR`

Example display ideas:

* `CPS210`
* `(CPS210 AND CPS220)`
* `((CPS310 AND CPS320) OR CPS330)`

Do not force React Flow here unless you want it. For admin editing, a structured tree editor is probably more practical than a fancy graph.

---

## 7. Edit Course Information

### 7.1 Edit Trigger

The selected course panel should include an **Edit Course** button.

### 7.2 Editable Fields

The admin can edit:

* course code
* crn
* title
* credits
* attributes

The admin cannot edit prerequisite data in this form.

That separation is already built into your backend spec, where basic course edits use `PATCH /api/courses/{id}` and prerequisite edits use a different endpoint .

### 7.3 Form Behavior

When edit mode opens:

* fields are pre-filled with current values
* admin can save or cancel

### 7.4 Validation Rules

* course code is required
* title is required
* credits must be a non-negative integer
* CRN format rules should be enforced if you decide to require a specific format
* attributes may be empty

### 7.5 Save Behavior

On save:

* send updated basic course info to backend
* do not send prerequisite tree data in this request
* refresh displayed course summary after success
* show success feedback

On failure:

* show validation or save error
* keep unsaved form data visible

---

## 8. Edit Prerequisites

This is the part that actually matters.

### 8.1 Edit Trigger

The selected course panel should include an **Edit Prerequisites** button.

### 8.2 Editing Model

The prerequisite editor must support the same logical structure as your domain and backend:

* single `COURSE` prerequisite
* `AND` groups
* `OR` groups
* nested combinations of those groups

That is required because your class model and API are built around recursive requirement structures, not just flat selected courses .

### 8.3 Editor Capabilities

The admin must be able to:

* add a prerequisite tree to a course with none
* remove the entire prerequisite tree
* add a course leaf node
* add an `AND` group
* add an `OR` group
* nest groups inside groups
* remove a node
* reorder child nodes within a group
* replace one node with another

### 8.4 Recommended Interaction Model

Best practical UI:

* show the tree as nested cards or rows
* each node has a small local action menu
* group nodes show their type: `AND` or `OR`
* course leaf nodes let the admin search/select an existing course
* admin edits the whole tree in memory, then saves the full tree at once

That matches your API decision to send the **entire tree** back on prerequisite update instead of patching individual nodes .

### 8.5 Save Behavior

When admin saves prerequisite edits:

* frontend sends the entire prerequisite tree
* backend replaces the old tree with the new validated tree
* refreshed returned data is displayed after success

### 8.6 Cancel Behavior

If admin cancels:

* discard unsaved prerequisite edits
* revert to last saved tree

---

## 9. Prerequisite Validation Rules

These should be enforced in the backend and reflected in the UI, because you already defined them in your API notes .

### 9.1 Valid Tree Rules

* a course may have no prerequisites
* the root may be a single `COURSE` node
* `AND` and `OR` nodes must have at least 2 children
* referenced prerequisite courses must already exist
* a course may not require itself
* indirect cycles are not allowed
* duplicate children under the same parent should not be allowed
* the tree must have exactly one valid root when present
* every child subtree must also be valid

### 9.2 Validation Feedback

If validation fails, the admin should see clear messages such as:

* “A course cannot require itself.”
* “AND groups must contain at least 2 children.”
* “Referenced prerequisite course does not exist.”
* “This prerequisite structure creates a cycle.”
* “Duplicate prerequisite child found in the same group.”

Do not show generic “invalid tree” garbage. That will make debugging miserable.

---

## 10. Add Course Capability

Your earlier requirements mention that admins should be able to add new courses and fill in course details , and your API spec already includes `POST /api/courses/` with an optional initial prerequisite tree .

So in the Courses view, include:

* an **Add Course** button above the course list

The Add Course flow should allow:

* entering basic course information
* optionally defining an initial prerequisite tree
* saving the course
* showing the created course in the list immediately

---

## 11. Delete / Deactivate Capability

Your API notes say deletion should come later and should really be an inactive flag, not a hard delete .

So for now:

* either omit delete from the UI
* or show a disabled/hidden **Deactivate Course** control marked “coming later”

That keeps the UI aligned with the backend reality.

---

## 12. Backend Dependencies

The Courses view depends on these backend operations already defined in your API doc:

* `GET /api/courses/` for all course summaries
* `GET /api/courses/{id}` for one course plus tree
* `POST /api/courses/` to create a course with optional tree
* `PATCH /api/courses/{id}` for basic course info edits
* `PUT /api/courses/{id}/prerequisites` for full prerequisite tree replacement 

The prerequisite editing workflow also matches your normalized database design, where:

* the course stores a `root_prerequisite_node_id`
* prerequisite nodes store `AND`, `OR`, or `COURSE`
* node edges define parent/child tree structure 

That matters because the admin UI should think in terms of one editable tree, while the backend handles translating that into normalized relational rows.

---

## 13. Acceptance Criteria

### 13.1 Admin Page Navigation

* admin can open the Admin Page
* admin can switch between Users and Courses views
* non-admin cannot access the page

### 13.2 Users View

* admin can view all users
* admin can search/filter users
* admin can select a user and inspect details

### 13.3 Courses View

* admin can view all courses
* admin can search/filter the course list
* admin can select a course
* selected course shows course info and prerequisite tree

### 13.4 Course Editing

* admin can edit basic course fields without editing prerequisites
* changes persist after save
* cancel leaves existing data unchanged

### 13.5 Prerequisite Editing

* admin can create, edit, and remove prerequisite trees
* admin can build nested `AND`/`OR` structures
* invalid trees are blocked with clear error messages
* saved trees reload correctly from backend

### 13.6 Add Course

* admin can create a new course
* admin may optionally assign prerequisites at creation
* new course appears in the course list after success

---

## 14. Straight recommendation

Do **not** make the prerequisite editor a dropdown checklist like the older elicitation doc says. That was okay at the very beginning, but it is too weak for the recursive AND/OR model you have now. Your current class design and API support full tree structures, so the admin page should use a **nested prerequisite tree editor** instead of pretending prerequisites are flat   .

If you want, I can turn this into a cleaner **requirements doc format** with sections like:
**Overview, Functional Requirements, Non-Functional Requirements, User Flows, and Acceptance Criteria**.
