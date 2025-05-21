# Simple Clock Application Guidelines

## Project Structure

The project should follow a consistent structure to ensure maintainability and organization. Here is the recommended directory layout:

```plaintext
simple-clock/
├── src/
│   ├── main.ts
│   ├── renderer/
│   │   ├── App.tsx
│   │   └── components/
│   │       ├── AnalogClock.tsx
│   │       └── DigitalClock.tsx
│   ├── models/
│   │   ├── AlarmModel.ts
│   │   └── ClockSettings.ts
│   ├── utils/
│   │   ├── timeUtil.ts
│   │   └── notificationUtils.ts
│   ├── services/
│   │   ├── sqliteService.ts
│   │   └── alarmService.ts
│   └── types/
│       └── IAlarm.ts
├── vite.config.ts
└── package.json
```

## General Code Organization Principles

1. **Modularity**: Each component, service, and utility should be designed to perform a specific task.
2. **Reusability**: Components and utilities should be reusable across the application.
3. **Separation of Concerns (SoC)**: Different concerns like data storage, business logic, and user interface should be separated into different modules.

## File Naming Conventions

### TypeScript

- Use lowercase letters with words separated by underscores if multiple words are used (`e.g., alarm_service.ts`).
- For typescript files where you're exporting functions or interfaces directly (`e.g., IAlarm.ts`), use PascalCase for the interface name and export.

### React (tsx)

- Follow standard react component naming conventions using camelCase (`e.g., DigitalClock`).

## Coding Conventions

### TypeScript

1. **Indentation**: 2 spaces.
2. **Brackets**: Curly brackets are used to define blocks.
3. **Line Length**: Maximum of 120 characters per line.
4. **Semicolons**: Use semicolons at the end of every statement.

### React (tsx)

1. **Indentation**: Same as TypeScript, use 2 spaces.
2. **Brackets**: Curly brackets are used to define blocks.
3. **Line Length**: Maximum of 120 characters per line.

## Testing

- **Vitest** should be used for unit testing in the project. Ensure all functions and modules are covered with appropriate tests.
- Use a mocking library if necessary for isolating dependencies during tests.

### Example of Unit Test
```ts
import { describe, it } from 'vitest';
import { createAlarmService } from './services/alarmService';

describe('alarmService', () => {
  it('should set an alarm', async () => {
    const service = createAlarmService();
    await service.setAlarm("08:00", "Wake me up");
    expect(service.getAlarms()).toEqual([{"id": 1, "time": "08:00", "message": "Wake me up"}]);
  });
});
```

## Build Tools

- **Vitest**: Will be used to run unit tests.
- **Electron**: Used for packaging the application.

### Example Vite Config
```ts
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

export default defineConfig({
  plugins: [react()],
});
```

These guidelines will help ensure that your code is well-organized and maintainable, adhering to best practices for both TypeScript and React.