# Simple Clock Application Guidelines

## Project Structure
```
simple-clock/
├── src/
│   ├── main.ts
│   ├── preload.ts
│   ├── index.tsx
│   └── components/
│       ├── AnalogClock.js
│       ├── DigitalClock.js
│       └── Alarm.js
└── tsconfig.json
└── package.json
└── vitest.config.js
```

## General Code Organization Principles
- All code should be organized within the `src/` directory.
- Use functional components and separate logic from rendering.

## File-Naming and Coding Conventions

### TypeScript (tsconfig.json)
- Use camelCase for property names and variable names.
- Functions should follow standard naming conventions: `verb + noun`.
- Class names should start with a capital letter and use PascalCase.
- Use spaces instead of tabs for indentation.
- Use type annotations consistently throughout the code.

```json
{
  "compilerOptions": {
    // options...
    "sourceMap": true,
    "outDir": "./dist/",
    "strict": true,
    "moduleResolution": "node",
    "esModuleInterop": true,
    "allowSyntheticDefaultImports": true
  },
  "include": ["src/**/*"],
  "exclude": ["node_modules", "dist"]
}
```

### React (Components)
- Components should be defined in separate files and use the `default` export.
- Use JSX syntax for rendering HTML-like structures.

```jsx
// src/components/AnalogClock.js
import { useState, useEffect } from 'react';

function AnalogClock() {
  // Implementation...
}

export default AnalogClock;
```

### Electron (Main and Preload Scripts)
- The `main.ts` file should manage the application's lifecycle.
- The `preload.ts` file should handle permissions and other setup tasks.

```typescript
// src/main.ts
import { app, BrowserWindow } from 'electron';
import { join } from 'path';

app.on('ready', () => {
  // Create main browser window...
});

app.on('window-all-closed', () => {
  // Handle windows close event...
});
```

### SQLite (Database)
- Use the `@sqlite3` package for interacting with the database.
- Ensure all database operations are transactional.

```typescript
import { open } from 'sqlite3';
import { join } from 'path';

const dbPath = join(process.cwd(), 'alarms.db');
const db = open(dbPath);

db.serialize(() => {
  // Perform database operations...
});
```

## Testing

### Vitest (Unit Tests)
- Use the `vitest` package to write unit tests for components and logic.
- Ensure all tests are kept separate from the source code.

```typescript
// src/components/AnalogClock.test.tsx
import { render, fireEvent } from '@testing-library/react';
import AnalogClock from './AnalogClock';

test('renders analog clock', () => {
  const { getByText } = render(<AnalogClock />);
  expect(getByText('12:00')).toBeInTheDocument();
});
```

### Integration Tests
- Use Vitest to write integration tests for Electron functionality.

```typescript
// src/main.test.ts
import { app, BrowserWindow } from 'electron';
import { join } from 'path';

test('main script runs without errors', async () => {
  const window = new BrowserWindow();
  await window.loadURL('file://src/index.html');
});
```

## Build Tools

### TypeScript (tsconfig.json)
- Use the `@typescript-eslint` package to enforce TypeScript rules.

```json
{
  // options...
  "extends": ["eslint:recommended", "@typescript-eslint"],
  "plugins": ["@typescript-eslint"]
}
```

This set of guidelines should ensure your codebase adheres to best practices for structure, organization, and testing, allowing for maintainable and scalable development.