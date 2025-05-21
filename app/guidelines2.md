Here are the guidelines for developing your application:

### Project Structure
The project should be structured into the following folders:
```
project/
├── src/
│   ├── main.ts
│   ├── public/
│   │   └── index.html
│   ├── renderer.tsx
│   ├── components/
│   │   ├── Clock.tsx
│   │   └── Alarms.tsx
│   ├── containers/
│   │   └── AppContainer.tsx
│   ├── models/
│   │   └── AlarmModel.ts
│   ├── services/
│   │   └── AlarmService.ts
│   ├── utils/
│   │   └── timeUtils.ts
│   └── db/
│       └── alarms.db
├── package.json
└── tsconfig.json
```

### General Code Organization Principles

- Use a consistent coding style throughout the project.
- Keep related logic within the same file or group of files (e.g., all components are in `src/components/`).
- Use React's component lifecycle methods for initialization and cleanup.

### File Naming Conventions

- Use camelCase for filenames, except for JSON files which should use snake_case.
- Components: Start with a descriptive prefix followed by the type of component (e.g., `ClockComponent.tsx`).

### Code Organization Principles

- Keep stateless components pure functions that only rely on their props.
- Stateful components can manage their own internal state but should not share it directly between siblings.

### Testing
Implement unit tests using Jest for all modules and a few integration tests to ensure end-to-end functionality:
```
src/
├── main.ts
│   └── tests/
│       ├── index.test.ts
└── components/
    ├── Clock.test.tsx
    └── Alarms.test.tsx
```

### Build Tools

- Use `electron-builder` for packaging and distributing the application.
- Install and configure a linting tool like ESLint to enforce coding conventions.

Here are some additional guidelines for specific parts of your project:

#### TypeScript Configuration
In your `tsconfig.json`, ensure you have:
```json
{
  "compilerOptions": {
    "target": "electron-main",
    "module": "commonjs",
    "outDir": "./build/",
    "sourceMap": true,
    "noImplicitAny": true
  }
}
```

#### React Setup in Main Process
- Use `@electron/worker` to communicate between main and renderer processes.
- Set up a simple routing system for handling page transitions.

#### Electron Renderer Setup
Use `React DOM` to render components. Implement a basic router if necessary.

### Persistence with Sqlite
For storing alarms, use the `AlarmModel` in your models folder. The `db/` folder will hold the database file (`alarms.db`) that stores alarm data persistently.

With these guidelines, you'll maintain a clear and structured project organization while developing an efficient and well-tested application using Electron, React, TypeScript, and Sqlite for persistence.