# Simple Clock Application Guidelines

## Project Structure
The project structure should follow a conventional layout to keep everything organized:

```bash
clock-app/
├── src/
│   ├── main.ts  # The entry point of the application
│   ├── renderer/
│   │   └── App.tsx  # The React component tree for the Electron browser window
│   ├── AlarmManager.ts  # Handles alarm persistence and notification logic
│   └── types/  # Type definitions for TypeScript
└── package.json  # Project metadata and dependencies
```

## General Code Organization Principles
1. **Modularity**: Each part of the application (like the clock, alarms, etc.) should be encapsulated in its own module or file.
2. **Reusability**: Reuse code where possible to maintain consistency and simplify updates.

## File-Naming and Coding Conventions

### TypeScript and JavaScript Files
- Use camelCase for variable names.
- Use consistent naming conventions for functions, classes, variables, and properties (e.g., `setAlarm`).
- Indent using 2 spaces for all code blocks.
- Keep a maximum of 80 characters per line.
- For React components:
  - Name components in PascalCase.
  - Pass props and use state directly instead of accessing `this.state`.

### SQL and SQLite Files
- Follow standard SQL naming conventions (snake_case) for database tables, views, indexes, triggers, functions, etc.
- Use transactions to ensure atomicity.

## Testing

### Unit Tests
- Write unit tests for individual modules using Vitest.
- Ensure each function or class is tested thoroughly.
- Use `vitest.mock` to mock dependencies when necessary.

### Integration and End-to-End Tests
- Write integration tests to test how components interact with each other.
- For end-to-end testing, use Electron's support for interacting with the application window programmatically.

## Build Tools
- Use ESLint for code quality checking and enforcing coding conventions.
- Use Prettier for formatting and auto-correcting style issues in TypeScript and JavaScript files. 

### Dependencies Management
- Pin versions of dependencies where necessary to ensure reproducibility.
- Keep a list of used libraries, frameworks, and their versions in the project's `package.json`.