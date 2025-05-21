Below are guidelines based on your requirements:

### Project Structure

The project structure should be as follows:

```
project/
├── src/
│   ├── main.ts
│   ├── electron.js
│   ├── index.html
│   ├── public/
│   │   └── favicon.ico
│   ├── components/
│   │   └── ClockComponent.tsx
│   ├── services/
│   │   └── AlarmService.ts
│   ├── utils/
│   │   └── constants.ts
│   ├── App.tsx
│   └── tsconfig.json
└── package.json
```

### General Code Organization Principles

- Use the Single Responsibility Principle for all components and modules.
- Keep related code together in logical folders (e.g., `components` for React components, `services` for data access).
- Use clear, concise variable names.

### File-Naming Conventions

- **TypeScript files**: Use `.tsx` for React functional components.
- **JavaScript files**: Use `.js`.

### Code Organization and Formatting

1. **Components (e.g., `components/ClockComponent.tsx`)**:
   - Use the component function or hooks (like `useState`, `useEffect`) as needed.
   - Import other necessary components directly.

2. **Services (e.g., `services/AlarmService.ts`)**:
   - Keep services separate from presentation logic.
   - Implement database interactions and other business logic here.

3. **Types (e.g., `utils/constants.ts`)**:
   - Define types as needed for clarity and consistency across the codebase.

### Testing

- Write unit tests using Jest for components, service methods, and utility functions.
- Use React testing library for simulating interactions with React components.

### Build Tools

1. **TypeScript Configuration (tsconfig.json)**:
   - Configure the TypeScript compiler to transpile `.tsx` files with JSX enabled.
   - Set `moduleResolution` to `node` if using ES6 modules and `outDir` to the appropriate output directory.

2. **npm Scripts**:
   - Use `build` for compiling and bundling the application.
   - Use `test` for running Jest tests.

Here’s an example of what these configurations might look like:

#### tsconfig.json
```json
{
  "compilerOptions": {
    "target": "es6",
    "module": "commonjs",
    "sourceMap": true,
    "outDir": "dist/",
    "jsx": "react"
  },
  "include": ["src/**/*"]
}
```

#### package.json (snippets)
```json
{
  ...
  "scripts": {
    "build": "electron-builder build",
    "test": "jest"
  }
}
```
This structure and guidelines will help maintain a clean, organized codebase that is consistent with the requirements provided.