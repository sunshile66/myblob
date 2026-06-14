import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { ElementPlusResolver } from 'unplugin-vue-components/resolvers'
import { fileURLToPath, URL } from 'node:url'
import fs from 'node:fs'
import path from 'node:path'

export default defineConfig({
  plugins: [
    vue(),
    Components({
      resolvers: [ElementPlusResolver({ importStyle: false })],
      dts: 'src/types/components.d.ts',
    }),
    {
      name: 'serve-tree-sitter-wasm',
      configureServer(server) {
        server.middlewares.use('/tree-sitter.wasm', (_req, res) => {
          const wasmPath = path.resolve(
            'node_modules/web-tree-sitter/tree-sitter.wasm'
          )
          if (fs.existsSync(wasmPath)) {
            res.setHeader('Content-Type', 'application/wasm')
            res.setHeader('Access-Control-Allow-Origin', '*')
            fs.createReadStream(wasmPath).pipe(res)
          } else {
            res.statusCode = 404
            res.end()
          }
        })
      }
    }
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@app': fileURLToPath(new URL('./src/app', import.meta.url)),
      '@features': fileURLToPath(new URL('./src/features', import.meta.url)),
      '@shared': fileURLToPath(new URL('./src/shared', import.meta.url)),
      '@legacy': fileURLToPath(new URL('./src', import.meta.url))
    }
  },
  optimizeDeps: {
    esbuildOptions: {
      target: 'es2022'
    }
  },
  build: {
    target: 'es2022',
    outDir: 'dist',
    assetsDir: 'assets',
    sourcemap: false,
    cssCodeSplit: true,
    chunkSizeWarningLimit: 500,
    minify: 'esbuild',
    rollupOptions: {
      output: {
        manualChunks: {
          'vue-vendor': ['vue', 'vue-router', 'pinia'],
          'element-plus': ['element-plus', '@element-plus/icons-vue'],
          'utils': ['axios', 'marked', 'markdown-it'],
          'crypto': ['crypto-js'],
          'fabric': ['fabric'],
          'curlconverter': ['curlconverter'],
          'editor': ['browser-image-compression'],
        },
        chunkFileNames: 'assets/js/[name]-[hash].js',
        entryFileNames: 'assets/js/[name]-[hash].js',
        assetFileNames: 'assets/[ext]/[name]-[hash].[ext]'
      }
    }
  },
  server: {
    port: 3001,
    strictPort: true,
    open: true,
    cors: true,
    warmup: {
      clientFiles: ['./src/main.ts', './src/app/bootstrap.ts'],
    },
    proxy: {
      '^/api(/|$)': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true
      },
      '/media': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true
      },
      '/static': {
        target: 'http://127.0.0.1:8000',
        changeOrigin: true
      }
    }
  }
})
