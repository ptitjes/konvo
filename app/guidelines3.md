Here are the software guidelines for your task:

**Project Structure:**
The project should be structured as follows:
```bash
alarm-clock/
├── src/
│   ├── main.ts
│   ├── index.html
│   ├── public/
│   └── node_modules/
└── package.json
```

**General Code Organization Principles:**
- All TypeScript files should have a `.ts` extension.
- All React components should be functional and follow the `hooks` pattern (i.e., use of `useState`, `useEffect`, etc.).
- The Electron's main process (`main.ts`) and renderer process should be clearly separated in different directories for better organization.

**File-Naming Conventions:**
- TypeScript files: Use camelCase with a descriptive name (e.g., `AlarmClockComponent.ts`).
- HTML/JavaScript files: Use lowercase with hyphens instead of underscores or camelCase (e.g., `index.html`, `clock-component.js`).

**Coding Conventions for Each Programming Language and Framework Used:**
1. **TypeScript:**
   - Import statements are sorted alphabetically.
   - Type annotations should be included for all variables, function parameters, and return types.
   - Use of enums instead of magic numbers.

2. **React:**
   - Components should have a unique, descriptive name (e.g., `AlarmClock`).
   - All React hooks should be used in the correct order (`useState`, `useEffect`, etc.).

3. **Electron:**
   - The main process and renderer process should communicate using IPC.
   - Use of Electron's built-in security features to prevent code injection from the renderer.

4. **Sqlite:**
   - Database schema should be defined in a separate file for better maintainability (e.g., `schema.sql`).
   - Queries should follow best practices like prepared statements and parameterized queries.

**Testing:**
- Unit tests should cover all functional components, hooks, and database interactions.
- Integration tests should verify the main process and renderer communication via IPC.

**Build Tools:**
To enforce these guidelines, use ESLint for code linting, Prettier for automatic formatting, and Jest with TypeScript for unit testing.