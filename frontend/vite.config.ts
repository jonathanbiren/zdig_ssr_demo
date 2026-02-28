import { defineConfig } from 'vite'
import solid from 'vite-plugin-solid'
import tailwindcss from '@tailwindcss/vite'
import { tanstackRouter } from '@tanstack/router-plugin/vite'

export default defineConfig({
  plugins: [
    tanstackRouter({
      target: 'solid',
      autoCodeSplitting: true,
    }),
    solid(),
    tailwindcss(),
  ],
  resolve: {
    alias: {
      '@app': '/src/1-app',
      '@pages': '/src/2-pages',
      '@widgets': '/src/3-widgets',
      '@features': '/src/4-features',
      '@entities': '/src/5-entities',
      '@shared': '/src/6-shared',
    },
  },
  server: {
    port: 3000,
    proxy: {
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
