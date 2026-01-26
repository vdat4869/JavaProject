import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'
import path from 'node:path'
import autoprefixer from 'autoprefixer'

export default defineConfig(() => {
  return {
    base: './',
    build: {
      outDir: 'build',
      rollupOptions: {
        output: {
          manualChunks: (id) => {
            // Vendor chunks
            if (id.includes('node_modules')) {
              if (id.includes('react') || id.includes('react-dom') || id.includes('react-router')) {
                return 'react-vendor'
              }
              if (id.includes('@coreui')) {
                return 'coreui-vendor'
              }
              if (id.includes('axios') || id.includes('i18next')) {
                return 'utils-vendor'
              }
              // Các vendor khác
              return 'vendor'
            }
          },
        },
      },
      chunkSizeWarningLimit: 1000,
      minify: 'esbuild', // Sử dụng esbuild thay vì terser (nhanh hơn và đã có sẵn)
    },
    css: {
      postcss: {
        plugins: [
          autoprefixer({}), // add options if needed
        ],
      },
    },
    plugins: [
      react({
        include: /\.(jsx|tsx)$/,
      }),
    ],
    resolve: {
      alias: [
        {
          find: 'src',
          replacement: path.resolve(__dirname, 'src'),
        },
      ],
      extensions: ['.mjs', '.js', '.ts', '.jsx', '.tsx', '.json', '.scss'],
    },
    server: {
      port: 3000,
      proxy: {
        '/api': {
          target: 'http://localhost:8080',
          changeOrigin: true,
          secure: false,
        },
      },
    },
  }
})
