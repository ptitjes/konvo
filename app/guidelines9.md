**Simple Clock Application Guidelines**
======================================

**Project Structure**
-------------------

- The project should be structured in the following manner:
  - `src/` (main directory)
    - `components/`
      - `digitalClock.js`
      - `analogicClock.js`
    - `containers/`
      - `App.js`
      - `AlarmSettings.js`
    - `hooks/`
      - `useTimer.js`
    - `models/`
      - `alarm.model.ts`
    - `services/`
      - `alarms.service.ts`
    - `types/`
      - `index.d.ts` (contains all type definitions)
  - `tests/` (directory for unit and integration tests)
  - `main.ts` (entry point of the application)

**General Code Organization Principles**
--------------------------------------

- Use functional components for views.
- Use hooks to manage state and side effects.
- Separate business logic from presentation layer.

**File-Naming and Coding Conventions**
------------------------------------

### TypeScript

- File names should follow PascalCase convention.
- Function names should be in camelCase.
- Variable names should be descriptive and in camelCase.

### React

- Component files should end with `.js` (not `.tsx`).
- Use `import` statements instead of `require`.

**Testing**
------------

- Use Vitest for unit testing.
- Use Jest for integration testing.

### Tests Directory Structure
  - `__tests__/`
    - `units/`
      - `components/`
        - `digitalClock.test.js`
        - `analogicClock.test.js`
      - `services/`
        - `alarms.service.test.ts`
    - `integration/`
      - `app.integration.test.js`

**Build Tools**
----------------

- Build tool: Webpack (with Babel for TypeScript support).

### Build Configuration
  - In the `package.json`, add scripts like:
    ```json
    "scripts": {
      "start": "webpack ./src/main.ts --output ./build/index.html",
      "test": "vitest tests/__tests__"
    }
    ```

**Sqlite**
----------

- Use a library to interact with Sqlite database in TypeScript.
- Use transactions for data integrity.

### Database Schema
  - Define the schema using type-safe migrations (e.g., `sql-migrate`).

These guidelines provide a structured approach to developing and testing your simple clock application, ensuring maintainability, readability, and testability.