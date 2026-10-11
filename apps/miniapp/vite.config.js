import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'
import path from 'node:path'
import { fileURLToPath } from 'node:url'

const __dirname = path.dirname(fileURLToPath(import.meta.url))
const variablesPath = path.resolve(__dirname, 'src/styles/variables.less').replace(/\\/g, '/')

export default defineConfig(() => {
  return {
    plugins: [uni()],
    css: {
      preprocessorOptions: {
        less: {
          additionalData: (content, filePath) => {
            const normalized = String(filePath || '').replace(/\\/g, '/')
            if (
              normalized.includes('/styles/variables.less') ||
              normalized.includes('/styles/mixins.less') ||
              normalized.includes('/styles/neu.less')
            ) {
              return content
            }
            return `@import "${variablesPath}";\n${content}`
          }
        }
      }
    },
    resolve: {
      alias: {
        '@': path.resolve(__dirname, 'src')
      }
    }
  }
})
