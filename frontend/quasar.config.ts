// https://quasar.dev/quasar-cli-vite/quasar-config-file
import { defineConfig } from '#q-app';
import { fileURLToPath } from 'node:url';

// 저장소 루트의 content/ 를 화면과 챗봇이 함께 쓴다
const contentDir = fileURLToPath(new URL('../content', import.meta.url));

export default defineConfig(() => {
  return {
    boot: [],
    css: ['app.css'],
    extras: ['material-icons'],
    build: {
      target: {},
      typescript: {
        strict: true,
        vueShim: true,
      },
      vueRouterMode: 'history',
      alias: {
        '@content': contentDir,
      },
    },
    devServer: {
      port: 9000,
      open: false,
      // Docker 컨테이너 안 Windows 연결 폴더는 파일 변경 알림이 안 와서 폴링한다
      watch: { usePolling: true },
      fs: { allow: ['..'] },
      proxy: {
        '/api': {
          target: process.env.API_PROXY_TARGET ?? 'http://localhost:8080',
          changeOrigin: true,
        },
      },
    },
    framework: {
      config: {},
      plugins: ['Dialog'],
    },
    animations: [],
  };
});
