import { defineConfig } from 'vitest/config';
import vue from '@vitejs/plugin-vue';
import { quasar, transformAssetUrls } from '@quasar/vite-plugin';
import { fileURLToPath } from 'node:url';

// Quasar CLI 는 vite.config 를 쓰지 않으므로 테스트용 설정을 따로 둔다
export default defineConfig({
  plugins: [vue({ template: { transformAssetUrls } }), quasar()],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
      '@content': fileURLToPath(new URL('../content', import.meta.url)),
    },
  },
  test: {
    environment: 'jsdom',
    include: ['src/**/*.test.ts'],
    server: { deps: { inline: ['quasar'] } },
  },
});
