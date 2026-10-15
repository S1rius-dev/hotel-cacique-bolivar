import js from '@eslint/js'
import globals from 'globals'
import reactHooks from 'eslint-plugin-react-hooks'
import reactRefresh from 'eslint-plugin-react-refresh'
import tseslint from 'typescript-eslint'
import { defineConfig, globalIgnores } from 'eslint/config'

export default defineConfig([
  // `src/api/generated/**` es output de Orval (`pnpm generate:api`): se regenera
  // entero y no se modifica a mano, así que queda fuera del lint.
  globalIgnores(['dist', 'src/api/generated/**']),
  {
    files: ['**/*.{ts,tsx}'],
    extends: [
      js.configs.recommended,
      tseslint.configs.recommended,
      reactHooks.configs.flat.recommended,
      reactRefresh.configs.vite,
    ],
    languageOptions: {
      ecmaVersion: 2020,
      globals: globals.browser,
    },
    rules: {
      // Alinea ESLint con el compilador: TypeScript ya exime los parámetros y
      // variables prefijados con `_`, que se usan para firmas intencionalmente
      // pendientes (p. ej. stubs de servicios aún sin conectar).
      '@typescript-eslint/no-unused-vars': [
        'error',
        {
          argsIgnorePattern: '^_',
          varsIgnorePattern: '^_',
          caughtErrorsIgnorePattern: '^_',
        },
      ],
    },
  },
  {
    // Componentes shadcn: conviven con sus `*Variants` de cva, que son la vía
    // pública para estilizar desde fuera. Partirlos rompería la librería.
    files: ['src/components/ui/**/*.{ts,tsx}'],
    rules: {
      'react-refresh/only-export-components': 'off',
    },
  },
  {
    // Archivos de columnas: exportan un array de configuración, no componentes.
    files: ['src/features/**/*Columns.tsx'],
    rules: {
      'react-refresh/only-export-components': 'off',
    },
  },
  {
    // El hook consumidor va junto al provider; separarlos obligaría a
    // re-importarlo en decenas de archivos sin ganar nada.
    files: [
      'src/app/auth/AuthContext.tsx',
      'src/layouts/public/PublicLayoutContext.tsx',
    ],
    rules: {
      'react-refresh/only-export-components': [
        'error',
        { allowExportNames: ['useAuth', 'usePublicLayout'] },
      ],
    },
  },
])
